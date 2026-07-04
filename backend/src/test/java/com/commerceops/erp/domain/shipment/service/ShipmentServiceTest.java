package com.commerceops.erp.domain.shipment.service;

import com.commerceops.erp.domain.order.entity.Order;
import com.commerceops.erp.domain.order.enums.OrderStatus;
import com.commerceops.erp.domain.order.repository.OrderRepository;
import com.commerceops.erp.domain.payment.enums.PaymentStatus;
import com.commerceops.erp.domain.shipment.dto.ShipmentResponse;
import com.commerceops.erp.domain.shipment.dto.TrackingUpdateRequest;
import com.commerceops.erp.domain.shipment.entity.Shipment;
import com.commerceops.erp.domain.shipment.enums.ShipmentStatus;
import com.commerceops.erp.domain.shipment.repository.ShipmentRepository;
import com.commerceops.erp.global.exception.BusinessException;
import com.commerceops.erp.domain.warehouse.service.WarehouseFulfillmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private WarehouseFulfillmentService warehouseFulfillmentService;

    @InjectMocks
    private ShipmentService shipmentService;

    @Test
    void updateTrackingMovesShipmentAndOrderToShipping() {
        Order order = order(OrderStatus.PREPARING);
        Shipment shipment = Shipment.builder()
                .id(3L)
                .order(order)
                .status(ShipmentStatus.READY)
                .build();
        when(shipmentRepository.findById(3L)).thenReturn(Optional.of(shipment));

        ShipmentResponse response = shipmentService.updateTracking(
                3L,
                new TrackingUpdateRequest("TRACK-001", "Commerce Express")
        );

        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.IN_TRANSIT);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPING);
        assertThat(response.trackingNumber()).isEqualTo("TRACK-001");
        assertThat(response.carrier()).isEqualTo("Commerce Express");
        assertThat(response.shippedAt()).isNotNull();
    }

    @Test
    void deliveredShipmentCannotReceiveAnotherTrackingNumber() {
        Shipment shipment = Shipment.builder()
                .id(4L)
                .order(order(OrderStatus.COMPLETED))
                .status(ShipmentStatus.DELIVERED)
                .build();
        when(shipmentRepository.findById(4L)).thenReturn(Optional.of(shipment));

        assertThatThrownBy(() -> shipmentService.updateTracking(
                4L,
                new TrackingUpdateRequest("TRACK-002", "Commerce Express")
        )).isInstanceOf(BusinessException.class);
    }

    private Order order(OrderStatus status) {
        return Order.builder()
                .id(10L)
                .orderNumber("ORD-20260704-000010")
                .totalPrice(45_000)
                .status(status)
                .paymentStatus(PaymentStatus.PAID)
                .receiverName("테스트 사용자")
                .receiverPhone("010-0000-0000")
                .address("서울시 테스트구")
                .detailAddress("101호")
                .build();
    }
}
