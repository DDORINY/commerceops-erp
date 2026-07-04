# API 명세

기준 버전: `v0.1.7`
기준 코드: `backend/src/main/java/com/commerceops/erp`

이 문서는 실제 Spring MVC Controller 기준으로 정리한다. 공통 응답은 `ApiResponse<T>` 래핑 구조이며, 페이지 응답은 `PageResponse<T>`를 사용한다.

## 공통

- Base URL: `http://localhost:8080/api`
- 인증: JWT Bearer Token
- 공통 응답: `{ success, statusCode, message, data }`
- 페이지 응답: `{ content, page, size, totalElements, totalPages }`

## 권한 기준

| 범위 | 권한 |
| --- | --- |
| `GET /api/health` | 공개 |
| `POST /api/auth/signup`, `POST /api/auth/login` | 공개 |
| `GET /api/categories/**`, `GET /api/products/**` | 공개 |
| `/api/admin/users/**` | `ADMIN`, `SUPER_ADMIN` |
| `/api/admin/orders/**` | `ADMIN`, `SUPER_ADMIN` |
| `/api/admin/dashboard/**` | `ADMIN`, `SUPER_ADMIN` |
| `/api/admin/inventory/**` | `ADMIN`, `SUPER_ADMIN` |
| `/api/admin/shipments/**` | `ADMIN`, `SUPER_ADMIN` |
| `/api/admin/returns/**` | `ADMIN`, `SUPER_ADMIN` |
| `/api/admin/inquiries/**` | `ADMIN`, `SUPER_ADMIN` |
| `/api/admin/reviews/**` | `ADMIN`, `SUPER_ADMIN` |
| `/api/admin/warehouses/**`, `/api/admin/warehouse-stocks/**`, `/api/admin/stock-transfers/**` | `ADMIN`, `SUPER_ADMIN` |
| `/api/admin/products/**`, `/api/admin/coupons/**`, `/api/admin/accounting/**` | `ADMIN`, `SUPER_ADMIN` |
| 기타 인증 API | 로그인 사용자 |

## 실제 엔드포인트

