package com.commerceops.erp.domain.review.controller;

import com.commerceops.erp.domain.permission.PermissionCodes;
import com.commerceops.erp.domain.permission.service.PermissionChecker;
import com.commerceops.erp.domain.review.dto.ReviewResponse;
import com.commerceops.erp.domain.review.service.ReviewService;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.response.PageResponse;
import com.commerceops.erp.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final ReviewService reviewService;
    private final PermissionChecker permissionChecker;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getReviews(
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.REVIEW_MODERATE);
        return ResponseEntity.ok(ApiResponse.ok(
                "리뷰 목록 조회가 완료되었습니다.",
                reviewService.getAdminReviews(rating, keyword, page, size)
        ));
    }

    @PatchMapping("/{reviewId}/hide")
    public ResponseEntity<ApiResponse<Void>> hideReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.REVIEW_MODERATE);
        reviewService.hideReview(reviewId, userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.<Void>ok("리뷰가 숨김 처리되었습니다.", null));
    }

    @PatchMapping("/{reviewId}/show")
    public ResponseEntity<ApiResponse<Void>> showReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.REVIEW_MODERATE);
        reviewService.showReview(reviewId, userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.<Void>ok("리뷰 숨김이 해제되었습니다.", null));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.REVIEW_MODERATE);
        reviewService.adminDeleteReview(reviewId, userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.<Void>ok("리뷰가 삭제되었습니다.", null));
    }
}
