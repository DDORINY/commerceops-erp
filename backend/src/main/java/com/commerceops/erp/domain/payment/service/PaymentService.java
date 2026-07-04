package com.commerceops.erp.domain.payment.service;

import com.commerceops.erp.domain.accounting.service.AccountingService;
import com.commerceops.erp.domain.inventory.entity.InventoryLog;
import com.commerceops.erp.domain.inventory.enums.InventoryLogType;
import com.commerceops.erp.domain.inventory.repository.InventoryLogRepository;
import com.commerceops.erp.domain.order.entity.Order;
import com.commerceops.erp.domain.order.entity.OrderItem;
import com.commerceops.erp.domain.order.enums.OrderStatus;
import com.commerceops.erp.domain.order.repository.OrderItemRepository;
import com.commerceops.erp.domain.order.repository.OrderRepository;
import com.commerceops.erp.domain.payment.dto.MockPaymentCompleteRequest;
import com.commerceops.erp.domain.payment.dto.PaymentResponse;
import com.commerceops.erp.domain.payment.entity.Payment;
import com.commerceops.erp.domain.payment.enums.PaymentStatus;
import com.commerceops.erp.domain.payment.repository.PaymentRepository;
import com.commerceops.erp.domain.product.entity.Product;
import com.commerceops.erp.domain.user.entity.User;
import com.commerceops.erp.domain.warehouse.service.WarehouseFulfillmentService;
import com.commerceops.erp.global.exception.BusinessException;
import com.commerceops.erp.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final AccountingService accountingService;
    private final WarehouseFulfillmentService warehouseFulfillmentService;

    @Transactional
    public PaymentResponse completePayment(User user, MockPaymentCompleteRequest request) {
        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.ORDER_ACCESS_DENIED);
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
        }

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new BusinessException(ErrorCode.ALREADY_PAID);
        }

        Payment payment = paymentRepository.findByOrder(order)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        List<OrderItem> items = orderItemRepository.findAllByOrderWithProduct(order);
        warehouseFulfillmentService.reserveOrder(order, items);

        List<InventoryLog> logs = new ArrayList<>();
        for (OrderItem item : items) {
            Product product = item.getProduct();
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new BusinessException(ErrorCode.OUT_OF_STOCK);
            }

            int beforeStock = product.getStockQuantity();
            product.decrementStock(item.getQuantity());
            int afterStock = product.getStockQuantity();

            logs.add(InventoryLog.builder()
                    .product(product)
                    .type(InventoryLogType.ORDER)
                    .quantity(item.getQuantity())
                    .beforeStock(beforeStock)
                    .afterStock(afterStock)
                    .memo("주문번호: " + order.getOrderNumber())
                    .build());
        }
        inventoryLogRepository.saveAll(logs);

        String transactionId = String.format("MOCK-%s-%06d",
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                payment.getId());

        payment.complete(request.paymentMethod(), order.getTotalPrice(), transactionId);
        order.markAsPaid();

        accountingService.recordSale(order.getOrderNumber(), order.getTotalPrice());

        return PaymentResponse.from(payment, order);
    }
}
