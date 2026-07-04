package com.commerceops.erp.domain.accounting.controller;

import com.commerceops.erp.domain.accounting.dto.AccountingEntryResponse;
import com.commerceops.erp.domain.accounting.dto.AccountingSummaryResponse;
import com.commerceops.erp.domain.accounting.enums.AccountingEntryType;
import com.commerceops.erp.domain.accounting.service.AccountingService;
import com.commerceops.erp.global.response.ApiResponse;
import com.commerceops.erp.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/accounting")
@RequiredArgsConstructor
public class AdminAccountingController {

    private final AccountingService accountingService;

    @GetMapping("/summary")
    public ApiResponse<AccountingSummaryResponse> getSummary() {
        return ApiResponse.ok(accountingService.getSummary());
    }

    @GetMapping("/entries")
    public ApiResponse<PageResponse<AccountingEntryResponse>> getEntries(
            @RequestParam(required = false) AccountingEntryType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(accountingService.getEntries(type, page, size));
    }
}
