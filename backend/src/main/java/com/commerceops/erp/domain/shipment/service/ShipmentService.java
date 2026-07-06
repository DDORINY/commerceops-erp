package com.commerceops.erp.domain.shipment.service;

import com.commerceops.erp.domain.audit.enums.AuditActionType;
import com.commerceops.erp.domain.audit.service.AuditLogService;
import com.commerceops.erp.domain.order.entity.Order;
import com.commerceops.erp.domain.order.enums.OrderStatus;
import com.commerceops.erp.domain.order.repository.OrderRepository;
import com.commerceops.erp.domain.shipment.dto.ShipmentResponse;
import com.commerceops.erp.domain.shipment.dto.ShipmentLabelPreviewResponse;
import com.commerceops.erp.domain.shipment.dto.ShipmentLabelRequest;
import com.commerceops.erp.domain.shipment.dto.ShipmentLabelResponse;
import com.commerceops.erp.domain.shipment.dto.TrackingUpdateRequest;
import com.commerceops.erp.domain.shipment.entity.Shipment;
import com.commerceops.erp.domain.shipment.entity.ShipmentLabel;
import com.commerceops.erp.domain.shipment.enums.ShipmentStatus;
import com.commerceops.erp.domain.shipment.enums.TrackingNumberSource;
import com.commerceops.erp.domain.shipment.repository.ShipmentLabelRepository;
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
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShipmentService {

    private static final String DEFAULT_LABEL_FORMAT = "SHIPMENT_100X150";

    private final ShipmentRepository shipmentRepository;
    private final ShipmentLabelRepository shipmentLabelRepository;
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

    public List<ShipmentLabelResponse> getShipmentLabels(Long shipmentId) {
        findShipment(shipmentId);
        return shipmentLabelRepository.findByShipmentIdOrderByCreatedAtDesc(shipmentId).stream()
                .map(ShipmentLabelResponse::from)
                .toList();
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
    public ShipmentLabelPreviewResponse createShipmentLabel(Long shipmentId, ShipmentLabelRequest request, User actor) {
        Shipment shipment = findShipment(shipmentId);
        validateLabelTarget(shipment);
        String format = normalize(request != null ? request.labelFormat() : null);
        if (format == null) {
            format = DEFAULT_LABEL_FORMAT;
        }

        ShipmentLabel label = ShipmentLabel.builder()
                .shipment(shipment)
                .trackingNumber(shipment.getTrackingNumber())
                .carrier(shipment.getCarrier())
                .labelFormat(format)
                .printCount(0)
                .createdBy(actor)
                .build();
        ShipmentLabel saved = shipmentLabelRepository.save(label);
        auditLogService.record(actor, AuditActionType.SHIPMENT_LABEL_CREATED, "SHIPMENT_LABEL", saved.getId(),
                null, saved.getTrackingNumber(), "송장 라벨을 생성했습니다: " + saved.getTrackingNumber(),
                null, null, labelMetadata(saved));
        return toLabelPreview(saved);
    }

    @Transactional
    public ShipmentLabelPreviewResponse markShipmentLabelPrinted(Long labelId, User actor) {
        ShipmentLabel label = shipmentLabelRepository.findById(labelId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "송장 라벨을 찾을 수 없습니다."));
        label.markPrinted();
        auditLogService.record(actor, AuditActionType.SHIPMENT_LABEL_PRINTED, "SHIPMENT_LABEL", label.getId(),
                null, String.valueOf(label.getPrintCount()), "송장 라벨 출력 이력을 기록했습니다: " + label.getTrackingNumber(),
                null, null, labelMetadata(label));
        return toLabelPreview(label);
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

    private void validateLabelTarget(Shipment shipment) {
        if (normalize(shipment.getTrackingNumber()) == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "송장번호가 있어야 라벨을 생성할 수 있습니다.");
        }
        if (normalize(shipment.getCarrier()) == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "택배사가 있어야 라벨을 생성할 수 있습니다.");
        }
        if (shipment.getStatus() == ShipmentStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.SHIPMENT_CANCELLED);
        }
    }

    private ShipmentLabelPreviewResponse toLabelPreview(ShipmentLabel label) {
        Order order = label.getShipment().getOrder();
        String address = order.getAddress() + " " + (order.getDetailAddress() != null ? order.getDetailAddress() : "");
        String html = "<div class=\"shipment-label\">"
                + "<div style=\"font-size:18px;font-weight:700\">" + escapeHtml(label.getCarrier()) + "</div>"
                + "<div style=\"margin-top:8px;font-size:26px;font-weight:700;letter-spacing:1px\">" + escapeHtml(label.getTrackingNumber()) + "</div>"
                + "<div style=\"margin-top:10px\">주문번호: " + escapeHtml(order.getOrderNumber()) + "</div>"
                + "<div>수령인: " + escapeHtml(order.getReceiverName()) + " / " + escapeHtml(order.getReceiverPhone()) + "</div>"
                + "<div>주소: " + escapeHtml(address.trim()) + "</div>"
                + "</div>";
        return new ShipmentLabelPreviewResponse(label.getId(), label.getLabelFormat(), label.getTrackingNumber(),
                label.getCarrier(), order.getOrderNumber(), order.getReceiverName(), order.getReceiverPhone(),
                address.trim(), label.getPrintCount(), html);
    }

    private String labelMetadata(ShipmentLabel label) {
        return "{\"shipmentId\":" + label.getShipment().getId()
                + ",\"orderId\":" + label.getShipment().getOrder().getId()
                + ",\"trackingNumber\":\"" + escapeJson(label.getTrackingNumber()) + "\""
                + ",\"carrier\":\"" + escapeJson(label.getCarrier()) + "\""
                + ",\"labelFormat\":\"" + escapeJson(label.getLabelFormat()) + "\""
                + ",\"printCount\":" + label.getPrintCount() + "}";
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

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
