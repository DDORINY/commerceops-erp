# 알고리즘/운영 로직 구조

## SKU code 생성 정책

- 상품 또는 옵션 조합 단위로 SKU를 생성한다.
- SKU code는 고유해야 하며 중복 입력을 차단한다.
- 기존 상품코드와 SKU code는 역할을 분리한다.

## Barcode 생성 정책

- SKU별 기본 바코드 1개를 우선한다.
- 바코드는 unique 제약으로 중복을 방지한다.
- 재발급 시 기존 바코드 변경 이력을 audit log에 남긴다.

## 생산번호 생성 정책

- 생산 주문은 고유 생산번호를 가진다.
- 생산 시작, 완료, 취소 상태 전환을 기록한다.

## 송장번호 생성 후보 정책

- 실제 택배사 API 연동 전까지 내부 테스트용 자동 생성과 수동 입력을 병행한다.
- 송장번호 생성/수정은 audit log 기록 대상이다.

## 재고 차감/증가 정책

- 생산 완료, 입고, 실사 조정은 재고 증가 또는 보정 대상이다.
- 출고와 주문 확정 흐름은 재고 차감 대상이다.
- 재고 변경은 상품/창고 재고와 이력 로그를 함께 갱신한다.

## 안전재고 부족 판단 정책

- SKU 또는 SKU+창고 단위로 안전재고 기준을 설정한다.
- 현재 수량이 기준 이하이면 재고 부족 항목으로 노출한다.

## PermissionChecker 권한 검증 구조

```text
현재 사용자 확인
  -> SUPER_ADMIN이면 전체 허용
  -> effective permission code 조회
  -> required permission 포함 여부 확인
  -> 없으면 403과 PERMISSION_DENIED audit log 후보
```

## AuditLog 기록 구조

- 작업자
- action
- target type/id
- 요청 method/path
- IP/User-Agent
- before JSON
- after JSON
- metadata JSON

## AI 수요 예측 후보 알고리즘

- baseline: 이동평균, 최근 판매량 기반 예측.
- ML 후보: scikit-learn regression, PyTorch time series 모델.

## 이상 탐지 후보 알고리즘

- rule-based baseline.
- Isolation Forest.
- AutoEncoder 후보.

## 리뷰 감성 분석 후보 알고리즘

- baseline: TF-IDF + Logistic Regression.
- 고도화 후보: KoBERT/KoELECTRA 계열 모델.
