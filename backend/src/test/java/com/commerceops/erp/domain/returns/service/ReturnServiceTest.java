package com.commerceops.erp.domain.returns.service;

import com.commerceops.erp.domain.accounting.service.AccountingService;
import com.commerceops.erp.domain.inventory.entity.InventoryLog;
import com.commerceops.erp.domain.inventory.repository.InventoryLogRepository;
import com.commerceops.erp.domain.notification.service.NotificationService;
import com.commerceops.erp.domain.order.entity.Order;
import com.commerceops.erp.domain.order.entity.OrderItem;
import com.commerceops.erp.domain.order.enums.OrderStatus;
import com.commerceops.erp.domain.order.repository.OrderItemRepository;
import com.commerceops.erp.domain.order.repository.OrderRepository;
import com.commerceops.erp.domain.payment.enums.PaymentStatus;
import com.commerceops.erp.domain.product.entity.Product;
import com.commerceops.erp.domain.product.enums.ProductStatus;
import com.commerceops.erp.domain.product.repository.ProductRepository;
import com.commerceops.erp.domain.returns.dto.ReturnAdminActionRequest;
import com.commerceops.erp.domain.returns.entity.ReturnRequest;
import com.commerceops.erp.domain.returns.enums.ReturnReason;
import com.commerceops.erp.domain.returns.enums.ReturnStatus;
import com.commerceops.erp.domain.returns.repository.ReturnRequestRepository;
import com.commerceops.erp.domain.user.entity.User;
import com.commerceops.erp.domain.user.enums.UserRole;
import com.commerceops.erp.domain.user.enums.UserStatus;
import com.commerceops.erp.domain.warehouse.service.WarehouseFulfillmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReturnServiceTest {

    @Mock private ReturnRequestRepository returnRequestRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private InventoryLogRepository inventoryLogRepository;
    @Mock private AccountingService accountingService;
    @Mock private WarehouseFulfillmentService warehouseFulfillmentService;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private ReturnService returnService;

    @Test
    void approveReturnRestoresStockAndRecordsRefund() {
        User user = User.builder()
                .id(1L).email("user@example.com").password("encoded").name("테스트 사용자")
                .role(UserRole.USER).status(UserStatus.ACTIVE).build();
        Order order = Order.builder()
                .id(10L).user(user).orderNumber("ORD-20260704-000010").totalPrice(45_000)
                .status(OrderStatus.COMPLETED).paymentStatus(PaymentStatus.PAID)
                .receiverName(user.getName()).receiverPhone("010-0000-0000")
                .address("서울시 테스트구").build();
        Product product = Product.builder()
                .id(20L).name("테스트 상품").price(15_000).stockQuantity(5)
                .status(ProductStatus.ON_SALE).build();
        OrderItem item = OrderItem.builder()
                .order(order).product(product).productName(product.getName())
                .price(product.getPrice()).quantity(2).build();
        ReturnRequest returnRequest = ReturnRequest.builder()
                .id(30L).order(order).user(user).reason(ReturnReason.DEFECTIVE)
                .status(ReturnStatus.REQUESTED).build();

        when(returnRequestRepository.findById(30L)).thenReturn(Optional.of(returnRequest));
        when(orderItemRepository.findAllByOrderWithProduct(order)).thenReturn(List.of(item));

        returnService.approveReturn(30L, new ReturnAdminActionRequest("검수 완료"));

        assertThat(returnRequest.getStatus()).isEqualTo(ReturnStatus.APPROVED);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.REFUNDED);
        assertThat(product.getStockQuantity()).isEqualTo(7);
        verify(accountingService).recordRefund(order.getOrderNumber(), order.getTotalPrice());

        ArgumentCaptor<InventoryLog> logCaptor = ArgumentCaptor.forClass(InventoryLog.class);
        verify(inventoryLogRepository).save(logCaptor.capture());
        assertThat(logCaptor.getValue().getBeforeStock()).isEqualTo(5);
        assertThat(logCaptor.getValue().getAfterStock()).isEqualTo(7);
    }
}
