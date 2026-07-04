package com.commerceops.erp.domain.category.controller;

import com.commerceops.erp.domain.category.dto.CategoryResponse;
import com.commerceops.erp.domain.category.service.CategoryService;
import com.commerceops.erp.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories() {
        return ResponseEntity.ok(
                ApiResponse.ok("카테고리 목록 조회가 완료되었습니다.", categoryService.getAllCategories())
        );
    }
}
