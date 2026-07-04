package com.commerceops.erp.domain.order.service;

import com.commerceops.erp.domain.accounting.service.AccountingService;
import com.commerceops.erp.domain.inventory.repository.InventoryLogRepository;
import com.commerceops.erp.domain.order.entity.Order;
import com.commerceops.erp.domain.order.entity.OrderItem;
import com.commerceops.erp.domain.order.enums.OrderStatus;
import com.commerceops.erp.domain.order.repository.OrderItemRepository;
import com.commerceops.erp.domain.payment.entity.Payment;
import com.commerceops.erp.domain.payment.enums.PaymentMethod;
import com.commerceops.erp.domain.payment.enums.PaymentStatus;
import com.commerceops.erp.domain.payment.repository.PaymentRepository;
import com.commerceops.erp.domain.product.entity.Product;
import com.commerceops.erp.domain.product.enums.ProductStatus;
import com.commerceops.erp.domain.product.repository.ProductRepository;
import com.commerceops.erp.domain.shipment.repository.ShipmentRepository;
import com.commerceops.erp.domain.warehouse.service.WarehouseFulfillmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderCancellationServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private InventoryLogRepository inventoryLogRepository;
    @Mock private AccountingService accountingService;
    @Mock private WarehouseFulfillmentService warehouseFulfillmentService;
    @Mock private ShipmentRepository shipmentRepository;

    @InjectMocks
    private OrderCancellationService cancellationService;

    @Test
    void paidOrderCancellationRestoresStockReservationPaymentAndAccounting() {
        Order order = order(OrderStatus.PAID, PaymentStatus.PAID);
        Payment payment = payment(order, PaymentStatus.PAID, 20_000);
        Product product = Product.builder()
                .id(10L).name("테스트 상품").price(10_000).stockQuantity(8)
                .status(ProductStatus.ON_SALE).build();
        OrderItem item = OrderItem.builder()
                .id(20L).order(order).product(product).productName(product.getName())
                .price(product.getPrice()).quantity(2).build();
        when(paymentRepository.findByOrder(order)).thenReturn(Optional.of(payment));
        when(orderItemRepository.findAllByOrderWithProduct(order)).thenReturn(List.of(item));
        when(productRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(product));

        cancellationService.cancel(order);

        verify(warehouseFulfillmentService).releaseOrder(order);
        verify(accountingService).recordRefund(order.getOrderNumber(), order.getTotalPrice());
        verify(inventoryLogRepository).saveAll(anyList());
        assertThat(product.getStockQuantity()).isEqualTo(10);
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getPaymentStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }

    @Test
    void pendingOrderCancellationDoesNotTouchStockOrAccounting() {
        Order order = order(OrderStatus.PENDING, PaymentStatus.READY);
        Payment payment = payment(order, PaymentStatus.READY, 0);
        when(paymentRepository.findByOrder(order)).thenReturn(Optional.of(payment));

        cancellationService.cancel(order);

        verify(warehouseFulfillmentService, never()).releaseOrder(order);
        verify(accountingService, never()).recordRefund(order.getOrderNumber(), order.getTotalPrice());
        verify(inventoryLogRepository, never()).saveAll(anyList());
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELLED);
    }

    private Order order(OrderStatus orderStatus, PaymentStatus paymentStatus) {
        return Order.builder()
                .id(1L).orderNumber("ORD-1").totalPrice(20_000)
                .status(orderStatus).paymentStatus(paymentStatus)
                .receiverName("테스트").receiverPhone("010").address("서울").build();
    }

    private Payment payment(Order order, PaymentStatus status, int paidAmount) {
        return Payment.builder()
                .id(2L).order(order).paymentMethod(PaymentMethod.MOCK_CARD)
                .paymentStatus(status).paidAmount(paidAmount).build();
    }
}
