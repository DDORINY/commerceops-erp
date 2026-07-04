package com.commerceops.erp.domain.cart.controller;

import com.commerceops.erp.domain.cart.dto.*;
import com.commerceops.erp.domain.cart.service.CartService;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.ok("장바구니 조회가 완료되었습니다.",
                        cartService.getCart(userDetails.getUser()))
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CartAddResponse>> addToCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CartAddRequest request) {
        CartAddResponse response = cartService.addToCart(userDetails.getUser(), request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("장바구니에 상품이 추가되었습니다.", response));
    }

    @PatchMapping("/{cartId}")
    public ResponseEntity<ApiResponse<CartUpdateResponse>> updateCartItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long cartId,
            @Valid @RequestBody CartUpdateRequest request) {
        CartUpdateResponse response = cartService.updateCartItem(userDetails.getUser(), cartId, request);
        return ResponseEntity.ok(ApiResponse.ok("장바구니 수량이 변경되었습니다.", response));
    }

    @DeleteMapping("/{cartId}")
    public ResponseEntity<ApiResponse<Void>> removeFromCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long cartId) {
        cartService.removeFromCart(userDetails.getUser(), cartId);
        return ResponseEntity.ok(ApiResponse.noContent("장바구니 상품이 삭제되었습니다."));
    }
}
