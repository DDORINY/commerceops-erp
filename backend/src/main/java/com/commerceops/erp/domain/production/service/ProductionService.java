package com.commerceops.erp.domain.production.service;

import com.commerceops.erp.domain.audit.enums.AuditActionType;
import com.commerceops.erp.domain.audit.service.AuditLogService;
import com.commerceops.erp.domain.inventory.entity.InventoryLog;
import com.commerceops.erp.domain.inventory.enums.InventoryLogType;
import com.commerceops.erp.domain.inventory.repository.InventoryLogRepository;
import com.commerceops.erp.domain.product.entity.Product;
import com.commerceops.erp.domain.product.enums.ProductStatus;
import com.commerceops.erp.domain.product.repository.ProductRepository;
import com.commerceops.erp.domain.production.dto.ProductionOrderCancelRequest;
import com.commerceops.erp.domain.production.dto.ProductionOrderCompleteRequest;
import com.commerceops.erp.domain.production.dto.ProductionOrderCreateRequest;
import com.commerceops.erp.domain.production.dto.ProductionOrderItemRequest;
import com.commerceops.erp.domain.production.dto.ProductionOrderListResponse;
import com.commerceops.erp.domain.production.dto.ProductionOrderResponse;
import com.commerceops.erp.domain.production.dto.ProductionOrderUpdateRequest;
import com.commerceops.erp.domain.production.dto.ProductionReceiptResponse;
import com.commerceops.erp.domain.production.entity.ProductionOrder;
import com.commerceops.erp.domain.production.entity.ProductionOrderItem;
import com.commerceops.erp.domain.production.entity.ProductionReceipt;
import com.commerceops.erp.domain.production.enums.ProductionOrderStatus;
import com.commerceops.erp.domain.production.repository.ProductionOrderRepository;
import com.commerceops.erp.domain.production.repository.ProductionReceiptRepository;
import com.commerceops.erp.domain.sku.entity.Sku;
import com.commerceops.erp.domain.sku.repository.SkuRepository;
import com.commerceops.erp.domain.user.entity.User;
import com.commerceops.erp.domain.warehouse.entity.Warehouse;
import com.commerceops.erp.domain.warehouse.entity.WarehouseStock;
import com.commerceops.erp.domain.warehouse.repository.WarehouseRepository;
import com.commerceops.erp.domain.warehouse.repository.WarehouseStockRepository;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductionService {

    private final ProductionOrderRepository productionOrderRepository;
    private final ProductionReceiptRepository productionReceiptRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final ProductRepository productRepository;
    private final SkuRepository skuRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final AuditLogService auditLogService;

    public PageResponse<ProductionOrderListResponse> getOrders(ProductionOrderStatus status, Long warehouseId,
                                                               Long skuId, String keyword,
                                                               LocalDateTime dateFrom, LocalDateTime dateTo,
                                                               int page, int size) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.max(1, Math.min(size, 100)),
                Sort.by("createdAt").descending());
        return PageResponse.from(productionOrderRepository.findAll(
                buildOrderSpec(status, warehouseId, skuId, keyword, dateFrom, dateTo), pageable)
                .map(ProductionOrderListResponse::from));
    }

    public ProductionOrderResponse getOrder(Long orderId) {
        return ProductionOrderResponse.from(findOrder(orderId));
    }

    public PageResponse<ProductionReceiptResponse> getReceipts(Long productionOrderId, Long skuId, Long warehouseId,
                                                               int page, int size) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.max(1, Math.min(size, 100)),
                Sort.by("createdAt").descending());
        return PageResponse.from(productionReceiptRepository.findAll(
                buildReceiptSpec(productionOrderId, skuId, warehouseId), pageable)
                .map(ProductionReceiptResponse::from));
    }

    public ProductionReceiptResponse getReceipt(Long receiptId) {
        return ProductionReceiptResponse.from(productionReceiptRepository.findWithRelationsById(receiptId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "생산 입고 이력을 찾을 수 없습니다.")));
    }

    @Transactional
    public ProductionOrderResponse createOrder(ProductionOrderCreateRequest request, User actor) {
        Warehouse warehouse = findActiveWarehouse(request.warehouseId());
        List<ProductionOrderItem> items = buildItems(request.items());
        ProductionOrder order = ProductionOrder.builder()
                .productionNumber(generateProductionNumber())
                .status(ProductionOrderStatus.PLANNED)
                .warehouse(warehouse)
                .plannedQuantity(0)
                .completedQuantity(0)
                .memo(normalize(request.memo()))
                .createdBy(actor)
                .updatedBy(actor)
                .build();
        order.replaceItems(items);
        ProductionOrder saved = productionOrderRepository.save(order);
        auditLogService.record(actor, AuditActionType.PRODUCTION_ORDER_CREATED, "PRODUCTION_ORDER", saved.getId(),
                null, saved.getStatus().name(), "생산 주문을 생성했습니다: " + saved.getProductionNumber(),
                null, toOrderJson(saved), null);
        return ProductionOrderResponse.from(saved);
    }

    @Transactional
    public ProductionOrderResponse updateOrder(Long orderId, ProductionOrderUpdateRequest request, User actor) {
        ProductionOrder order = productionOrderRepository.findForUpdate(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "생산 주문을 찾을 수 없습니다."));
        ensureEditable(order);
        String beforeJson = toOrderJson(order);
        Warehouse warehouse = findActiveWarehouse(request.warehouseId());
        order.update(warehouse, normalize(request.memo()), actor, buildItems(request.items()));
        auditLogService.record(actor, AuditActionType.PRODUCTION_ORDER_UPDATED, "PRODUCTION_ORDER", order.getId(),
                null, order.getStatus().name(), "생산 주문을 수정했습니다: " + order.getProductionNumber(),
                beforeJson, toOrderJson(order), null);
        return ProductionOrderResponse.from(order);
    }

    @Transactional
    public ProductionOrderResponse startOrder(Long orderId, User actor) {
        ProductionOrder order = productionOrderRepository.findForUpdate(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "생산 주문을 찾을 수 없습니다."));
        if (order.getStatus() != ProductionOrderStatus.PLANNED) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "계획 상태의 생산 주문만 시작할 수 있습니다.");
        }
        order.start(actor);
        auditLogService.record(actor, AuditActionType.PRODUCTION_ORDER_STARTED, "PRODUCTION_ORDER", order.getId(),
                ProductionOrderStatus.PLANNED.name(), order.getStatus().name(),
                "생산 주문을 시작했습니다: " + order.getProductionNumber(), null, toOrderJson(order), null);
        return ProductionOrderResponse.from(order);
    }

    @Transactional
    public ProductionOrderResponse cancelOrder(Long orderId, ProductionOrderCancelRequest request, User actor) {
        ProductionOrder order = productionOrderRepository.findForUpdate(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "생산 주문을 찾을 수 없습니다."));
        if (order.getStatus() == ProductionOrderStatus.COMPLETED || order.getStatus() == ProductionOrderStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "완료 또는 취소된 생산 주문은 취소할 수 없습니다.");
        }
        ProductionOrderStatus before = order.getStatus();
        order.cancel(normalize(request != null ? request.memo() : null), actor);
        auditLogService.record(actor, AuditActionType.PRODUCTION_ORDER_CANCELLED, "PRODUCTION_ORDER", order.getId(),
                before.name(), order.getStatus().name(), "생산 주문을 취소했습니다: " + order.getProductionNumber(),
                null, toOrderJson(order), null);
        return ProductionOrderResponse.from(order);
    }

    @Transactional
    public ProductionOrderResponse completeOrder(Long orderId, ProductionOrderCompleteRequest request, User actor) {
        ProductionOrder order = productionOrderRepository.findForUpdate(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "생산 주문을 찾을 수 없습니다."));
        if (order.getStatus() == ProductionOrderStatus.COMPLETED || order.getStatus() == ProductionOrderStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "완료 또는 취소된 생산 주문은 완료 처리할 수 없습니다.");
        }

        Map<Long, Integer> completedBySkuId = new HashMap<>();
        request.items().forEach(item -> completedBySkuId.merge(item.skuId(), item.completedQuantity(), Integer::sum));
        int totalCompleted = 0;
        List<ProductionReceipt> receipts = new ArrayList<>();

        for (ProductionOrderItem item : order.getItems()) {
            Integer completedQuantity = completedBySkuId.get(item.getSku().getId());
            if (completedQuantity == null) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "모든 생산 품목의 완료 수량을 입력해주세요.");
            }
            if (completedQuantity > item.getPlannedQuantity()) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "완료 수량은 예정 수량을 초과할 수 없습니다.");
            }
            item.complete(completedQuantity);
            totalCompleted += completedQuantity;
            if (completedQuantity > 0) {
                receipts.add(receiveStock(order, item, completedQuantity, actor));
            }
        }

        if (totalCompleted <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "완료 수량은 1개 이상이어야 합니다.");
        }
        order.complete(totalCompleted, normalize(request.memo()), actor);
        productionReceiptRepository.saveAll(receipts);
        auditLogService.record(actor, AuditActionType.PRODUCTION_ORDER_COMPLETED, "PRODUCTION_ORDER", order.getId(),
                ProductionOrderStatus.IN_PROGRESS.name(), order.getStatus().name(),
                "생산 주문을 완료했습니다: " + order.getProductionNumber(),
                null, toOrderJson(order), "{\"receiptCount\":" + receipts.size() + "}");
        return ProductionOrderResponse.from(order);
    }

    private ProductionReceipt receiveStock(ProductionOrder order, ProductionOrderItem item, int quantity, User actor) {
        Sku sku = item.getSku();
        if (!Boolean.TRUE.equals(sku.getActive())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "비활성 SKU는 생산 완료 처리할 수 없습니다.");
        }
        Product product = productRepository.findByIdForUpdate(item.getProduct().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        if (product.getStatus() == ProductStatus.DELETED) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_AVAILABLE);
        }
        WarehouseStock warehouseStock = warehouseStockRepository.findForUpdate(order.getWarehouse().getId(), product.getId())
                .orElseGet(() -> WarehouseStock.builder()
                        .warehouse(order.getWarehouse())
                        .product(product)
                        .quantity(0)
                        .build());

        int beforeStock = product.getStockQuantity();
        product.incrementStock(quantity);
        warehouseStock.increase(quantity);
        warehouseStockRepository.save(warehouseStock);

        InventoryLog log = inventoryLogRepository.save(InventoryLog.builder()
                .product(product)
                .type(InventoryLogType.PRODUCTION_RECEIPT)
                .quantity(quantity)
                .beforeStock(beforeStock)
                .afterStock(product.getStockQuantity())
                .memo("생산 입고: " + order.getProductionNumber() + " / SKU " + sku.getSkuCode())
                .build());

        ProductionReceipt receipt = ProductionReceipt.builder()
                .productionOrder(order)
                .sku(sku)
                .product(product)
                .warehouse(order.getWarehouse())
                .quantity(quantity)
                .inventoryLogId(log.getId())
                .createdBy(actor)
                .build();
        ProductionReceipt saved = productionReceiptRepository.save(receipt);
        auditLogService.record(actor, AuditActionType.PRODUCTION_RECEIPT_CREATED, "PRODUCTION_RECEIPT", saved.getId(),
                null, String.valueOf(quantity), "생산 입고 이력을 생성했습니다: " + order.getProductionNumber(),
                null, null, "{\"productionOrderId\":" + order.getId()
                        + ",\"skuId\":" + sku.getId()
                        + ",\"productId\":" + product.getId()
                        + ",\"warehouseId\":" + order.getWarehouse().getId()
                        + ",\"quantity\":" + quantity + "}");
        return saved;
    }

    private List<ProductionOrderItem> buildItems(List<ProductionOrderItemRequest> requests) {
        return requests.stream()
                .map(request -> {
                    Sku sku = skuRepository.findById(request.skuId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "SKU를 찾을 수 없습니다."));
                    if (!Boolean.TRUE.equals(sku.getActive())) {
                        throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "비활성 SKU는 생산 주문에 추가할 수 없습니다.");
                    }
                    return ProductionOrderItem.builder()
                            .sku(sku)
                            .product(sku.getProduct())
                            .plannedQuantity(request.plannedQuantity())
                            .completedQuantity(0)
                            .build();
                })
                .toList();
    }

    private ProductionOrder findOrder(Long orderId) {
        return productionOrderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "생산 주문을 찾을 수 없습니다."));
    }

    private Warehouse findActiveWarehouse(Long warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WAREHOUSE_NOT_FOUND));
        if (!warehouse.isActive()) {
            throw new BusinessException(ErrorCode.WAREHOUSE_INACTIVE);
        }
        return warehouse;
    }

    private void ensureEditable(ProductionOrder order) {
        if (order.getStatus() == ProductionOrderStatus.COMPLETED || order.getStatus() == ProductionOrderStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "완료 또는 취소된 생산 주문은 수정할 수 없습니다.");
        }
    }

    private String generateProductionNumber() {
        LocalDate today = LocalDate.now();
        LocalDateTime from = today.atStartOfDay();
        LocalDateTime to = today.atTime(LocalTime.MAX);
        long sequence = productionOrderRepository.countByCreatedAtBetween(from, to) + 1;
        String date = today.format(DateTimeFormatter.BASIC_ISO_DATE);
        String candidate;
        do {
            candidate = "PRD-" + date + "-" + String.format("%04d", sequence++);
        } while (productionOrderRepository.existsByProductionNumber(candidate));
        return candidate;
    }

    private Specification<ProductionOrder> buildOrderSpec(ProductionOrderStatus status, Long warehouseId, Long skuId,
                                                          String keyword, LocalDateTime dateFrom, LocalDateTime dateTo) {
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
            if (skuId != null) {
                predicates.add(cb.equal(root.join("items", JoinType.LEFT).get("sku").get("id"), skuId));
            }
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), dateFrom));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), dateTo));
            }
            String normalizedKeyword = normalize(keyword);
            if (normalizedKeyword != null) {
                String pattern = "%" + normalizedKeyword.toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("productionNumber")), pattern),
                        cb.like(cb.lower(root.join("items", JoinType.LEFT).get("sku").get("skuCode")), pattern),
                        cb.like(cb.lower(root.join("items", JoinType.LEFT).get("product").get("name")), pattern)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<ProductionReceipt> buildReceiptSpec(Long productionOrderId, Long skuId, Long warehouseId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (productionOrderId != null) {
                predicates.add(cb.equal(root.get("productionOrder").get("id"), productionOrderId));
            }
            if (skuId != null) {
                predicates.add(cb.equal(root.get("sku").get("id"), skuId));
            }
            if (warehouseId != null) {
                predicates.add(cb.equal(root.get("warehouse").get("id"), warehouseId));
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

    private String toOrderJson(ProductionOrder order) {
        return "{"
                + "\"productionOrderId\":" + order.getId()
                + ",\"productionNumber\":\"" + escapeJson(order.getProductionNumber()) + "\""
                + ",\"status\":\"" + order.getStatus().name() + "\""
                + ",\"warehouseId\":" + order.getWarehouse().getId()
                + ",\"plannedQuantity\":" + order.getPlannedQuantity()
                + ",\"completedQuantity\":" + order.getCompletedQuantity()
                + "}";
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
