package com.commerceops.erp.domain.wishlist.controller;

import com.commerceops.erp.domain.wishlist.dto.WishlistItemResponse;
import com.commerceops.erp.domain.wishlist.dto.WishlistToggleResponse;
import com.commerceops.erp.domain.wishlist.service.WishlistService;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping("/{productId}")
    public ResponseEntity<ApiResponse<WishlistToggleResponse>> toggle(
            @PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        WishlistToggleResponse response = wishlistService.toggle(productId, userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.ok("찜 상태가 변경되었습니다.", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WishlistItemResponse>>> getMyWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.ok("찜 목록 조회가 완료되었습니다.",
                        wishlistService.getMyWishlist(userDetails.getUser()))
        );
    }

    @GetMapping("/{productId}/status")
    public ResponseEntity<ApiResponse<WishlistToggleResponse>> getStatus(
            @PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean liked = wishlistService.isLiked(productId, userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.ok("찜 상태 조회 완료.", new WishlistToggleResponse(productId, liked)));
    }
}
