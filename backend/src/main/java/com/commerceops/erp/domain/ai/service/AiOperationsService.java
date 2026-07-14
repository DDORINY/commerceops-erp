package com.commerceops.erp.domain.ai.service;

import com.commerceops.erp.domain.ai.dto.AiDatasetCatalogResponse;
import com.commerceops.erp.domain.ai.dto.AiInsightResponse;
import com.commerceops.erp.domain.ai.dto.AiOperationsHealthResponse;
import com.commerceops.erp.domain.ai.dto.AiOperationsOverviewResponse;
import com.commerceops.erp.domain.ai.dto.AiReportResponse;
import com.commerceops.erp.domain.ai.enums.AiRiskLevel;
import com.commerceops.erp.domain.accounting.entity.SettlementBatch;
import com.commerceops.erp.domain.accounting.enums.SettlementBatchStatus;
import com.commerceops.erp.domain.accounting.repository.SettlementBatchRepository;
import com.commerceops.erp.domain.order.entity.Order;
import com.commerceops.erp.domain.order.enums.OrderStatus;
import com.commerceops.erp.domain.order.repository.OrderRepository;
import com.commerceops.erp.domain.payment.enums.PaymentStatus;
import com.commerceops.erp.domain.product.entity.Product;
import com.commerceops.erp.domain.product.enums.ProductDisplayStatus;
import com.commerceops.erp.domain.product.enums.ProductSalesStatus;
import com.commerceops.erp.domain.product.enums.ProductStatus;
import com.commerceops.erp.domain.product.repository.ProductRepository;
import com.commerceops.erp.domain.review.entity.Review;
import com.commerceops.erp.domain.review.enums.ReviewStatus;
import com.commerceops.erp.domain.review.repository.ReviewRepository;
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
public class AiOperationsService {

    private final AiDatasetExportService aiDatasetExportService;
    private final AiDatasetPrivacyMaskingService privacyMaskingService;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final SettlementBatchRepository settlementBatchRepository;

    public AiOperationsOverviewResponse getOverview() {
        LocalDateTime generatedAt = LocalDateTime.now();
        List<AiDatasetCatalogResponse> catalog = aiDatasetExportService.getCatalog();
        return new AiOperationsOverviewResponse(
                catalog.isEmpty() ? "데이터셋 카탈로그 없음" : "데이터셋 카탈로그 준비됨",
                "포트폴리오 데모 baseline 모델 구조 준비됨",
                List.of("상품 추천 후보", "수요 예측", "리뷰 분석", "이상 주문 탐지", "재고/정산 리스크", "AI 리포트"),
                List.of(
                        insight(
                                "ai-foundation-demand",
                                "DATASET",
                                null,
                                "수요 예측 데모 파이프라인",
                                0.82,
                                AiRiskLevel.LOW,
                                "주문/수요 데이터셋과 baseline 학습 스크립트가 준비되어 수요 예측 화면으로 확장할 수 있습니다.",
                                Map.of("datasetCount", catalog.size(), "demoModel", "commerceops_demo_demand_baseline"),
                                "commerceops_demo_demand_baseline",
                                generatedAt
                        ),
                        insight(
                                "ai-foundation-review",
                                "DATASET",
                                null,
                                "리뷰 감성 분석 데모 파이프라인",
                                0.78,
                                AiRiskLevel.LOW,
                                "개인정보 마스킹된 리뷰 데이터셋과 감성 분석 baseline 학습 구조가 준비되어 있습니다.",
                                Map.of("datasetKey", "PRODUCT_REVIEWS", "demoModel", "commerceops_demo_review_sentiment_baseline"),
                                "commerceops_demo_review_sentiment_baseline",
                                generatedAt
                        ),
                        insight(
                                "ai-foundation-operations",
                                "ADMIN_SCREEN",
                                null,
                                "관리자 AI 운영 화면 기반",
                                0.65,
                                AiRiskLevel.MEDIUM,
                                "이번 버전에서는 공통 응답과 화면 기반을 만들고, 세부 추천/예측/탐지는 v0.9.2 이후 단계별로 연결합니다.",
                                Map.of("scope", "v0.9.1", "mode", "portfolio-demo"),
                                "rule_based_foundation",
                                generatedAt
                        )
                ),
                generatedAt
        );
    }

