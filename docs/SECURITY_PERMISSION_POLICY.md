# 관리자 API Permission 정책

기준 버전: `v0.4.7-admin-audit-log-expansion`

## 목적

v0.4.6부터 관리자 API는 기존 role 기반 접근 제어를 유지하면서, 실제 기능 실행은 permission code 기준으로 한 번 더 검증한다. role은 관리자 영역에 들어올 수 있는 1차 기준이고, permission code는 읽기, 쓰기, 상태 변경, 환불, 권한 관리 같은 세부 작업 허용 기준이다.

## 기본 정책

- `USER`와 비로그인 사용자는 `/api/admin/**`에 접근할 수 없다.
- `MANAGER`, `ADMIN`, `SUPER_ADMIN`은 role 기반 1차 접근 대상이다.
- `SUPER_ADMIN`은 모든 활성 permission을 보유한 것으로 간주한다.
- `ADMIN`, `MANAGER`는 권한 그룹과 role 기본 그룹으로 계산된 effective permission code를 기준으로 API 실행 권한을 검증한다.
- 권한이 없으면 403과 “해당 작업을 수행할 권한이 없습니다. 관리자에게 권한을 요청하세요.” 메시지를 반환한다.

## 구현 구조

- `PermissionCodes`: permission code 상수 모음.
- `PermissionChecker`: 현재 사용자와 필요한 permission code를 받아 권한을 검증한다.
- `PermissionMatrixService`: 사용자 effective permission을 계산한다.
- Controller method 시작부에서 `permissionChecker.require(currentUser, PermissionCodes.X)`를 호출한다.

## 주요 권한 매핑

| 영역 | permission code |
| --- | --- |
| 대시보드/운영 분석 | `DASHBOARD_READ` |
| 상품 조회 | `PRODUCT_READ` |
| 상품 생성/수정/삭제/상세 블록/운영 메모 | `PRODUCT_WRITE` |
| 상품 상태 변경 | `PRODUCT_STATUS_CHANGE` |
| 상품 대량 변경 | `PRODUCT_BULK_UPDATE` |
| 카테고리 관리 | `CATEGORY_MANAGE` |
| 배너 관리 | `BANNER_MANAGE` |
| 주문/배송/반품 조회 | `ORDER_READ` |
| 주문/배송/반품 상태 변경 | `ORDER_STATUS_CHANGE` |
| 결제 취소/환불 | `PAYMENT_REFUND` |
| 재고 조회 | `INVENTORY_READ` |
| 재고 조정/입고/할당 | `INVENTORY_WRITE` |
| 창고/재고 이동 관리 | `WAREHOUSE_MANAGE` |
| 회계/매출 조회 | `ACCOUNTING_READ` |
| 회계 마감 후보 | `ACCOUNTING_CLOSE` |
| 쿠폰 관리 | `COUPON_MANAGE` |
| 리뷰 운영 | `REVIEW_MODERATE` |
| 문의 답변/종료 | `INQUIRY_REPLY` |
| 직원/HR 관리 | `STAFF_MANAGE` |
| 권한 그룹/권한 매트릭스 관리 | `ROLE_MANAGE` |
| 감사 로그 조회 | `AUDIT_LOG_READ` |

## 후속 이슈

- v0.4.7부터 인증된 관리자의 permission code 부족으로 인한 403은 `PERMISSION_DENIED` audit log로 기록한다.
- Controller 직접 호출 방식에서 annotation/AOP 기반 선언형 권한 검증으로 고도화할 수 있다.
- 전체 관리자 API의 자동화된 권한별 200/403 테스트 커버리지는 후속 안정화 범위로 유지한다.
