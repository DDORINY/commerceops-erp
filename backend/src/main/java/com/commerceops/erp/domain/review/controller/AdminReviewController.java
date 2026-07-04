package com.commerceops.erp.domain.review.controller;

import com.commerceops.erp.domain.review.dto.ReviewResponse;
import com.commerceops.erp.domain.review.service.ReviewService;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getReviews(
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                "리뷰 목록 조회가 완료되었습니다.",
                reviewService.getAdminReviews(rating, keyword, page, size)
        ));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long reviewId) {
        reviewService.adminDeleteReview(reviewId);
        return ResponseEntity.ok(ApiResponse.<Void>ok("리뷰가 삭제되었습니다.", null));
    }
}
