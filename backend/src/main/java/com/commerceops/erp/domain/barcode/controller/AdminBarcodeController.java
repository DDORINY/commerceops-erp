package com.commerceops.erp.domain.barcode.controller;

import com.commerceops.erp.domain.barcode.dto.BarcodeLabelPreviewResponse;
import com.commerceops.erp.domain.barcode.dto.BarcodeLabelRequest;
import com.commerceops.erp.domain.barcode.dto.BarcodeLabelResponse;
import com.commerceops.erp.domain.barcode.dto.BarcodeSkuResponse;
import com.commerceops.erp.domain.barcode.dto.BarcodeStockChangeRequest;
import com.commerceops.erp.domain.barcode.dto.BarcodeStockChangeResponse;
import com.commerceops.erp.domain.barcode.dto.BarcodeStockResponse;
import com.commerceops.erp.domain.barcode.service.BarcodeService;
import com.commerceops.erp.domain.permission.PermissionCodes;
import com.commerceops.erp.domain.permission.service.PermissionChecker;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.response.PageResponse;
import com.commerceops.erp.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminBarcodeController {

    private final BarcodeService barcodeService;
    private final PermissionChecker permissionChecker;

    @GetMapping("/barcodes")
    public ResponseEntity<ApiResponse<PageResponse<BarcodeSkuResponse>>> searchSkus(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.INVENTORY_READ);
        return ResponseEntity.ok(ApiResponse.ok("바코드 SKU 목록을 조회했습니다.",
                barcodeService.searchSkus(keyword, page, size)));
    }

    @GetMapping("/barcodes/{barcode}")
    public ResponseEntity<ApiResponse<BarcodeSkuResponse>> getByBarcode(
            @PathVariable String barcode,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.INVENTORY_READ);
        return ResponseEntity.ok(ApiResponse.ok("바코드 SKU를 조회했습니다.", barcodeService.getByBarcode(barcode)));
    }

    @GetMapping("/barcodes/{barcode}/stock")
    public ResponseEntity<ApiResponse<BarcodeStockResponse>> getStockByBarcode(
            @PathVariable String barcode,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.INVENTORY_READ);
        return ResponseEntity.ok(ApiResponse.ok("바코드 재고를 조회했습니다.", barcodeService.getStockByBarcode(barcode)));
    }

    @PostMapping("/barcodes/{barcode}/inbound")
    public ResponseEntity<ApiResponse<BarcodeStockChangeResponse>> inboundByBarcode(
            @PathVariable String barcode,
            @Valid @RequestBody BarcodeStockChangeRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.INVENTORY_WRITE);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("바코드 입고를 처리했습니다.",
                        barcodeService.inboundByBarcode(barcode, request, userDetails.getUser())));
    }

    @PostMapping("/barcodes/{barcode}/outbound")
    public ResponseEntity<ApiResponse<BarcodeStockChangeResponse>> outboundByBarcode(
            @PathVariable String barcode,
            @Valid @RequestBody BarcodeStockChangeRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.INVENTORY_WRITE);
        return ResponseEntity.ok(ApiResponse.ok("바코드 출고를 처리했습니다.",
                barcodeService.outboundByBarcode(barcode, request, userDetails.getUser())));
    }

    @GetMapping("/barcode-labels")
    public ResponseEntity<ApiResponse<PageResponse<BarcodeLabelResponse>>> getLabels(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.INVENTORY_READ);
        return ResponseEntity.ok(ApiResponse.ok("바코드 라벨 이력을 조회했습니다.",
                barcodeService.getLabels(keyword, page, size)));
    }

    @PostMapping("/barcodes/{skuId}/labels")
    public ResponseEntity<ApiResponse<BarcodeLabelPreviewResponse>> createLabel(
            @PathVariable Long skuId,
            @Valid @RequestBody(required = false) BarcodeLabelRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.BARCODE_MANAGE);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("바코드 라벨을 생성했습니다.",
                        barcodeService.createLabel(skuId, request, userDetails.getUser())));
    }

    @PostMapping("/barcode-labels/{labelId}/print")
    public ResponseEntity<ApiResponse<BarcodeLabelPreviewResponse>> markPrinted(
            @PathVariable Long labelId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.BARCODE_MANAGE);
        return ResponseEntity.ok(ApiResponse.ok("바코드 라벨 출력 이력을 기록했습니다.",
                barcodeService.markPrinted(labelId, userDetails.getUser())));
    }
}
