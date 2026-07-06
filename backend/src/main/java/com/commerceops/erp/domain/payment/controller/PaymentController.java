package com.commerceops.erp.domain.payment.controller;

import com.commerceops.erp.domain.payment.dto.MockPaymentCompleteRequest;
import com.commerceops.erp.domain.payment.dto.PaymentApproveRequest;
import com.commerceops.erp.domain.payment.dto.PaymentCancelRequest;
import com.commerceops.erp.domain.payment.dto.PaymentResponse;
import com.commerceops.erp.domain.payment.service.PaymentService;
import com.commerceops.erp.domain.permission.PermissionCodes;
import com.commerceops.erp.domain.permission.service.PermissionChecker;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PermissionChecker permissionChecker;

    @PostMapping("/approve")
    public ResponseEntity<ApiResponse<PaymentResponse>> approvePayment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PaymentApproveRequest request) {
        PaymentResponse response = paymentService.approvePayment(userDetails.getUser(), request);
        return ResponseEntity.ok(ApiResponse.ok("결제가 승인되었습니다.", response));
    }

    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<ApiResponse<PaymentResponse>> cancelPayment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long paymentId,
            @RequestBody(required = false) PaymentCancelRequest request) {
        permissionChecker.require(userDetails, PermissionCodes.PAYMENT_REFUND);
        PaymentResponse response = paymentService.cancelPayment(userDetails.getUser(), paymentId, request);
        return ResponseEntity.ok(ApiResponse.ok("결제가 취소 또는 환불되었습니다.", response));
    }

    @PostMapping("/mock/complete")
    public ResponseEntity<ApiResponse<PaymentResponse>> completePayment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody MockPaymentCompleteRequest request) {
        PaymentResponse response = paymentService.completePayment(userDetails.getUser(), request);
        return ResponseEntity.ok(ApiResponse.ok("결제가 완료되었습니다.", response));
    }
}
