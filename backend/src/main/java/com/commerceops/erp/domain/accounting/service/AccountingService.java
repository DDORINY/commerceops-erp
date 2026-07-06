package com.commerceops.erp.domain.accounting.service;

import com.commerceops.erp.domain.accounting.dto.AccountingEntryResponse;
import com.commerceops.erp.domain.accounting.dto.AccountingLedgerResponse;
import com.commerceops.erp.domain.accounting.dto.AccountingSummaryResponse;
import com.commerceops.erp.domain.accounting.dto.AccountingTransactionResponse;
import com.commerceops.erp.domain.accounting.dto.OrderRevenueRecognitionResponse;
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
import com.commerceops.erp.domain.audit.enums.AuditActionType;
import com.commerceops.erp.domain.audit.service.AuditLogService;
import com.commerceops.erp.domain.order.entity.Order;
import com.commerceops.erp.domain.order.enums.OrderStatus;
import com.commerceops.erp.domain.order.repository.OrderRepository;
import com.commerceops.erp.domain.payment.enums.PaymentStatus;
import com.commerceops.erp.domain.user.entity.User;
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
    private final OrderRepository orderRepository;
    private final AuditLogService auditLogService;

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

    @Transactional
    public OrderRevenueRecognitionResponse recognizeOrderRevenue(Long orderId, User actor) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        return recognizeOrderRevenue(order, actor);
    }

    @Transactional
    public OrderRevenueRecognitionResponse recognizeOrderRevenue(Order order, User actor) {
        validateRevenueRecognizable(order);
        var existing = accountingTransactionRepository
                .findFirstByReferenceTypeAndReferenceIdAndTypeOrderByOccurredAtDesc(
                        AccountingReferenceType.ORDER,
                        order.getId(),
                        AccountingTransactionType.SALES
                )
                .orElse(null);
        if (existing != null) {
            if (actor != null) {
                auditLogService.record(
                        actor,
                        AuditActionType.ACCOUNTING_TRANSACTION_DUPLICATE_SKIPPED,
                        "ORDER",
                        order.getId(),
                        null,
                        existing.getTransactionNumber(),
                        "이미 인식된 주문 매출 거래가 있어 중복 생성을 건너뛰었습니다."
                );
            }
            return OrderRevenueRecognitionResponse.from(order, existing, "이미 매출 인식된 주문입니다.");
        }

        AccountingTransaction transaction = accountingTransactionRepository.save(AccountingTransaction.builder()
                .transactionNumber("REV-ORDER-" + order.getId() + "-SALES")
                .type(AccountingTransactionType.SALES)
                .direction(AccountingTransactionDirection.INCOME)
                .amount(order.getTotalPrice().longValue())
                .referenceType(AccountingReferenceType.ORDER)
                .referenceId(order.getId())
                .occurredAt(LocalDateTime.now())
                .memo("주문 매출 인식 - 주문번호: " + order.getOrderNumber())
                .createdBy(actor)
                .build());

        if (actor != null) {
            auditLogService.record(
                    actor,
                    AuditActionType.REVENUE_RECOGNIZED,
                    "ORDER",
                    order.getId(),
                    null,
                    transaction.getTransactionNumber(),
                    "주문 매출을 회계 거래로 인식했습니다.",
                    null,
                    toTransactionJson(transaction),
                    "{\"orderNumber\":\"" + escapeJson(order.getOrderNumber()) + "\"}"
            );
        }
        return OrderRevenueRecognitionResponse.from(order, transaction, "주문 매출을 인식했습니다.");
    }

    public OrderRevenueRecognitionResponse getOrderRevenue(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        AccountingTransaction transaction = accountingTransactionRepository
                .findFirstByReferenceTypeAndReferenceIdAndTypeOrderByOccurredAtDesc(
                        AccountingReferenceType.ORDER,
                        order.getId(),
                        AccountingTransactionType.SALES
                )
                .orElse(null);
        return OrderRevenueRecognitionResponse.from(
                order,
                transaction,
                transaction != null ? "매출 인식 거래가 있습니다." : "매출 인식 거래가 없습니다."
        );
    }

    public PageResponse<AccountingTransactionResponse> getRevenueEvents(int page, int size) {
        return getTransactions(null, AccountingTransactionType.SALES, null, null, null, null, null, page, size);
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

    private void validateRevenueRecognizable(Order order) {
        if (order.getPaymentStatus() != PaymentStatus.PAID) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
        }
        if (order.getStatus() != OrderStatus.PAID
                && order.getStatus() != OrderStatus.PREPARING
                && order.getStatus() != OrderStatus.SHIPPING
                && order.getStatus() != OrderStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
        }
    }

    private String toTransactionJson(AccountingTransaction transaction) {
        return "{"
                + "\"transactionNumber\":\"" + escapeJson(transaction.getTransactionNumber()) + "\","
                + "\"type\":\"" + transaction.getType().name() + "\","
                + "\"direction\":\"" + transaction.getDirection().name() + "\","
                + "\"amount\":" + transaction.getAmount() + ","
                + "\"referenceType\":\"" + transaction.getReferenceType().name() + "\","
                + "\"referenceId\":" + transaction.getReferenceId()
                + "}";
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
