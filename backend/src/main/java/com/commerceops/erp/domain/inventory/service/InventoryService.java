package com.commerceops.erp.domain.inventory.service;

import com.commerceops.erp.domain.accounting.service.AccountingService;
import com.commerceops.erp.domain.inventory.dto.*;
import com.commerceops.erp.domain.inventory.entity.InventoryLog;
import com.commerceops.erp.domain.inventory.enums.InventoryLogType;
import com.commerceops.erp.domain.inventory.repository.InventoryLogRepository;
import com.commerceops.erp.domain.product.entity.Product;
import com.commerceops.erp.domain.product.enums.ProductStatus;
import com.commerceops.erp.domain.product.repository.ProductRepository;
import com.commerceops.erp.domain.warehouse.entity.Warehouse;
import com.commerceops.erp.domain.warehouse.entity.WarehouseStock;
import com.commerceops.erp.domain.warehouse.repository.WarehouseRepository;
import com.commerceops.erp.domain.warehouse.repository.WarehouseStockRepository;
import com.commerceops.erp.global.exception.BusinessException;
import com.commerceops.erp.global.exception.ErrorCode;
import com.commerceops.erp.global.response.PageResponse;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {

    private static final int LOW_STOCK_THRESHOLD = 5;

    private final ProductRepository productRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final AccountingService accountingService;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseStockRepository warehouseStockRepository;

    public PageResponse<InventoryResponse> getInventoryList(String keyword, String status, boolean lowStockOnly, Pageable pageable) {
        Specification<Product> spec = buildInventorySpec(keyword, status, lowStockOnly);
        return PageResponse.from(productRepository.findAll(spec, pageable).map(InventoryResponse::from));
    }

    @Transactional
    public InventoryStockChangeResponse inbound(InventoryInboundRequest request) {
        Warehouse warehouse = findActiveWarehouse(request.warehouseId());
        Product product = findActiveProductForUpdate(request.productId());
        WarehouseStock warehouseStock = warehouseStockRepository.findForUpdate(warehouse.getId(), product.getId())
                .orElseGet(() -> WarehouseStock.builder()
                        .warehouse(warehouse)
                        .product(product)
                        .quantity(0)
                        .build());

        int beforeStock = product.getStockQuantity();
        int beforeWarehouseStock = warehouseStock.getQuantity();
        product.incrementStock(request.quantity());
        warehouseStock.increase(request.quantity());
        int afterStock = product.getStockQuantity();
        warehouseStockRepository.save(warehouseStock);

        inventoryLogRepository.save(InventoryLog.builder()
                .product(product)
                .type(InventoryLogType.INBOUND)
                .quantity(request.quantity())
                .beforeStock(beforeStock)
                .afterStock(afterStock)
                .memo(request.memo())
                .build());

        accountingService.recordInbound(product.getId(), product.getName(), product.getPrice(), request.quantity());

        return new InventoryStockChangeResponse(warehouse.getId(), product.getId(), request.quantity(),
                beforeStock, afterStock, beforeWarehouseStock, warehouseStock.getQuantity(),
                InventoryLogType.INBOUND.name());
    }

    @Transactional
    public InventoryStockChangeResponse adjust(InventoryAdjustRequest request) {
        Warehouse warehouse = findActiveWarehouse(request.warehouseId());
        Product product = findActiveProductForUpdate(request.productId());
        WarehouseStock warehouseStock = warehouseStockRepository.findForUpdate(warehouse.getId(), product.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.WAREHOUSE_STOCK_NOT_FOUND));
        if (request.quantity() < warehouseStock.getReservedQuantity()) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_WAREHOUSE_STOCK);
        }

        int beforeStock = product.getStockQuantity();
        int beforeWarehouseStock = warehouseStock.getQuantity();
        int delta = request.quantity() - beforeWarehouseStock;
        product.adjustStock(beforeStock + delta);
        warehouseStock.adjustQuantity(request.quantity());
        int afterStock = product.getStockQuantity();

        inventoryLogRepository.save(InventoryLog.builder()
                .product(product)
                .type(InventoryLogType.ADJUST)
                .quantity(Math.abs(delta))
                .beforeStock(beforeStock)
                .afterStock(afterStock)
                .memo(request.memo())
                .build());

        return new InventoryStockChangeResponse(warehouse.getId(), product.getId(), null,
                beforeStock, afterStock, beforeWarehouseStock, warehouseStock.getQuantity(),
                InventoryLogType.ADJUST.name());
    }

    public PageResponse<InventoryLogResponse> getLogs(Long productId, InventoryLogType type, Pageable pageable) {
        return PageResponse.from(
                inventoryLogRepository.findAllForAdmin(productId, type, pageable).map(InventoryLogResponse::from));
    }

    private Product findActiveProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        if (product.getStatus() == ProductStatus.DELETED) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_AVAILABLE);
        }
        return product;
    }

    private Product findActiveProductForUpdate(Long productId) {
        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        if (product.getStatus() == ProductStatus.DELETED) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_AVAILABLE);
        }
        return product;
    }

    private Warehouse findActiveWarehouse(Long warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WAREHOUSE_NOT_FOUND));
        if (!warehouse.isActive()) {
            throw new BusinessException(ErrorCode.WAREHOUSE_INACTIVE);
        }
        return warehouse;
    }

    private Specification<Product> buildInventorySpec(String keyword, String status, boolean lowStockOnly) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.notEqual(root.get("status"), ProductStatus.DELETED));

            if (keyword != null && !keyword.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%"));
            }
            if (lowStockOnly) {
                predicates.add(cb.lessThanOrEqualTo(root.get("stockQuantity"), LOW_STOCK_THRESHOLD));
            }
            if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
                switch (status.toUpperCase()) {
                    case "NORMAL" -> predicates.add(cb.greaterThan(root.get("stockQuantity"), LOW_STOCK_THRESHOLD));
                    case "LOW_STOCK", "LOW" -> {
                        predicates.add(cb.greaterThan(root.get("stockQuantity"), 0));
                        predicates.add(cb.lessThanOrEqualTo(root.get("stockQuantity"), LOW_STOCK_THRESHOLD));
                    }
                    case "OUT_OF_STOCK" -> predicates.add(cb.lessThanOrEqualTo(root.get("stockQuantity"), 0));
                    default -> {
                    }
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
