package com.commerceops.erp.domain.accounting.service;

import com.commerceops.erp.domain.accounting.dto.AccountingEntryResponse;
import com.commerceops.erp.domain.accounting.dto.AccountingSummaryResponse;
import com.commerceops.erp.domain.accounting.entity.AccountingEntry;
import com.commerceops.erp.domain.accounting.enums.AccountingEntryType;
import com.commerceops.erp.domain.accounting.repository.AccountingEntryRepository;
import com.commerceops.erp.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountingService {

    private final AccountingEntryRepository accountingEntryRepository;

    @Transactional
    public void recordSale(String orderNumber, int amount) {
        accountingEntryRepository.save(AccountingEntry.builder()
                .type(AccountingEntryType.SALE)
                .amount(amount)
                .description("매출 - 주문번호: " + orderNumber)
                .referenceId(orderNumber)
                .build());
    }

    @Transactional
    public void recordRefund(String orderNumber, int amount) {
        accountingEntryRepository.save(AccountingEntry.builder()
                .type(AccountingEntryType.REFUND)
                .amount(amount)
                .description("환불 - 주문번호: " + orderNumber)
                .referenceId(orderNumber)
                .build());
    }

    @Transactional
    public void recordInbound(Long productId, String productName, int unitPrice, int quantity) {
        int totalCost = unitPrice * quantity;
        accountingEntryRepository.save(AccountingEntry.builder()
                .type(AccountingEntryType.INBOUND)
                .amount(totalCost)
                .description("매입 입고 - " + productName + " " + quantity + "개")
                .referenceId("PRODUCT-" + productId)
                .build());
    }

    public AccountingSummaryResponse getSummary() {
        long totalSales = accountingEntryRepository.sumByType(AccountingEntryType.SALE);
        long totalRefunds = accountingEntryRepository.sumByType(AccountingEntryType.REFUND);
        long totalInbound = accountingEntryRepository.sumByType(AccountingEntryType.INBOUND);
        return new AccountingSummaryResponse(totalSales, totalRefunds, totalInbound, totalSales - totalRefunds);
    }

    public PageResponse<AccountingEntryResponse> getEntries(AccountingEntryType type, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return PageResponse.from(
                accountingEntryRepository.findAllByType(type, pageable).map(AccountingEntryResponse::from));
    }
}
