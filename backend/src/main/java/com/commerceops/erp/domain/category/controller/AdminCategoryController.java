package com.commerceops.erp.domain.category.controller;

import com.commerceops.erp.domain.category.dto.CategoryCreateRequest;
import com.commerceops.erp.domain.category.dto.CategoryResponse;
import com.commerceops.erp.domain.category.dto.CategoryTreeResponse;
import com.commerceops.erp.domain.category.dto.CategoryUpdateRequest;
import com.commerceops.erp.domain.category.service.CategoryService;
import com.commerceops.erp.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<CategoryTreeResponse>>> getCategoryTree() {
        return ResponseEntity.ok(
                ApiResponse.ok("Admin category tree loaded.", categoryService.getAdminCategoryTree())
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CategoryCreateRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("카테고리가 등록되었습니다.", response));
    }
    @PatchMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long categoryId,
            @RequestBody CategoryUpdateRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok("Category updated.", categoryService.updateCategory(categoryId, request))
        );
    }
}
