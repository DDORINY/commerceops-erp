# DB 스키마 문서

기준 버전: `v0.6.7`
기준 코드: JPA Entity (`backend/src/main/java/com/commerceops/erp/domain/**/entity`)

v0.2.5부터 Flyway 기반 초기 DDL을 함께 관리한다.

- 초기 마이그레이션: `backend/src/main/resources/db/migration/V1__initial_schema.sql`
- 알림 마이그레이션: `backend/src/main/resources/db/migration/V2__add_notifications.sql`
- 상품 마스터 확장 마이그레이션: `backend/src/main/resources/db/migration/V3__extend_product_catalog_master.sql`
- 상품 상세 블록 마이그레이션: `backend/src/main/resources/db/migration/V4__create_product_detail_blocks.sql`
- 카테고리 네비 확장 마이그레이션: `backend/src/main/resources/db/migration/V5__extend_categories_navigation.sql`
- 메인 배너 CMS 마이그레이션: `backend/src/main/resources/db/migration/V6__create_main_banners.sql`
- 상품 판매/전시 상태 마이그레이션: `backend/src/main/resources/db/migration/V7__extend_product_sales_display_status.sql`
- 상품 상태 변경 이력 마이그레이션: `backend/src/main/resources/db/migration/V8__create_product_status_histories.sql`
- 상품 운영 메모 마이그레이션: `backend/src/main/resources/db/migration/V9__create_product_operation_notes.sql`
- HR/직원 조직 기본 마이그레이션: `backend/src/main/resources/db/migration/V10__create_hr_staff_base.sql`
- 권한 그룹 마이그레이션: `backend/src/main/resources/db/migration/V11__create_permission_groups.sql`
- 메뉴/기능 권한 매트릭스 마이그레이션: `backend/src/main/resources/db/migration/V12__create_permission_matrix.sql`
- 관리자 사이드바 menuKey 보강 마이그레이션: `backend/src/main/resources/db/migration/V13__seed_admin_sidebar_menu_permissions.sql`
- 감사 로그 컨텍스트 확장 마이그레이션: `backend/src/main/resources/db/migration/V14__extend_audit_logs_context.sql`
- 사업자/약관 설정 마이그레이션: `backend/src/main/resources/db/migration/V15__create_business_terms_settings.sql`
- SKU/바코드 마스터 마이그레이션: `backend/src/main/resources/db/migration/V16__create_skus.sql`
- 생산 입고 마이그레이션: `backend/src/main/resources/db/migration/V17__create_production_receipts.sql`
- 바코드 라벨 출력 이력 마이그레이션: `backend/src/main/resources/db/migration/V18__create_barcode_labels.sql`
- 바코드 입출고 메뉴 seed 마이그레이션: `backend/src/main/resources/db/migration/V19__seed_barcode_stock_menu.sql`
- 재고 실사 마이그레이션: `backend/src/main/resources/db/migration/V20__create_stock_counts.sql`
- 출고 지시 마이그레이션: `backend/src/main/resources/db/migration/V23__create_outbound_orders.sql`
- 택배사/배송 방법 마이그레이션: `backend/src/main/resources/db/migration/V24__create_carriers_shipping_methods.sql`
- 송장번호 발급 정보 마이그레이션: `backend/src/main/resources/db/migration/V25__extend_shipments_tracking_number.sql`
- 송장 라벨 출력 이력 마이그레이션: `backend/src/main/resources/db/migration/V26__create_shipment_labels.sql`
- 배송 추적 이벤트 마이그레이션: `backend/src/main/resources/db/migration/V27__create_shipment_tracking_events.sql`
- 반품 배송 정보 마이그레이션: `backend/src/main/resources/db/migration/V28__create_return_shipment_infos.sql`
- 출고 바코드 검수 로그 마이그레이션: `backend/src/main/resources/db/migration/V29__create_outbound_scan_logs.sql`
- 회계 원장/거래 마이그레이션: `backend/src/main/resources/db/migration/V30__create_accounting_ledgers_transactions.sql`
- v0.2.8 운영 분석 기초 API는 기존 회계/주문/결제/창고/재고 예약 테이블을 읽기 전용으로 집계하므로 신규 테이블과 마이그레이션을 추가하지 않는다.
- 기준 DB: MySQL 8.0
- 테스트 프로파일: 기존 H2 `create-drop` 테스트를 유지하기 위해 Flyway 비활성화
- 기존 개발 DB에 `flyway_schema_history`가 없는 경우 `baseline-on-migrate=true`, `baseline-version=0` 기준으로 시작한다.

## 테이블 요약

