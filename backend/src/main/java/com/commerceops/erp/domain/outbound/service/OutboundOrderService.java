package com.commerceops.erp.domain.outbound.service;

import com.commerceops.erp.domain.audit.enums.AuditActionType;
import com.commerceops.erp.domain.audit.service.AuditLogService;
import com.commerceops.erp.domain.order.entity.Order;
import com.commerceops.erp.domain.order.entity.OrderItem;
import com.commerceops.erp.domain.order.repository.OrderItemRepository;
import com.commerceops.erp.domain.order.repository.OrderRepository;
import com.commerceops.erp.domain.outbound.dto.OutboundBarcodeScanRequest;
import com.commerceops.erp.domain.outbound.dto.OutboundOrderCreateRequest;
import com.commerceops.erp.domain.outbound.dto.OutboundOrderResponse;
import com.commerceops.erp.domain.outbound.dto.OutboundOrderUpdateRequest;
import com.commerceops.erp.domain.outbound.entity.OutboundOrder;
import com.commerceops.erp.domain.outbound.entity.OutboundOrderItem;
import com.commerceops.erp.domain.outbound.enums.OutboundOrderStatus;
import com.commerceops.erp.domain.outbound.repository.OutboundOrderRepository;
import com.commerceops.erp.domain.outbound.entity.OutboundScanLog;
import com.commerceops.erp.domain.outbound.repository.OutboundScanLogRepository;
import com.commerceops.erp.domain.sku.entity.Sku;
import com.commerceops.erp.domain.sku.repository.SkuRepository;
import com.commerceops.erp.domain.user.entity.User;
import com.commerceops.erp.domain.warehouse.entity.Warehouse;
import com.commerceops.erp.domain.warehouse.repository.WarehouseRepository;
import com.commerceops.erp.global.exception.BusinessException;
import com.commerceops.erp.global.exception.ErrorCode;
import com.commerceops.erp.global.response.PageResponse;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OutboundOrderService {

    private final OutboundOrderRepository outboundOrderRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final WarehouseRepository warehouseRepository;
    private final SkuRepository skuRepository;
    private final OutboundScanLogRepository outboundScanLogRepository;
    private final AuditLogService auditLogService;

    public PageResponse<OutboundOrderResponse> getOutboundOrders(OutboundOrderStatus status, Long warehouseId,
                                                                 Long orderId, String keyword,
                                                                 int page, int size) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.max(1, Math.min(size, 100)),
                Sort.by("createdAt").descending());
        return PageResponse.from(outboundOrderRepository.findAll(
                buildSpec(status, warehouseId, orderId, keyword), pageable)
                .map(OutboundOrderResponse::from));
    }

    public OutboundOrderResponse getOutboundOrder(Long outboundOrderId) {
        return OutboundOrderResponse.from(findOutboundOrder(outboundOrderId));
    }

    @Transactional
    public OutboundOrderResponse createOutboundOrder(OutboundOrderCreateRequest request, User actor) {
        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        Warehouse warehouse = findActiveWarehouse(request.warehouseId());
        List<OrderItem> orderItems = orderItemRepository.findAllByOrderWithProduct(order);
        if (orderItems.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "출고 지시를 만들 주문 상품이 없습니다.");
        }
        OutboundOrder outboundOrder = OutboundOrder.builder()
                .outboundNumber(generateOutboundNumber())
                .order(order)
                .warehouse(warehouse)
                .status(OutboundOrderStatus.REQUESTED)
                .requestedAt(LocalDateTime.now())
                .memo(normalize(request.memo()))
                .createdBy(actor)
                .updatedBy(actor)
                .build();
        outboundOrder.replaceItems(buildItems(orderItems));
        OutboundOrder saved = outboundOrderRepository.save(outboundOrder);
        auditLogService.record(actor, AuditActionType.OUTBOUND_ORDER_CREATED, "OUTBOUND_ORDER", saved.getId(),
                null, saved.getStatus().name(), "출고 지시를 생성했습니다: " + saved.getOutboundNumber(),
                null, toJson(saved), "{\"orderId\":" + order.getId() + "}");
        return OutboundOrderResponse.from(saved);
    }

    @Transactional
    public OutboundOrderResponse updateOutboundOrder(Long outboundOrderId, OutboundOrderUpdateRequest request, User actor) {
        OutboundOrder outboundOrder = outboundOrderRepository.findForUpdate(outboundOrderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "출고 지시를 찾을 수 없습니다."));
        ensureEditable(outboundOrder);
        String beforeJson = toJson(outboundOrder);
        Warehouse warehouse = findActiveWarehouse(request.warehouseId());
        outboundOrder.update(warehouse, normalize(request.memo()), actor);
        auditLogService.record(actor, AuditActionType.OUTBOUND_ORDER_UPDATED, "OUTBOUND_ORDER", outboundOrder.getId(),
                null, outboundOrder.getStatus().name(), "출고 지시를 수정했습니다: " + outboundOrder.getOutboundNumber(),
                beforeJson, toJson(outboundOrder), null);
        return OutboundOrderResponse.from(outboundOrder);
    }

    @Transactional
    public OutboundOrderResponse pickOutboundOrder(Long outboundOrderId, User actor) {
        OutboundOrder outboundOrder = outboundOrderRepository.findForUpdate(outboundOrderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "출고 지시를 찾을 수 없습니다."));
        if (outboundOrder.getStatus() == OutboundOrderStatus.SHIPPED || outboundOrder.getStatus() == OutboundOrderStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "배송 완료 또는 취소된 출고 지시는 피킹 처리할 수 없습니다.");
        }
        OutboundOrderStatus before = outboundOrder.getStatus();
        outboundOrder.markPicked(actor);
        auditLogService.record(actor, AuditActionType.OUTBOUND_ORDER_PICKED, "OUTBOUND_ORDER", outboundOrder.getId(),
                before.name(), outboundOrder.getStatus().name(), "출고 지시를 피킹 완료 처리했습니다: " + outboundOrder.getOutboundNumber(),
                null, toJson(outboundOrder), null);
        return OutboundOrderResponse.from(outboundOrder);
    }

    @Transactional
    public OutboundOrderResponse scanOutboundItem(Long outboundOrderId, OutboundBarcodeScanRequest request, User actor) {
        OutboundOrder outboundOrder = outboundOrderRepository.findWithItemsById(outboundOrderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "출고 지시를 찾을 수 없습니다."));
        if (outboundOrder.getStatus() == OutboundOrderStatus.SHIPPED || outboundOrder.getStatus() == OutboundOrderStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "배송 완료 또는 취소된 출고 지시는 검수할 수 없습니다.");
        }

        String barcode = normalize(request.barcode());
        if (barcode == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "바코드를 입력해주세요.");
        }
        int scanQuantity = request.quantity() == null ? 1 : request.quantity();
        Sku sku = skuRepository.findByBarcode(barcode)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "바코드와 일치하는 SKU를 찾을 수 없습니다."));
        OutboundOrderItem matchedItem = outboundOrder.getItems().stream()
                .filter(item -> item.getSku() != null && item.getSku().getId().equals(sku.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "해당 출고 지시에 포함되지 않은 바코드입니다."));

        try {
            matchedItem.scan(scanQuantity);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, ex.getMessage());
        }

        outboundOrder.markPicking(actor);
        outboundOrder.markPickedIfFullyScanned(actor);
        outboundScanLogRepository.save(OutboundScanLog.builder()
                .outboundOrder(outboundOrder)
                .outboundOrderItem(matchedItem)
                .sku(sku)
                .barcode(barcode)
                .quantity(scanQuantity)
                .scannedBy(actor)
                .build());

        auditLogService.record(actor, AuditActionType.OUTBOUND_ORDER_ITEM_SCANNED, "OUTBOUND_ORDER", outboundOrder.getId(),
                null, outboundOrder.getStatus().name(),
                "출고 바코드 검수를 기록했습니다: " + outboundOrder.getOutboundNumber(),
                null, toJson(outboundOrder), "{\"barcode\":\"" + escapeJson(barcode) + "\",\"quantity\":" + scanQuantity + "}");
        return OutboundOrderResponse.from(outboundOrder);
    }

    @Transactional
    public OutboundOrderResponse cancelOutboundOrder(Long outboundOrderId, User actor) {
        OutboundOrder outboundOrder = outboundOrderRepository.findForUpdate(outboundOrderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "출고 지시를 찾을 수 없습니다."));
        if (outboundOrder.getStatus() == OutboundOrderStatus.SHIPPED || outboundOrder.getStatus() == OutboundOrderStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "배송 완료 또는 이미 취소된 출고 지시는 취소할 수 없습니다.");
        }
        OutboundOrderStatus before = outboundOrder.getStatus();
        outboundOrder.cancel(actor);
        auditLogService.record(actor, AuditActionType.OUTBOUND_ORDER_CANCELLED, "OUTBOUND_ORDER", outboundOrder.getId(),
                before.name(), outboundOrder.getStatus().name(), "출고 지시를 취소했습니다: " + outboundOrder.getOutboundNumber(),
                null, toJson(outboundOrder), null);
        return OutboundOrderResponse.from(outboundOrder);
    }

    private List<OutboundOrderItem> buildItems(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(orderItem -> {
                    Sku sku = skuRepository.findTopByProductIdOrderByIdDesc(orderItem.getProduct().getId()).orElse(null);
                    return OutboundOrderItem.builder()
                            .orderItem(orderItem)
                            .sku(sku)
                            .product(orderItem.getProduct())
                            .quantity(orderItem.getQuantity())
                            .pickedQuantity(0)
                            .scannedQuantity(0)
                            .build();
                })
                .toList();
    }

    private OutboundOrder findOutboundOrder(Long outboundOrderId) {
        return outboundOrderRepository.findWithItemsById(outboundOrderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "출고 지시를 찾을 수 없습니다."));
    }

    private Warehouse findActiveWarehouse(Long warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WAREHOUSE_NOT_FOUND));
        if (!warehouse.isActive()) {
            throw new BusinessException(ErrorCode.WAREHOUSE_INACTIVE);
        }
        return warehouse;
    }

    private void ensureEditable(OutboundOrder outboundOrder) {
        if (outboundOrder.getStatus() == OutboundOrderStatus.SHIPPED || outboundOrder.getStatus() == OutboundOrderStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "배송 완료 또는 취소된 출고 지시는 수정할 수 없습니다.");
        }
    }

    private String generateOutboundNumber() {
        LocalDate today = LocalDate.now();
        LocalDateTime from = today.atStartOfDay();
        LocalDateTime to = today.atTime(LocalTime.MAX);
        long sequence = outboundOrderRepository.countByCreatedAtBetween(from, to) + 1;
        String date = today.format(DateTimeFormatter.BASIC_ISO_DATE);
        String candidate;
        do {
            candidate = "OUT-" + date + "-" + String.format("%04d", sequence++);
        } while (outboundOrderRepository.existsByOutboundNumber(candidate));
        return candidate;
    }

    private Specification<OutboundOrder> buildSpec(OutboundOrderStatus status, Long warehouseId, Long orderId, String keyword) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (query != null) {
                query.distinct(true);
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (warehouseId != null) {
                predicates.add(cb.equal(root.get("warehouse").get("id"), warehouseId));
            }
            if (orderId != null) {
                predicates.add(cb.equal(root.get("order").get("id"), orderId));
            }
            String normalizedKeyword = normalize(keyword);
            if (normalizedKeyword != null) {
                String pattern = "%" + normalizedKeyword.toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("outboundNumber")), pattern),
                        cb.like(cb.lower(root.get("order").get("orderNumber")), pattern),
                        cb.like(cb.lower(root.get("order").get("receiverName")), pattern),
                        cb.like(cb.lower(root.join("items", JoinType.LEFT).get("product").get("name")), pattern)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String toJson(OutboundOrder outboundOrder) {
        return "{"
                + "\"outboundOrderId\":" + outboundOrder.getId()
                + ",\"outboundNumber\":\"" + escapeJson(outboundOrder.getOutboundNumber()) + "\""
                + ",\"orderId\":" + outboundOrder.getOrder().getId()
                + ",\"warehouseId\":" + outboundOrder.getWarehouse().getId()
                + ",\"status\":\"" + outboundOrder.getStatus().name() + "\""
                + "}";
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
