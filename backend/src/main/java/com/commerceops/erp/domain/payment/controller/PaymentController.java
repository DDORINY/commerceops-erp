package com.commerceops.erp.domain.payment.controller;

import com.commerceops.erp.domain.audit.enums.AuditActionType;
import com.commerceops.erp.domain.audit.service.AuditLogService;
import com.commerceops.erp.domain.payment.dto.PaymentApproveRequest;
import com.commerceops.erp.domain.payment.dto.PaymentCancelRequest;
import com.commerceops.erp.domain.payment.dto.PaymentResponse;
import com.commerceops.erp.domain.payment.service.PaymentService;
import com.commerceops.erp.domain.payment.service.TossPaymentService;
import com.commerceops.erp.domain.payment.dto.TossPaymentPrepareRequest;
import com.commerceops.erp.domain.payment.dto.TossPaymentPrepareResponse;
import com.commerceops.erp.domain.payment.dto.TossPaymentConfirmRequest;
import com.commerceops.erp.domain.payment.dto.TossPaymentConfirmResponse;
import com.commerceops.erp.domain.permission.PermissionCodes;
import com.commerceops.erp.domain.permission.service.PermissionChecker;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PermissionChecker permissionChecker;
    private final AuditLogService auditLogService;
    private final TossPaymentService tossPaymentService;

    @PostMapping("/toss/prepare")
    public ResponseEntity<ApiResponse<TossPaymentPrepareResponse>> prepareToss(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TossPaymentPrepareRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(tossPaymentService.prepare(userDetails.getUser(), request)));
    }

    @PostMapping("/toss/confirm")
    public ResponseEntity<ApiResponse<TossPaymentConfirmResponse>> confirmToss(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TossPaymentConfirmRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("결제가 승인되었습니다.", tossPaymentService.confirm(userDetails.getUser(), request)));
    }

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
        auditLogService.record(
                userDetails.getUser(),
                AuditActionType.PAYMENT_CANCELLED,
                "PAYMENT",
                paymentId,
                null,
                response.paymentStatus(),
                "결제 취소 또는 환불을 처리했습니다.",
                null,
                null,
                request != null ? "{\"reason\":\"" + request.reason() + "\"}" : null
        );
        return ResponseEntity.ok(ApiResponse.ok("결제가 취소 또는 환불되었습니다.", response));
    }

}