| Domain | Method | Path | Request/Query | Response | 권한 |
| --- | --- | --- | --- | --- | --- |
| Health | GET | `/api/health` | - | health payload | 공개 |
| Auth | POST | `/api/auth/signup` | `SignupRequest` | `SignupResponse` | 공개 |
| Auth | POST | `/api/auth/login` | `LoginRequest` | `LoginResponse` | 공개 |
| Auth | GET | `/api/auth/me` | - | `MeResponse` | 인증 |
| Category | GET | `/api/categories` | - | `List<CategoryResponse>` | 공개 |
| Category | POST | `/api/admin/categories` | `CategoryCreateRequest` | `CategoryResponse` | 관리자 |
| Product | GET | `/api/products` | `categoryId`, `keyword`, `sort`, `minPrice`, `maxPrice`, `inStock`, `page`, `size` | `PageResponse<ProductListResponse>` | 공개 |
| Product | GET | `/api/products/{productId}` | - | `ProductResponse` | 공개 |
| Product | GET | `/api/admin/products` | `status`, `keyword`, `page`, `size` | `PageResponse<ProductListResponse>` | 관리자 |
| Product | GET | `/api/admin/products/{productId}` | - | `ProductResponse` | 관리자 |
| Product | POST | `/api/admin/products` | `ProductCreateRequest` | `ProductResponse` | 관리자 |
| Product | PATCH | `/api/admin/products/{productId}` | `ProductUpdateRequest` | `ProductResponse` | 관리자 |
| Product | DELETE | `/api/admin/products/{productId}` | - | `null` | 관리자 |
| Cart | GET | `/api/cart` | - | `CartResponse` | 인증 |
| Cart | POST | `/api/cart` | `CartAddRequest` | `CartAddResponse` | 인증 |
| Cart | PATCH | `/api/cart/{cartId}` | `CartUpdateRequest` | `CartUpdateResponse` | 인증 |
| Cart | DELETE | `/api/cart/{cartId}` | - | `null` | 인증 |
| Order | POST | `/api/orders` | `OrderCreateRequest` | `OrderCreateResponse` | 인증 |
| Order | GET | `/api/orders` | - | `List<OrderResponse>` | 인증 |
| Order | GET | `/api/orders/{orderId}` | - | `OrderDetailResponse` | 인증 |
| Order | PATCH | `/api/orders/{orderId}/cancel` | - | `OrderStatusUpdateResponse` | 인증 |
| Order Admin | GET | `/api/admin/orders` | `status`, `keyword`, `page`, `size` | `PageResponse<AdminOrderResponse>` | 관리자 |
| Order Admin | PATCH | `/api/admin/orders/{orderId}/status` | `OrderStatusUpdateRequest` | `OrderStatusUpdateResponse` | 관리자 |
| Payment | POST | `/api/payments/mock/complete` | `MockPaymentCompleteRequest` | `PaymentResponse` | 인증 |
| Shipment | GET | `/api/orders/{orderId}/shipment` | - | `ShipmentResponse` | 인증 |
| Shipment Admin | GET | `/api/admin/shipments` | `status`, `keyword`, `page`, `size` | `PageResponse<ShipmentResponse>` | 관리자 |
| Shipment Admin | GET | `/api/admin/shipments/{id}` | - | `ShipmentResponse` | 관리자 |
| Shipment Admin | PATCH | `/api/admin/shipments/{id}/tracking` | `TrackingUpdateRequest` | `ShipmentResponse` | 관리자 |
| Shipment Admin | PATCH | `/api/admin/shipments/{id}/deliver` | - | `ShipmentResponse` | 관리자 |
| Return | POST | `/api/orders/{orderId}/returns` | `ReturnCreateRequest` | `ReturnResponse` | 인증 |
| Return | GET | `/api/returns` | - | `List<ReturnResponse>` | 인증 |
| Return Admin | GET | `/api/admin/returns` | `status`, `keyword`, `page`, `size` | `PageResponse<ReturnResponse>` | 관리자 |
| Return Admin | PATCH | `/api/admin/returns/{id}/approve` | `ReturnAdminActionRequest` | `ReturnResponse` | 관리자 |
| Return Admin | PATCH | `/api/admin/returns/{id}/reject` | `ReturnAdminActionRequest` | `ReturnResponse` | 관리자 |
| Inquiry | POST | `/api/products/{productId}/inquiries` | `InquiryCreateRequest` | `InquiryResponse` | 인증 |
| Inquiry | POST | `/api/inquiries` | `InquiryCreateRequest` | `InquiryResponse` | 인증 |
| Inquiry | GET | `/api/my/inquiries` | - | `List<InquiryResponse>` | 인증 |
| Inquiry | GET | `/api/products/{productId}/inquiries` | - | `List<InquiryResponse>` | 공개 |
| Inquiry Admin | GET | `/api/admin/inquiries` | `status`, `keyword`, `page`, `size` | `PageResponse<InquiryResponse>` | 관리자 |
| Inquiry Admin | PATCH | `/api/admin/inquiries/{id}/answer` | `InquiryAnswerRequest` | `InquiryResponse` | 관리자 |
| Inquiry Admin | PATCH | `/api/admin/inquiries/{id}/close` | - | `InquiryResponse` | 관리자 |
| Review | POST | `/api/orders/{orderId}/items/{orderItemId}/reviews` | `ReviewCreateRequest` | `ReviewResponse` | 인증 |
| Review | GET | `/api/products/{productId}/reviews` | `page`, `size` | `PageResponse<ReviewResponse>` | 공개 |
| Review | GET | `/api/my/reviews` | - | `List<ReviewResponse>` | 인증 |
| Review | DELETE | `/api/reviews/{reviewId}` | - | `null` | 인증 |
| Review Admin | GET | `/api/admin/reviews` | `rating`, `keyword`, `page`, `size` | `PageResponse<ReviewResponse>` | 관리자 |
| Review Admin | DELETE | `/api/admin/reviews/{reviewId}` | - | `null` | 관리자 |
| Wishlist | POST | `/api/wishlist/{productId}` | - | `WishlistToggleResponse` | 인증 |
| Wishlist | GET | `/api/wishlist` | - | `List<WishlistItemResponse>` | 인증 |
| Wishlist | GET | `/api/wishlist/{productId}/status` | - | `WishlistToggleResponse` | 인증 |
| Coupon | POST | `/api/coupons/validate` | `code`, `orderAmount` | `CouponValidateResponse` | 인증 |
| Coupon Admin | GET | `/api/admin/coupons` | - | `List<CouponResponse>` | 관리자 |
| Coupon Admin | POST | `/api/admin/coupons` | `CouponCreateRequest` | `CouponResponse` | 관리자 |
| Coupon Admin | DELETE | `/api/admin/coupons/{couponId}` | - | `null` | 관리자 |
| User Admin | GET | `/api/admin/users` | `keyword`, `page`, `size` | `PageResponse<UserSummaryResponse>` | 관리자 |
| User Admin | PATCH | `/api/admin/users/{userId}/role` | `role` query | `UserSummaryResponse` | 관리자 |
| Dashboard Admin | GET | `/api/admin/dashboard/summary` | - | `DashboardSummaryResponse` | 관리자 |
| Dashboard Admin | GET | `/api/admin/dashboard/sales` | `period`, `startDate`, `endDate` | `List<SalesResponse>` | 관리자 |
| Dashboard Admin | GET | `/api/admin/dashboard/low-stock` | `limit` | `List<LowStockProductResponse>` | 관리자 |
| Dashboard Admin | GET | `/api/admin/dashboard/top-products` | `limit` | `List<TopProductResponse>` | 관리자 |
| Inventory Admin | GET | `/api/admin/inventory` | `keyword`, `status`, `lowStockOnly`, `page`, `size` | `PageResponse<InventoryResponse>` | 관리자 |
| Inventory Admin | POST | `/api/admin/inventory/inbound` | `InventoryInboundRequest` | `InventoryStockChangeResponse` | 관리자 |
| Inventory Admin | POST | `/api/admin/inventory/adjust` | `InventoryAdjustRequest` | `InventoryStockChangeResponse` | 관리자 |
| Inventory Admin | GET | `/api/admin/inventory/logs` | `productId`, `page`, `size` | `PageResponse<InventoryLogResponse>` | 관리자 |
| Accounting Admin | GET | `/api/admin/accounting/summary` | - | `AccountingSummaryResponse` | 관리자 |
| Accounting Admin | GET | `/api/admin/accounting/entries` | `type`, `page`, `size` | `PageResponse<AccountingEntryResponse>` | 관리자 |
| Warehouse Admin | GET | `/api/admin/warehouses` | - | `List<WarehouseResponse>` | 관리자 |
| Warehouse Admin | POST | `/api/admin/warehouses` | `WarehouseCreateRequest` | `WarehouseResponse` | 관리자 |
| Warehouse Admin | GET | `/api/admin/warehouse-stocks` | `warehouseId`, `keyword`, `page`, `size` | `PageResponse<WarehouseStockResponse>` | 관리자 |
| Warehouse Admin | POST | `/api/admin/warehouse-stocks/allocate` | `WarehouseStockAllocateRequest` | `WarehouseStockResponse` | 관리자 |
| Warehouse Admin | GET | `/api/admin/stock-transfers` | `status`, `page`, `size` | `PageResponse<StockTransferResponse>` | 관리자 |
| Warehouse Admin | POST | `/api/admin/stock-transfers` | `StockTransferCreateRequest` | `StockTransferResponse` | 관리자 |
| Warehouse Admin | PATCH | `/api/admin/stock-transfers/{id}/complete` | - | `StockTransferResponse` | 관리자 |

