# 기능명세서

기준 버전: `v1.0.1`

| 기능 | 목적 | 주요 사용자 | 주요 화면 | 주요 API/문서 | 권한 | 상태 |
| --- | --- | --- | --- | --- | --- | --- |
| Auth/User | 로그인, 세션, 내 정보 관리 | 사용자, 관리자 | `/login`, `/signup`, `/mypage` | `POST /api/auth/login`, `GET /api/auth/me` | USER 이상 | 구현 |
| Product/Catalog | 상품 마스터와 카테고리 관리 | 상품 담당자 | `/products`, `/admin/products`, `/admin/categories` | [API Reference](../architecture/API_REFERENCE.md) | PRODUCT_READ/WRITE, CATEGORY_MANAGE | 구현 |
| Display/CMS | 상세 블록, 카테고리 네비, 배너 CMS | 상품/전시 담당자 | `/`, `/products/[id]`, `/admin/banners` | detail block, banner, navigation API | BANNER_MANAGE, CATEGORY_MANAGE | 구현 |
| Cart/Order/Payment | 장바구니, 주문, 결제/환불 | 구매자, 주문 담당자 | `/cart`, `/orders`, `/admin/orders` | order/payment API | ORDER_READ, ORDER_STATUS_CHANGE, PAYMENT_REFUND | 구현 |
| Review/Inquiry | 리뷰와 문의 운영 | 구매자, CS 담당자 | `/products/[id]`, `/admin/reviews`, `/admin/inquiries` | review/inquiry API | REVIEW_MODERATE, INQUIRY_REPLY | 구현 |
| Admin Dashboard | 운영 지표 확인 | 관리자 | `/admin`, `/admin/sales` | dashboard, sales API | DASHBOARD_READ | 구현 |
| HR/Permission | 직원/권한 관리 | 최고관리자 | `/admin/settings/staff`, `/admin/settings/roles` | staff/permission API | STAFF_MANAGE, ROLE_MANAGE | 구현 |
| Audit Log | 관리자 작업 이력 추적 | 관리자, 감사 담당자 | `/admin/settings/audit-logs` | audit log API | AUDIT_LOG_READ | 구현 |
| Settings/Terms | 사업자/약관 설정 | 최고관리자 | `/admin/settings` | settings API | SETTINGS_MANAGE | 구현 |
| Inventory/SKU/Barcode | SKU/바코드와 입출고 관리 | 재고 담당자 | `/admin/skus`, `/admin/barcodes`, `/admin/barcode-stock` | SKU/barcode API | SKU_MANAGE, BARCODE_MANAGE, INVENTORY_WRITE | 구현 |
| Production Receipt | 생산 주문과 생산 입고 | 생산/재고 담당자 | `/admin/production` | production API | PRODUCTION_MANAGE | 구현 |
| Warehouse/Stock Count/Safety Stock | 창고, 실사, 안전재고 | 창고 담당자 | `/admin/warehouses`, `/admin/stock-counts`, `/admin/inventory-alerts` | warehouse/stock count API | WAREHOUSE_MANAGE, STOCK_COUNT_MANAGE | 구현 |
| Distribution/Outbound/Shipment | 출고, 송장, 배송, 반품 배송 | 배송 담당자 | `/admin/outbound-orders`, `/admin/shipments`, `/admin/returns` | outbound/shipment/return shipping API | OUTBOUND_MANAGE, SHIPMENT_MANAGE | 구현 |
| Accounting | 회계/정산/마감 고도화 | 회계 담당자 | `/admin/accounting`, `/admin/sales` | accounting/settlement/report API | ACCOUNTING_READ, ACCOUNTING_CLOSE | 구현 |
| AI | 추천, 수요 예측, 리뷰 분석, 이상 탐지, 리스크 알림 | 운영 관리자 | `/admin/ai`, `/admin/ai/recommendations`, `/admin/ai/demand-forecast`, `/admin/ai/reports` | [AI 운영 계획](../plans/V0.9_AI_OPERATIONS_PLAN.md) | AI_RECOMMENDATION_READ, AI_FORECAST_READ, AI_REPORT_READ | 구현 |