| 테이블 | 엔티티 | 주요 컬럼/관계 |
| --- | --- | --- |
| `users` | `User` | `email`, `password`, `name`, `phone`, `role`, `status`, timestamps |
| `departments` | `Department` | `name`, unique nullable `code`, nullable `parent_id`, `sort_order`, `active`, timestamps |
| `positions` | `Position` | `name`, `level`, `sort_order`, `active`, timestamps |
| `staff_profiles` | `StaffProfile` | unique `user_id`, nullable `department_id`, nullable `position_id`, unique nullable `employee_no`, `employment_status`, `joined_at`, `left_at`, `active`, timestamps |
| `permission_groups` | `PermissionGroup` | unique `code`, `name`, `description`, `system_group`, `active`, timestamps |
| `user_permission_groups` | `UserPermissionGroup` | `user_id`, `permission_group_id`, nullable `created_by`, `created_at`, unique `(user_id, permission_group_id)` |
| `permissions` | `Permission` | unique `code`, `name`, `domain`, `action`, `description`, `active`, timestamps |
| `permission_group_permissions` | `PermissionGroupPermission` | `permission_group_id`, `permission_id`, `created_at`, unique `(permission_group_id, permission_id)` |
| `admin_menu_permissions` | `AdminMenuPermission` | unique `menu_key`, `menu_label`, `menu_path`, `required_permission_code`, `visible`, `sort_order`, timestamps |
| `categories` | `Category` | `name`, `parent_id`, `depth`, `sort_order`, `active`, `visible_in_nav`, `slug`, timestamps |
| `products` | `Product` | `category_id`, `name`, `description`, 판매가 `price`, `product_code`, `brand`, `manufacturer`, `model_name`, `origin`, `original_price`, `discount_price`, `purchase_price`, `search_keywords`, `tags`, 판매 기간, 배송/SEO 필드, `stock_quantity`, `image_url`, 호환 상태 `status`, 판매 상태 `sales_status`, 전시 상태 `display_status`, `deleted_at`, `safety_stock_quantity`, `options`, timestamps |
| `skus` | `Sku` | `product_id`, `option_signature`, unique `sku_code`, unique nullable `barcode`, `name`, `safety_stock_quantity`, `active`, timestamps |
| `barcode_labels` | `BarcodeLabel` | `sku_id`, `barcode`, `label_format`, `print_count`, nullable `last_printed_at`, nullable `created_by`, timestamps |
| `production_orders` | `ProductionOrder` | unique `production_number`, `status`, `warehouse_id`, `planned_quantity`, `completed_quantity`, `started_at`, `completed_at`, `memo`, `created_by`, `updated_by`, timestamps |
| `production_order_items` | `ProductionOrderItem` | `production_order_id`, `sku_id`, `product_id`, `planned_quantity`, `completed_quantity` |
| `production_receipts` | `ProductionReceipt` | `production_order_id`, `sku_id`, `product_id`, `warehouse_id`, `quantity`, nullable `inventory_log_id`, `created_by`, `created_at` |
| `outbound_orders` | `OutboundOrder` | unique `outbound_number`, `order_id`, `warehouse_id`, `status`, `requested_at`, `picked_at`, `shipped_at`, `memo`, `created_by`, `updated_by`, timestamps |
| `outbound_order_items` | `OutboundOrderItem` | `outbound_order_id`, `order_item_id`, nullable `sku_id`, `product_id`, `quantity`, `picked_quantity`, `scanned_quantity`, timestamps |
| `outbound_scan_logs` | `OutboundScanLog` | `outbound_order_id`, `outbound_order_item_id`, nullable `sku_id`, `barcode`, `quantity`, nullable `scanned_by`, `created_at` |
| `carriers` | `Carrier` | unique `code`, `name`, `tracking_url_template`, `active`, timestamps |
| `shipping_methods` | `ShippingMethod` | unique `code`, `name`, nullable `carrier_id`, `default_fee`, `description`, `active`, timestamps |
| `stock_count_sessions` | `StockCountSession` | unique `count_number`, `warehouse_id`, `status`, `memo`, `started_by`, `completed_by`, `started_at`, `completed_at`, timestamps |
| `stock_count_items` | `StockCountItem` | `session_id`, `sku_id`, `product_id`, `system_quantity`, `counted_quantity`, `difference_quantity`, `memo`, timestamps |
| `product_detail_blocks` | `ProductDetailBlock` | `product_id`, `block_type`, `title`, `content`, `image_url`, `spec_json`, `sort_order`, `visible`, timestamps |
| `product_status_histories` | `ProductStatusHistory` | `product_id`, `changed_by_user_id`, `changed_by_email`, 이전/변경 판매 상태, 이전/변경 전시 상태, `reason`, `created_at` |
| `product_operation_notes` | `ProductOperationNote` | `product_id`, `writer_user_id`, `writer_email`, `content`, timestamps |
| `main_banners` | `MainBanner` | `title`, `subtitle`, `description`, `image_url`, `link_url`, `position`, `sort_order`, `active`, `starts_at`, `ends_at`, timestamps |
| `media_files` | `MediaFile` | `original_filename`, `stored_filename`, `storage_path`, `public_url`, `content_type`, `size`, `media_type`, `created_at` |
| `carts` | `Cart` | `user_id`, `product_id`, `quantity`, `selected_options`, timestamps |
| `orders` | `Order` | `user_id`, `order_number`, `total_price`, `discount_amount`, `coupon_code`, `status`, receiver fields, `payment_status`, timestamps |
| `order_items` | `OrderItem` | `order_id`, `product_id`, snapshot `product_name`, `price`, `quantity`, `selected_options`, `created_at` |
| `payments` | `Payment` | `order_id`, `payment_method`, `payment_status`, `paid_amount`, `transaction_id`, `idempotency_key`, `provider`, timestamps |
| `shipments` | `Shipment` | `order_id`, `status`, `tracking_number`, `carrier`, `tracking_number_source`, `tracking_number_issued_at`, `shipped_at`, `delivered_at`, timestamps |
| `shipment_labels` | `ShipmentLabel` | `shipment_id`, `tracking_number`, `carrier`, `label_format`, `print_count`, `last_printed_at`, `created_by`, timestamps |
| `shipment_tracking_events` | `ShipmentTrackingEvent` | `shipment_id`, `status`, `description`, `event_at`, `raw_payload`, `created_at` |
| `return_requests` | `ReturnRequest` | `order_id`, `user_id`, `reason`, `reason_detail`, `status`, `admin_note`, timestamps |
| `return_shipment_infos` | `ReturnShipmentInfo` | unique `return_request_id`, `carrier`, `tracking_number`, `status`, `shipping_fee`, `fee_payer`, `memo`, timestamps |
| `reviews` | `Review` | `product_id`, `user_id`, `order_item_id`, `rating`, `content`, `status`, `created_at` |
| `audit_logs` | `AuditLog` | `actor_id`, `actor_email`, `actor_name`, `action_type`, `target_type`, nullable `target_id`, `before_status`, `after_status`, `summary`, `ip_address`, `user_agent`, `request_method`, `request_path`, `before_json`, `after_json`, `metadata_json`, `created_at` |
| `business_settings` | `BusinessSettings` | 단일 row 설정, `company_name`, `representative_name`, `business_registration_number`, `mail_order_business_number`, `address`, `customer_service_phone`, `customer_service_email`, `brand_name`, `updated_by`, timestamps |
| `terms_versions` | `TermsVersion` | `type`, `title`, `content`, `version`, `effective_from`, `active`, `created_by`, `created_at`, unique `(type, version)` |
| `notifications` | `Notification` | `user_id`, `type`, `title`, `message`, `target_type`, `target_id`, `read_at`, `created_at` |
| `inquiries` | `Inquiry` | `user_id`, nullable `product_id`, `type`, `subject`, `content`, `answer`, `status`, timestamps |
| `wishlists` | `Wishlist` | `user_id`, `product_id`, `created_at`; 사용자-상품 unique |
| `coupons` | `Coupon` | `code`, `discount_type`, `discount_value`, `min_order_amount`, `max_usage`, `used_count`, `expires_at`, `active`, `created_at` |
| `inventory_logs` | `InventoryLog` | `product_id`, `type`, `quantity`, `before_stock`, `after_stock`, `memo`, `created_at` |
| `accounting_entries` | `AccountingEntry` | `type`, `amount`, `description`, `reference_id`, `created_at` |
| `accounting_ledgers` | `AccountingLedger` | unique `ledger_number`, `period`, `status`, nullable `closed_at`, nullable `closed_by`, timestamps |
| `accounting_transactions` | `AccountingTransaction` | nullable `ledger_id`, unique `transaction_number`, `type`, `direction`, `amount`, `reference_type`, `reference_id`, `occurred_at`, nullable `memo`, nullable `created_by`, timestamps |
| `warehouses` | `Warehouse` | `code`, `name`, `address`, `active`, timestamps |
| `warehouse_stocks` | `WarehouseStock` | `warehouse_id`, `product_id`, `quantity`, `reserved_quantity`, `version`, timestamps; 창고-상품 unique |
| `stock_reservations` | `StockReservation` | `order_id`, `order_item_id`, `warehouse_stock_id`, `quantity`, `status`, timestamps |
| `stock_transfers` | `StockTransfer` | `transfer_number`, `from_warehouse_id`, `to_warehouse_id`, `product_id`, `quantity`, `status`, `requested_at`, `completed_at` |

