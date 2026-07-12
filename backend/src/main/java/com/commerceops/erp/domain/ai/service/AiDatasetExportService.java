package com.commerceops.erp.domain.ai.service;

import com.commerceops.erp.domain.accounting.entity.AccountingTransaction;
import com.commerceops.erp.domain.accounting.repository.AccountingTransactionRepository;
import com.commerceops.erp.domain.ai.dto.AiDatasetCatalogResponse;
import com.commerceops.erp.domain.ai.dto.AiDatasetExportResponse;
import com.commerceops.erp.domain.ai.enums.AiDatasetKey;
import com.commerceops.erp.domain.order.entity.Order;
import com.commerceops.erp.domain.order.repository.OrderRepository;
import com.commerceops.erp.domain.product.entity.Product;
import com.commerceops.erp.domain.product.repository.ProductRepository;
import com.commerceops.erp.domain.review.entity.Review;
import com.commerceops.erp.domain.review.repository.ReviewRepository;
import com.commerceops.erp.global.exception.BusinessException;
import com.commerceops.erp.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiDatasetExportService {

    private static final int MAX_LIMIT = 500;

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final AccountingTransactionRepository accountingTransactionRepository;
    private final AiDatasetPrivacyMaskingService privacyMaskingService;

    public List<AiDatasetCatalogResponse> getCatalog() {
        return List.of(
                catalog(AiDatasetKey.PRODUCTS, "상품 데이터셋", "상품 마스터, 가격, 상태, 태그 기반 학습 후보",
                        List.of("productId", "productCode", "name", "categoryId", "brand", "price", "stockQuantity", "salesStatus", "displayStatus", "tags")),
                catalog(AiDatasetKey.ORDERS, "주문 데이터셋", "주문 상태, 결제 상태, 금액, 할인 기반 수요 예측 후보",
                        List.of("orderId", "orderNumber", "totalPrice", "discountAmount", "orderStatus", "paymentStatus", "createdAt")),
                catalog(AiDatasetKey.ORDER_DEMAND, "주문/수요 예측 데이터셋", "주문 일자, 금액, 상태, 할인 피처 기반 수요 예측 후보",
                        List.of("orderId", "orderNumber", "orderedDate", "orderedHour", "dayOfWeek", "totalPrice", "discountAmount", "netAmount", "orderStatus", "paymentStatus")),
                catalog(AiDatasetKey.REVIEWS, "리뷰 데이터셋", "평점, 본문, 숨김 상태 기반 리뷰 분석 후보",
                        List.of("reviewId", "productId", "rating", "content", "status", "createdAt")),
                catalog(AiDatasetKey.PRODUCT_REVIEWS, "상품/리뷰 결합 데이터셋", "상품 속성과 리뷰 평점/본문을 결합한 추천/리뷰 분석 후보",
                        List.of("reviewId", "productId", "productCode", "productName", "categoryId", "brand", "price", "tags", "rating", "content", "reviewStatus", "createdAt")),
                catalog(AiDatasetKey.ACCOUNTING_TRANSACTIONS, "회계 거래 데이터셋", "매출, 환불, 배송비, 정산 기반 손익/이상 탐지 후보",
                        List.of("transactionId", "transactionNumber", "type", "direction", "amount", "referenceType", "referenceId", "occurredAt"))
        );
    }

    public AiDatasetExportResponse export(AiDatasetKey key, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, MAX_LIMIT));
        return switch (key) {
            case PRODUCTS -> exportProducts(safeLimit);
            case ORDERS -> exportOrders(safeLimit);
            case ORDER_DEMAND -> exportOrderDemand(safeLimit);
            case REVIEWS -> exportReviews(safeLimit);
            case PRODUCT_REVIEWS -> exportProductReviews(safeLimit);
            case ACCOUNTING_TRANSACTIONS -> exportAccountingTransactions(safeLimit);
        };
    }

    private AiDatasetExportResponse exportProducts(int limit) {
        var pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "updatedAt"));
        List<Map<String, Object>> rows = productRepository.findAll(pageable).stream()
                .map(this::productRow)
                .toList();
        return response(AiDatasetKey.PRODUCTS, rows);
    }

    private AiDatasetExportResponse exportOrders(int limit) {
        var pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Map<String, Object>> rows = orderRepository.findAll(pageable).stream()
                .map(this::orderRow)
                .toList();
        return response(AiDatasetKey.ORDERS, rows);
    }

    private AiDatasetExportResponse exportOrderDemand(int limit) {
        var pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Map<String, Object>> rows = orderRepository.findAll(pageable).stream()
                .map(this::orderDemandRow)
                .toList();
        return response(AiDatasetKey.ORDER_DEMAND, rows);
    }

    private AiDatasetExportResponse exportReviews(int limit) {
        var pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Map<String, Object>> rows = reviewRepository.findAll(pageable).stream()
                .map(this::reviewRow)
                .toList();
        return response(AiDatasetKey.REVIEWS, rows);
    }

    private AiDatasetExportResponse exportProductReviews(int limit) {
        var pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Map<String, Object>> rows = reviewRepository.findAll(pageable).stream()
                .map(this::productReviewRow)
                .toList();
        return response(AiDatasetKey.PRODUCT_REVIEWS, rows);
    }

    private AiDatasetExportResponse exportAccountingTransactions(int limit) {
        var pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "occurredAt"));
        List<Map<String, Object>> rows = accountingTransactionRepository.findAll(pageable).stream()
                .map(this::accountingTransactionRow)
                .toList();
        return response(AiDatasetKey.ACCOUNTING_TRANSACTIONS, rows);
    }

    private Map<String, Object> productRow(Product product) {
        return Map.ofEntries(
                Map.entry("productId", product.getId()),
                Map.entry("productCode", nullable(product.getProductCode())),
                Map.entry("name", product.getName()),
                Map.entry("categoryId", product.getCategory().getId()),
                Map.entry("brand", nullable(product.getBrand())),
                Map.entry("price", nullable(product.getPrice())),
                Map.entry("stockQuantity", nullable(product.getStockQuantity())),
                Map.entry("salesStatus", product.getSalesStatus().name()),
                Map.entry("displayStatus", product.getDisplayStatus().name()),
                Map.entry("tags", nullable(product.getTags()))
        );
    }

    private Map<String, Object> orderRow(Order order) {
        return Map.ofEntries(
                Map.entry("orderId", order.getId()),
                Map.entry("orderNumber", order.getOrderNumber()),
                Map.entry("totalPrice", nullable(order.getTotalPrice())),
                Map.entry("discountAmount", nullable(order.getDiscountAmount())),
                Map.entry("orderStatus", order.getStatus().name()),
                Map.entry("paymentStatus", order.getPaymentStatus().name()),
                Map.entry("createdAt", order.getCreatedAt())
        );
    }

    private Map<String, Object> orderDemandRow(Order order) {
        LocalDateTime orderedAt = order.getCreatedAt();
        Integer discountAmount = order.getDiscountAmount() == null ? 0 : order.getDiscountAmount();
        Integer totalPrice = order.getTotalPrice() == null ? 0 : order.getTotalPrice();
        return Map.ofEntries(
                Map.entry("orderId", order.getId()),
                Map.entry("orderNumber", order.getOrderNumber()),
                Map.entry("orderedDate", orderedAt != null ? orderedAt.toLocalDate().toString() : ""),
                Map.entry("orderedHour", orderedAt != null ? orderedAt.getHour() : ""),
                Map.entry("dayOfWeek", orderedAt != null ? orderedAt.getDayOfWeek().name() : ""),
                Map.entry("totalPrice", totalPrice),
                Map.entry("discountAmount", discountAmount),
                Map.entry("netAmount", totalPrice - discountAmount),
                Map.entry("orderStatus", order.getStatus().name()),
                Map.entry("paymentStatus", order.getPaymentStatus().name())
        );
    }

    private Map<String, Object> reviewRow(Review review) {
        return Map.ofEntries(
                Map.entry("reviewId", review.getId()),
                Map.entry("productId", review.getProduct().getId()),
                Map.entry("rating", review.getRating()),
                Map.entry("content", nullable(privacyMaskingService.maskText(review.getContent()))),
                Map.entry("status", review.getEffectiveStatus().name()),
                Map.entry("createdAt", review.getCreatedAt())
        );
    }

    private Map<String, Object> productReviewRow(Review review) {
        Product product = review.getProduct();
        return Map.ofEntries(
                Map.entry("reviewId", review.getId()),
                Map.entry("productId", product.getId()),
                Map.entry("productCode", nullable(product.getProductCode())),
                Map.entry("productName", product.getName()),
                Map.entry("categoryId", product.getCategory().getId()),
                Map.entry("brand", nullable(product.getBrand())),
                Map.entry("price", nullable(product.getPrice())),
                Map.entry("tags", nullable(product.getTags())),
                Map.entry("rating", review.getRating()),
                Map.entry("content", nullable(privacyMaskingService.maskText(review.getContent()))),
                Map.entry("reviewStatus", review.getEffectiveStatus().name()),
                Map.entry("createdAt", review.getCreatedAt())
        );
    }

    private Map<String, Object> accountingTransactionRow(AccountingTransaction transaction) {
        return Map.ofEntries(
                Map.entry("transactionId", transaction.getId()),
                Map.entry("transactionNumber", transaction.getTransactionNumber()),
                Map.entry("type", transaction.getType().name()),
                Map.entry("direction", transaction.getDirection().name()),
                Map.entry("amount", transaction.getAmount()),
                Map.entry("referenceType", transaction.getReferenceType().name()),
                Map.entry("referenceId", transaction.getReferenceId()),
                Map.entry("occurredAt", transaction.getOccurredAt())
        );
    }

    private AiDatasetCatalogResponse catalog(AiDatasetKey key, String label, String description, List<String> fields) {
        return new AiDatasetCatalogResponse(key, label, description, fields);
    }

    private AiDatasetExportResponse response(AiDatasetKey key, List<Map<String, Object>> rows) {
        AiDatasetCatalogResponse catalog = getCatalog().stream()
                .filter(item -> item.key() == key)
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        return new AiDatasetExportResponse(key, catalog.label(), LocalDateTime.now(), true, rows.size(), catalog.fields(), rows);
    }

    private Object nullable(Object value) {
        return value == null ? "" : value;
    }
}