## 주요 DTO 메모

- `ProductCreateRequest`, `ProductUpdateRequest`: `categoryId`, `name`, `description`, `price`, `stockQuantity`, `imageUrl`, `status`, `options`.
- `OrderCreateRequest`: `receiverName`, `receiverPhone`, `address`, `detailAddress`, `paymentMethod`, `cartItemIds`, `couponCode`.
- `UserSummaryResponse`: `id`, `name`, `email`, `phone`, `role`, `status`, `createdAt`, `orderCount`, `totalOrderAmount`.
- `DashboardSummaryResponse`: 전체/오늘 주문, 전체/오늘 매출, 고객/상품/품절/재고부족 수, 상태별 주문 수.
- `WarehouseStockResponse`: 창고, 상품, 수량, 예약 수량, 가용 수량, 상품 총 재고.

## enum

| Enum | 값 |
| --- | --- |
| `UserRole` | `USER`, `MANAGER`, `ADMIN`, `SUPER_ADMIN` |
| `UserStatus` | `ACTIVE`, `INACTIVE`, `BLOCKED` |
| `ProductStatus` | `ON_SALE`, `SOLD_OUT`, `HIDDEN`, `DELETED` |
| `OrderStatus` | `PENDING`, `PAID`, `PREPARING`, `SHIPPING`, `COMPLETED`, `CANCELLED`, `REFUNDED` |
| `PaymentStatus` | `READY`, `PAID`, `FAILED`, `CANCELLED`, `REFUNDED` |
| `PaymentMethod` | `MOCK_CARD`, `MOCK_BANK`, `MOCK_SIMPLE_PAY` |
| `ShipmentStatus` | `READY`, `SHIPPING`, `DELIVERED` |
| `ReturnReason` | `CHANGE_OF_MIND`, `DEFECTIVE`, `WRONG_DELIVERY` |
| `ReturnStatus` | `REQUESTED`, `APPROVED`, `REJECTED` |
| `InquiryType` | `PRODUCT`, `ORDER`, `OTHER` |
| `InquiryStatus` | `WAITING`, `ANSWERED`, `CLOSED` |
| `AccountingEntryType` | `SALE`, `REFUND`, `INBOUND` |
| `InventoryLogType` | `INBOUND`, `OUTBOUND`, `ORDER`, `CANCEL`, `ADJUST`, `RETURN_RESTOCK` |
| `StockTransferStatus` | `PENDING`, `COMPLETED` |
| `StockReservationStatus` | `RESERVED`, `RELEASED`, `SHIPPED`, `RETURNED` |

## 미구현/예정으로 분리된 항목

- 실제 PG 승인, 취소, 환불 API. 현재는 `/api/payments/mock/complete`만 있다.
- 로그아웃/리프레시 토큰 API.
- 리뷰 숨김 또는 상태 변경 API. 현재는 관리자 목록 조회와 삭제만 있다.
- 상품 이미지 업로드 API.
- 고급 BI, 복식부기, 정산 리포트 API.
- 피킹, 패킹, 출고 자동화 API.
