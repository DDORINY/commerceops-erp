package com.commerceops.erp.domain.accounting.controller;

import com.commerceops.erp.domain.accounting.dto.AccountingEntryResponse;
import com.commerceops.erp.domain.accounting.dto.AccountingLedgerResponse;
import com.commerceops.erp.domain.accounting.dto.AccountingSummaryResponse;
import com.commerceops.erp.domain.accounting.dto.AccountingTransactionResponse;
import com.commerceops.erp.domain.accounting.enums.AccountingEntryType;
import com.commerceops.erp.domain.accounting.enums.AccountingLedgerStatus;
import com.commerceops.erp.domain.accounting.enums.AccountingReferenceType;
import com.commerceops.erp.domain.accounting.enums.AccountingTransactionDirection;
import com.commerceops.erp.domain.accounting.enums.AccountingTransactionType;
import com.commerceops.erp.domain.accounting.service.AccountingService;
import com.commerceops.erp.domain.permission.PermissionCodes;
import com.commerceops.erp.domain.permission.service.PermissionChecker;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.response.PageResponse;
import com.commerceops.erp.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

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

    @GetMapping("/ledgers")
    public ApiResponse<PageResponse<AccountingLedgerResponse>> getLedgers(
            @RequestParam(required = false) AccountingLedgerStatus status,
            @RequestParam(required = false) String period,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.ACCOUNTING_READ);
        return ApiResponse.ok(accountingService.getLedgers(status, period, page, size));
    }

    @GetMapping("/ledgers/{ledgerId}")
    public ApiResponse<AccountingLedgerResponse> getLedger(
            @PathVariable Long ledgerId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.ACCOUNTING_READ);
        return ApiResponse.ok(accountingService.getLedger(ledgerId));
    }

    @GetMapping("/transactions")
    public ApiResponse<PageResponse<AccountingTransactionResponse>> getTransactions(
            @RequestParam(required = false) Long ledgerId,
            @RequestParam(required = false) AccountingTransactionType type,
            @RequestParam(required = false) AccountingTransactionDirection direction,
            @RequestParam(required = false) AccountingReferenceType referenceType,
            @RequestParam(required = false) Long referenceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.ACCOUNTING_READ);
        return ApiResponse.ok(accountingService.getTransactions(
                ledgerId, type, direction, referenceType, referenceId, dateFrom, dateTo, page, size));
    }

    @GetMapping("/transactions/{transactionId}")
    public ApiResponse<AccountingTransactionResponse> getTransaction(
            @PathVariable Long transactionId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.ACCOUNTING_READ);
        return ApiResponse.ok(accountingService.getTransaction(transactionId));
    }
}
