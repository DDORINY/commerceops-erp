package com.commerceops.erp.domain.review.controller;

import com.commerceops.erp.domain.review.dto.ReviewCreateRequest;
import com.commerceops.erp.domain.review.dto.ReviewResponse;
import com.commerceops.erp.domain.review.service.ReviewService;
import com.commerceops.erp.domain.user.entity.User;
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
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/api/orders/{orderId}/items/{orderItemId}/reviews")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @PathVariable Long orderId,
            @PathVariable Long orderItemId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ReviewCreateRequest request) {
        User user = userDetails.getUser();
        ReviewResponse response = reviewService.createReview(orderId, orderItemId, user, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("리뷰가 등록되었습니다.", response));
    }

    @GetMapping("/api/products/{productId}/reviews")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                ApiResponse.ok("리뷰 목록 조회가 완료되었습니다.",
                        reviewService.getProductReviews(productId, page, size))
        );
    }

    @GetMapping("/api/my/reviews")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getMyReviews(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.ok("내 리뷰 목록 조회가 완료되었습니다.",
                        reviewService.getMyReviews(userDetails.getUser()))
        );
    }

    @DeleteMapping("/api/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        reviewService.deleteReview(reviewId, userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.<Void>ok("리뷰가 삭제되었습니다.", null));
    }
}
