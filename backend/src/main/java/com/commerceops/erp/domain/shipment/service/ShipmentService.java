package com.commerceops.erp.domain.shipment.service;

import com.commerceops.erp.domain.order.entity.Order;
import com.commerceops.erp.domain.order.enums.OrderStatus;
import com.commerceops.erp.domain.order.repository.OrderRepository;
import com.commerceops.erp.domain.shipment.dto.ShipmentResponse;
import com.commerceops.erp.domain.shipment.dto.TrackingUpdateRequest;
import com.commerceops.erp.domain.shipment.entity.Shipment;
import com.commerceops.erp.domain.shipment.enums.ShipmentStatus;
import com.commerceops.erp.domain.shipment.repository.ShipmentRepository;
import com.commerceops.erp.domain.user.entity.User;
import com.commerceops.erp.domain.warehouse.service.WarehouseFulfillmentService;
import com.commerceops.erp.global.exception.BusinessException;
import com.commerceops.erp.global.exception.ErrorCode;
import com.commerceops.erp.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final WarehouseFulfillmentService warehouseFulfillmentService;

    public PageResponse<ShipmentResponse> getAdminShipments(ShipmentStatus status, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return PageResponse.from(
                shipmentRepository.findAllForAdmin(status, keyword, pageable).map(ShipmentResponse::from));
    }

    public ShipmentResponse getAdminShipment(Long shipmentId) {
        Shipment shipment = findShipment(shipmentId);
        return ShipmentResponse.from(shipment);
    }

    public ShipmentResponse getShipmentByOrderId(Long orderId, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        if (!order.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.ORDER_ACCESS_DENIED);
        }
        Shipment shipment = shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHIPMENT_NOT_FOUND));
        return ShipmentResponse.from(shipment);
    }

    @Transactional
    public ShipmentResponse updateTracking(Long shipmentId, TrackingUpdateRequest request) {
        Shipment shipment = findShipment(shipmentId);
        if (shipment.getStatus() == ShipmentStatus.DELIVERED) {
            throw new BusinessException(ErrorCode.SHIPMENT_ALREADY_DELIVERED);
        }
        if (shipment.getStatus() == ShipmentStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.SHIPMENT_CANCELLED);
        }

        Order order = shipment.getOrder();
        warehouseFulfillmentService.shipOrder(order);
        shipment.updateTracking(request.trackingNumber(), request.carrier());
        order.updateStatus(OrderStatus.SHIPPING);

        return ShipmentResponse.from(shipment);
    }

    @Transactional
    public ShipmentResponse markDelivered(Long shipmentId) {
        Shipment shipment = findShipment(shipmentId);
        if (shipment.getStatus() != ShipmentStatus.IN_TRANSIT) {
            throw new BusinessException(ErrorCode.SHIPMENT_NOT_IN_TRANSIT);
        }

        shipment.markDelivered();

        Order order = shipment.getOrder();
        order.updateStatus(OrderStatus.COMPLETED);

        return ShipmentResponse.from(shipment);
    }

    private Shipment findShipment(Long shipmentId) {
        return shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHIPMENT_NOT_FOUND));
    }
}
