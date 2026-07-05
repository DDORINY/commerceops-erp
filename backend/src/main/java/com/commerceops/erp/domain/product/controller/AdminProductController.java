package com.commerceops.erp.domain.product.controller;

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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;
    private final ProductDetailBlockService productDetailBlockService;

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
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                ApiResponse.ok("관리자 상품 목록 조회가 완료되었습니다.",
                        productService.getAdminProducts(status, salesStatus, displayStatus, categoryId,
                                stockStatus, lowStockOnly, salePeriodStatus, keyword, page, size))
        );
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<AdminProductResponse>> getProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(
                ApiResponse.ok("관리자 상품 상세 조회가 완료되었습니다.", productService.getAdminProduct(productId))
        );
    }

    @GetMapping("/{productId}/detail-blocks")
    public ResponseEntity<ApiResponse<java.util.List<ProductDetailBlockResponse>>> getDetailBlocks(
            @PathVariable Long productId) {
        return ResponseEntity.ok(
                ApiResponse.ok("Product detail blocks loaded.",
                        productDetailBlockService.getAdminBlocks(productId))
        );
    }

    @PutMapping("/{productId}/detail-blocks")
    public ResponseEntity<ApiResponse<java.util.List<ProductDetailBlockResponse>>> replaceDetailBlocks(
            @PathVariable Long productId,
            @RequestBody java.util.List<ProductDetailBlockRequest> requests) {
        return ResponseEntity.ok(
                ApiResponse.ok("Product detail blocks saved.",
                        productDetailBlockService.replaceAdminBlocks(productId, requests))
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdminProductResponse>> createProduct(
            @Valid @RequestBody ProductCreateRequest request) {
        AdminProductResponse response = productService.createProduct(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("상품이 등록되었습니다.", response));
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<ApiResponse<AdminProductResponse>> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.ok("상품이 수정되었습니다.",
                        productService.updateProduct(productId, request, userDetails.getUser()))
        );
    }

    @PatchMapping("/{productId}/status")
    public ResponseEntity<ApiResponse<AdminProductResponse>> updateProductStatus(
            @PathVariable Long productId,
            @Valid @RequestBody ProductStatusUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.ok("Product operation status updated.",
                        productService.updateProductStatus(productId, request, userDetails.getUser()))
        );
    }

    @PatchMapping("/bulk-status")
    public ResponseEntity<ApiResponse<ProductBulkStatusUpdateResponse>> bulkUpdateProductStatus(
            @Valid @RequestBody ProductBulkStatusUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.ok("Product operation statuses updated.",
                        productService.bulkUpdateProductStatus(request, userDetails.getUser()))
        );
    }

    @GetMapping("/{productId}/status-history")
    public ResponseEntity<ApiResponse<List<ProductStatusHistoryResponse>>> getProductStatusHistory(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(
                ApiResponse.ok("Product status history loaded.",
                        productService.getProductStatusHistory(productId, limit))
        );
    }

    @GetMapping("/{productId}/operation-notes")
    public ResponseEntity<ApiResponse<List<ProductOperationNoteResponse>>> getProductOperationNotes(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(
                ApiResponse.ok("Product operation notes loaded.",
                        productService.getProductOperationNotes(productId, limit))
        );
    }

    @PostMapping("/{productId}/operation-notes")
    public ResponseEntity<ApiResponse<ProductOperationNoteResponse>> createProductOperationNote(
            @PathVariable Long productId,
            @Valid @RequestBody ProductOperationNoteRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("Product operation note created.",
                        productService.createProductOperationNote(productId, request, userDetails.getUser()))
        );
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.ok(ApiResponse.<Void>ok("상품이 삭제되었습니다.", null));
    }
}
