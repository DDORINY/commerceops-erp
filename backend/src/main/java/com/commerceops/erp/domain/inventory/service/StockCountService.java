package com.commerceops.erp.domain.inventory.service;

import com.commerceops.erp.domain.audit.enums.AuditActionType;
import com.commerceops.erp.domain.audit.service.AuditLogService;
import com.commerceops.erp.domain.inventory.dto.StockCountCreateRequest;
import com.commerceops.erp.domain.inventory.dto.StockCountItemInput;
import com.commerceops.erp.domain.inventory.dto.StockCountItemsUpdateRequest;
import com.commerceops.erp.domain.inventory.dto.StockCountResponse;
import com.commerceops.erp.domain.inventory.entity.InventoryLog;
import com.commerceops.erp.domain.inventory.entity.StockCountItem;
import com.commerceops.erp.domain.inventory.entity.StockCountSession;
import com.commerceops.erp.domain.inventory.enums.InventoryLogType;
import com.commerceops.erp.domain.inventory.enums.StockCountStatus;
import com.commerceops.erp.domain.inventory.repository.InventoryLogRepository;
import com.commerceops.erp.domain.inventory.repository.StockCountItemRepository;
import com.commerceops.erp.domain.inventory.repository.StockCountSessionRepository;
import com.commerceops.erp.domain.product.entity.Product;
import com.commerceops.erp.domain.product.enums.ProductStatus;
import com.commerceops.erp.domain.product.repository.ProductRepository;
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockCountService {

    private final StockCountSessionRepository sessionRepository;
    private final StockCountItemRepository itemRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final SkuRepository skuRepository;
    private final ProductRepository productRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final AuditLogService auditLogService;

    public PageResponse<StockCountResponse> getSessions(StockCountStatus status, int page, int size) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.max(1, Math.min(size, 100)), Sort.by("createdAt").descending());
        if (status == null) {
            return PageResponse.from(sessionRepository.findAll(pageable).map(StockCountResponse::summary));
        }
        return PageResponse.from(sessionRepository.findByStatus(status, pageable).map(StockCountResponse::summary));
    }

    public StockCountResponse getSession(Long sessionId) {
        return StockCountResponse.from(findDetail(sessionId));
    }

    @Transactional
    public StockCountResponse create(StockCountCreateRequest request, User actor) {
        Warehouse warehouse = findActiveWarehouse(request.warehouseId());
        StockCountSession session = StockCountSession.builder()
                .countNumber(generateCountNumber())
                .warehouse(warehouse)
                .status(StockCountStatus.DRAFT)
                .memo(request.memo())
                .build();
        StockCountSession saved = sessionRepository.save(session);
        auditLogService.record(actor, AuditActionType.STOCK_COUNT_CREATED, "STOCK_COUNT", saved.getId(),
                null, saved.getStatus().name(), "재고 실사 세션을 생성했습니다: " + saved.getCountNumber(),
                null, null, "{\"warehouseId\":" + warehouse.getId() + "}");
        return StockCountResponse.from(saved);
    }

    @Transactional
    public StockCountResponse updateItems(Long sessionId, StockCountItemsUpdateRequest request) {
        StockCountSession session = findDetail(sessionId);
        ensureEditable(session);
        for (StockCountItemInput input : request.items()) {
            Sku sku = skuRepository.findById(input.skuId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "SKU를 찾을 수 없습니다."));
            Product product = sku.getProduct();
            if (product.getStatus() == ProductStatus.DELETED) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_AVAILABLE);
            }
            int systemQuantity = warehouseStockRepository.findForUpdate(session.getWarehouse().getId(), product.getId())
                    .map(WarehouseStock::getQuantity)
                    .orElse(0);
            StockCountItem item = itemRepository.findBySessionIdAndSkuId(session.getId(), sku.getId())
                    .orElseGet(() -> StockCountItem.builder()
                            .session(session)
                            .sku(sku)
                            .product(product)
                            .systemQuantity(systemQuantity)
                            .build());
            item.updateCount(input.countedQuantity(), input.memo());
            itemRepository.save(item);
        }
        return StockCountResponse.from(findDetail(sessionId));
    }

    @Transactional
    public StockCountResponse start(Long sessionId, User actor) {
        StockCountSession session = findDetail(sessionId);
        if (session.getStatus() != StockCountStatus.DRAFT) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "임시저장 상태의 실사만 시작할 수 있습니다.");
        }
        session.start(actor);
        auditLogService.record(actor, AuditActionType.STOCK_COUNT_STARTED, "STOCK_COUNT", session.getId(),
                StockCountStatus.DRAFT.name(), session.getStatus().name(), "재고 실사를 시작했습니다: " + session.getCountNumber(),
                null, null, "{\"warehouseId\":" + session.getWarehouse().getId() + "}");
        return StockCountResponse.from(session);
    }

    @Transactional
    public StockCountResponse complete(Long sessionId, User actor) {
        StockCountSession session = findDetail(sessionId);
        if (session.getStatus() != StockCountStatus.IN_PROGRESS && session.getStatus() != StockCountStatus.DRAFT) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "진행 중 또는 임시저장 상태의 실사만 완료할 수 있습니다.");
        }

        for (StockCountItem item : session.getItems()) {
            if (item.getCountedQuantity() == null || item.getDifferenceQuantity() == 0) {
                continue;
            }
            applyAdjustment(session, item, actor);
        }

        StockCountStatus before = session.getStatus();
        session.complete(actor);
        auditLogService.record(actor, AuditActionType.STOCK_COUNT_COMPLETED, "STOCK_COUNT", session.getId(),
                before.name(), session.getStatus().name(), "재고 실사를 완료했습니다: " + session.getCountNumber(),
                null, null, "{\"warehouseId\":" + session.getWarehouse().getId() + ",\"itemCount\":" + session.getItems().size() + "}");
        return StockCountResponse.from(session);
    }

    @Transactional
    public StockCountResponse cancel(Long sessionId, User actor) {
        StockCountSession session = findDetail(sessionId);
        if (session.getStatus() == StockCountStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "완료된 실사는 취소할 수 없습니다.");
        }
        StockCountStatus before = session.getStatus();
        session.cancel();
        auditLogService.record(actor, AuditActionType.STOCK_COUNT_CANCELLED, "STOCK_COUNT", session.getId(),
                before.name(), session.getStatus().name(), "재고 실사를 취소했습니다: " + session.getCountNumber(),
                null, null, "{\"warehouseId\":" + session.getWarehouse().getId() + "}");
        return StockCountResponse.from(session);
    }

    private void applyAdjustment(StockCountSession session, StockCountItem item, User actor) {
        Product product = productRepository.findByIdForUpdate(item.getProduct().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        WarehouseStock stock = warehouseStockRepository.findForUpdate(session.getWarehouse().getId(), product.getId())
                .orElseGet(() -> WarehouseStock.builder()
                        .warehouse(session.getWarehouse())
                        .product(product)
                        .quantity(0)
                        .build());

        int beforeProductStock = product.getStockQuantity();
        int beforeWarehouseStock = stock.getQuantity();
        int targetWarehouseStock = item.getCountedQuantity();
        int delta = targetWarehouseStock - beforeWarehouseStock;
        int targetProductStock = beforeProductStock + delta;
        if (targetProductStock < 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "실사 조정 후 상품 재고가 음수가 될 수 없습니다.");
        }
        if (targetWarehouseStock < stock.getReservedQuantity()) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_WAREHOUSE_STOCK);
        }

        product.adjustStock(targetProductStock);
        stock.adjustQuantity(targetWarehouseStock);
        warehouseStockRepository.save(stock);
        InventoryLog log = inventoryLogRepository.save(InventoryLog.builder()
                .product(product)
                .type(InventoryLogType.ADJUST)
                .quantity(Math.abs(delta))
                .beforeStock(beforeProductStock)
                .afterStock(product.getStockQuantity())
                .memo("재고 실사 조정 - " + session.getCountNumber() + " / SKU " + item.getSku().getSkuCode())
                .build());

        auditLogService.record(actor, AuditActionType.STOCK_COUNT_ADJUSTMENT_CREATED, "PRODUCT", product.getId(),
                String.valueOf(beforeProductStock), String.valueOf(product.getStockQuantity()),
                "재고 실사 차이를 조정했습니다: " + session.getCountNumber(),
                null, null, "{\"stockCountId\":" + session.getId() + ",\"itemId\":" + item.getId()
                        + ",\"warehouseId\":" + session.getWarehouse().getId()
                        + ",\"beforeWarehouseStock\":" + beforeWarehouseStock
                        + ",\"afterWarehouseStock\":" + stock.getQuantity()
                        + ",\"inventoryLogId\":" + log.getId() + "}");
    }

    private StockCountSession findDetail(Long sessionId) {
        return sessionRepository.findDetailById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "재고 실사 세션을 찾을 수 없습니다."));
    }

    private Warehouse findActiveWarehouse(Long warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WAREHOUSE_NOT_FOUND));
        if (!warehouse.isActive()) {
            throw new BusinessException(ErrorCode.WAREHOUSE_INACTIVE);
        }
        return warehouse;
    }

    private void ensureEditable(StockCountSession session) {
        if (session.getStatus() == StockCountStatus.COMPLETED || session.getStatus() == StockCountStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "완료 또는 취소된 실사는 수정할 수 없습니다.");
        }
    }

    private String generateCountNumber() {
        String prefix = "SC-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-";
        String number;
        do {
            number = prefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (sessionRepository.existsByCountNumber(number));
        return number;
    }
}
