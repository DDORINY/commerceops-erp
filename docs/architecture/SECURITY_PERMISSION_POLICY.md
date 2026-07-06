# 관리자 API 권한 정책

기준 버전: `v0.4.7-admin-audit-log-expansion`

## 목적

관리자 API는 기존 role 기반 1차 접근 제어를 유지하면서, 실제 기능 실행은 permission code 기준으로 세분화한다. role은 관리자 영역에 들어올 수 있는 큰 기준이고, permission code는 조회, 생성/수정, 상태 변경, 환불, 권한 관리 같은 세부 작업을 허용하는 기준이다.

## 기본 정책

- 비로그인 사용자와 `USER` role은 `/api/admin/**`에 접근할 수 없다.
- `MANAGER`, `ADMIN`, `SUPER_ADMIN`은 관리자 API의 1차 접근 대상이다.
- `SUPER_ADMIN`은 모든 permission code를 보유한 것으로 간주한다.
- `ADMIN`, `MANAGER`는 role 기본 권한 그룹과 사용자에게 할당된 권한 그룹을 합산한 effective permission code 기준으로 세부 실행 권한을 검증한다.
- 권한이 없으면 403 응답을 반환한다.
- 403 응답 메시지는 사용자가 이해할 수 있도록 “해당 작업을 수행할 권한이 없습니다. 관리자에게 권한을 요청하세요.” 기준으로 정리한다.

## 구현 구조

- `PermissionCodes`: permission code 상수 모음.
- `PermissionChecker`: 현재 사용자와 필요한 permission code를 받아 권한을 검증한다.
- `PermissionMatrixService`: 사용자 effective permission code를 계산한다.
- `PermissionGroup`: 권한 그룹을 표현한다.
- `PermissionGroupPermission`: 권한 그룹과 permission code를 연결한다.
- `UserPermissionGroup`: 사용자와 권한 그룹을 연결한다.
- `AdminMenuPermission`: 관리자 메뉴와 필요한 permission code를 연결한다.

Controller method 시작부에서 `permissionChecker.require(currentUser, PermissionCodes.X)`를 호출하는 방식을 기본으로 한다. annotation/AOP 기반 고도화는 후속 후보로 둔다.

## 주요 권한 매핑

| 영역 | permission code | 사용 기준 |
| --- | --- | --- |
| 대시보드/운영 분석 | `DASHBOARD_READ` | 관리자 대시보드와 운영 분석 조회 |
| 상품 조회 | `PRODUCT_READ` | 상품 목록/상세 조회 |
| 상품 변경 | `PRODUCT_WRITE` | 상품 생성/수정/삭제, 상세 블록, 운영 메모 |
| 상품 상태 변경 | `PRODUCT_STATUS_CHANGE` | 판매 상태, 전시 상태, 구매 가능 여부 변경 |
| 상품 대량 변경 | `PRODUCT_BULK_UPDATE` | 선택 상품 일괄 상태 변경 |
| 카테고리 관리 | `CATEGORY_MANAGE` | 카테고리 생성/수정/활성/노출 관리 |
| 배너 관리 | `BANNER_MANAGE` | 메인 배너 생성/수정/비활성화 |
| 주문 조회 | `ORDER_READ` | 주문 목록/상세 조회 |
| 주문 상태 변경 | `ORDER_STATUS_CHANGE` | 주문 상태 변경 |
| 결제/환불 | `PAYMENT_REFUND` | 결제 취소와 환불 처리 |
| 재고 조회 | `INVENTORY_READ` | 재고, SKU, 바코드, 실사, 안전재고 조회 |
| 재고 변경 | `INVENTORY_WRITE` | 입고, 조정, 재고 변경 |
| SKU 관리 | `SKU_MANAGE` | SKU 생성/수정/활성 변경 |
| 바코드 관리 | `BARCODE_MANAGE` | 바코드 발급/재발급, 라벨 생성 |
| 생산 관리 | `PRODUCTION_MANAGE` | 생산 주문 생성/수정/시작/완료/취소 |
| 창고 관리 | `WAREHOUSE_MANAGE` | 창고, 창고 위치, 창고 재고 이동 관리 |
| 회계 조회 | `ACCOUNTING_READ` | 회계/매출 조회 |
| 회계 마감 후보 | `ACCOUNTING_CLOSE` | 정산/마감 후보 작업 |
| 쿠폰 관리 | `COUPON_MANAGE` | 쿠폰 생성/삭제/관리 |
| 리뷰 운영 | `REVIEW_MODERATE` | 리뷰 숨김/해제/삭제 |
| 문의 답변 | `INQUIRY_REPLY` | 문의 답변/종료 |
| 설정 관리 | `SETTINGS_MANAGE` | 사업자 설정, 약관/정책 저장 |
| 직원 관리 | `STAFF_MANAGE` | 직원 계정과 인사 프로필 관리 |
| 권한 관리 | `ROLE_MANAGE` | 권한 그룹, 권한 매트릭스, 메뉴 권한 관리 |
| 감사 로그 조회 | `AUDIT_LOG_READ` | 관리자 작업 이력과 감사 로그 조회 |

## v0.6 권한 후보

v0.6 유통관리/송장/배송 작업에서는 아래 권한을 추가 후보로 둔다.

| 영역 | permission code | 사용 기준 |
| --- | --- | --- |
| 출고 조회 | `OUTBOUND_READ` | 출고 지시와 출고 품목 조회 |
| 출고 관리 | `OUTBOUND_MANAGE` | 출고 지시 생성/수정/검수/출고 처리 |
| 배송 조회 | `SHIPMENT_READ` | 배송과 송장 정보 조회 |
| 배송 관리 | `SHIPMENT_MANAGE` | 송장번호, 배송 상태, 추적 이벤트 관리 |
| 택배사 관리 | `CARRIER_MANAGE` | 택배사와 배송 방법 관리 |
| 송장 라벨 출력 | `SHIPPING_LABEL_PRINT` | 송장 라벨 생성/출력 |
| 반품 배송 관리 | `RETURN_SHIPPING_MANAGE` | 반품/교환 배송 관리 |

SUPER_ADMIN은 전체 권한을 보유한다. 기존 role 기반 1차 관리자 접근은 유지하고, 세부 실행은 permission code 기준으로 점진 전환한다.

## 403 처리와 감사 로그

- 권한이 없는 API 호출은 403을 반환한다.
- v0.4.7부터 인증된 관리자의 permission code 부족으로 발생한 403은 `PERMISSION_DENIED` 감사 로그로 기록한다.
- 감사 로그에는 작업자, 대상, 요청 경로, 요청 메서드, IP, user agent, 요약 정보를 기록한다.
- 민감 정보는 감사 로그에 원문으로 남기지 않는다.

## 후속 이슈

- Controller 직접 호출 방식에서 annotation/AOP 기반 선언형 권한 검증으로 고도화할 수 있다.
- 권한별 200/403 자동 테스트 커버리지는 후속 안정화 범위로 확장한다.
- 조직도 기반 권한 상속, 승인 워크플로우, 외부 IAM/SSO는 장기 후보로 둔다.
