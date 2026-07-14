package com.commerceops.erp.domain.payment.controller;

import com.commerceops.erp.domain.payment.dto.MockPaymentCompleteRequest;
import com.commerceops.erp.domain.payment.dto.PaymentResponse;
import com.commerceops.erp.domain.payment.service.PaymentService;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Profile({"local", "test"})
@RestController
@RequestMapping("/api/payments/mock")
@RequiredArgsConstructor
public class MockPaymentController {
    private final PaymentService paymentService;

    @PostMapping("/complete")
    public ResponseEntity<ApiResponse<PaymentResponse>> complete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody MockPaymentCompleteRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("결제가 완료되었습니다.", paymentService.completePayment(userDetails.getUser(), request)));
    }
}
