package com.commerceops.erp.domain.warehouse.controller;

import com.commerceops.erp.domain.warehouse.dto.*;
import com.commerceops.erp.domain.warehouse.enums.StockTransferStatus;
import com.commerceops.erp.domain.warehouse.service.WarehouseService;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminWarehouseController {

    private final WarehouseService warehouseService;

    @GetMapping("/warehouses")
    public ResponseEntity<ApiResponse<List<WarehouseResponse>>> getWarehouses() {
        return ResponseEntity.ok(ApiResponse.ok("창고 목록 조회가 완료되었습니다.", warehouseService.getWarehouses()));
    }

    @PostMapping("/warehouses")
    public ResponseEntity<ApiResponse<WarehouseResponse>> createWarehouse(
            @Valid @RequestBody WarehouseCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("창고가 등록되었습니다.", warehouseService.createWarehouse(request)));
    }

    @GetMapping("/warehouse-stocks")
    public ResponseEntity<ApiResponse<PageResponse<WarehouseStockResponse>>> getWarehouseStocks(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                "창고별 재고 조회가 완료되었습니다.",
                warehouseService.getWarehouseStocks(warehouseId, keyword, page, size)
        ));
    }

    @PostMapping("/warehouse-stocks/allocate")
    public ResponseEntity<ApiResponse<WarehouseStockResponse>> allocateStock(
            @Valid @RequestBody WarehouseStockAllocateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("재고가 창고에 배치되었습니다.", warehouseService.allocateStock(request)));
    }

    @GetMapping("/stock-transfers")
    public ResponseEntity<ApiResponse<PageResponse<StockTransferResponse>>> getStockTransfers(
            @RequestParam(required = false) StockTransferStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                "재고 이동 목록 조회가 완료되었습니다.",
                warehouseService.getStockTransfers(status, page, size)
        ));
    }

    @PostMapping("/stock-transfers")
    public ResponseEntity<ApiResponse<StockTransferResponse>> createStockTransfer(
            @Valid @RequestBody StockTransferCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("재고 이동이 요청되었습니다.", warehouseService.createStockTransfer(request)));
    }

    @PatchMapping("/stock-transfers/{id}/complete")
    public ResponseEntity<ApiResponse<StockTransferResponse>> completeStockTransfer(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(
                "재고 이동이 완료되었습니다.", warehouseService.completeStockTransfer(id)
        ));
    }
}
