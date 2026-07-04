package com.commerceops.erp.domain.product.controller;

import com.commerceops.erp.domain.product.dto.ProductListResponse;
import com.commerceops.erp.domain.product.dto.ProductResponse;
import com.commerceops.erp.domain.product.service.ProductService;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductListResponse>>> getProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(defaultValue = "false") boolean inStock,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                ApiResponse.ok("상품 목록 조회가 완료되었습니다.",
                        productService.getProducts(categoryId, keyword, sort, minPrice, maxPrice, inStock, page, size))
        );
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(
                ApiResponse.ok("상품 상세 조회가 완료되었습니다.", productService.getProduct(productId))
        );
    }
}
