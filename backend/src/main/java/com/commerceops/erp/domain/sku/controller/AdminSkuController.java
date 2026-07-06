package com.commerceops.erp.domain.sku.controller;

import com.commerceops.erp.domain.permission.PermissionCodes;
import com.commerceops.erp.domain.permission.service.PermissionChecker;
import com.commerceops.erp.domain.sku.dto.SkuActiveUpdateRequest;
import com.commerceops.erp.domain.sku.dto.SkuCreateRequest;
import com.commerceops.erp.domain.sku.dto.SkuListResponse;
import com.commerceops.erp.domain.sku.dto.SkuResponse;
import com.commerceops.erp.domain.sku.dto.SkuUpdateRequest;
import com.commerceops.erp.domain.sku.service.SkuService;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.response.PageResponse;
import com.commerceops.erp.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminSkuController {

    private final SkuService skuService;
    private final PermissionChecker permissionChecker;

    @GetMapping("/skus")
    public ResponseEntity<ApiResponse<PageResponse<SkuListResponse>>> getSkus(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean hasBarcode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.INVENTORY_READ);
        return ResponseEntity.ok(ApiResponse.ok("SKU 목록을 조회했습니다.",
                skuService.getSkus(keyword, productId, active, hasBarcode, page, size)));
    }

    @GetMapping("/skus/{skuId}")
    public ResponseEntity<ApiResponse<SkuResponse>> getSku(
            @PathVariable Long skuId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.INVENTORY_READ);
        return ResponseEntity.ok(ApiResponse.ok("SKU 상세를 조회했습니다.", skuService.getSku(skuId)));
    }

    @GetMapping("/products/{productId}/skus")
    public ResponseEntity<ApiResponse<List<SkuResponse>>> getProductSkus(
            @PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.INVENTORY_READ);
        return ResponseEntity.ok(ApiResponse.ok("상품 SKU 목록을 조회했습니다.", skuService.getProductSkus(productId)));
    }

    @PostMapping("/skus")
    public ResponseEntity<ApiResponse<SkuResponse>> createSku(
            @Valid @RequestBody SkuCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.SKU_MANAGE);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("SKU를 생성했습니다.", skuService.createSku(request, userDetails.getUser())));
    }

    @PatchMapping("/skus/{skuId}")
    public ResponseEntity<ApiResponse<SkuResponse>> updateSku(
            @PathVariable Long skuId,
            @Valid @RequestBody SkuUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.SKU_MANAGE);
        return ResponseEntity.ok(ApiResponse.ok("SKU를 수정했습니다.",
                skuService.updateSku(skuId, request, userDetails.getUser())));
    }

    @PatchMapping("/skus/{skuId}/active")
    public ResponseEntity<ApiResponse<SkuResponse>> updateActive(
            @PathVariable Long skuId,
            @Valid @RequestBody SkuActiveUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.SKU_MANAGE);
        return ResponseEntity.ok(ApiResponse.ok("SKU 활성 상태를 변경했습니다.",
                skuService.updateActive(skuId, request.active(), userDetails.getUser())));
    }

    @PostMapping("/skus/{skuId}/barcode/regenerate")
    public ResponseEntity<ApiResponse<SkuResponse>> regenerateBarcode(
            @PathVariable Long skuId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.BARCODE_MANAGE);
        return ResponseEntity.ok(ApiResponse.ok("바코드를 재발급했습니다.",
                skuService.regenerateBarcode(skuId, userDetails.getUser())));
    }
}
