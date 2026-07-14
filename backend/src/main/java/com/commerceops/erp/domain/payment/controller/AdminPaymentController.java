package com.commerceops.erp.domain.payment.controller;

import com.commerceops.erp.domain.payment.dto.AdminPaymentResponse;
import com.commerceops.erp.domain.payment.enums.PaymentStatus;
import com.commerceops.erp.domain.payment.repository.PaymentRepository;
import com.commerceops.erp.domain.permission.PermissionCodes;
import com.commerceops.erp.domain.permission.service.PermissionChecker;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.response.PageResponse;
import com.commerceops.erp.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final PaymentRepository paymentRepository;
    private final PermissionChecker permissionChecker;

    @GetMapping
    @Transactional(readOnly = true)
    public ApiResponse<PageResponse<AdminPaymentResponse>> getPayments(
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.ACCOUNTING_READ);
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        var payments = paymentRepository.findAllForAdmin(
                status, normalizedKeyword,
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        return ApiResponse.ok(new PageResponse<>(
                payments.getContent().stream().map(AdminPaymentResponse::from).toList(),
                payments.getNumber(), payments.getSize(), payments.getTotalElements(), payments.getTotalPages()
        ));
    }
}
