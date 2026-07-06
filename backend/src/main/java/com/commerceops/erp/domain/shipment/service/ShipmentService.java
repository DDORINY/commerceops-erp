package com.commerceops.erp.domain.shipment.service;

import com.commerceops.erp.domain.audit.enums.AuditActionType;
import com.commerceops.erp.domain.audit.service.AuditLogService;
import com.commerceops.erp.domain.order.entity.Order;
import com.commerceops.erp.domain.order.enums.OrderStatus;
import com.commerceops.erp.domain.order.repository.OrderRepository;
import com.commerceops.erp.domain.shipment.dto.ShipmentResponse;
import com.commerceops.erp.domain.shipment.dto.TrackingUpdateRequest;
import com.commerceops.erp.domain.shipment.entity.Shipment;
import com.commerceops.erp.domain.shipment.enums.ShipmentStatus;
import com.commerceops.erp.domain.shipment.enums.TrackingNumberSource;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final WarehouseFulfillmentService warehouseFulfillmentService;
    private final AuditLogService auditLogService;

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
    public ShipmentResponse updateTracking(Long shipmentId, TrackingUpdateRequest request, User actor) {
        Shipment shipment = findShipment(shipmentId);
        String before = shipment.getTrackingNumber();
        if (shipment.getStatus() == ShipmentStatus.DELIVERED) {
            throw new BusinessException(ErrorCode.SHIPMENT_ALREADY_DELIVERED);
        }
        if (shipment.getStatus() == ShipmentStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.SHIPMENT_CANCELLED);
        }

        Order order = shipment.getOrder();
        if (shipment.getStatus() == ShipmentStatus.READY) {
            warehouseFulfillmentService.shipOrder(order);
            order.updateStatus(OrderStatus.SHIPPING);
        }
        shipment.updateTracking(request.trackingNumber(), request.carrier(), TrackingNumberSource.MANUAL);
        auditLogService.record(actor, AuditActionType.TRACKING_NUMBER_UPDATED, "SHIPMENT", shipment.getId(),
                before, shipment.getTrackingNumber(), "송장번호를 수동 저장했습니다: " + shipment.getTrackingNumber(),
                "{\"trackingNumber\":\"" + escapeJson(before) + "\"}", toTrackingJson(shipment), null);

        return ShipmentResponse.from(shipment);
    }

    @Transactional
    public ShipmentResponse generateTrackingNumber(Long shipmentId, String carrier, User actor) {
        Shipment shipment = findShipment(shipmentId);
        String before = shipment.getTrackingNumber();
        if (shipment.getStatus() == ShipmentStatus.DELIVERED) {
            throw new BusinessException(ErrorCode.SHIPMENT_ALREADY_DELIVERED);
        }
        if (shipment.getStatus() == ShipmentStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.SHIPMENT_CANCELLED);
        }

        Order order = shipment.getOrder();
        if (shipment.getStatus() == ShipmentStatus.READY) {
            warehouseFulfillmentService.shipOrder(order);
            order.updateStatus(OrderStatus.SHIPPING);
        }
        shipment.updateTracking(generateTrackingNumberCandidate(), carrier, TrackingNumberSource.SYSTEM);
        auditLogService.record(actor, AuditActionType.TRACKING_NUMBER_GENERATED, "SHIPMENT", shipment.getId(),
                before, shipment.getTrackingNumber(), "송장번호를 자동 생성했습니다: " + shipment.getTrackingNumber(),
                "{\"trackingNumber\":\"" + escapeJson(before) + "\"}", toTrackingJson(shipment), null);
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

    private String generateTrackingNumberCandidate() {
        LocalDate today = LocalDate.now();
        LocalDateTime from = today.atStartOfDay();
        LocalDateTime to = today.atTime(LocalTime.MAX);
        String date = today.format(DateTimeFormatter.BASIC_ISO_DATE);
        long sequence = shipmentRepository.countByTrackingNumberIssuedAtBetween(from, to) + 1;
        String candidate;
        do {
            candidate = "TRK-" + date + "-" + String.format("%06d", sequence++);
        } while (shipmentRepository.existsByTrackingNumber(candidate));
        return candidate;
    }

    private String toTrackingJson(Shipment shipment) {
        return "{\"shipmentId\":" + shipment.getId()
                + ",\"trackingNumber\":\"" + escapeJson(shipment.getTrackingNumber()) + "\""
                + ",\"carrier\":\"" + escapeJson(shipment.getCarrier()) + "\""
                + ",\"source\":\"" + (shipment.getTrackingNumberSource() != null ? shipment.getTrackingNumberSource().name() : "") + "\""
                + "}";
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
