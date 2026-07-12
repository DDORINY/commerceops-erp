package com.commerceops.erp.domain.accounting.service;

import com.commerceops.erp.domain.accounting.dto.AccountingEntryResponse;
import com.commerceops.erp.domain.accounting.dto.AccountingLedgerResponse;
import com.commerceops.erp.domain.accounting.dto.AccountingRecognitionResponse;
import com.commerceops.erp.domain.accounting.dto.AccountingSummaryResponse;
import com.commerceops.erp.domain.accounting.dto.AccountingTransactionResponse;
import com.commerceops.erp.domain.accounting.dto.OrderRevenueRecognitionResponse;
import com.commerceops.erp.domain.accounting.dto.ShippingCostEntryResponse;
import com.commerceops.erp.domain.accounting.entity.AccountingEntry;
import com.commerceops.erp.domain.accounting.entity.AccountingLedger;
import com.commerceops.erp.domain.accounting.entity.AccountingTransaction;
import com.commerceops.erp.domain.accounting.entity.ShippingCostEntry;
import com.commerceops.erp.domain.accounting.enums.AccountingEntryType;
import com.commerceops.erp.domain.accounting.enums.AccountingLedgerStatus;
import com.commerceops.erp.domain.accounting.enums.AccountingReferenceType;
import com.commerceops.erp.domain.accounting.enums.AccountingTransactionDirection;
import com.commerceops.erp.domain.accounting.enums.AccountingTransactionType;
import com.commerceops.erp.domain.accounting.enums.ShippingCostSettlementStatus;
import com.commerceops.erp.domain.accounting.repository.AccountingEntryRepository;
import com.commerceops.erp.domain.accounting.repository.AccountingLedgerRepository;
import com.commerceops.erp.domain.accounting.repository.AccountingTransactionRepository;
import com.commerceops.erp.domain.accounting.repository.ShippingCostEntryRepository;
import com.commerceops.erp.domain.audit.enums.AuditActionType;
import com.commerceops.erp.domain.audit.service.AuditLogService;
import com.commerceops.erp.domain.order.entity.Order;
import com.commerceops.erp.domain.order.enums.OrderStatus;
import com.commerceops.erp.domain.order.repository.OrderRepository;
import com.commerceops.erp.domain.payment.entity.Payment;
import com.commerceops.erp.domain.payment.enums.PaymentStatus;
import com.commerceops.erp.domain.payment.repository.PaymentRepository;
import com.commerceops.erp.domain.returns.entity.ReturnRequest;
import com.commerceops.erp.domain.returns.entity.ReturnShipmentInfo;
import com.commerceops.erp.domain.returns.enums.ReturnShippingFeePayer;
import com.commerceops.erp.domain.returns.enums.ReturnStatus;
import com.commerceops.erp.domain.returns.repository.ReturnRequestRepository;
import com.commerceops.erp.domain.returns.repository.ReturnShipmentInfoRepository;
import com.commerceops.erp.domain.shipment.entity.Shipment;
import com.commerceops.erp.domain.shipment.enums.ShipmentStatus;
import com.commerceops.erp.domain.shipment.repository.ShipmentRepository;
import com.commerceops.erp.domain.shipping.entity.Carrier;
import com.commerceops.erp.domain.shipping.entity.ShippingMethod;
import com.commerceops.erp.domain.shipping.repository.CarrierRepository;
import com.commerceops.erp.domain.shipping.repository.ShippingMethodRepository;
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
    private final ShippingCostEntryRepository shippingCostEntryRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ReturnRequestRepository returnRequestRepository;
    private final ReturnShipmentInfoRepository returnShipmentInfoRepository;
    private final ShipmentRepository shipmentRepository;
    private final CarrierRepository carrierRepository;
    private final ShippingMethodRepository shippingMethodRepository;
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

    @Transactional
    public AccountingRecognitionResponse recognizePaymentRefund(Long paymentId, User actor) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        return recognizePaymentRefund(payment, actor);
    }

    @Transactional
    public AccountingRecognitionResponse recognizePaymentRefund(Payment payment, User actor) {
        validateRefundRecognizable(payment);
        AccountingTransaction transaction = saveTransactionIfAbsent(
                "REFUND-PAYMENT-" + payment.getId(),
                AccountingTransactionType.REFUND,
                AccountingTransactionDirection.EXPENSE,
                payment.getPaidAmount() != null ? payment.getPaidAmount().longValue() : 0L,
                AccountingReferenceType.PAYMENT,
                payment.getId(),
                "결제 환불 회계 반영 - 주문번호: " + payment.getOrder().getOrderNumber(),
                actor,
                AuditActionType.REFUND_ACCOUNTING_RECOGNIZED,
                "PAYMENT",
                payment.getId()
        );
        return AccountingRecognitionResponse.from(
                AccountingTransactionType.REFUND,
                AccountingReferenceType.PAYMENT,
                payment.getId(),
                payment.getOrder().getId(),
                payment.getOrder().getOrderNumber(),
                payment.getPaidAmount() != null ? payment.getPaidAmount().longValue() : 0L,
                transaction,
                transaction != null ? "결제 환불 거래가 회계에 반영되었습니다." : "환불 거래 생성 조건을 충족하지 않습니다."
        );
    }

    @Transactional
    public AccountingRecognitionResponse recognizeReturnRefund(Long returnId, User actor) {
        ReturnRequest returnRequest = returnRequestRepository.findById(returnId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RETURN_NOT_FOUND));
        return recognizeReturnRefund(returnRequest, actor);
    }

    @Transactional
    public AccountingRecognitionResponse recognizeReturnRefund(ReturnRequest returnRequest, User actor) {
        if (returnRequest.getStatus() != ReturnStatus.APPROVED) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
        }
        Order order = returnRequest.getOrder();
        AccountingTransaction transaction = saveTransactionIfAbsent(
                "REFUND-RETURN-" + returnRequest.getId(),
                AccountingTransactionType.REFUND,
                AccountingTransactionDirection.EXPENSE,
                order.getTotalPrice().longValue(),
                AccountingReferenceType.RETURN,
                returnRequest.getId(),
                "반품 환불 회계 반영 - 주문번호: " + order.getOrderNumber(),
                actor,
                AuditActionType.REFUND_ACCOUNTING_RECOGNIZED,
                "RETURN",
                returnRequest.getId()
        );
        return AccountingRecognitionResponse.from(
                AccountingTransactionType.REFUND,
                AccountingReferenceType.RETURN,
                returnRequest.getId(),
                order.getId(),
                order.getOrderNumber(),
                order.getTotalPrice().longValue(),
                transaction,
                "반품 환불 거래가 회계에 반영되었습니다."
        );
    }

    @Transactional
    public AccountingRecognitionResponse recognizeReturnShippingFee(Long returnId, User actor) {
        ReturnRequest returnRequest = returnRequestRepository.findById(returnId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RETURN_NOT_FOUND));
        ReturnShipmentInfo info = returnShipmentInfoRepository.findByReturnRequestId(returnRequest.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        return recognizeReturnShippingFee(info, actor);
    }

    @Transactional
    public AccountingRecognitionResponse recognizeReturnShippingFee(ReturnShipmentInfo info, User actor) {
        if (info.getShippingFee() == null || info.getShippingFee().signum() <= 0
                || info.getFeePayer() == ReturnShippingFeePayer.UNDECIDED) {
            return AccountingRecognitionResponse.from(
                    AccountingTransactionType.RETURN_FEE,
                    AccountingReferenceType.RETURN,
                    info.getReturnRequest().getId(),
                    info.getReturnRequest().getOrder().getId(),
                    info.getReturnRequest().getOrder().getOrderNumber(),
                    info.getShippingFee() != null ? info.getShippingFee().longValue() : 0L,
                    null,
                    "반품 배송비 또는 부담 주체가 확정되지 않아 회계 거래를 생성하지 않았습니다."
            );
        }

        AccountingTransactionDirection direction = info.getFeePayer() == ReturnShippingFeePayer.CUSTOMER
                ? AccountingTransactionDirection.INCOME
                : AccountingTransactionDirection.EXPENSE;
        String payerLabel = info.getFeePayer() == ReturnShippingFeePayer.CUSTOMER ? "고객 부담" : "회사 부담";
        AccountingTransaction transaction = saveTransactionIfAbsent(
                "RETURN-FEE-" + info.getReturnRequest().getId(),
                AccountingTransactionType.RETURN_FEE,
                direction,
                info.getShippingFee().longValue(),
                AccountingReferenceType.RETURN,
                info.getReturnRequest().getId(),
                "반품 배송비 회계 반영 - " + payerLabel,
                actor,
                AuditActionType.RETURN_FEE_ACCOUNTING_RECOGNIZED,
                "RETURN",
                info.getReturnRequest().getId()
        );
        return AccountingRecognitionResponse.from(
                AccountingTransactionType.RETURN_FEE,
                AccountingReferenceType.RETURN,
                info.getReturnRequest().getId(),
                info.getReturnRequest().getOrder().getId(),
                info.getReturnRequest().getOrder().getOrderNumber(),
                info.getShippingFee().longValue(),
                transaction,
                "반품 배송비 거래가 회계에 반영되었습니다."
        );
    }

    public AccountingRecognitionResponse getReturnShippingFeeAccounting(Long returnId) {
        ReturnRequest returnRequest = returnRequestRepository.findById(returnId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RETURN_NOT_FOUND));
        AccountingTransaction transaction = accountingTransactionRepository
                .findFirstByReferenceTypeAndReferenceIdAndTypeOrderByOccurredAtDesc(
                        AccountingReferenceType.RETURN,
                        returnRequest.getId(),
                        AccountingTransactionType.RETURN_FEE
                )
                .orElse(null);
        return AccountingRecognitionResponse.from(
                AccountingTransactionType.RETURN_FEE,
                AccountingReferenceType.RETURN,
                returnRequest.getId(),
                returnRequest.getOrder().getId(),
                returnRequest.getOrder().getOrderNumber(),
                transaction != null ? transaction.getAmount() : 0L,
                transaction,
                transaction != null ? "반품 배송비 회계 거래가 있습니다." : "반품 배송비 회계 거래가 없습니다."
        );
    }

    public PageResponse<AccountingTransactionResponse> getRefundEvents(int page, int size) {
        return getTransactions(null, AccountingTransactionType.REFUND, null, null, null, null, null, page, size);
    }

    public PageResponse<AccountingTransactionResponse> getReturnFeeEvents(int page, int size) {
        return getTransactions(null, AccountingTransactionType.RETURN_FEE, null, null, null, null, null, page, size);
    }

    @Transactional
    public AccountingRecognitionResponse recognizeShippingCost(Long shipmentId, User actor) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHIPMENT_NOT_FOUND));
        return recognizeShippingCost(shipment, actor);
    }

    @Transactional
    public AccountingRecognitionResponse recognizeShippingCost(Shipment shipment, User actor) {
        if (shipment.getStatus() != ShipmentStatus.IN_TRANSIT && shipment.getStatus() != ShipmentStatus.DELIVERED) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "배송중 또는 배송완료 상태에서만 택배비 회계 거래를 생성할 수 있습니다.");
        }

        var existing = accountingTransactionRepository
                .findFirstByReferenceTypeAndReferenceIdAndTypeOrderByOccurredAtDesc(
                        AccountingReferenceType.SHIPMENT,
                        shipment.getId(),
                        AccountingTransactionType.SHIPPING_COST
                )
                .orElse(null);
        if (existing != null) {
            if (actor != null) {
                auditLogService.record(
                        actor,
                        AuditActionType.ACCOUNTING_TRANSACTION_DUPLICATE_SKIPPED,
                        "SHIPMENT",
                        shipment.getId(),
                        null,
                        existing.getTransactionNumber(),
                        "이미 배송비 회계 거래가 있어 중복 생성을 건너뛰었습니다."
                );
            }
            return AccountingRecognitionResponse.from(
                    AccountingTransactionType.SHIPPING_COST,
                    AccountingReferenceType.SHIPMENT,
                    shipment.getId(),
                    shipment.getOrder().getId(),
                    shipment.getOrder().getOrderNumber(),
                    existing.getAmount(),
                    existing,
                    "이미 배송비 회계 거래가 있습니다."
            );
        }

        Carrier carrier = resolveCarrier(shipment.getCarrier());
        ShippingMethod method = resolveShippingMethod(carrier);
        long costAmount = method != null && method.getDefaultFee() != null ? method.getDefaultFee().longValue() : 0L;
        if (costAmount <= 0) {
            return AccountingRecognitionResponse.from(
                    AccountingTransactionType.SHIPPING_COST,
                    AccountingReferenceType.SHIPMENT,
                    shipment.getId(),
                    shipment.getOrder().getId(),
                    shipment.getOrder().getOrderNumber(),
                    0L,
                    null,
                    "배송 방법 기본 택배비가 없어 회계 거래를 생성하지 않았습니다."
            );
        }

        ShippingCostEntry entry = shippingCostEntryRepository.findByShipmentId(shipment.getId())
                .orElseGet(() -> shippingCostEntryRepository.save(ShippingCostEntry.builder()
                        .shipment(shipment)
                        .carrier(carrier)
                        .shippingMethod(method)
                        .costAmount(costAmount)
                        .chargedAmount(0L)
                        .occurredAt(LocalDateTime.now())
                        .settlementStatus(ShippingCostSettlementStatus.PENDING)
                        .memo("택배비 매입 회계 후보 - 주문번호: " + shipment.getOrder().getOrderNumber())
                        .build()));

        AccountingTransaction transaction = saveTransactionIfAbsent(
                "SHIP-COST-" + shipment.getId(),
                AccountingTransactionType.SHIPPING_COST,
                AccountingTransactionDirection.EXPENSE,
                entry.getCostAmount(),
                AccountingReferenceType.SHIPMENT,
                shipment.getId(),
                "택배비 매입 회계 반영 - 주문번호: " + shipment.getOrder().getOrderNumber(),
                actor,
                AuditActionType.SHIPPING_COST_ACCOUNTING_RECOGNIZED,
                "SHIPMENT",
                shipment.getId()
        );
        return AccountingRecognitionResponse.from(
                AccountingTransactionType.SHIPPING_COST,
                AccountingReferenceType.SHIPMENT,
                shipment.getId(),
                shipment.getOrder().getId(),
                shipment.getOrder().getOrderNumber(),
                entry.getCostAmount(),
                transaction,
                "택배비 매입 거래가 회계에 반영되었습니다."
        );
    }

    public ShippingCostEntryResponse getShippingCostEntry(Long shipmentId) {
        return shippingCostEntryRepository.findByShipmentId(shipmentId)
                .map(ShippingCostEntryResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    }

    public PageResponse<ShippingCostEntryResponse> getShippingCostEntries(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(1, Math.min(size, 100)),
                Sort.by("occurredAt").descending().and(Sort.by("createdAt").descending()));
        return PageResponse.from(shippingCostEntryRepository.findAllByOrderByOccurredAtDesc(pageable)
                .map(ShippingCostEntryResponse::from));
    }

    public PageResponse<AccountingTransactionResponse> getShippingCostEvents(int page, int size) {
        return getTransactions(null, AccountingTransactionType.SHIPPING_COST, null, null, null, null, null, page, size);
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

    private void validateRefundRecognizable(Payment payment) {
        if (payment.getPaymentStatus() != PaymentStatus.REFUNDED
                && payment.getPaymentStatus() != PaymentStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.PAYMENT_FAILED);
        }
    }

    private Carrier resolveCarrier(String carrierNameOrCode) {
        if (carrierNameOrCode == null || carrierNameOrCode.isBlank()) {
            return null;
        }
        return carrierRepository.findFirstByCodeIgnoreCaseOrNameIgnoreCase(carrierNameOrCode.trim(), carrierNameOrCode.trim())
                .orElse(null);
    }

    private ShippingMethod resolveShippingMethod(Carrier carrier) {
        if (carrier != null) {
            return shippingMethodRepository.findFirstByCarrierIdAndActiveTrueOrderByIdAsc(carrier.getId())
                    .orElse(null);
        }
        return shippingMethodRepository.findFirstByActiveTrueOrderByIdAsc().orElse(null);
    }

    private AccountingTransaction saveTransactionIfAbsent(
            String transactionNumber,
            AccountingTransactionType type,
            AccountingTransactionDirection direction,
            Long amount,
            AccountingReferenceType referenceType,
            Long referenceId,
            String memo,
            User actor,
            AuditActionType auditAction,
            String targetType,
            Long targetId
    ) {
        AccountingTransaction existing = accountingTransactionRepository
                .findFirstByReferenceTypeAndReferenceIdAndTypeOrderByOccurredAtDesc(referenceType, referenceId, type)
                .orElse(null);
        if (existing != null) {
            if (actor != null) {
                auditLogService.record(
                        actor,
                        AuditActionType.ACCOUNTING_TRANSACTION_DUPLICATE_SKIPPED,
                        targetType,
                        targetId,
                        null,
                        existing.getTransactionNumber(),
                        "이미 회계 거래가 있어 중복 생성을 건너뛰었습니다."
                );
            }
            return existing;
        }

        AccountingTransaction transaction = accountingTransactionRepository.save(AccountingTransaction.builder()
                .transactionNumber(transactionNumber)
                .type(type)
                .direction(direction)
                .amount(amount)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .occurredAt(LocalDateTime.now())
                .memo(memo)
                .createdBy(actor)
                .build());
        if (actor != null) {
            auditLogService.record(
                    actor,
                    auditAction,
                    targetType,
                    targetId,
                    null,
                    transaction.getTransactionNumber(),
                    memo,
                    null,
                    toTransactionJson(transaction),
                    null
            );
        }
        return transaction;
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
