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
import com.commerceops.erp.domain.payment.client.TossConfirmResult;
import com.commerceops.erp.domain.payment.client.TossPaymentClient;
import com.commerceops.erp.domain.payment.client.TossPaymentClientException;
import com.commerceops.erp.domain.payment.dto.*;
import com.commerceops.erp.domain.payment.entity.Payment;
import com.commerceops.erp.domain.payment.enums.PaymentMethod;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TossPaymentService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final AccountingService accountingService;
    private final WarehouseFulfillmentService warehouseFulfillmentService;
    private final TossPaymentClient tossPaymentClient;

    @Transactional
    public TossPaymentPrepareResponse prepare(User user, TossPaymentPrepareRequest request) {
        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        validateOwner(order, user);
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT && order.getStatus() != OrderStatus.PAYMENT_FAILED) {
            if (order.getStatus() == OrderStatus.PAID) throw new BusinessException(ErrorCode.ALREADY_PAID);
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
        }
        Payment payment = paymentRepository.findByOrder(order)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        if (payment.getPaymentStatus() == PaymentStatus.DONE || payment.getPaymentStatus() == PaymentStatus.PAID) {
            throw new BusinessException(ErrorCode.ALREADY_PAID);
        }
        if (order.getStatus() == OrderStatus.PAYMENT_FAILED && payment.getPaymentStatus() == PaymentStatus.ABORTED) {
            payment.restart("ORD-" + order.getId() + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16),
                    UUID.randomUUID().toString(), java.time.LocalDateTime.now());
            order.retryPayment();
        }
        if (!order.getTotalPrice().equals(payment.getRequestedAmount())) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
        List<OrderItem> items = orderItemRepository.findAllByOrderWithProduct(order);
        if (items.isEmpty()) throw new BusinessException(ErrorCode.PAYMENT_FAILED);
        String orderName = items.get(0).getProductName();
        if (items.size() > 1) orderName += " 외 " + (items.size() - 1) + "건";
        return new TossPaymentPrepareResponse(order.getId(), payment.getProviderOrderId(), orderName,
                order.getTotalPrice(), "USER-" + user.getId() + "-" + UUID.randomUUID().toString().substring(0, 8),
                user.getName(), user.getEmail());
    }

    @Transactional(noRollbackFor = TossPaymentClientException.class)
    public TossPaymentConfirmResponse confirm(User user, TossPaymentConfirmRequest request) {
        Payment payment = paymentRepository.findByProviderOrderIdForUpdate(request.orderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        Order order = payment.getOrder();
        validateOwner(order, user);
        if (payment.getPaymentStatus() == PaymentStatus.DONE) return TossPaymentConfirmResponse.from(payment);
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
        if (!request.orderId().equals(payment.getProviderOrderId())
                || !request.amount().equals(payment.getRequestedAmount())
                || !request.amount().equals(order.getTotalPrice())) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
        paymentRepository.findByPaymentKey(request.paymentKey()).ifPresent(existing -> {
            if (!existing.getId().equals(payment.getId())) throw new BusinessException(ErrorCode.DUPLICATE_PAYMENT_KEY);
        });
        if (payment.getPaymentKey() != null && !payment.getPaymentKey().equals(request.paymentKey())) {
            throw new BusinessException(ErrorCode.DUPLICATE_PAYMENT_KEY);
        }
        payment.markInProgress(request.paymentKey());
        TossConfirmResult result;
        try {
            result = tossPaymentClient.confirm(request.paymentKey(), request.orderId(), order.getTotalPrice(), payment.getIdempotencyKey());
        } catch (TossPaymentClientException e) {
            payment.recordFailure(e.getCode(), e.getMessage(), e.isRetryable());
            if (!e.isRetryable()) order.markPaymentFailed();
            throw e;
        }
        if (!"DONE".equals(result.status()) || !request.paymentKey().equals(result.paymentKey())
                || !payment.getProviderOrderId().equals(result.orderId()) || !order.getTotalPrice().equals(result.totalAmount())) {
            TossPaymentClientException error = new TossPaymentClientException("TOSS_RESPONSE_MISMATCH", "토스페이먼츠 승인 응답이 주문 정보와 일치하지 않습니다.", 502, false);
            payment.recordFailure(error.getCode(), error.getMessage(), false);
            order.markPaymentFailed();
            throw error;
        }
        completeInventory(order);
        PaymentMethod method = "간편결제".equals(result.method()) ? PaymentMethod.TOSS_EASY_PAY : PaymentMethod.TOSS_CARD;
        payment.completeToss(method, result.totalAmount(), result.approvedAt(), result.rawResponse());
        order.markAsPaid();
        accountingService.recordSale(order.getOrderNumber(), order.getTotalPrice());
        accountingService.recognizeOrderRevenue(order, user);
        return TossPaymentConfirmResponse.from(payment);
    }

    private void completeInventory(Order order) {
        List<OrderItem> items = orderItemRepository.findAllByOrderWithProduct(order);
        warehouseFulfillmentService.reserveOrder(order, items);
        List<InventoryLog> logs = new ArrayList<>();
        for (OrderItem item : items) {
            Product product = item.getProduct();
            if (product.getStockQuantity() < item.getQuantity()) throw new BusinessException(ErrorCode.OUT_OF_STOCK);
            int before = product.getStockQuantity();
            product.decrementStock(item.getQuantity());
            logs.add(InventoryLog.builder().product(product).type(InventoryLogType.ORDER)
                    .quantity(item.getQuantity()).beforeStock(before).afterStock(product.getStockQuantity())
                    .memo("주문번호: " + order.getOrderNumber()).build());
        }
        inventoryLogRepository.saveAll(logs);
    }

    private void validateOwner(Order order, User user) {
        if (!order.getUser().getId().equals(user.getId())) throw new BusinessException(ErrorCode.ORDER_ACCESS_DENIED);
    }
}
