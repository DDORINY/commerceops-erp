# DB 스키마 문서

기준 버전: `v0.1.7`
기준 코드: JPA Entity (`backend/src/main/java/com/commerceops/erp/domain/**/entity`)

현재 레포에는 별도 Flyway/Liquibase DDL 마이그레이션이 없다. 이 문서는 실제 엔티티 기준의 논리 스키마이며, 물리 DDL, 인덱스, 외래키 이름은 운영 DB 도입 시 별도로 확정해야 한다.

## 테이블 요약

| 테이블 | 엔티티 | 주요 컬럼/관계 |
| --- | --- | --- |
| `users` | `User` | `email`, `password`, `name`, `phone`, `role`, `status`, timestamps |
| `categories` | `Category` | `name`, timestamps |
| `products` | `Product` | `category_id`, `name`, `description`, `price`, `stock_quantity`, `image_url`, `status`, `options`, timestamps |
| `carts` | `Cart` | `user_id`, `product_id`, `quantity`, `selected_options`, timestamps |
| `orders` | `Order` | `user_id`, `order_number`, `total_price`, `discount_amount`, `coupon_code`, `status`, receiver fields, `payment_status`, timestamps |
| `order_items` | `OrderItem` | `order_id`, `product_id`, snapshot `product_name`, `price`, `quantity`, `selected_options`, `created_at` |
| `payments` | `Payment` | `order_id`, `payment_method`, `payment_status`, `paid_amount`, `transaction_id`, timestamps |
| `shipments` | `Shipment` | `order_id`, `status`, `tracking_number`, `carrier`, `shipped_at`, `delivered_at`, timestamps |
| `return_requests` | `ReturnRequest` | `order_id`, `user_id`, `reason`, `reason_detail`, `status`, `admin_note`, timestamps |
| `reviews` | `Review` | `product_id`, `user_id`, `order_item_id`, `rating`, `content`, `created_at` |
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
| `OrderStatus` | `PENDING`, `PAID`, `PREPARING`, `SHIPPING`, `COMPLETED`, `CANCELLED`, `REFUNDED` |
| `PaymentMethod` | `MOCK_CARD`, `MOCK_BANK`, `MOCK_SIMPLE_PAY` |
| `PaymentStatus` | `READY`, `PAID`, `FAILED`, `CANCELLED`, `REFUNDED` |
| `ShipmentStatus` | `READY`, `SHIPPING`, `DELIVERED` |
| `ReturnReason` | `CHANGE_OF_MIND`, `DEFECTIVE`, `WRONG_DELIVERY` |
| `ReturnStatus` | `REQUESTED`, `APPROVED`, `REJECTED` |
| `InquiryType` | `PRODUCT`, `ORDER`, `OTHER` |
| `InquiryStatus` | `WAITING`, `ANSWERED`, `CLOSED` |
| `AccountingEntryType` | `SALE`, `REFUND`, `INBOUND` |
| `InventoryLogType` | `INBOUND`, `OUTBOUND`, `ORDER`, `CANCEL`, `ADJUST`, `RETURN_RESTOCK` |
| `DiscountType` | `FIXED`, `PERCENT` |
| `StockReservationStatus` | `RESERVED`, `RELEASED`, `SHIPPED`, `RETURNED` |
| `StockTransferStatus` | `PENDING`, `COMPLETED` |

## 관계 요약

- `User` 1:N `Cart`, `Order`, `Inquiry`, `Review`, `Wishlist`, `ReturnRequest`.
- `Category` 1:N `Product`.
- `Product` 1:N `Cart`, `OrderItem`, `Review`, `Inquiry`, `Wishlist`, `InventoryLog`, `WarehouseStock`, `StockTransfer`.
- `Order` 1:N `OrderItem`; `Order` 1:1 `Payment`, `Shipment`; `Order` 1:N `ReturnRequest`, `StockReservation`.
- `Warehouse` 1:N `WarehouseStock`; `WarehouseStock` 1:N `StockReservation`.
- `StockTransfer`는 출발/도착 `Warehouse`와 `Product`를 참조한다.

## 현재 스키마 주의점

- 실제 DDL 파일이 없으므로 컬럼 타입 길이, 인덱스명, 외래키명은 JPA/Hibernate 설정에 의존한다.
- `Product.options`, `Cart.selectedOptions`, `OrderItem.selectedOptions`는 JSON 문자열 또는 converter 기반 저장이다.
- `Product.stockQuantity`와 `WarehouseStock.quantity/reservedQuantity`가 함께 존재한다. 창고 기능에서는 창고별 재고와 예약이 source of truth가 되며, 상품 총 재고는 보조/요약 값으로 함께 갱신된다.
- 실제 PG 결제/환불 테이블 확장은 아직 없다.
- 감사 로그, 관리자 작업 이력, 파일 업로드 메타데이터 테이블은 아직 없다.
