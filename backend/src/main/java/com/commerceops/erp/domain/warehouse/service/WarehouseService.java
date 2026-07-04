package com.commerceops.erp.domain.warehouse.service;

import com.commerceops.erp.domain.product.entity.Product;
import com.commerceops.erp.domain.product.enums.ProductStatus;
import com.commerceops.erp.domain.product.repository.ProductRepository;
import com.commerceops.erp.domain.warehouse.dto.*;
import com.commerceops.erp.domain.warehouse.entity.StockTransfer;
import com.commerceops.erp.domain.warehouse.entity.Warehouse;
import com.commerceops.erp.domain.warehouse.entity.WarehouseStock;
import com.commerceops.erp.domain.warehouse.enums.StockTransferStatus;
import com.commerceops.erp.domain.warehouse.repository.StockTransferRepository;
import com.commerceops.erp.domain.warehouse.repository.WarehouseRepository;
import com.commerceops.erp.domain.warehouse.repository.WarehouseStockRepository;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final StockTransferRepository stockTransferRepository;
    private final ProductRepository productRepository;

    public List<WarehouseResponse> getWarehouses() {
        return warehouseRepository.findAllByOrderByNameAsc().stream()
                .map(WarehouseResponse::from)
                .toList();
    }

    @Transactional
    public WarehouseResponse createWarehouse(WarehouseCreateRequest request) {
        String normalizedCode = request.code().trim().toUpperCase();
        if (warehouseRepository.existsByCodeIgnoreCase(normalizedCode)) {
            throw new BusinessException(ErrorCode.DUPLICATE_WAREHOUSE_CODE);
        }
        Warehouse warehouse = Warehouse.builder()
                .code(normalizedCode)
                .name(request.name().trim())
                .address(request.address().trim())
                .active(true)
                .build();
        return WarehouseResponse.from(warehouseRepository.save(warehouse));
    }

    public PageResponse<WarehouseStockResponse> getWarehouseStocks(
            Long warehouseId, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        return PageResponse.from(
                warehouseStockRepository.findAllForAdmin(warehouseId, normalizedKeyword, pageable)
                        .map(WarehouseStockResponse::from)
        );
    }

    @Transactional
    public WarehouseStockResponse allocateStock(WarehouseStockAllocateRequest request) {
        Warehouse warehouse = findActiveWarehouse(request.warehouseId());
        Product product = productRepository.findByIdForUpdate(request.productId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        if (product.getStatus() == ProductStatus.DELETED) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_AVAILABLE);
        }

        long allocatedAvailable = warehouseStockRepository.sumAvailableQuantityByProductId(product.getId());
        long unallocated = product.getStockQuantity() - allocatedAvailable;
        if (request.quantity() > unallocated) {
            throw new BusinessException(ErrorCode.WAREHOUSE_ALLOCATION_EXCEEDS_TOTAL);
        }

        WarehouseStock stock = warehouseStockRepository.findForUpdate(warehouse.getId(), product.getId())
                .orElseGet(() -> WarehouseStock.builder()
                        .warehouse(warehouse)
                        .product(product)
                        .quantity(0)
                        .build());
        stock.increase(request.quantity());
        return WarehouseStockResponse.from(warehouseStockRepository.save(stock));
    }

    public PageResponse<StockTransferResponse> getStockTransfers(
            StockTransferStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("requestedAt").descending());
        return PageResponse.from(
                stockTransferRepository.findAllForAdmin(status, pageable).map(StockTransferResponse::from)
        );
    }

    @Transactional
    public StockTransferResponse createStockTransfer(StockTransferCreateRequest request) {
        validateDifferentWarehouses(request.fromWarehouseId(), request.toWarehouseId());
        Warehouse from = findActiveWarehouse(request.fromWarehouseId());
        Warehouse to = findActiveWarehouse(request.toWarehouseId());
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        WarehouseStock source = warehouseStockRepository.findForUpdate(from.getId(), product.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.WAREHOUSE_STOCK_NOT_FOUND));
        if (source.getAvailableQuantity() < request.quantity()) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_WAREHOUSE_STOCK);
        }

        String transferNumber = "TRF-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        StockTransfer transfer = StockTransfer.builder()
                .transferNumber(transferNumber)
                .fromWarehouse(from)
                .toWarehouse(to)
                .product(product)
                .quantity(request.quantity())
                .status(StockTransferStatus.PENDING)
                .build();
        return StockTransferResponse.from(stockTransferRepository.save(transfer));
    }

    @Transactional
    public StockTransferResponse completeStockTransfer(Long transferId) {
        StockTransfer transfer = stockTransferRepository.findById(transferId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STOCK_TRANSFER_NOT_FOUND));
        if (transfer.getStatus() == StockTransferStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.STOCK_TRANSFER_ALREADY_COMPLETED);
        }

        Long productId = transfer.getProduct().getId();
        Long fromId = transfer.getFromWarehouse().getId();
        Long toId = transfer.getToWarehouse().getId();

        // Lock in a stable order to reduce deadlock risk for opposite-direction transfers.
        Long firstId = Math.min(fromId, toId);
        Long secondId = Math.max(fromId, toId);
        WarehouseStock first = warehouseStockRepository.findForUpdate(firstId, productId).orElse(null);
        WarehouseStock second = warehouseStockRepository.findForUpdate(secondId, productId).orElse(null);

        WarehouseStock source = fromId.equals(firstId) ? first : second;
        WarehouseStock destination = toId.equals(firstId) ? first : second;
        if (source == null) {
            throw new BusinessException(ErrorCode.WAREHOUSE_STOCK_NOT_FOUND);
        }
        if (source.getAvailableQuantity() < transfer.getQuantity()) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_WAREHOUSE_STOCK);
        }
        if (destination == null) {
            destination = WarehouseStock.builder()
                    .warehouse(transfer.getToWarehouse())
                    .product(transfer.getProduct())
                    .quantity(0)
                    .build();
        }

        source.decrease(transfer.getQuantity());
        destination.increase(transfer.getQuantity());
        warehouseStockRepository.save(source);
        warehouseStockRepository.save(destination);
        transfer.complete();

        return StockTransferResponse.from(transfer);
    }

    private Warehouse findActiveWarehouse(Long warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WAREHOUSE_NOT_FOUND));
        if (!warehouse.isActive()) {
            throw new BusinessException(ErrorCode.WAREHOUSE_INACTIVE);
        }
        return warehouse;
    }

    private void validateDifferentWarehouses(Long fromWarehouseId, Long toWarehouseId) {
        if (fromWarehouseId.equals(toWarehouseId)) {
            throw new BusinessException(ErrorCode.SAME_WAREHOUSE_TRANSFER);
        }
    }
}
