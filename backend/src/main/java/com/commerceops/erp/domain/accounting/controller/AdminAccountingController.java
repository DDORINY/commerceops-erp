package com.commerceops.erp.domain.accounting.controller;

import com.commerceops.erp.domain.accounting.dto.AccountingConsistencyReportResponse;
import com.commerceops.erp.domain.accounting.dto.AccountingEntryResponse;
import com.commerceops.erp.domain.accounting.dto.AccountingLedgerResponse;
import com.commerceops.erp.domain.accounting.dto.AccountingRecognitionResponse;
import com.commerceops.erp.domain.accounting.dto.AccountingSummaryResponse;
import com.commerceops.erp.domain.accounting.dto.AccountingTransactionResponse;
import com.commerceops.erp.domain.accounting.dto.OrderRevenueRecognitionResponse;
import com.commerceops.erp.domain.accounting.dto.SettlementBatchCreateRequest;
import com.commerceops.erp.domain.accounting.dto.SettlementBatchResponse;
import com.commerceops.erp.domain.accounting.dto.ShippingCostEntryResponse;
import com.commerceops.erp.domain.accounting.enums.AccountingEntryType;
import com.commerceops.erp.domain.accounting.enums.AccountingLedgerStatus;
import com.commerceops.erp.domain.accounting.enums.AccountingReferenceType;
import com.commerceops.erp.domain.accounting.enums.AccountingTransactionDirection;
import com.commerceops.erp.domain.accounting.enums.AccountingTransactionType;
import com.commerceops.erp.domain.accounting.enums.SettlementBatchStatus;
import com.commerceops.erp.domain.accounting.service.AccountingService;
import com.commerceops.erp.domain.accounting.service.SettlementBatchService;
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
    private final SettlementBatchService settlementBatchService;
    private final PermissionChecker permissionChecker;

    @GetMapping("/summary")
    public ApiResponse<AccountingSummaryResponse> getSummary(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionChecker.require(userDetails, PermissionCodes.ACCOUNTING_READ);
        return ApiResponse.ok(accountingService.getSummary());
    }

    @GetMapping("/consistency-report")
    public ApiResponse<AccountingConsistencyReportResponse> getConsistencyReport(
            @RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.ACCOUNTING_READ);
        return ApiResponse.ok(accountingService.getConsistencyReport(limit));
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

    @PostMapping("/orders/{orderId}/recognize-revenue")
    public ApiResponse<OrderRevenueRecognitionResponse> recognizeOrderRevenue(
            @PathVariable Long orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.ACCOUNTING_MANAGE);
        return ApiResponse.ok(accountingService.recognizeOrderRevenue(orderId, userDetails.getUser()));
    }

    @GetMapping("/orders/{orderId}/revenue")
    public ApiResponse<OrderRevenueRecognitionResponse> getOrderRevenue(
            @PathVariable Long orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.ACCOUNTING_READ);
        return ApiResponse.ok(accountingService.getOrderRevenue(orderId));
    }

    @GetMapping("/revenue-events")
    public ApiResponse<PageResponse<AccountingTransactionResponse>> getRevenueEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.ACCOUNTING_READ);
        return ApiResponse.ok(accountingService.getRevenueEvents(page, size));
    }

    @PostMapping("/payments/{paymentId}/recognize-refund")
    public ApiResponse<AccountingRecognitionResponse> recognizePaymentRefund(
            @PathVariable Long paymentId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.PAYMENT_REFUND);
        return ApiResponse.ok(accountingService.recognizePaymentRefund(paymentId, userDetails.getUser()));
    }

    @PostMapping("/returns/{returnId}/recognize-refund")
    public ApiResponse<AccountingRecognitionResponse> recognizeReturnRefund(
            @PathVariable Long returnId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.PAYMENT_REFUND);
        return ApiResponse.ok(accountingService.recognizeReturnRefund(returnId, userDetails.getUser()));
    }

    @PostMapping("/returns/{returnId}/recognize-return-fee")
    public ApiResponse<AccountingRecognitionResponse> recognizeReturnFee(
            @PathVariable Long returnId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.RETURN_FEE_MANAGE);
        return ApiResponse.ok(accountingService.recognizeReturnShippingFee(returnId, userDetails.getUser()));
    }

    @GetMapping("/returns/{returnId}/return-fee")
    public ApiResponse<AccountingRecognitionResponse> getReturnFeeAccounting(
            @PathVariable Long returnId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.ACCOUNTING_READ);
        return ApiResponse.ok(accountingService.getReturnShippingFeeAccounting(returnId));
    }

    @GetMapping("/refund-events")
    public ApiResponse<PageResponse<AccountingTransactionResponse>> getRefundEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.ACCOUNTING_READ);
        return ApiResponse.ok(accountingService.getRefundEvents(page, size));
    }

    @GetMapping("/return-fees")
    public ApiResponse<PageResponse<AccountingTransactionResponse>> getReturnFeeEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.ACCOUNTING_READ);
        return ApiResponse.ok(accountingService.getReturnFeeEvents(page, size));
    }

    @PostMapping("/shipments/{shipmentId}/recognize-shipping-cost")
    public ApiResponse<AccountingRecognitionResponse> recognizeShippingCost(
            @PathVariable Long shipmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.SHIPPING_COST_MANAGE);
        return ApiResponse.ok(accountingService.recognizeShippingCost(shipmentId, userDetails.getUser()));
    }

    @GetMapping("/shipments/{shipmentId}/shipping-cost")
    public ApiResponse<ShippingCostEntryResponse> getShippingCost(
            @PathVariable Long shipmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.ACCOUNTING_READ);
        return ApiResponse.ok(accountingService.getShippingCostEntry(shipmentId));
    }

    @GetMapping("/shipping-costs")
    public ApiResponse<PageResponse<ShippingCostEntryResponse>> getShippingCosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.ACCOUNTING_READ);
        return ApiResponse.ok(accountingService.getShippingCostEntries(page, size));
    }

    @GetMapping("/shipping-cost-events")
    public ApiResponse<PageResponse<AccountingTransactionResponse>> getShippingCostEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.ACCOUNTING_READ);
        return ApiResponse.ok(accountingService.getShippingCostEvents(page, size));
    }

    @GetMapping("/settlements")
    public ApiResponse<PageResponse<SettlementBatchResponse>> getSettlementBatches(
            @RequestParam(required = false) SettlementBatchStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.ACCOUNTING_READ);
        return ApiResponse.ok(settlementBatchService.getBatches(status, page, size));
    }

    @GetMapping("/settlements/{settlementId}")
    public ApiResponse<SettlementBatchResponse> getSettlementBatch(
            @PathVariable Long settlementId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.ACCOUNTING_READ);
        return ApiResponse.ok(settlementBatchService.getBatch(settlementId));
    }

    @PostMapping("/settlements")
    public ApiResponse<SettlementBatchResponse> createSettlementBatch(
            @RequestBody SettlementBatchCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.SETTLEMENT_MANAGE);
        return ApiResponse.ok(settlementBatchService.createBatch(request, userDetails.getUser()));
    }

    @PatchMapping("/settlements/{settlementId}/close")
    public ApiResponse<SettlementBatchResponse> closeSettlementBatch(
            @PathVariable Long settlementId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        permissionChecker.require(userDetails, PermissionCodes.ACCOUNTING_CLOSE);
        return ApiResponse.ok(settlementBatchService.closeBatch(settlementId, userDetails.getUser()));
    }
}
