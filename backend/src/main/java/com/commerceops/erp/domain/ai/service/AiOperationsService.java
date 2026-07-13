package com.commerceops.erp.domain.ai.service;

import com.commerceops.erp.domain.ai.dto.AiDatasetCatalogResponse;
import com.commerceops.erp.domain.ai.dto.AiInsightResponse;
import com.commerceops.erp.domain.ai.dto.AiOperationsHealthResponse;
import com.commerceops.erp.domain.ai.dto.AiOperationsOverviewResponse;
import com.commerceops.erp.domain.ai.enums.AiRiskLevel;
import lombok.RequiredArgsConstructor;
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
}
