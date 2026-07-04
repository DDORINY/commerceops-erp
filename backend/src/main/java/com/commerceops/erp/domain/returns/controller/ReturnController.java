package com.commerceops.erp.domain.returns.controller;

import com.commerceops.erp.domain.returns.dto.ReturnCreateRequest;
import com.commerceops.erp.domain.returns.dto.ReturnResponse;
import com.commerceops.erp.domain.returns.service.ReturnService;
import com.commerceops.erp.domain.user.entity.User;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReturnController {

    private final ReturnService returnService;

    @PostMapping("/api/orders/{orderId}/returns")
    public ApiResponse<ReturnResponse> createReturn(
            @PathVariable Long orderId,
            @Valid @RequestBody ReturnCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User user = userDetails.getUser();
        return ApiResponse.ok(returnService.createReturn(orderId, user, request));
    }

    @GetMapping("/api/returns")
    public ApiResponse<List<ReturnResponse>> getMyReturns(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.ok(returnService.getMyReturns(userDetails.getUser()));
    }
}
