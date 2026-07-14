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
import com.commerceops.erp.domain.order.service.OrderCancellationService;
import com.commerceops.erp.domain.payment.dto.MockPaymentCompleteRequest;
import com.commerceops.erp.domain.payment.dto.PaymentApproveRequest;
import com.commerceops.erp.domain.payment.dto.PaymentCancelRequest;
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
    private final OrderCancellationService orderCancellationService;

    @Transactional
    public PaymentResponse completePayment(User user, MockPaymentCompleteRequest request) {
        return approvePayment(user, new PaymentApproveRequest(
                request.orderId(),
                request.paymentMethod(),
                null,
                request.idempotencyKey()
        ));
    }

    @Transactional
    public PaymentResponse approvePayment(User user, PaymentApproveRequest request) {
        if (request.idempotencyKey() != null && !request.idempotencyKey().isBlank()) {
            Payment existing = paymentRepository.findByIdempotencyKey(request.idempotencyKey())
                    .orElse(null);
            if (existing != null) {
                validateOrderOwner(existing.getOrder(), user);
                return PaymentResponse.from(existing, existing.getOrder());
            }
        }

        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        validateOrderOwner(order, user);

        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PENDING_PAYMENT) {
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

        String transactionId = request.providerTransactionId();
        if (transactionId == null || transactionId.isBlank()) {
            transactionId = String.format("MOCK-%s-%06d",
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                payment.getId());
        }

        payment.complete(request.paymentMethod(), order.getTotalPrice(), transactionId, normalizeIdempotencyKey(request.idempotencyKey()));
        order.markAsPaid();

        accountingService.recordSale(order.getOrderNumber(), order.getTotalPrice());
        accountingService.recognizeOrderRevenue(order, user);

        return PaymentResponse.from(payment, order);
    }

    @Transactional
    public PaymentResponse cancelPayment(User user, Long paymentId, PaymentCancelRequest request) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        Order order = payment.getOrder();
        validateOrderOwner(order, user);

        if (payment.getPaymentStatus() == PaymentStatus.REFUNDED
                || payment.getPaymentStatus() == PaymentStatus.CANCELLED) {
            return PaymentResponse.from(payment, order);
        }

        validateCancelable(order);
        orderCancellationService.cancel(order, user);
        return PaymentResponse.from(payment, order);
    }

    private void validateOrderOwner(Order order, User user) {
        if (!order.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.ORDER_ACCESS_DENIED);
        }
    }

    private void validateCancelable(Order order) {
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PENDING_PAYMENT
                && order.getStatus() != OrderStatus.PAID
                && order.getStatus() != OrderStatus.PREPARING) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
        }
    }

    private String normalizeIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return null;
        }
        return idempotencyKey.trim();
    }
}
