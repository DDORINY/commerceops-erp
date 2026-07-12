package com.commerceops.erp.domain.ai.service;

import com.commerceops.erp.domain.accounting.dto.AccountingConsistencyIssueResponse;
import com.commerceops.erp.domain.accounting.entity.AccountingTransaction;
import com.commerceops.erp.domain.accounting.entity.SettlementBatch;
import com.commerceops.erp.domain.accounting.repository.AccountingTransactionRepository;
import com.commerceops.erp.domain.accounting.repository.SettlementBatchRepository;
import com.commerceops.erp.domain.accounting.service.AccountingService;
import com.commerceops.erp.domain.ai.dto.AiDatasetCatalogResponse;
import com.commerceops.erp.domain.ai.dto.AiDatasetExportResponse;
import com.commerceops.erp.domain.ai.enums.AiDatasetKey;
import com.commerceops.erp.domain.order.entity.Order;
import com.commerceops.erp.domain.order.repository.OrderRepository;
import com.commerceops.erp.domain.product.entity.Product;
import com.commerceops.erp.domain.product.repository.ProductRepository;
import com.commerceops.erp.domain.review.entity.Review;
import com.commerceops.erp.domain.review.repository.ReviewRepository;
import com.commerceops.erp.domain.shipment.entity.Shipment;
import com.commerceops.erp.domain.shipment.repository.ShipmentRepository;
import com.commerceops.erp.domain.sku.entity.Sku;
import com.commerceops.erp.domain.sku.repository.SkuRepository;
import com.commerceops.erp.domain.warehouse.entity.WarehouseStock;
import com.commerceops.erp.domain.warehouse.repository.WarehouseStockRepository;
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
    private final SkuRepository skuRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final ShipmentRepository shipmentRepository;
    private final SettlementBatchRepository settlementBatchRepository;
    private final AccountingService accountingService;

    public List<AiDatasetCatalogResponse> getCatalog() {
        return List.of(
                catalog(AiDatasetKey.PRODUCTS, "상품 데이터셋", "상품 마스터, 가격, 상태, 태그 기반 학습 후보",
                        List.of("productId", "productCode", "name", "categoryId", "brand", "price", "stockQuantity", "salesStatus", "displayStatus", "tags")),
                catalog(AiDatasetKey.ORDERS, "주문 데이터셋", "주문 상태, 결제 상태, 금액, 할인 기반 수요 예측 후보",
                        List.of("orderId", "orderNumber", "totalPrice", "discountAmount", "orderStatus", "paymentStatus", "createdAt")),
                catalog(AiDatasetKey.ORDER_DEMAND, "주문/수요 예측 데이터셋", "주문 일자, 금액, 상태, 할인 피처 기반 수요 예측 후보",
                        List.of("orderId", "orderNumber", "orderedDate", "orderedHour", "dayOfWeek", "totalPrice", "discountAmount", "netAmount", "orderStatus", "paymentStatus")),
                catalog(AiDatasetKey.REVIEWS, "리뷰 데이터셋", "평점, 본문, 공개 상태 기반 리뷰 분석 후보",
                        List.of("reviewId", "productId", "rating", "content", "status", "createdAt")),
                catalog(AiDatasetKey.PRODUCT_REVIEWS, "상품/리뷰 결합 데이터셋", "상품 속성과 리뷰 평점/본문을 결합한 추천/리뷰 분석 후보",
                        List.of("reviewId", "productId", "productCode", "productName", "categoryId", "brand", "price", "tags", "rating", "content", "reviewStatus", "createdAt")),
                catalog(AiDatasetKey.INVENTORY_SHIPPING, "재고 데이터셋", "SKU, 안전재고, 창고 재고 기반 재고 부족 예측 후보",
                        List.of("skuId", "skuCode", "barcode", "productId", "productName", "safetyStockQuantity", "warehouseId", "warehouseName", "quantity", "reservedQuantity", "availableQuantity", "active")),
                catalog(AiDatasetKey.SHIPPING_LEADTIME, "배송 리드타임 데이터셋", "배송 상태와 송장/출고/배송완료 시점 기반 배송 지연 예측 후보",
                        List.of("shipmentId", "orderId", "orderNumber", "carrier", "status", "trackingIssuedAt", "shippedAt", "deliveredAt", "leadTimeHours")),
                catalog(AiDatasetKey.ACCOUNTING_TRANSACTIONS, "회계 거래 데이터셋", "매출, 환불, 배송비, 정산 기반 손익/이상 탐지 후보",
                        List.of("transactionId", "transactionNumber", "type", "direction", "amount", "referenceType", "referenceId", "occurredAt")),
                catalog(AiDatasetKey.SETTLEMENT_BATCHES, "정산 배치 데이터셋", "정산 기간, 매출/환불/배송비/순액 기반 정산 분석 후보",
                        List.of("settlementBatchId", "batchNumber", "periodStart", "periodEnd", "status", "totalSales", "totalRefunds", "totalShippingFee", "totalShippingCost", "netAmount", "closedAt", "createdAt")),
                catalog(AiDatasetKey.ACCOUNTING_CONSISTENCY_ISSUES, "회계 정합성 이슈 데이터셋", "누락 매출/환불/배송비 거래 후보 기반 회계 이상 탐지 후보",
                        List.of("issueType", "sourceType", "sourceId", "sourceNumber", "expectedTransactionType", "expectedAmount", "status", "message", "createdAt"))
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
            case INVENTORY_SHIPPING -> exportInventoryShipping(safeLimit);
            case SHIPPING_LEADTIME -> exportShippingLeadtime(safeLimit);
            case ACCOUNTING_TRANSACTIONS -> exportAccountingTransactions(safeLimit);
            case SETTLEMENT_BATCHES -> exportSettlementBatches(safeLimit);
            case ACCOUNTING_CONSISTENCY_ISSUES -> exportAccountingConsistencyIssues(safeLimit);
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

    private AiDatasetExportResponse exportInventoryShipping(int limit) {
        var pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "updatedAt"));
        List<Map<String, Object>> rows = warehouseStockRepository.findAll(pageable).stream()
                .map(this::warehouseStockRow)
                .toList();
        if (rows.isEmpty()) {
            rows = skuRepository.findAll(pageable).stream()
                    .map(this::skuInventoryRow)
                    .toList();
        }
        return response(AiDatasetKey.INVENTORY_SHIPPING, rows);
    }

    private AiDatasetExportResponse exportShippingLeadtime(int limit) {
        var pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "updatedAt"));
        List<Map<String, Object>> rows = shipmentRepository.findAll(pageable).stream()
                .map(this::shippingLeadtimeRow)
                .toList();
        return response(AiDatasetKey.SHIPPING_LEADTIME, rows);
    }

    private AiDatasetExportResponse exportAccountingTransactions(int limit) {
        var pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "occurredAt"));
        List<Map<String, Object>> rows = accountingTransactionRepository.findAll(pageable).stream()
                .map(this::accountingTransactionRow)
                .toList();
        return response(AiDatasetKey.ACCOUNTING_TRANSACTIONS, rows);
    }

    private AiDatasetExportResponse exportSettlementBatches(int limit) {
        var pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "updatedAt"));
        List<Map<String, Object>> rows = settlementBatchRepository.findAll(pageable).stream()
                .map(this::settlementBatchRow)
                .toList();
        return response(AiDatasetKey.SETTLEMENT_BATCHES, rows);
    }

    private AiDatasetExportResponse exportAccountingConsistencyIssues(int limit) {
        List<Map<String, Object>> rows = accountingService.getConsistencyReport(limit).issues().stream()
                .map(this::accountingConsistencyIssueRow)
                .toList();
        return response(AiDatasetKey.ACCOUNTING_CONSISTENCY_ISSUES, rows);
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

    private Map<String, Object> warehouseStockRow(WarehouseStock stock) {
        Product product = stock.getProduct();
        return Map.ofEntries(
                Map.entry("skuId", ""),
                Map.entry("skuCode", ""),
                Map.entry("barcode", ""),
                Map.entry("productId", product.getId()),
                Map.entry("productName", product.getName()),
                Map.entry("safetyStockQuantity", nullable(product.getSafetyStockQuantity())),
                Map.entry("warehouseId", stock.getWarehouse().getId()),
                Map.entry("warehouseName", stock.getWarehouse().getName()),
                Map.entry("quantity", stock.getQuantity()),
                Map.entry("reservedQuantity", stock.getReservedQuantity()),
                Map.entry("availableQuantity", stock.getAvailableQuantity()),
                Map.entry("active", true)
        );
    }

    private Map<String, Object> skuInventoryRow(Sku sku) {
        Product product = sku.getProduct();
        return Map.ofEntries(
                Map.entry("skuId", sku.getId()),
                Map.entry("skuCode", sku.getSkuCode()),
                Map.entry("barcode", nullable(sku.getBarcode())),
                Map.entry("productId", product.getId()),
                Map.entry("productName", product.getName()),
                Map.entry("safetyStockQuantity", nullable(sku.getSafetyStockQuantity())),
                Map.entry("warehouseId", ""),
                Map.entry("warehouseName", ""),
                Map.entry("quantity", nullable(product.getStockQuantity())),
                Map.entry("reservedQuantity", 0),
                Map.entry("availableQuantity", nullable(product.getStockQuantity())),
                Map.entry("active", sku.getActive())
        );
    }

    private Map<String, Object> shippingLeadtimeRow(Shipment shipment) {
        Long leadTimeHours = null;
        if (shipment.getShippedAt() != null && shipment.getDeliveredAt() != null) {
            leadTimeHours = java.time.Duration.between(shipment.getShippedAt(), shipment.getDeliveredAt()).toHours();
        }
        return Map.ofEntries(
                Map.entry("shipmentId", shipment.getId()),
                Map.entry("orderId", shipment.getOrder().getId()),
                Map.entry("orderNumber", shipment.getOrder().getOrderNumber()),
                Map.entry("carrier", nullable(shipment.getCarrier())),
                Map.entry("status", shipment.getStatus().name()),
                Map.entry("trackingIssuedAt", nullable(shipment.getTrackingNumberIssuedAt())),
                Map.entry("shippedAt", nullable(shipment.getShippedAt())),
                Map.entry("deliveredAt", nullable(shipment.getDeliveredAt())),
                Map.entry("leadTimeHours", nullable(leadTimeHours))
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

    private Map<String, Object> settlementBatchRow(SettlementBatch batch) {
        long netAmount = batch.getTotalSales() + batch.getTotalShippingFee()
                - batch.getTotalRefunds() - batch.getTotalShippingCost();
        return Map.ofEntries(
                Map.entry("settlementBatchId", batch.getId()),
                Map.entry("batchNumber", batch.getBatchNumber()),
                Map.entry("periodStart", batch.getPeriodStart()),
                Map.entry("periodEnd", batch.getPeriodEnd()),
                Map.entry("status", batch.getStatus().name()),
                Map.entry("totalSales", batch.getTotalSales()),
                Map.entry("totalRefunds", batch.getTotalRefunds()),
                Map.entry("totalShippingFee", batch.getTotalShippingFee()),
                Map.entry("totalShippingCost", batch.getTotalShippingCost()),
                Map.entry("netAmount", netAmount),
                Map.entry("closedAt", nullable(batch.getClosedAt())),
                Map.entry("createdAt", batch.getCreatedAt())
        );
    }

    private Map<String, Object> accountingConsistencyIssueRow(AccountingConsistencyIssueResponse issue) {
        return Map.ofEntries(
                Map.entry("issueType", issue.issueType()),
                Map.entry("sourceType", issue.sourceType()),
                Map.entry("sourceId", nullable(issue.sourceId())),
                Map.entry("sourceNumber", nullable(issue.sourceNumber())),
                Map.entry("expectedTransactionType", issue.expectedTransactionType().name()),
                Map.entry("expectedAmount", nullable(issue.expectedAmount())),
                Map.entry("status", nullable(issue.status())),
                Map.entry("message", nullable(issue.message())),
                Map.entry("createdAt", nullable(issue.createdAt()))
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
