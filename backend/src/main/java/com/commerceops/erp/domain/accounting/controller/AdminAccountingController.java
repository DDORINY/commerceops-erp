package com.commerceops.erp.domain.accounting.controller;

import com.commerceops.erp.domain.accounting.dto.AccountingEntryResponse;
import com.commerceops.erp.domain.accounting.dto.AccountingSummaryResponse;
import com.commerceops.erp.domain.accounting.enums.AccountingEntryType;
import com.commerceops.erp.domain.accounting.service.AccountingService;
import com.commerceops.erp.domain.permission.PermissionCodes;
import com.commerceops.erp.domain.permission.service.PermissionChecker;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.response.PageResponse;
import com.commerceops.erp.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/accounting")
@RequiredArgsConstructor
public class AdminAccountingController {

    private final AccountingService accountingService;
    private final PermissionChecker permissionChecker;

    @GetMapping("/summary")
    public ApiResponse<AccountingSummaryResponse> getSummary(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.ACCOUNTING_READ);
        return ApiResponse.ok(accountingService.getSummary());
    }

    @GetMapping("/entries")
    public ApiResponse<PageResponse<AccountingEntryResponse>> getEntries(
            @RequestParam(required = false) AccountingEntryType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.ACCOUNTING_READ);
        return ApiResponse.ok(accountingService.getEntries(type, page, size));
    }
}