    public AiOperationsHealthResponse getHealth() {
        List<AiDatasetCatalogResponse> catalog = aiDatasetExportService.getCatalog();
        boolean available = !catalog.isEmpty();
        return new AiOperationsHealthResponse(
                available,
                available ? "OK" : "WARN",
                available
                        ? "AI 운영 데모에 필요한 데이터셋 카탈로그와 공통 응답 구조가 준비되어 있습니다."
                        : "AI 데이터셋 카탈로그를 확인할 수 없습니다.",
                List.of("데이터셋 카탈로그", "개인정보 마스킹 export", "baseline 학습 스크립트", "관리자 AI 메뉴 권한"),
                LocalDateTime.now()
        );
    }

    public List<AiInsightResponse> getProductRecommendations(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 30));
        var pageable = PageRequest.of(0, Math.max(safeLimit * 3, 10), Sort.by(Sort.Direction.DESC, "updatedAt"));
        return productRepository.findAll(pageable)
                .stream()
                .filter(product -> product.getDeletedAt() == null)
                .filter(product -> product.getDisplayStatus() == ProductDisplayStatus.VISIBLE)
                .filter(product -> product.getSalesStatus() == ProductSalesStatus.ON_SALE)
                .map(this::productRecommendationInsight)
                .sorted((left, right) -> Double.compare(right.score(), left.score()))
                .limit(safeLimit)
                .toList();
    }

    public List<AiInsightResponse> getDemandForecasts(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 30));
        var pageable = PageRequest.of(0, Math.max(safeLimit * 3, 10), Sort.by(Sort.Direction.ASC, "stockQuantity"));
        return productRepository.findAll(pageable)
                .stream()
                .filter(product -> product.getDeletedAt() == null)
                .filter(product -> product.getDisplayStatus() == ProductDisplayStatus.VISIBLE)
                .filter(product -> product.getSalesStatus() == ProductSalesStatus.ON_SALE
                        || product.getSalesStatus() == ProductSalesStatus.SOLD_OUT)
                .map(this::demandForecastInsight)
                .sorted((left, right) -> {
                    int riskCompare = Integer.compare(riskWeight(right.riskLevel()), riskWeight(left.riskLevel()));
                    if (riskCompare != 0) return riskCompare;
                    return Double.compare(right.score(), left.score());
                })
                .limit(safeLimit)
                .toList();
    }

    public List<AiInsightResponse> getReviewAnalyses(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 30));
        var pageable = PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return reviewRepository.findAll(pageable)
                .stream()
                .filter(review -> review.getEffectiveStatus() != ReviewStatus.DELETED)
                .map(this::reviewAnalysisInsight)
                .toList();
    }

    public List<AiInsightResponse> getOrderAnomalies(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 30));
        var pageable = PageRequest.of(0, Math.max(safeLimit * 3, 10), Sort.by(Sort.Direction.DESC, "createdAt"));
        return orderRepository.findAll(pageable)
                .stream()
                .map(this::orderAnomalyInsight)
                .sorted((left, right) -> {
                    int riskCompare = Integer.compare(riskWeight(right.riskLevel()), riskWeight(left.riskLevel()));
                    if (riskCompare != 0) return riskCompare;
                    return Double.compare(right.score(), left.score());
                })
                .limit(safeLimit)
                .toList();
    }

    public List<AiInsightResponse> getInventoryRiskAlerts(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 30));
        return productRepository.findLowStockProducts(10, ProductStatus.DELETED, PageRequest.of(0, safeLimit))
                .stream()
                .map(this::inventoryRiskInsight)
                .toList();
    }

    public List<AiInsightResponse> getSettlementRiskAlerts(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 30));
        var pageable = PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "updatedAt"));
        return settlementBatchRepository.findAllForAdmin(null, pageable)
                .stream()
                .filter(batch -> batch.getStatus() != SettlementBatchStatus.CLOSED && batch.getStatus() != SettlementBatchStatus.CANCELLED)
                .map(this::settlementRiskInsight)
                .toList();
    }

    public List<AiReportResponse> getReports() {
        LocalDateTime generatedAt = LocalDateTime.now();
        return List.of(
                report(
                        "ai-report-product-recommendation",
                        "상품 추천 후보 리포트",
                        "상품 노출 상태, 판매 상태, 재고, 태그/검색 키워드, 이미지 여부를 기반으로 추천 후보 점수를 설명합니다.",
                        "AI 상품 추천",
                        "rule_based_product_recommendation_v0.9.2",
                        List.of("products", "tags", "searchKeywords", "stockQuantity"),
                        List.of("점수가 높아도 자동 전시하지 않습니다.", "재고가 부족하면 추천 전 재고 보충 여부를 확인합니다.", "태그/키워드 품질이 낮으면 점수 해석에 한계가 있습니다."),
                        generatedAt
                ),
                report(
                        "ai-report-demand-forecast",
                        "수요 예측 리포트",
                        "현재 재고와 안전재고, 데모 수요지수를 기준으로 재고 소진 위험을 설명합니다.",
                        "AI 수요 예측",
                        "commerceops_demo_demand_baseline",
                        List.of("products", "stockQuantity", "safetyStockQuantity", "tags", "searchKeywords"),
                        List.of("자동 발주 기준이 아닙니다.", "실제 운영에서는 주문 시계열과 시즌성을 추가해야 합니다.", "품절 임박 후보는 관리자 확인 후 보충 계획을 세웁니다."),
                        generatedAt
                ),
                report(
                        "ai-report-review-analysis",
                        "리뷰 분석 리포트",
                        "리뷰 평점과 마스킹된 본문을 기준으로 감성 후보와 운영 확인 포인트를 설명합니다.",
                        "AI 리뷰 분석",
                        "commerceops_demo_review_sentiment_baseline",
                        List.of("reviews", "rating", "maskedContent", "reviewStatus"),
                        List.of("자동 숨김이나 제재에 사용하지 않습니다.", "본문은 개인정보 마스킹 후 확인합니다.", "낮은 평점 리뷰는 상품 품질/배송/CS 원인을 함께 확인합니다."),
                        generatedAt
                ),
                report(
                        "ai-report-order-risk",
                        "이상 주문/리스크 리포트",
                        "주문 금액, 할인율, 주문/결제 상태, 재고 부족, 정산 상태를 기반으로 확인이 필요한 운영 리스크를 설명합니다.",
                        "AI 이상 주문/리스크 알림",
                        "rule_based_operations_risk_v0.9",
                        List.of("orders", "paymentStatus", "products", "settlementBatches"),
                        List.of("자동 주문 차단이나 정산 마감에 사용하지 않습니다.", "고액/고할인 주문은 결제와 배송 상태를 함께 확인합니다.", "정산 리스크는 회계 화면에서 원천 데이터를 확인합니다."),
                        generatedAt
                )
        );
    }

    private AiInsightResponse productRecommendationInsight(Product product) {
        LocalDateTime generatedAt = LocalDateTime.now();
        int stock = product.getStockQuantity() == null ? 0 : product.getStockQuantity();
        int safetyStock = product.getSafetyStockQuantity() == null ? 0 : product.getSafetyStockQuantity();
        int price = product.getPrice() == null ? 0 : product.getPrice();
        int tagCount = countTokens(product.getTags());
        int keywordCount = countTokens(product.getSearchKeywords());
        double score = Math.min(0.95, 0.35
                + Math.min(stock, 50) * 0.006
                + Math.min(tagCount + keywordCount, 10) * 0.035
                + (price > 0 ? 0.08 : 0)
                + (product.getImageUrl() != null && !product.getImageUrl().isBlank() ? 0.07 : 0));
        AiRiskLevel riskLevel = stock <= 0 ? AiRiskLevel.HIGH : stock <= safetyStock ? AiRiskLevel.MEDIUM : AiRiskLevel.LOW;
        String title = product.getName() + " 추천 후보";
        String reason = String.format(
                "노출/판매 상태가 정상이고 재고 %d개, 태그 %d개, 검색 키워드 %d개를 기준으로 추천 후보 점수를 계산했습니다.",
                stock,
                tagCount,
                keywordCount
        );
        return insight(
                "product-recommendation-" + product.getId(),
                "PRODUCT",
                product.getId(),
                title,
                Math.round(score * 100.0) / 100.0,
                riskLevel,
                reason,
                Map.of(
                        "productName", product.getName(),
                        "brand", product.getBrand() == null ? "" : product.getBrand(),
                        "price", price,
                        "stockQuantity", stock,
                        "tagCount", tagCount,
                        "keywordCount", keywordCount
                ),
                "rule_based_product_recommendation_v0.9.2",
                generatedAt
        );
    }

    private AiInsightResponse demandForecastInsight(Product product) {
        LocalDateTime generatedAt = LocalDateTime.now();
        int stock = product.getStockQuantity() == null ? 0 : product.getStockQuantity();
        int safetyStock = product.getSafetyStockQuantity() == null ? 0 : product.getSafetyStockQuantity();
        int demandIndex = Math.max(1, safetyStock * 2 + countTokens(product.getSearchKeywords()) + countTokens(product.getTags()));
        int daysOfStock = demandIndex <= 0 ? 0 : Math.max(0, stock / demandIndex);
        double score = Math.min(0.98, 0.45 + Math.max(0, safetyStock * 2 - stock) * 0.03 + Math.min(demandIndex, 20) * 0.015);
        AiRiskLevel riskLevel = stock <= 0 ? AiRiskLevel.HIGH : stock <= safetyStock ? AiRiskLevel.MEDIUM : AiRiskLevel.LOW;
        String reason = String.format(
                "재고 %d개, 안전재고 %d개, 데모 수요지수 %d를 기준으로 재고 소진 위험을 추정했습니다.",
                stock,
                safetyStock,
                demandIndex
        );
        return insight(
                "demand-forecast-" + product.getId(),
                "PRODUCT",
                product.getId(),
                product.getName() + " 수요 예측",
                Math.round(score * 100.0) / 100.0,
                riskLevel,
                reason,
                Map.of(
                        "productName", product.getName(),
                        "stockQuantity", stock,
                        "safetyStockQuantity", safetyStock,
                        "demandIndex", demandIndex,
                        "estimatedDaysOfStock", daysOfStock
                ),
                "commerceops_demo_demand_baseline",
                generatedAt
        );
    }

    private AiInsightResponse reviewAnalysisInsight(Review review) {
        LocalDateTime generatedAt = LocalDateTime.now();
        int rating = review.getRating() == null ? 0 : review.getRating();
        String content = review.getContent() == null ? "" : review.getContent();
        double sentimentScore = Math.max(0.05, Math.min(0.95, rating / 5.0));
        AiRiskLevel riskLevel = rating <= 2 ? AiRiskLevel.HIGH : rating == 3 ? AiRiskLevel.MEDIUM : AiRiskLevel.LOW;
        String reason = String.format(
                "평점 %d점과 본문 길이 %d자를 기준으로 리뷰 감성 후보를 계산했습니다.",
                rating,
                content.length()
        );
        return insight(
                "review-analysis-" + review.getId(),
                "REVIEW",
                review.getId(),
                review.getProduct().getName() + " 리뷰 분석",
                Math.round(sentimentScore * 100.0) / 100.0,
                riskLevel,
                reason,
                Map.of(
                        "productName", review.getProduct().getName(),
                        "rating", rating,
                        "status", review.getEffectiveStatus().name(),
                        "maskedContent", privacyMaskingService.maskText(content),
                        "contentLength", content.length()
                ),
                "commerceops_demo_review_sentiment_baseline",
                generatedAt
        );
    }

    private AiInsightResponse orderAnomalyInsight(Order order) {
        LocalDateTime generatedAt = LocalDateTime.now();
        int totalPrice = order.getTotalPrice() == null ? 0 : order.getTotalPrice();
        int discountAmount = order.getDiscountAmount() == null ? 0 : order.getDiscountAmount();
        double discountRate = totalPrice <= 0 ? 0 : (double) discountAmount / totalPrice;
        boolean highAmount = totalPrice >= 1_000_000;
        boolean highDiscount = discountRate >= 0.5;
        boolean statusMismatch = order.getStatus() == OrderStatus.CANCELLED
                && (order.getPaymentStatus() == PaymentStatus.PAID || order.getPaymentStatus() == PaymentStatus.DONE);
        double score = Math.min(0.99,
                0.2
                        + (highAmount ? 0.25 : 0)
                        + (highDiscount ? 0.3 : discountRate * 0.3)
                        + (statusMismatch ? 0.35 : 0)
        );
        AiRiskLevel riskLevel = statusMismatch || (highAmount && highDiscount)
                ? AiRiskLevel.HIGH
                : highAmount || highDiscount ? AiRiskLevel.MEDIUM : AiRiskLevel.LOW;
        String reason = String.format(
                "주문금액 %,d원, 할인율 %.0f%%, 주문상태 %s, 결제상태 %s 기준으로 이상 후보 점수를 계산했습니다.",
                totalPrice,
                discountRate * 100,
                order.getStatus().name(),
                order.getPaymentStatus().name()
        );
        return insight(
                "order-anomaly-" + order.getId(),
                "ORDER",
                order.getId(),
                order.getOrderNumber() + " 이상 주문 후보",
                Math.round(score * 100.0) / 100.0,
                riskLevel,
                reason,
                Map.of(
                        "orderNumber", order.getOrderNumber(),
                        "totalPrice", totalPrice,
                        "discountAmount", discountAmount,
                        "discountRate", Math.round(discountRate * 10000.0) / 100.0,
                        "orderStatus", order.getStatus().name(),
                        "paymentStatus", order.getPaymentStatus().name()
                ),
                "rule_based_order_anomaly_v0.9.5",
                generatedAt
        );
    }

    private AiInsightResponse inventoryRiskInsight(Product product) {
        LocalDateTime generatedAt = LocalDateTime.now();
        int stock = product.getStockQuantity() == null ? 0 : product.getStockQuantity();
        int safetyStock = product.getSafetyStockQuantity() == null ? 0 : product.getSafetyStockQuantity();
        AiRiskLevel riskLevel = stock <= 0 ? AiRiskLevel.HIGH : stock <= safetyStock ? AiRiskLevel.MEDIUM : AiRiskLevel.LOW;
        double score = Math.min(0.99, stock <= 0 ? 0.95 : 0.45 + Math.max(0, safetyStock - stock) * 0.05);
        return insight(
                "inventory-risk-" + product.getId(),
                "PRODUCT",
                product.getId(),
                product.getName() + " 재고 리스크",
                Math.round(score * 100.0) / 100.0,
                riskLevel,
                String.format("현재 재고 %d개, 안전재고 %d개 기준으로 재고 부족 리스크를 계산했습니다.", stock, safetyStock),
                Map.of(
                        "productName", product.getName(),
                        "stockQuantity", stock,
                        "safetyStockQuantity", safetyStock,
                        "salesStatus", product.getSalesStatus().name()
                ),
                "rule_based_inventory_risk_v0.9.6",
                generatedAt
        );
    }

    private AiInsightResponse settlementRiskInsight(SettlementBatch batch) {
        LocalDateTime generatedAt = LocalDateTime.now();
        long netAmount = batch.getTotalSales() + batch.getTotalShippingFee()
                - batch.getTotalRefunds() - batch.getTotalShippingCost();
        boolean negativeNet = netAmount < 0;
        AiRiskLevel riskLevel = negativeNet ? AiRiskLevel.HIGH : batch.getStatus() == SettlementBatchStatus.DRAFT ? AiRiskLevel.MEDIUM : AiRiskLevel.LOW;
        double score = Math.min(0.99, 0.35 + (negativeNet ? 0.45 : 0) + (batch.getStatus() == SettlementBatchStatus.DRAFT ? 0.15 : 0));
        return insight(
                "settlement-risk-" + batch.getId(),
                "SETTLEMENT_BATCH",
                batch.getId(),
                batch.getBatchNumber() + " 정산 리스크",
                Math.round(score * 100.0) / 100.0,
                riskLevel,
                String.format("순정산금액 %,d원, 상태 %s 기준으로 정산 확인 필요 여부를 계산했습니다.", netAmount, batch.getStatus().name()),
                Map.of(
                        "batchNumber", batch.getBatchNumber(),
                        "status", batch.getStatus().name(),
                        "netAmount", netAmount,
                        "periodStart", batch.getPeriodStart().toString(),
                        "periodEnd", batch.getPeriodEnd().toString()
                ),
                "rule_based_settlement_risk_v0.9.6",
                generatedAt
        );
    }

    private int riskWeight(AiRiskLevel riskLevel) {
        return switch (riskLevel) {
            case HIGH -> 3;
            case MEDIUM -> 2;
            case LOW -> 1;
        };
    }

    private int countTokens(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        return (int) java.util.Arrays.stream(value.split("[,\\s]+"))
                .filter(token -> !token.isBlank())
                .count();
    }

    private AiInsightResponse insight(
            String id,
            String targetType,
            Long targetId,
            String title,
            double score,
            AiRiskLevel riskLevel,
            String reason,
            Map<String, Object> features,
            String modelName,
            LocalDateTime generatedAt
    ) {
        return new AiInsightResponse(id, targetType, targetId, title, score, riskLevel, reason, features, modelName, generatedAt);
    }

    private AiReportResponse report(
            String id,
            String title,
            String summary,
            String relatedModule,
            String modelName,
            List<String> evidenceSources,
            List<String> interpretationGuide,
            LocalDateTime generatedAt
    ) {
        return new AiReportResponse(id, title, summary, relatedModule, modelName, evidenceSources, interpretationGuide, generatedAt);
    }
}
