package com.commerceops.erp.domain.payment.service;

import com.commerceops.erp.domain.accounting.service.AccountingService;
import com.commerceops.erp.domain.inventory.repository.InventoryLogRepository;
import com.commerceops.erp.domain.order.entity.Order;
import com.commerceops.erp.domain.order.entity.OrderItem;
import com.commerceops.erp.domain.order.enums.OrderStatus;
import com.commerceops.erp.domain.order.repository.OrderItemRepository;
import com.commerceops.erp.domain.order.repository.OrderRepository;
import com.commerceops.erp.domain.payment.client.*;
import com.commerceops.erp.domain.payment.dto.*;
import com.commerceops.erp.domain.payment.entity.Payment;
import com.commerceops.erp.domain.payment.enums.*;
import com.commerceops.erp.domain.payment.repository.PaymentRepository;
import com.commerceops.erp.domain.product.entity.Product;
import com.commerceops.erp.domain.product.enums.ProductStatus;
import com.commerceops.erp.domain.user.entity.User;
import com.commerceops.erp.domain.user.enums.UserRole;
import com.commerceops.erp.domain.user.enums.UserStatus;
import com.commerceops.erp.domain.warehouse.service.WarehouseFulfillmentService;
import com.commerceops.erp.global.exception.BusinessException;
import com.commerceops.erp.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class TossPaymentServiceTest {
    @Mock OrderRepository orderRepository;
    @Mock OrderItemRepository orderItemRepository;
    @Mock PaymentRepository paymentRepository;
    @Mock InventoryLogRepository inventoryLogRepository;
    @Mock AccountingService accountingService;
    @Mock WarehouseFulfillmentService warehouseFulfillmentService;
    @Mock TossPaymentClient tossPaymentClient;
    @InjectMocks TossPaymentService service;

    User owner;
    Order order;
    Payment payment;
    OrderItem item;

    @BeforeEach void setUp() {
        owner = User.builder().id(10L).email("owner@example.com").name("홍길동").password("x")
                .role(UserRole.USER).status(UserStatus.ACTIVE).build();
        order = Order.builder().id(1L).user(owner).orderNumber("ORD-1").totalPrice(50_000)
                .status(OrderStatus.PENDING_PAYMENT).paymentStatus(PaymentStatus.READY)
                .receiverName("홍길동").receiverPhone("010").address("서울").build();
        payment = Payment.builder().id(2L).order(order).provider("TOSS").providerOrderId("ORD-1-a1b2c3d4")
                .paymentMethod(PaymentMethod.TOSS_CARD).paymentStatus(PaymentStatus.READY).paidAmount(0)
                .requestedAmount(50_000).approvedAmount(0).idempotencyKey("idem-1").requestedAt(LocalDateTime.now()).build();
        Product product = Product.builder().id(3L).name("상품").price(50_000).stockQuantity(10).status(ProductStatus.ON_SALE).build();
        item = OrderItem.builder().id(4L).order(order).product(product).productName("상품").price(50_000).quantity(1).build();
    }

    @Test void preparesPaymentFromDatabaseAmount() {
        stubPrepare();
        var result = service.prepare(owner, new TossPaymentPrepareRequest(1L));
        assertThat(result.amount()).isEqualTo(50_000);
        assertThat(result.paymentOrderId()).isEqualTo(payment.getProviderOrderId());
    }

    @Test void blocksOtherUsersPrepare() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        User other = User.builder().id(99L).build();
        assertThatThrownBy(() -> service.prepare(other, new TossPaymentPrepareRequest(1L)))
                .isInstanceOf(BusinessException.class).extracting("errorCode").isEqualTo(ErrorCode.ORDER_ACCESS_DENIED);
    }

    @Test void blocksAlreadyPaidPrepare() {
        order.updateStatus(OrderStatus.PAID);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        assertThatThrownBy(() -> service.prepare(owner, new TossPaymentPrepareRequest(1L)))
                .isInstanceOf(BusinessException.class).extracting("errorCode").isEqualTo(ErrorCode.ALREADY_PAID);
    }

    @Test void blocksAmountMismatchBeforeConfirm() {
        stubConfirmLookup();
        assertThatThrownBy(() -> service.confirm(owner, new TossPaymentConfirmRequest("pk", payment.getProviderOrderId(), 100)))
                .isInstanceOf(BusinessException.class).extracting("errorCode").isEqualTo(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        verifyNoInteractions(tossPaymentClient);
    }

    @Test void successfulConfirmMarksPaymentDoneAndOrderPaid() {
        stubSuccessfulConfirm();
        var result = service.confirm(owner, confirmRequest());
        assertThat(result.status()).isEqualTo("DONE");
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.DONE);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test void repeatedDoneConfirmDoesNotCallTossAgain() {
        payment.markInProgress("pk");
        payment.completeToss(PaymentMethod.TOSS_CARD, 50_000, LocalDateTime.now(), "{}");
        stubConfirmLookup();
        service.confirm(owner, confirmRequest());
        verifyNoInteractions(tossPaymentClient);
    }

    @Test void blocksPaymentKeyOwnedByAnotherPayment() {
        stubConfirmLookup();
        Payment other = Payment.builder().id(9L).build();
        when(paymentRepository.findByPaymentKey("pk")).thenReturn(Optional.of(other));
        assertThatThrownBy(() -> service.confirm(owner, confirmRequest()))
                .isInstanceOf(BusinessException.class).extracting("errorCode").isEqualTo(ErrorCode.DUPLICATE_PAYMENT_KEY);
    }

    @Test void tossFailureNeverMarksOrderPaid() {
        stubConfirmLookup();
        when(paymentRepository.findByPaymentKey("pk")).thenReturn(Optional.empty());
        when(tossPaymentClient.confirm(anyString(), anyString(), anyInt(), anyString()))
                .thenThrow(new TossPaymentClientException("REJECTED", "거절", 400, false));
        assertThatThrownBy(() -> service.confirm(owner, confirmRequest())).isInstanceOf(TossPaymentClientException.class);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_FAILED);
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.ABORTED);
    }

    @Test void networkFailureRemainsRetryableInProgress() {
        stubConfirmLookup();
        when(paymentRepository.findByPaymentKey("pk")).thenReturn(Optional.empty());
        when(tossPaymentClient.confirm(anyString(), anyString(), anyInt(), anyString()))
                .thenThrow(new TossPaymentClientException("NETWORK", "재시도", 503, true));
        assertThatThrownBy(() -> service.confirm(owner, confirmRequest())).isInstanceOf(TossPaymentClientException.class);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.IN_PROGRESS);
    }

    private void stubPrepare() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrder(order)).thenReturn(Optional.of(payment));
        when(orderItemRepository.findAllByOrderWithProduct(order)).thenReturn(List.of(item));
    }
    private void stubConfirmLookup() {
        when(paymentRepository.findByProviderOrderIdForUpdate(payment.getProviderOrderId())).thenReturn(Optional.of(payment));
    }
    private TossPaymentConfirmRequest confirmRequest() { return new TossPaymentConfirmRequest("pk", payment.getProviderOrderId(), 50_000); }
    private void stubSuccessfulConfirm() {
        stubConfirmLookup();
        when(paymentRepository.findByPaymentKey("pk")).thenReturn(Optional.empty());
        when(orderItemRepository.findAllByOrderWithProduct(order)).thenReturn(List.of(item));
        when(tossPaymentClient.confirm("pk", payment.getProviderOrderId(), 50_000, "idem-1"))
                .thenReturn(new TossConfirmResult("DONE", "pk", payment.getProviderOrderId(), 50_000, "카드", LocalDateTime.now(), "{}"));
    }
}