## enum 값

| Enum | 값 |
| --- | --- |
| `UserRole` | `USER`, `MANAGER`, `ADMIN`, `SUPER_ADMIN` |
| `UserStatus` | `ACTIVE`, `INACTIVE`, `BLOCKED` |
| `EmploymentStatus` | `ACTIVE`, `ON_LEAVE`, `RESIGNED` |
| `ProductStatus` | `ON_SALE`, `SOLD_OUT`, `HIDDEN`, `DELETED` |
| `ProductSalesStatus` | `DRAFT`, `ON_SALE`, `PAUSED`, `SOLD_OUT`, `DISCONTINUED` |
| `ProductDisplayStatus` | `VISIBLE`, `HIDDEN` |
| `StockDisplayStatus` | `IN_STOCK`, `LOW_STOCK`, `SOLD_OUT` |
| `ProductDetailBlockType` | `HEADING`, `TEXT`, `IMAGE`, `NOTICE`, `SPEC_TABLE`, `HTML` |
| `BannerPosition` | `MAIN_TOP`, `MAIN_MIDDLE`, `MAIN_BOTTOM` |
| `ReviewStatus` | `VISIBLE`, `HIDDEN`, `DELETED` |
| `AuditActionType` | `PRODUCT_CREATED`, `PRODUCT_UPDATED`, `PRODUCT_DELETED`, `PRODUCT_STATUS_CHANGED`, `PRODUCT_BULK_STATUS_CHANGED`, `PRODUCT_OPERATION_NOTE_CREATED`, `CATEGORY_CREATED`, `CATEGORY_UPDATED`, `CATEGORY_ACTIVE_CHANGED`, `BANNER_CREATED`, `BANNER_UPDATED`, `BANNER_ACTIVE_CHANGED`, `ORDER_STATUS_CHANGED`, `PAYMENT_CANCELLED`, `REFUND_PROCESSED`, `INVENTORY_ADJUSTED`, `INVENTORY_INBOUNDED`, `STOCK_INBOUNDED`, `STOCK_OUTBOUNDED`, `STOCK_COUNT_CREATED`, `STOCK_COUNT_STARTED`, `STOCK_COUNT_COMPLETED`, `STOCK_COUNT_CANCELLED`, `STOCK_COUNT_ADJUSTMENT_CREATED`, `SKU_CREATED`, `SKU_UPDATED`, `SKU_ACTIVE_CHANGED`, `SKU_BARCODE_REGENERATED`, `BARCODE_LABEL_CREATED`, `BARCODE_LABEL_PRINTED`, `PRODUCTION_ORDER_CREATED`, `PRODUCTION_ORDER_UPDATED`, `PRODUCTION_ORDER_STARTED`, `PRODUCTION_ORDER_COMPLETED`, `PRODUCTION_ORDER_CANCELLED`, `PRODUCTION_RECEIPT_CREATED`, `OUTBOUND_ORDER_CREATED`, `OUTBOUND_ORDER_UPDATED`, `OUTBOUND_ORDER_PICKED`, `OUTBOUND_ORDER_CANCELLED`, `CARRIER_CREATED`, `CARRIER_UPDATED`, `CARRIER_ACTIVE_CHANGED`, `SHIPPING_METHOD_CREATED`, `SHIPPING_METHOD_UPDATED`, `SHIPPING_METHOD_ACTIVE_CHANGED`, `TRACKING_NUMBER_GENERATED`, `TRACKING_NUMBER_UPDATED`, `SHIPMENT_LABEL_CREATED`, `SHIPMENT_LABEL_PRINTED`, `WAREHOUSE_CREATED`, `WAREHOUSE_UPDATED`, `WAREHOUSE_LOCATION_CREATED`, `WAREHOUSE_LOCATION_UPDATED`, `WAREHOUSE_LOCATION_ACTIVE_CHANGED`, `WAREHOUSE_LOCATION_STOCK_UPDATED`, `INVENTORY_ALERT_RULE_CREATED`, `INVENTORY_ALERT_RULE_UPDATED`, `INVENTORY_ALERT_RULE_ACTIVE_CHANGED`, `LOW_STOCK_ALERT_GENERATED`, `STOCK_TRANSFERRED`, `COUPON_CREATED`, `COUPON_UPDATED`, `COUPON_DELETED`, `REVIEW_HIDDEN`, `REVIEW_SHOWN`, `REVIEW_DELETED`, `INQUIRY_ANSWERED`, `INQUIRY_CLOSED`, `STAFF_CREATED`, `STAFF_UPDATED`, `STAFF_STATUS_CHANGED`, `STAFF_ACTIVE_CHANGED`, `PERMISSION_GROUP_CREATED`, `PERMISSION_GROUP_UPDATED`, `PERMISSION_GROUP_ACTIVE_CHANGED`, `USER_PERMISSION_GROUPS_UPDATED`, `PERMISSION_MATRIX_UPDATED`, `MENU_PERMISSION_UPDATED`, `SETTINGS_UPDATED`, `BUSINESS_SETTINGS_UPDATED`, `TERMS_VERSION_CREATED`, `POLICY_VERSION_CREATED`, `PERMISSION_DENIED` |
| `TermsType` | `TERMS_OF_SERVICE`, `PRIVACY_POLICY`, `SHIPPING_RETURN_POLICY` |
| `NotificationType` | `ORDER_STATUS`, `INQUIRY_ANSWERED`, `RETURN_PROCESSED`, `SYSTEM` |
| `OrderStatus` | `PENDING`, `PAID`, `PREPARING`, `SHIPPING`, `COMPLETED`, `CANCELLED`, `REFUNDED` |
| `PaymentMethod` | `MOCK_CARD`, `MOCK_BANK`, `MOCK_SIMPLE_PAY` |
| `PaymentStatus` | `READY`, `PAID`, `FAILED`, `CANCELLED`, `REFUNDED` |
| `ShipmentStatus` | `READY`, `IN_TRANSIT`, `DELIVERED`, `CANCELLED` |
| `TrackingNumberSource` | `MANUAL`, `SYSTEM` |
| `OutboundOrderStatus` | `REQUESTED`, `PICKING`, `PICKED`, `SHIPPED`, `CANCELLED` |
| `ReturnReason` | `CHANGE_OF_MIND`, `DEFECTIVE`, `WRONG_DELIVERY` |
| `ReturnStatus` | `REQUESTED`, `APPROVED`, `REJECTED` |
| `InquiryType` | `PRODUCT`, `ORDER`, `OTHER` |
| `InquiryStatus` | `WAITING`, `ANSWERED`, `CLOSED` |
| `AccountingEntryType` | `SALE`, `REFUND`, `INBOUND` |
| `InventoryLogType` | `INBOUND`, `OUTBOUND`, `ORDER`, `CANCEL`, `ADJUST`, `RETURN_RESTOCK` |
| `StockCountStatus` | `DRAFT`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED` |
| `DiscountType` | `FIXED`, `PERCENT` |
| `StockReservationStatus` | `RESERVED`, `RELEASED`, `SHIPPED`, `RETURNED` |
| `StockTransferStatus` | `PENDING`, `COMPLETED` |

## 주요 인덱스/제약 기준

- 유니크: `users.email`, `departments.code`, `staff_profiles.user_id`, `staff_profiles.employee_no`, `permission_groups.code`, `user_permission_groups(user_id, permission_group_id)`, `permissions.code`, `permission_group_permissions(permission_group_id, permission_id)`, `admin_menu_permissions.menu_key`, `categories.slug`, `products.product_code`, `skus.sku_code`, `skus.barcode`, `orders.order_number`, `payments.order_id`, `payments.idempotency_key`, `shipments.order_id`, `reviews.order_item_id`, `wishlists(user_id, product_id)`, `coupons.code`, `warehouses.code`, `warehouse_stocks(warehouse_id, product_id)`, `stock_transfers.transfer_number`, `accounting_ledgers.ledger_number`, `accounting_transactions.transaction_number`.
- 조회 인덱스: 상태/생성일 기반 관리자 목록 조회를 위해 주문, 상품, 결제, 배송, 문의, 리뷰, 회계, 감사 로그, 알림, 창고 이동 테이블에 상태/일시 인덱스를 둔다. 상세 블록은 `product_detail_blocks(product_id, sort_order)` 기준으로 정렬 조회한다. 상품 운영 이력은 `product_status_histories(product_id, created_at)`, 운영 메모는 `product_operation_notes(product_id, created_at)` 기준으로 최근순 조회한다. 송장번호 조회는 `shipments(tracking_number)`, 발급일 기준 내부 생성 번호 순번 계산은 `shipments(tracking_number_issued_at)` 인덱스를 사용한다.
- 알림 조회 인덱스: `notifications(user_id, read_at, created_at)`, `notifications(type, created_at)`, `notifications(target_type, target_id)`.
- FK: 사용자/상품/주문/창고 주요 관계는 DDL에 FK를 둔다. `categories.parent_id`는 자기 참조 FK다. `audit_logs`는 운영 이력 스냅샷 성격이므로 actor/target FK를 두지 않는다.

## 관계 요약

- `User` 1:N `Cart`, `Order`, `Inquiry`, `Review`, `Wishlist`, `ReturnRequest`, `Notification`.
- `User` 1:1 `StaffProfile`. 직원 프로필은 관리자 조직/권한관리 확장용이며 로그인 계정 상태는 기존 `User.status`가 계속 담당한다.
- `User` N:M `PermissionGroup`은 `user_permission_groups`로 연결한다. v0.4.3 기준 관리자 계열 role 사용자에게만 할당한다.
- `PermissionGroup` N:M `Permission`은 `permission_group_permissions`로 연결한다. v0.4.4 기준 시스템 그룹에는 기본 권한을 seed한다.
- `AdminMenuPermission`은 관리자 메뉴 key와 필요 permission code를 저장한다. v0.4.4에서는 매트릭스 관리 기반이며 실제 사이드바 권한 적용은 v0.4.5에서 고도화한다.
- `Department`는 `parent_id` 자기 참조로 간단한 상하위 부서를 표현하며 `StaffProfile`과 1:N 관계다.
- `Position`은 `StaffProfile`과 1:N 관계다. v0.4.1 기준 `level`은 낮을수록 낮은 직급, 높을수록 높은 직급으로 해석한다.
- `AuditLog`는 v0.2.4 기준 리뷰 운영 작업 이력을 저장하며 FK 없이 actor/target 스냅샷 값을 보관한다.
- `Category` 1:N `Product`.
- `Category`는 `parent_id` 자기 참조로 트리 구조를 구성한다.
- `Product` 1:N `Cart`, `OrderItem`, `Review`, `Inquiry`, `Wishlist`, `InventoryLog`, `WarehouseStock`, `StockTransfer`, `ProductDetailBlock`, `ProductStatusHistory`, `ProductOperationNote`.
- `Product` 1:N `Sku`. v0.5.1 기준 SKU는 재고/입출고/바코드 운영 단위 코드이며, 상품 마스터 코드 `product_code`와 분리한다.
- `Sku` 1:N `BarcodeLabel`. v0.5.3 기준 라벨 출력 이력은 SKU와 현재 barcode snapshot을 함께 저장한다.
- `Warehouse` 1:N `StockCountSession`; `StockCountSession` 1:N `StockCountItem`. 실사 품목은 SKU와 상품을 함께 참조한다.
- `ProductionOrder` 1:N `ProductionOrderItem`; `ProductionOrder` 1:N `ProductionReceipt`.
- `ProductionOrderItem`은 `Sku`와 `Product`를 참조한다. v0.5.2 기준 BOM/자재 차감 없이 완제품 SKU 입고만 처리한다.
- `ProductionReceipt`는 생산 완료 입고 이력이며 `Sku`, `Product`, `Warehouse`, 선택 `InventoryLog`를 참조한다.
- `Order` 1:N `OutboundOrder`; `OutboundOrder` 1:N `OutboundOrderItem`. v0.6.1 기준 출고 지시는 주문과 창고를 기준으로 생성하고, 출고 품목은 주문 품목과 상품, 가능한 경우 SKU/바코드를 함께 참조한다.
- `Carrier` 1:N `ShippingMethod`. v0.6.2 기준 배송 방법은 택배사와 선택적으로 연결하며, 비활성 택배사는 신규 배송 방법에 연결하지 않는다.
- v0.2.3 기준 상품 대표 이미지는 `products.image_url`에 업로드 결과 URL을 저장한다. `media_files`는 파일 메타데이터를 보관하며 상품과의 별도 FK는 아직 두지 않는다.
- `Order` 1:N `OrderItem`; `Order` 1:1 `Payment`, `Shipment`; `Order` 1:N `ReturnRequest`, `StockReservation`.
- `Warehouse` 1:N `WarehouseStock`; `WarehouseStock` 1:N `StockReservation`.
- `StockTransfer`는 출발/도착 `Warehouse`와 `Product`를 참조한다.

## 현재 스키마 주의점

- v0.2.5 이전 개발 DB는 Hibernate `ddl-auto=update`로 만들어졌을 수 있다. 운영 반영 전에는 `V1__initial_schema.sql`과 실제 DB 스키마 차이를 별도로 점검해야 한다.
- `Product.options`, `Cart.selectedOptions`, `OrderItem.selectedOptions`는 JSON 문자열 또는 converter 기반 저장이다.
- v0.3.1 기준 `Product.price`는 판매가, `originalPrice`는 정상가, `discountPrice`는 할인 금액, `purchasePrice`는 관리자 전용 매입가로 사용한다. 마진율은 저장하지 않고 관리자 응답에서 `(판매가 - 매입가) / 판매가 * 100`으로 계산한다.
- v0.3.1 기준 `Product.searchKeywords`, `Product.tags`, `Product.seoKeywords`는 TEXT 구분자 문자열로 저장한다. AI 태그 추천과 고급 검색이 필요해지면 별도 테이블로 분리할 수 있다.
- v0.3.5 기준 상품 운영 상태는 `sales_status`와 `display_status`를 기준으로 판단한다. 기존 `status`는 호환 필드로 유지하며, 삭제는 `deleted_at` soft delete 기준을 함께 사용한다.
- v0.3.5 기준 사용자 구매 가능 여부는 `sales_status=ON_SALE`, `display_status=VISIBLE`, `deleted_at IS NULL`, 재고 1개 이상, 판매 기간 범위 충족 여부로 계산한다.
- v0.3.5 기준 `safety_stock_quantity` 이하이면 사용자 응답에서 `LOW_STOCK`/`품절 임박`으로 표시한다.
- v0.3.6 기준 판매/전시 상태가 실제 변경된 경우 `product_status_histories`에 변경 전/후 상태와 작업자 snapshot을 저장한다.
- v0.3.6 기준 상품 운영 메모는 `product_operation_notes`에 누적 추가만 지원한다. 수정/삭제는 후속 운영 고도화 범위다.
- v0.4.1 기준 HR 기본 모델은 조회 API와 repository/service 기반만 제공한다. 부서/직급/직원 생성·수정 화면과 API는 v0.4.2 이후 범위다.
- v0.4.1 기준 `departments.active`, `positions.active`, `staff_profiles.active`는 비활성 표시 기준이다. 직원의 로그인 가능 여부는 기존 `users.status`와 인증 정책이 담당한다.
- v0.4.3 기준 `permission_groups`는 기존 `User.role`을 대체하지 않고 병행 운영한다. `SUPER_ADMIN_GROUP`, `ADMIN_GROUP`, `MANAGER_GROUP`은 시스템 기본 그룹으로 seed하며, 시스템 그룹은 비활성화하지 않는다.
- v0.4.3 기준 inactive permission group은 사용자에게 신규 할당할 수 없다. 사용자별 권한 그룹 변경은 `user_permission_groups`를 교체 저장하고 `audit_logs`에 요약 기록한다.
- v0.4.4 기준 `permissions`는 기능 권한 코드 master다. `SUPER_ADMIN`은 모든 활성 권한을 보유한 것으로 계산하고, `ADMIN`/`MANAGER`는 사용자 할당 권한 그룹 또는 role 기본 시스템 그룹으로 유효 권한을 계산한다.
- v0.4.4 기준 권한 그룹-권한 매핑과 메뉴 필요 권한 변경은 `audit_logs`에 `PERMISSION_MATRIX_UPDATED`, `MENU_PERMISSION_UPDATED`로 기록한다.
- v0.4.5 기준 `admin_menu_permissions.menu_key`는 프론트 `ADMIN_MENU_GROUPS`의 `menuKey`와 매칭한다. v0.4.4 seed에 없던 query 기반 메뉴는 `V13__seed_admin_sidebar_menu_permissions.sql`로 보강한다.
- `Product.stockQuantity`와 `WarehouseStock.quantity/reservedQuantity`가 함께 존재한다. 창고 기능에서는 창고별 재고와 예약이 source of truth가 되며, 상품 총 재고는 보조/요약 값으로 함께 갱신된다.
- 실제 PG 벤더별 거래 원장/웹훅 이벤트 테이블은 아직 없다. v0.2.2에서는 `payments.idempotency_key`, `provider`만 추가했다.
- v0.4.7 기준 `audit_logs`는 `V14__extend_audit_logs_context.sql`로 요청 IP/User-Agent/method/path와 before/after/metadata JSON을 추가한다. 상품, 카테고리, 배너, 주문, 결제, 재고, 창고, 쿠폰, 문의, 리뷰, 직원, 권한 작업과 permission denied 이력을 기록한다. 로그 삭제/수정 API는 제공하지 않는다.
- v0.4.8 기준 `business_settings`는 단일 row 설정으로 운영한다. row가 없으면 저장 시 최초 생성하고 이후에는 기존 row를 갱신한다.
- v0.4.8 기준 `terms_versions`는 기존 row를 overwrite하지 않고 새 버전을 생성한다. 서비스 레벨에서 type별 active=true 최신 버전을 하나만 유지하고 과거 버전 조회를 허용한다. 삭제 API는 제공하지 않는다.
- v0.5.1 기준 `skus.sku_code`는 수동 입력 또는 서버 자동 생성 값을 저장한다. `barcode`는 nullable이지만 값이 있으면 unique이며, 비어 있는 생성 요청은 서버가 고유 바코드를 자동 생성한다.
- v0.5.2 기준 생산 완료 처리는 `Product.stockQuantity`와 `WarehouseStock.quantity`를 함께 증가시키고 `inventory_logs.type=PRODUCTION_RECEIPT`를 생성한다.
- v0.5.3 기준 `barcode_labels`는 라벨 생성/출력 이력을 저장한다. HTML 미리보기 기반이며 실제 프린터 드라이버/PDF 출력은 아직 제공하지 않는다.
- v0.5.4 기준 바코드 입고/출고는 신규 재고 테이블을 만들지 않고 기존 `warehouse_stocks`와 `inventory_logs`를 사용한다. 통합 `inventory_movements` 원장은 후속 고도화 범위다.
- v0.5.5 기준 재고 실사 완료 시 차이 수량만 기존 `InventoryLog(ADJUST)`에 기록하고 상품/창고 재고를 조정한다. 위치별 실사는 v0.5.6으로 이관한다.
- v0.5.6 기준 `warehouse_locations`는 창고별 위치 코드와 구역/통로/랙/셀 정보를 저장한다. `(warehouse_id, code)`는 unique이며, 비활성 위치는 조회되지만 운영상 신규 이동/배치 대상에서 제외할 수 있다.
- v0.5.6 기준 `warehouse_location_stocks`는 위치별 SKU 재고 기반 테이블이다. 현재는 조회 기반을 우선 제공하고, 위치 간 이동/수량 조정은 v0.5.7 재고 이동 고도화로 이관한다.
- v0.5.7 기준 `inventory_alert_rules`는 SKU 또는 SKU+창고 단위 안전재고 기준을 저장한다. `warehouse_id`가 null이면 전체 창고 기준이며, 재고 부족 조회는 활성 rule만 대상으로 한다.
- v0.6.7 기준 `outbound_orders`는 `outbound_number`, 주문, 창고, 상태, 요청/피킹/배송 일시와 메모를 저장한다. `outbound_order_items`는 주문 품목, 상품, nullable SKU, 지시/피킹/스캔 수량을 저장한다. `outbound_scan_logs`는 바코드 검수 이력을 저장한다. 실제 재고 차감 source of truth 고도화는 후속 범위다.
- v0.6.2 기준 `carriers`와 `shipping_methods`는 송장/배송 처리에서 선택할 master 데이터다. 실제 택배사 API 호출과 자동 운임 계산은 구현하지 않는다.
- v0.6.3 기준 `shipments.tracking_number_source`는 `MANUAL` 또는 `SYSTEM`으로 송장번호 입력 방식을 기록하고, `tracking_number_issued_at`은 수동 저장 또는 자동 생성 시각을 기록한다. READY 상태에서 최초 송장 등록 시에만 예약 재고 출고와 주문 `SHIPPING` 전환을 수행하며, IN_TRANSIT 상태의 송장 수정은 재고 차감을 반복하지 않는다.
- v0.6.4 기준 `shipment_labels`는 송장 라벨 생성과 출력 이력을 저장한다. 송장번호와 택배사는 생성 시점 snapshot으로 저장하며, 실제 프린터 드라이버/PDF 출력은 제공하지 않는다.
- v0.6.5 기준 `shipment_tracking_events`는 배송 상태 변경과 수동 추적 이벤트를 저장한다. 실제 택배사 tracking API 연동과 웹훅 자동 갱신은 후속 범위다.
- `media_files` 운영 DDL과 인덱스는 `V1__initial_schema.sql`에 포함했다.

## v0.7.4 택배비 매입/배송비 정산 DB

`shipping_cost_entries` 테이블을 추가했다.

| 컬럼 | 설명 |
| --- | --- |
| `id` | 택배비 비용 항목 ID |
| `shipment_id` | 배송 ID, 1건당 1개 항목으로 unique 처리 |
| `carrier_id` | 택배사 ID |
| `shipping_method_id` | 배송 방법 ID |
| `cost_amount` | 내부 택배비 매입 비용 |
| `charged_amount` | 고객 청구 배송비. 현재 주문 배송비 컬럼이 없어 0으로 저장 |
| `occurred_at` | 비용 발생 일시 |
| `settlement_status` | `PENDING`, `SETTLED`, `EXCLUDED` |
| `memo` | 처리 메모 |

`SHIPPING_COST_MANAGE` 권한 seed도 함께 추가했다.
