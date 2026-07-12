package com.commerceops.erp.domain.accounting.service;

import com.commerceops.erp.domain.accounting.dto.SettlementBatchCreateRequest;
import com.commerceops.erp.domain.accounting.dto.SettlementBatchItemResponse;
import com.commerceops.erp.domain.accounting.dto.SettlementBatchResponse;
import com.commerceops.erp.domain.accounting.entity.AccountingTransaction;
import com.commerceops.erp.domain.accounting.entity.SettlementBatch;
import com.commerceops.erp.domain.accounting.entity.SettlementBatchItem;
import com.commerceops.erp.domain.accounting.enums.AccountingTransactionType;
import com.commerceops.erp.domain.accounting.enums.SettlementBatchItemStatus;
import com.commerceops.erp.domain.accounting.enums.SettlementBatchItemType;
import com.commerceops.erp.domain.accounting.enums.SettlementBatchStatus;
import com.commerceops.erp.domain.accounting.repository.AccountingTransactionRepository;
import com.commerceops.erp.domain.accounting.repository.SettlementBatchItemRepository;
import com.commerceops.erp.domain.accounting.repository.SettlementBatchRepository;
import com.commerceops.erp.domain.audit.enums.AuditActionType;
import com.commerceops.erp.domain.audit.service.AuditLogService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementBatchService {

    private final SettlementBatchRepository settlementBatchRepository;
    private final SettlementBatchItemRepository settlementBatchItemRepository;
    private final AccountingTransactionRepository accountingTransactionRepository;
    private final AuditLogService auditLogService;

    public PageResponse<SettlementBatchResponse> getBatches(SettlementBatchStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(1, Math.min(size, 100)),
                Sort.by("createdAt").descending());
        return PageResponse.from(settlementBatchRepository.findAllForAdmin(status, pageable)
                .map(SettlementBatchResponse::from));
    }

    public SettlementBatchResponse getBatch(Long batchId) {
        SettlementBatch batch = findBatch(batchId);
        List<SettlementBatchItemResponse> items = settlementBatchItemRepository
                .findBySettlementBatchIdOrderByIdAsc(batchId)
                .stream()
                .map(SettlementBatchItemResponse::from)
                .toList();
        return SettlementBatchResponse.from(batch, items);
    }

    @Transactional
    public SettlementBatchResponse createBatch(SettlementBatchCreateRequest request, User actor) {
        validatePeriod(request.periodStart(), request.periodEnd());
        if (settlementBatchRepository.existsByPeriodStartAndPeriodEndAndStatusNot(
                request.periodStart(), request.periodEnd(), SettlementBatchStatus.CANCELLED)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "같은 기간의 정산 배치가 이미 있습니다.");
        }

        LocalDateTime from = request.periodStart().atStartOfDay();
        LocalDateTime to = request.periodEnd().atTime(LocalTime.MAX);
        List<AccountingTransaction> transactions = accountingTransactionRepository.findAll(buildSettlementSpec(from, to));

        long totalSales = sumByType(transactions, AccountingTransactionType.SALES);
        long totalRefunds = sumByType(transactions, AccountingTransactionType.REFUND);
        long totalShippingFee = sumByType(transactions, AccountingTransactionType.SHIPPING_REVENUE)
                + sumByType(transactions, AccountingTransactionType.RETURN_FEE);
        long totalShippingCost = sumByType(transactions, AccountingTransactionType.SHIPPING_COST);

        SettlementBatch batch = SettlementBatch.builder()
                .batchNumber(generateBatchNumber(request.periodStart(), request.periodEnd()))
                .periodStart(request.periodStart())
                .periodEnd(request.periodEnd())
                .status(SettlementBatchStatus.DRAFT)
                .totalSales(totalSales)
                .totalRefunds(totalRefunds)
                .totalShippingFee(totalShippingFee)
                .totalShippingCost(totalShippingCost)
                .build();

        transactions.forEach(transaction -> batch.addItem(toItem(transaction)));
        SettlementBatch saved = settlementBatchRepository.save(batch);
        auditLogService.record(actor, AuditActionType.SETTLEMENT_BATCH_CREATED, "SETTLEMENT_BATCH", saved.getId(),
                null, saved.getBatchNumber(), "정산 배치를 생성했습니다.");
        return getBatch(saved.getId());
    }

    @Transactional
    public SettlementBatchResponse closeBatch(Long batchId, User actor) {
        SettlementBatch batch = findBatch(batchId);
        if (batch.getStatus() == SettlementBatchStatus.CLOSED) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 마감된 정산 배치입니다.");
        }
        if (batch.getStatus() == SettlementBatchStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "취소된 정산 배치는 마감할 수 없습니다.");
        }
        batch.close(actor);
        auditLogService.record(actor, AuditActionType.SETTLEMENT_BATCH_CLOSED, "SETTLEMENT_BATCH", batch.getId(),
                null, batch.getStatus().name(), "정산 배치를 마감했습니다.");
        return getBatch(batch.getId());
    }

    private SettlementBatch findBatch(Long batchId) {
        return settlementBatchRepository.findById(batchId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    }

    private void validatePeriod(LocalDate periodStart, LocalDate periodEnd) {
        if (periodStart == null || periodEnd == null || periodStart.isAfter(periodEnd)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "정산 기간이 올바르지 않습니다.");
        }
    }

    private Specification<AccountingTransaction> buildSettlementSpec(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> cb.and(
                root.get("type").in(
                        AccountingTransactionType.SALES,
                        AccountingTransactionType.REFUND,
                        AccountingTransactionType.SHIPPING_REVENUE,
                        AccountingTransactionType.SHIPPING_COST,
                        AccountingTransactionType.RETURN_FEE
                ),
                cb.greaterThanOrEqualTo(root.get("occurredAt"), from),
                cb.lessThanOrEqualTo(root.get("occurredAt"), to)
        );
    }

    private long sumByType(List<AccountingTransaction> transactions, AccountingTransactionType type) {
        return transactions.stream()
                .filter(transaction -> transaction.getType() == type)
                .mapToLong(AccountingTransaction::getAmount)
                .sum();
    }

    private SettlementBatchItem toItem(AccountingTransaction transaction) {
        return SettlementBatchItem.builder()
                .referenceType(transaction.getReferenceType())
                .referenceId(transaction.getReferenceId())
                .itemType(toItemType(transaction.getType()))
                .amount(transaction.getAmount())
                .memo(transaction.getMemo())
                .status(SettlementBatchItemStatus.INCLUDED)
                .build();
    }

    private SettlementBatchItemType toItemType(AccountingTransactionType type) {
        return switch (type) {
            case SALES -> SettlementBatchItemType.SALES;
            case REFUND -> SettlementBatchItemType.REFUND;
            case SHIPPING_COST -> SettlementBatchItemType.SHIPPING_COST;
            case RETURN_FEE -> SettlementBatchItemType.RETURN_FEE;
            case SHIPPING_REVENUE -> SettlementBatchItemType.SHIPPING_REVENUE;
            default -> SettlementBatchItemType.ADJUSTMENT;
        };
    }

    private String generateBatchNumber(LocalDate periodStart, LocalDate periodEnd) {
        DateTimeFormatter formatter = DateTimeFormatter.BASIC_ISO_DATE;
        return "SET-" + periodStart.format(formatter) + "-" + periodEnd.format(formatter) + "-" + System.currentTimeMillis();
    }
}
