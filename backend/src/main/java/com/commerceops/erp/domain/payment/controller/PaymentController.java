package com.commerceops.erp.domain.payment.controller;

import com.commerceops.erp.domain.payment.dto.MockPaymentCompleteRequest;
import com.commerceops.erp.domain.payment.dto.PaymentResponse;
import com.commerceops.erp.domain.payment.service.PaymentService;
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

    @PostMapping("/mock/complete")
    public ResponseEntity<ApiResponse<PaymentResponse>> completePayment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody MockPaymentCompleteRequest request) {
        PaymentResponse response = paymentService.completePayment(userDetails.getUser(), request);
        return ResponseEntity.ok(ApiResponse.ok("결제가 완료되었습니다.", response));
    }
}
