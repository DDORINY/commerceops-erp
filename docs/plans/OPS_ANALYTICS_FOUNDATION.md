# 운영 분석 기반 문서

기준 버전: `v0.2.8`

v0.2.8은 고급 회계, BI, WMS 자동화를 완성하는 버전이 아니라, 다음 단계에서 확장할 수 있는 기초 지표와 API 경계를 정리하는 버전이다.

## API

- `GET /api/admin/ops-analytics/overview`
- 권한: `MANAGER`, `ADMIN`, `SUPER_ADMIN` 조회 가능
- 응답 DTO: `OpsAnalyticsOverviewResponse`

## 현재 집계 기준

### 회계

- `accounting_entries`의 `SALE`, `REFUND`, `INBOUND` 금액 합계를 사용한다.
- `netSales`는 `SALE - REFUND`로 계산한다.
- 복식부기, 계정과목, 마감 전표, 세무 리포트는 후속 범위다.

### 매출/주문

- 전체 주문 수는 `orders` 기준이다.
- 결제 완료 매출은 `payments.payment_status = PAID`의 `paid_amount` 합계 기준이다.
- 주문 상태별 개수는 `OrderStatus` enum 값을 모두 집계한다.
- 평균 결제 주문 금액은 `결제 완료 매출 / PAID 주문 수`로 계산한다.

### 창고/WMS

- 창고 수는 `warehouses` 기준이다.
- 재고 수량은 `warehouse_stocks.quantity`, 예약 수량은 `reserved_quantity`, 가용 수량은 `quantity - reserved_quantity`로 계산한다.
- 예약 상태별 개수는 `StockReservationStatus` enum 값을 모두 집계한다.
- 피킹/패킹/출고 자동화와 복잡한 할당 정책은 후속 범위다.

## 후속 후보

- 관리자 화면에 운영 분석 overview 카드 추가.
- 기간별 회계/매출 비교 API.
- 상품/카테고리/창고별 수익성 분석.
- 정산 마감, 전표 잠금, 계정과목 기반 복식부기.
- 창고별 피킹 리스트, 패킹, 출고 지시, 작업자 할당.
- BI 차트 라이브러리 도입과 CSV/Excel 리포트.
