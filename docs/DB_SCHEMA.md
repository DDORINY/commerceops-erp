# DB 스키마 문서

기준 버전: `v0.3.1`
기준 코드: JPA Entity (`backend/src/main/java/com/commerceops/erp/domain/**/entity`)

v0.2.5부터 Flyway 기반 초기 DDL을 함께 관리한다.

- 초기 마이그레이션: `backend/src/main/resources/db/migration/V1__initial_schema.sql`
- 알림 마이그레이션: `backend/src/main/resources/db/migration/V2__add_notifications.sql`
- 상품 마스터 확장 마이그레이션: `backend/src/main/resources/db/migration/V3__extend_product_catalog_master.sql`
- v0.2.8 운영 분석 기초 API는 기존 회계/주문/결제/창고/재고 예약 테이블을 읽기 전용으로 집계하므로 신규 테이블과 마이그레이션을 추가하지 않는다.
- 기준 DB: MySQL 8.0
- 테스트 프로파일: 기존 H2 `create-drop` 테스트를 유지하기 위해 Flyway 비활성화
- 기존 개발 DB에 `flyway_schema_history`가 없는 경우 `baseline-on-migrate=true`, `baseline-version=0` 기준으로 시작한다.

## 테이블 요약

| 테이블 | 엔티티 | 주요 컬럼/관계 |
| --- | --- | --- |
| `users` | `User` | `email`, `password`, `name`, `phone`, `role`, `status`, timestamps |
| `categories` | `Category` | `name`, timestamps |
| `products` | `Product` | `category_id`, `name`, `description`, 판매가 `price`, `product_code`, `brand`, `manufacturer`, `model_name`, `origin`, `original_price`, `discount_price`, `purchase_price`, `search_keywords`, `tags`, 판매 기간, 배송/SEO 필드, `stock_quantity`, `image_url`, `status`, `options`, timestamps |
| `media_files` | `MediaFile` | `original_filename`, `stored_filename`, `storage_path`, `public_url`, `content_type`, `size`, `media_type`, `created_at` |
| `carts` | `Cart` | `user_id`, `product_id`, `quantity`, `selected_options`, timestamps |
| `orders` | `Order` | `user_id`, `order_number`, `total_price`, `discount_amount`, `coupon_code`, `status`, receiver fields, `payment_status`, timestamps |
| `order_items` | `OrderItem` | `order_id`, `product_id`, snapshot `product_name`, `price`, `quantity`, `selected_options`, `created_at` |
| `payments` | `Payment` | `order_id`, `payment_method`, `payment_status`, `paid_amount`, `transaction_id`, `idempotency_key`, `provider`, timestamps |
| `shipments` | `Shipment` | `order_id`, `status`, `tracking_number`, `carrier`, `shipped_at`, `delivered_at`, timestamps |
| `return_requests` | `ReturnRequest` | `order_id`, `user_id`, `reason`, `reason_detail`, `status`, `admin_note`, timestamps |
| `reviews` | `Review` | `product_id`, `user_id`, `order_item_id`, `rating`, `content`, `status`, `created_at` |
| `audit_logs` | `AuditLog` | `actor_id`, `actor_email`, `actor_name`, `action_type`, `target_type`, `target_id`, `before_status`, `after_status`, `summary`, `created_at` |
| `notifications` | `Notification` | `user_id`, `type`, `title`, `message`, `target_type`, `target_id`, `read_at`, `created_at` |
| `inquiries` | `Inquiry` | `user_id`, nullable `product_id`, `type`, `subject`, `content`, `answer`, `status`, timestamps |
| `wishlists` | `Wishlist` | `user_id`, `product_id`, `created_at`; 사용자-상품 unique |
| `coupons` | `Coupon` | `code`, `discount_type`, `discount_value`, `min_order_amount`, `max_usage`, `used_count`, `expires_at`, `active`, `created_at` |
| `inventory_logs` | `InventoryLog` | `product_id`, `type`, `quantity`, `before_stock`, `after_stock`, `memo`, `created_at` |
| `accounting_entries` | `AccountingEntry` | `type`, `amount`, `description`, `reference_id`, `created_at` |
| `warehouses` | `Warehouse` | `code`, `name`, `address`, `active`, timestamps |
| `warehouse_stocks` | `WarehouseStock` | `warehouse_id`, `product_id`, `quantity`, `reserved_quantity`, `version`, timestamps; 창고-상품 unique |
| `stock_reservations` | `StockReservation` | `order_id`, `order_item_id`, `warehouse_stock_id`, `quantity`, `status`, timestamps |
| `stock_transfers` | `StockTransfer` | `transfer_number`, `from_warehouse_id`, `to_warehouse_id`, `product_id`, `quantity`, `status`, `requested_at`, `completed_at` |

## enum 값

