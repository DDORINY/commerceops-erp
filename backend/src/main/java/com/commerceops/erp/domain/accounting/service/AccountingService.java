package com.commerceops.erp.domain.accounting.service;

import com.commerceops.erp.domain.accounting.dto.AccountingEntryResponse;
import com.commerceops.erp.domain.accounting.dto.AccountingLedgerResponse;
import com.commerceops.erp.domain.accounting.dto.AccountingSummaryResponse;
import com.commerceops.erp.domain.accounting.dto.AccountingTransactionResponse;
import com.commerceops.erp.domain.accounting.entity.AccountingEntry;
import com.commerceops.erp.domain.accounting.entity.AccountingLedger;
import com.commerceops.erp.domain.accounting.entity.AccountingTransaction;
import com.commerceops.erp.domain.accounting.enums.AccountingEntryType;
import com.commerceops.erp.domain.accounting.enums.AccountingLedgerStatus;
import com.commerceops.erp.domain.accounting.enums.AccountingReferenceType;
import com.commerceops.erp.domain.accounting.enums.AccountingTransactionDirection;
import com.commerceops.erp.domain.accounting.enums.AccountingTransactionType;
import com.commerceops.erp.domain.accounting.repository.AccountingEntryRepository;
import com.commerceops.erp.domain.accounting.repository.AccountingLedgerRepository;
import com.commerceops.erp.domain.accounting.repository.AccountingTransactionRepository;
import com.commerceops.erp.global.exception.BusinessException;
import com.commerceops.erp.global.exception.ErrorCode;
import com.commerceops.erp.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountingService {

    private final AccountingEntryRepository accountingEntryRepository;
    private final AccountingLedgerRepository accountingLedgerRepository;
    private final AccountingTransactionRepository accountingTransactionRepository;

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

    public PageResponse<AccountingLedgerResponse> getLedgers(AccountingLedgerStatus status, String period,
                                                             int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(1, Math.min(size, 100)),
                Sort.by("createdAt").descending());
        return PageResponse.from(accountingLedgerRepository.findAll(buildLedgerSpec(status, period), pageable)
                .map(AccountingLedgerResponse::from));
    }

    public AccountingLedgerResponse getLedger(Long ledgerId) {
        return AccountingLedgerResponse.from(accountingLedgerRepository.findById(ledgerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND)));
    }

    public PageResponse<AccountingTransactionResponse> getTransactions(
            Long ledgerId,
            AccountingTransactionType type,
            AccountingTransactionDirection direction,
            AccountingReferenceType referenceType,
            Long referenceId,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(1, Math.min(size, 100)),
                Sort.by("occurredAt").descending().and(Sort.by("createdAt").descending()));
        return PageResponse.from(accountingTransactionRepository.findAll(
                buildTransactionSpec(ledgerId, type, direction, referenceType, referenceId, dateFrom, dateTo),
                pageable
        ).map(AccountingTransactionResponse::from));
    }

    public AccountingTransactionResponse getTransaction(Long transactionId) {
        return AccountingTransactionResponse.from(accountingTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND)));
    }

    private Specification<AccountingLedger> buildLedgerSpec(AccountingLedgerStatus status, String period) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();
            if (status != null) {
                predicates = cb.and(predicates, cb.equal(root.get("status"), status));
            }
            if (period != null && !period.isBlank()) {
                predicates = cb.and(predicates, cb.equal(root.get("period"), period.trim()));
            }
            return predicates;
        };
    }

    private Specification<AccountingTransaction> buildTransactionSpec(
            Long ledgerId,
            AccountingTransactionType type,
            AccountingTransactionDirection direction,
            AccountingReferenceType referenceType,
            Long referenceId,
            LocalDateTime dateFrom,
            LocalDateTime dateTo
    ) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();
            if (ledgerId != null) {
                predicates = cb.and(predicates, cb.equal(root.get("ledger").get("id"), ledgerId));
            }
            if (type != null) {
                predicates = cb.and(predicates, cb.equal(root.get("type"), type));
            }
            if (direction != null) {
                predicates = cb.and(predicates, cb.equal(root.get("direction"), direction));
            }
            if (referenceType != null) {
                predicates = cb.and(predicates, cb.equal(root.get("referenceType"), referenceType));
            }
            if (referenceId != null) {
                predicates = cb.and(predicates, cb.equal(root.get("referenceId"), referenceId));
            }
            if (dateFrom != null) {
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("occurredAt"), dateFrom));
            }
            if (dateTo != null) {
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("occurredAt"), dateTo));
            }
            return predicates;
        };
    }
}
