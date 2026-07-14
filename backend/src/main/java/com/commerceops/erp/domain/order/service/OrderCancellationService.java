package com.commerceops.erp.domain.order.service;

import com.commerceops.erp.domain.accounting.service.AccountingService;
import com.commerceops.erp.domain.inventory.entity.InventoryLog;
import com.commerceops.erp.domain.inventory.enums.InventoryLogType;
import com.commerceops.erp.domain.inventory.repository.InventoryLogRepository;
import com.commerceops.erp.domain.order.entity.Order;
import com.commerceops.erp.domain.order.entity.OrderItem;
import com.commerceops.erp.domain.order.repository.OrderItemRepository;
import com.commerceops.erp.domain.payment.entity.Payment;
import com.commerceops.erp.domain.payment.enums.PaymentStatus;
import com.commerceops.erp.domain.payment.repository.PaymentRepository;
import com.commerceops.erp.domain.product.entity.Product;
import com.commerceops.erp.domain.product.repository.ProductRepository;
import com.commerceops.erp.domain.shipment.enums.ShipmentStatus;
import com.commerceops.erp.domain.shipment.repository.ShipmentRepository;
import com.commerceops.erp.domain.user.entity.User;
import com.commerceops.erp.domain.warehouse.service.WarehouseFulfillmentService;
import com.commerceops.erp.global.exception.BusinessException;
import com.commerceops.erp.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderCancellationService {

    private final PaymentRepository paymentRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final AccountingService accountingService;
    private final WarehouseFulfillmentService warehouseFulfillmentService;
    private final ShipmentRepository shipmentRepository;

    @Transactional
    public void cancel(Order order, User actor) {
        Payment payment = paymentRepository.findByOrder(order)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        boolean paid = payment.getPaymentStatus() == PaymentStatus.PAID
                || payment.getPaymentStatus() == PaymentStatus.DONE;

        if (paid) {
            warehouseFulfillmentService.releaseOrder(order);
            restoreProductStock(order);
            payment.refund();
            accountingService.recordRefund(order.getOrderNumber(), order.getTotalPrice());
            accountingService.recognizePaymentRefund(payment, actor);
        } else {
            payment.cancelReadyPayment();
        }

        order.cancel(paid);
        shipmentRepository.findByOrderId(order.getId()).ifPresent(shipment -> {
            if (shipment.getStatus() == ShipmentStatus.READY) {
                shipment.cancel();
            }
        });
    }

    @Transactional
    public void cancel(Order order) {
        cancel(order, null);
    }

    private void restoreProductStock(Order order) {
        List<OrderItem> items = orderItemRepository.findAllByOrderWithProduct(order);
        List<InventoryLog> logs = new ArrayList<>();
        for (OrderItem item : items) {
            Product product = productRepository.findByIdForUpdate(item.getProduct().getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
            int beforeStock = product.getStockQuantity();
            product.incrementStock(item.getQuantity());
            logs.add(InventoryLog.builder()
                    .product(product)
                    .type(InventoryLogType.CANCEL)
                    .quantity(item.getQuantity())
                    .beforeStock(beforeStock)
                    .afterStock(product.getStockQuantity())
                    .memo("주문 취소 - 주문번호: " + order.getOrderNumber())
                    .build());
        }
        inventoryLogRepository.saveAll(logs);
    }
}