| Enum | 값 |
| --- | --- |
| `UserRole` | `USER`, `MANAGER`, `ADMIN`, `SUPER_ADMIN` |
| `UserStatus` | `ACTIVE`, `INACTIVE`, `BLOCKED` |
| `ProductStatus` | `ON_SALE`, `SOLD_OUT`, `HIDDEN`, `DELETED` |
| `ReviewStatus` | `VISIBLE`, `HIDDEN`, `DELETED` |
| `AuditActionType` | `REVIEW_HIDE`, `REVIEW_SHOW`, `REVIEW_DELETE` |
| `NotificationType` | `ORDER_STATUS`, `INQUIRY_ANSWERED`, `RETURN_PROCESSED`, `SYSTEM` |
| `OrderStatus` | `PENDING`, `PAID`, `PREPARING`, `SHIPPING`, `COMPLETED`, `CANCELLED`, `REFUNDED` |
| `PaymentMethod` | `MOCK_CARD`, `MOCK_BANK`, `MOCK_SIMPLE_PAY` |
| `PaymentStatus` | `READY`, `PAID`, `FAILED`, `CANCELLED`, `REFUNDED` |
| `ShipmentStatus` | `READY`, `IN_TRANSIT`, `DELIVERED`, `CANCELLED` |
| `ReturnReason` | `CHANGE_OF_MIND`, `DEFECTIVE`, `WRONG_DELIVERY` |
| `ReturnStatus` | `REQUESTED`, `APPROVED`, `REJECTED` |
| `InquiryType` | `PRODUCT`, `ORDER`, `OTHER` |
| `InquiryStatus` | `WAITING`, `ANSWERED`, `CLOSED` |
| `AccountingEntryType` | `SALE`, `REFUND`, `INBOUND` |
| `InventoryLogType` | `INBOUND`, `OUTBOUND`, `ORDER`, `CANCEL`, `ADJUST`, `RETURN_RESTOCK` |
| `DiscountType` | `FIXED`, `PERCENT` |
| `StockReservationStatus` | `RESERVED`, `RELEASED`, `SHIPPED`, `RETURNED` |
| `StockTransferStatus` | `PENDING`, `COMPLETED` |

## 주요 인덱스/제약 기준

- 유니크: `users.email`, `products.product_code`, `orders.order_number`, `payments.order_id`, `payments.idempotency_key`, `shipments.order_id`, `reviews.order_item_id`, `wishlists(user_id, product_id)`, `coupons.code`, `warehouses.code`, `warehouse_stocks(warehouse_id, product_id)`, `stock_transfers.transfer_number`.
- 조회 인덱스: 상태/생성일 기반 관리자 목록 조회를 위해 주문, 상품, 결제, 배송, 문의, 리뷰, 회계, 감사 로그, 알림, 창고 이동 테이블에 상태/일시 인덱스를 둔다.
- 알림 조회 인덱스: `notifications(user_id, read_at, created_at)`, `notifications(type, created_at)`, `notifications(target_type, target_id)`.
- FK: 사용자/상품/주문/창고 주요 관계는 DDL에 FK를 둔다. `audit_logs`는 운영 이력 스냅샷 성격이므로 actor/target FK를 두지 않는다.

## 관계 요약

- `User` 1:N `Cart`, `Order`, `Inquiry`, `Review`, `Wishlist`, `ReturnRequest`, `Notification`.
- `AuditLog`는 v0.2.4 기준 리뷰 운영 작업 이력을 저장하며 FK 없이 actor/target 스냅샷 값을 보관한다.
- `Category` 1:N `Product`.
- `Product` 1:N `Cart`, `OrderItem`, `Review`, `Inquiry`, `Wishlist`, `InventoryLog`, `WarehouseStock`, `StockTransfer`.
- v0.2.3 기준 상품 대표 이미지는 `products.image_url`에 업로드 결과 URL을 저장한다. `media_files`는 파일 메타데이터를 보관하며 상품과의 별도 FK는 아직 두지 않는다.
- `Order` 1:N `OrderItem`; `Order` 1:1 `Payment`, `Shipment`; `Order` 1:N `ReturnRequest`, `StockReservation`.
- `Warehouse` 1:N `WarehouseStock`; `WarehouseStock` 1:N `StockReservation`.
- `StockTransfer`는 출발/도착 `Warehouse`와 `Product`를 참조한다.

## 현재 스키마 주의점

- v0.2.5 이전 개발 DB는 Hibernate `ddl-auto=update`로 만들어졌을 수 있다. 운영 반영 전에는 `V1__initial_schema.sql`과 실제 DB 스키마 차이를 별도로 점검해야 한다.
- `Product.options`, `Cart.selectedOptions`, `OrderItem.selectedOptions`는 JSON 문자열 또는 converter 기반 저장이다.
- v0.3.1 기준 `Product.price`는 판매가, `originalPrice`는 정상가, `discountPrice`는 할인 금액, `purchasePrice`는 관리자 전용 매입가로 사용한다. 마진율은 저장하지 않고 관리자 응답에서 `(판매가 - 매입가) / 판매가 * 100`으로 계산한다.
- v0.3.1 기준 `Product.searchKeywords`, `Product.tags`, `Product.seoKeywords`는 TEXT 구분자 문자열로 저장한다. AI 태그 추천과 고급 검색이 필요해지면 별도 테이블로 분리할 수 있다.
- `Product.stockQuantity`와 `WarehouseStock.quantity/reservedQuantity`가 함께 존재한다. 창고 기능에서는 창고별 재고와 예약이 source of truth가 되며, 상품 총 재고는 보조/요약 값으로 함께 갱신된다.
- 실제 PG 벤더별 거래 원장/웹훅 이벤트 테이블은 아직 없다. v0.2.2에서는 `payments.idempotency_key`, `provider`만 추가했다.
- `audit_logs`는 리뷰 숨김/해제/삭제 이력부터 기록한다. 전체 관리자 기능 감사 로그는 후속 확장 범위다.
- `media_files` 운영 DDL과 인덱스는 `V1__initial_schema.sql`에 포함했다.
