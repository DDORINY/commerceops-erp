package com.commerceops.erp.domain.product.controller;

import com.commerceops.erp.domain.audit.enums.AuditActionType;
import com.commerceops.erp.domain.audit.service.AuditLogService;
import com.commerceops.erp.domain.permission.PermissionCodes;
import com.commerceops.erp.domain.permission.service.PermissionChecker;
import com.commerceops.erp.domain.product.dto.AdminProductListResponse;
import com.commerceops.erp.domain.product.dto.AdminProductResponse;
import com.commerceops.erp.domain.product.dto.ProductBulkStatusUpdateRequest;
import com.commerceops.erp.domain.product.dto.ProductBulkStatusUpdateResponse;
import com.commerceops.erp.domain.product.dto.ProductCreateRequest;
import com.commerceops.erp.domain.product.dto.ProductDetailBlockRequest;
import com.commerceops.erp.domain.product.dto.ProductDetailBlockResponse;
import com.commerceops.erp.domain.product.dto.ProductOperationNoteRequest;
import com.commerceops.erp.domain.product.dto.ProductOperationNoteResponse;
import com.commerceops.erp.domain.product.dto.ProductStatusUpdateRequest;
import com.commerceops.erp.domain.product.dto.ProductStatusHistoryResponse;
import com.commerceops.erp.domain.product.dto.ProductUpdateRequest;
import com.commerceops.erp.domain.product.enums.ProductDisplayStatus;
import com.commerceops.erp.domain.product.enums.ProductSalesStatus;
import com.commerceops.erp.domain.product.enums.ProductStatus;
import com.commerceops.erp.domain.product.service.ProductDetailBlockService;
import com.commerceops.erp.domain.product.service.ProductService;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.response.PageResponse;
import com.commerceops.erp.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;
    private final ProductDetailBlockService productDetailBlockService;
    private final PermissionChecker permissionChecker;
    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminProductListResponse>>> getProducts(
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) ProductSalesStatus salesStatus,
            @RequestParam(required = false) ProductDisplayStatus displayStatus,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String stockStatus,
            @RequestParam(required = false) Boolean lowStockOnly,
            @RequestParam(required = false) String salePeriodStatus,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.PRODUCT_READ);
        return ResponseEntity.ok(
                ApiResponse.ok("관리자 상품 목록 조회가 완료되었습니다.",
                        productService.getAdminProducts(status, salesStatus, displayStatus, categoryId,
                                stockStatus, lowStockOnly, salePeriodStatus, keyword, page, size))
        );
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<AdminProductResponse>> getProduct(
            @PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.PRODUCT_READ);
        return ResponseEntity.ok(
                ApiResponse.ok("관리자 상품 상세 조회가 완료되었습니다.", productService.getAdminProduct(productId))
        );
    }

    @GetMapping("/{productId}/detail-blocks")
    public ResponseEntity<ApiResponse<List<ProductDetailBlockResponse>>> getDetailBlocks(
            @PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.PRODUCT_READ);
        return ResponseEntity.ok(
                ApiResponse.ok("상품 상세 블록 조회가 완료되었습니다.",
                        productDetailBlockService.getAdminBlocks(productId))
        );
    }

    @PutMapping("/{productId}/detail-blocks")
    public ResponseEntity<ApiResponse<List<ProductDetailBlockResponse>>> replaceDetailBlocks(
            @PathVariable Long productId,
            @RequestBody List<ProductDetailBlockRequest> requests,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.PRODUCT_WRITE);
        List<ProductDetailBlockResponse> response = productDetailBlockService.replaceAdminBlocks(productId, requests);
        auditLogService.record(
                userDetails.getUser(),
                AuditActionType.PRODUCT_UPDATED,
                "PRODUCT",
                productId,
                null,
                "detailBlocks=" + response.size(),
                "상품 상세 블록을 저장했습니다."
        );
        return ResponseEntity.ok(ApiResponse.ok("상품 상세 블록이 저장되었습니다.", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdminProductResponse>> createProduct(
            @Valid @RequestBody ProductCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.PRODUCT_WRITE);
        AdminProductResponse response = productService.createProduct(request);
        auditLogService.record(
                userDetails.getUser(),
                AuditActionType.PRODUCT_CREATED,
                "PRODUCT",
                response.id(),
                null,
                response.name(),
                "상품을 등록했습니다: " + response.name()
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("상품이 등록되었습니다.", response));
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<ApiResponse<AdminProductResponse>> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.PRODUCT_WRITE);
        AdminProductResponse response = productService.updateProduct(productId, request, userDetails.getUser());
        auditLogService.record(
                userDetails.getUser(),
                AuditActionType.PRODUCT_UPDATED,
                "PRODUCT",
                productId,
                null,
                response.name(),
                "상품 정보를 수정했습니다: " + response.name()
        );
        return ResponseEntity.ok(ApiResponse.ok("상품이 수정되었습니다.", response));
    }

    @PatchMapping("/{productId}/status")
    public ResponseEntity<ApiResponse<AdminProductResponse>> updateProductStatus(
            @PathVariable Long productId,
            @Valid @RequestBody ProductStatusUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.PRODUCT_STATUS_CHANGE);
        return ResponseEntity.ok(
                ApiResponse.ok("상품 운영 상태가 변경되었습니다.",
                        productService.updateProductStatus(productId, request, userDetails.getUser()))
        );
    }

    @PatchMapping("/bulk-status")
    public ResponseEntity<ApiResponse<ProductBulkStatusUpdateResponse>> bulkUpdateProductStatus(
            @Valid @RequestBody ProductBulkStatusUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.PRODUCT_BULK_UPDATE);
        return ResponseEntity.ok(
                ApiResponse.ok("상품 운영 상태가 일괄 변경되었습니다.",
                        productService.bulkUpdateProductStatus(request, userDetails.getUser()))
        );
    }

    @GetMapping("/{productId}/status-history")
    public ResponseEntity<ApiResponse<List<ProductStatusHistoryResponse>>> getProductStatusHistory(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.PRODUCT_READ);
        return ResponseEntity.ok(
                ApiResponse.ok("상품 상태 이력 조회가 완료되었습니다.",
                        productService.getProductStatusHistory(productId, limit))
        );
    }

    @GetMapping("/{productId}/operation-notes")
    public ResponseEntity<ApiResponse<List<ProductOperationNoteResponse>>> getProductOperationNotes(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.PRODUCT_READ);
        return ResponseEntity.ok(
                ApiResponse.ok("상품 운영 메모 조회가 완료되었습니다.",
                        productService.getProductOperationNotes(productId, limit))
        );
    }

    @PostMapping("/{productId}/operation-notes")
    public ResponseEntity<ApiResponse<ProductOperationNoteResponse>> createProductOperationNote(
            @PathVariable Long productId,
            @Valid @RequestBody ProductOperationNoteRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.PRODUCT_WRITE);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("상품 운영 메모가 등록되었습니다.",
                        productService.createProductOperationNote(productId, request, userDetails.getUser()))
        );
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.PRODUCT_WRITE);
        productService.deleteProduct(productId);
        auditLogService.record(
                userDetails.getUser(),
                AuditActionType.PRODUCT_DELETED,
                "PRODUCT",
                productId,
                null,
                "DELETED",
                "상품을 삭제했습니다."
        );
        return ResponseEntity.ok(ApiResponse.<Void>ok("상품이 삭제되었습니다.", null));
    }
}
