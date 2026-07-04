# API 명세

기준 버전: `v0.3.1`
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
| `POST /api/auth/signup`, `POST /api/auth/login`, `POST /api/auth/refresh`, `POST /api/auth/logout` | 공개 |
| `GET /api/categories/**`, `GET /api/products/**` | 공개 |
| `/api/admin/users/**` | `ADMIN`, `SUPER_ADMIN` |
| `/api/admin/orders/**` | `ADMIN`, `SUPER_ADMIN` |
| `/api/admin/dashboard/**` | `ADMIN`, `SUPER_ADMIN` |
| `/api/admin/inventory/**` | `ADMIN`, `SUPER_ADMIN` |
| `/api/admin/shipments/**` | `ADMIN`, `SUPER_ADMIN` |
| `/api/admin/returns/**` | `ADMIN`, `SUPER_ADMIN` |
| `/api/admin/inquiries/**` | `ADMIN`, `SUPER_ADMIN` |
| `/api/admin/reviews/**` | `ADMIN`, `SUPER_ADMIN` |
| `/api/admin/audit-logs/**` | `ADMIN`, `SUPER_ADMIN` |
| `GET /api/admin/dashboard/**`, `GET /api/admin/orders/**`, `GET /api/admin/inventory/**`, `GET /api/admin/shipments/**`, `GET /api/admin/returns/**`, `GET /api/admin/inquiries/**`, `GET /api/admin/warehouses/**`, `GET /api/admin/warehouse-stocks/**`, `GET /api/admin/stock-transfers/**`, `GET /api/admin/products/**`, `GET /api/admin/notifications/**`, `GET /api/admin/ops-analytics/**` | `MANAGER`, `ADMIN`, `SUPER_ADMIN` |
| 변경성 `/api/admin/warehouses/**`, `/api/admin/warehouse-stocks/**`, `/api/admin/stock-transfers/**` | `ADMIN`, `SUPER_ADMIN` |
| 변경성 `/api/admin/products/**`, `/api/admin/coupons/**`, `/api/admin/accounting/**`, `/api/admin/media/**` | `ADMIN`, `SUPER_ADMIN` |
| `/uploads/**` | 공개 정적 파일 |
| 기타 인증 API | 로그인 사용자 |

## 실제 엔드포인트

| Domain | Method | Path | Request/Query | Response | 권한 |
| --- | --- | --- | --- | --- | --- |
| Health | GET | `/api/health` | - | health payload | 공개 |
| Auth | POST | `/api/auth/signup` | `SignupRequest` | `SignupResponse` | 공개 |
| Auth | POST | `/api/auth/login` | `LoginRequest` | `LoginResponse` | 공개 |
| Auth | POST | `/api/auth/refresh` | `RefreshTokenRequest` | `RefreshTokenResponse` | 공개 |
| Auth | POST | `/api/auth/logout` | - | `null` | 공개 |
| Auth | GET | `/api/auth/me` | - | `MeResponse` | 인증 |
| Category | GET | `/api/categories` | - | `List<CategoryResponse>` | 공개 |
| Category | POST | `/api/admin/categories` | `CategoryCreateRequest` | `CategoryResponse` | 관리자 |
| Product | GET | `/api/products` | `categoryId`, `keyword`, `sort`, `minPrice`, `maxPrice`, `inStock`, `page`, `size` | `PageResponse<ProductListResponse>` | 공개 |
| Product | GET | `/api/products/{productId}` | - | `ProductResponse` | 공개 |
| Product | GET | `/api/admin/products` | `status`, `keyword`, `page`, `size` | `PageResponse<AdminProductListResponse>` | 관리자 |
| Product | GET | `/api/admin/products/{productId}` | - | `AdminProductResponse` | 관리자 |
| Product | POST | `/api/admin/products` | `ProductCreateRequest` | `AdminProductResponse` | 관리자 |
| Product | PATCH | `/api/admin/products/{productId}` | `ProductUpdateRequest` | `AdminProductResponse` | 관리자 |
| Product | DELETE | `/api/admin/products/{productId}` | - | `null` | 관리자 |
| Media Admin | POST | `/api/admin/media/product-images` | multipart `file` | `MediaFileResponse` | 관리자 |
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
| Payment | POST | `/api/payments/approve` | `PaymentApproveRequest` | `PaymentResponse` | 인증 |
| Payment | POST | `/api/payments/{paymentId}/cancel` | `PaymentCancelRequest` | `PaymentResponse` | 인증 |
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
| Review Admin | PATCH | `/api/admin/reviews/{reviewId}/hide` | - | `null` | 관리자 |
| Review Admin | PATCH | `/api/admin/reviews/{reviewId}/show` | - | `null` | 관리자 |
| Review Admin | DELETE | `/api/admin/reviews/{reviewId}` | - | `null` | 관리자 |
| Audit Admin | GET | `/api/admin/audit-logs` | `targetType`, `page`, `size` | `PageResponse<AuditLogResponse>` | 관리자 |
| Notification | GET | `/api/notifications` | `page`, `size` | `PageResponse<NotificationResponse>` | 인증 |
| Notification | GET | `/api/notifications/unread-count` | - | `UnreadNotificationCountResponse` | 인증 |
| Notification | PATCH | `/api/notifications/{notificationId}/read` | - | `NotificationResponse` | 인증 |
| Notification | PATCH | `/api/notifications/read-all` | - | `UnreadNotificationCountResponse` | 인증 |
| Notification Admin | GET | `/api/admin/notifications` | `page`, `size` | `PageResponse<NotificationResponse>` | 관리자/매니저 |
| Ops Analytics Admin | GET | `/api/admin/ops-analytics/overview` | - | `OpsAnalyticsOverviewResponse` | 관리자/매니저 |
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

- `ProductCreateRequest`, `ProductUpdateRequest`: `categoryId`, `name`, `description`, `price`, `productCode`, `brand`, `manufacturer`, `modelName`, `origin`, `originalPrice`, `discountPrice`, `purchasePrice`, `searchKeywords`, `tags`, `saleStartAt`, `saleEndAt`, `deliveryInfo`, `seoTitle`, `seoDescription`, `seoKeywords`, `stockQuantity`, `imageUrl`, `status`, `options`.
- `ProductResponse`, `ProductListResponse`: 사용자 공개 상품 필드. `purchasePrice`, `marginRate`는 포함하지 않는다.
- `AdminProductResponse`, `AdminProductListResponse`: 관리자 상품 필드. `purchasePrice`, 계산 필드 `marginRate`를 포함한다.
- `MediaFileResponse`: `id`, `originalFilename`, `storedFilename`, `url`, `contentType`, `size`, `mediaType`, `createdAt`.
- `ReviewResponse`: `reviewId`, `productId`, `productName`, `userName`, `orderItemId`, `rating`, `content`, `status`, `createdAt`.
- `AuditLogResponse`: `id`, `actorId`, `actorEmail`, `actorName`, `actionType`, `targetType`, `targetId`, `beforeStatus`, `afterStatus`, `summary`, `createdAt`.
- `NotificationResponse`: `id`, `userId`, `type`, `title`, `message`, `targetType`, `targetId`, `read`, `readAt`, `createdAt`.
- `UnreadNotificationCountResponse`: `unreadCount`.
- `OpsAnalyticsOverviewResponse`: `accounting`, `sales`, `warehouse`, `notes`.
- `OpsAnalyticsOverviewResponse.accounting`: `totalSales`, `totalRefunds`, `totalInboundAmount`, `netSales`, `entryCount`.
- `OpsAnalyticsOverviewResponse.sales`: `totalOrders`, `paidOrders`, `cancelledOrders`, `refundedOrders`, `totalRevenue`, `averagePaidOrderAmount`, `orderStatusCounts`.
- `OpsAnalyticsOverviewResponse.warehouse`: `totalWarehouses`, `activeWarehouses`, `inactiveWarehouses`, `totalStockQuantity`, `totalReservedQuantity`, `totalAvailableQuantity`, `reservationStatusCounts`.
- `LoginResponse`: `accessToken`, `refreshToken`, `tokenType`, `user`.
- `RefreshTokenRequest`: `refreshToken`.
- `RefreshTokenResponse`: `accessToken`, `refreshToken`, `tokenType`.
- `OrderCreateRequest`: `receiverName`, `receiverPhone`, `address`, `detailAddress`, `paymentMethod`, `cartItemIds`, `couponCode`.
- `PaymentApproveRequest`: `orderId`, `paymentMethod`, `providerTransactionId`, `idempotencyKey`.
- `PaymentCancelRequest`: `reason`.
- `PaymentResponse`: `paymentId`, `orderId`, `paymentMethod`, `paymentStatus`, `paidAmount`, `transactionId`, `idempotencyKey`.
- `UserSummaryResponse`: `id`, `name`, `email`, `phone`, `role`, `status`, `createdAt`, `orderCount`, `totalOrderAmount`.
- `DashboardSummaryResponse`: 전체/오늘 주문, 전체/오늘 매출, 고객/상품/품절/재고부족 수, 상태별 주문 수.
- `WarehouseStockResponse`: 창고, 상품, 수량, 예약 수량, 가용 수량, 상품 총 재고.

## enum

| Enum | 값 |
| --- | --- |
| `UserRole` | `USER`, `MANAGER`, `ADMIN`, `SUPER_ADMIN` |
| `UserStatus` | `ACTIVE`, `INACTIVE`, `BLOCKED` |
| `ProductStatus` | `ON_SALE`, `SOLD_OUT`, `HIDDEN`, `DELETED` |
| `ReviewStatus` | `VISIBLE`, `HIDDEN`, `DELETED` |
| `AuditActionType` | `REVIEW_HIDE`, `REVIEW_SHOW`, `REVIEW_DELETE` |
| `NotificationType` | `ORDER_STATUS`, `INQUIRY_ANSWERED`, `RETURN_PROCESSED`, `SYSTEM` |
| `OrderStatus` | `PENDING`, `PAID`, `PREPARING`, `SHIPPING`, `COMPLETED`, `CANCELLED`, `REFUNDED` |
| `PaymentStatus` | `READY`, `PAID`, `FAILED`, `CANCELLED`, `REFUNDED` |
| `PaymentMethod` | `MOCK_CARD`, `MOCK_BANK`, `MOCK_SIMPLE_PAY` |
| `ShipmentStatus` | `READY`, `IN_TRANSIT`, `DELIVERED`, `CANCELLED` |
| `ReturnReason` | `CHANGE_OF_MIND`, `DEFECTIVE`, `WRONG_DELIVERY` |
| `ReturnStatus` | `REQUESTED`, `APPROVED`, `REJECTED` |
| `InquiryType` | `PRODUCT`, `ORDER`, `OTHER` |
| `InquiryStatus` | `WAITING`, `ANSWERED`, `CLOSED` |
| `AccountingEntryType` | `SALE`, `REFUND`, `INBOUND` |
| `InventoryLogType` | `INBOUND`, `OUTBOUND`, `ORDER`, `CANCEL`, `ADJUST`, `RETURN_RESTOCK` |
| `StockTransferStatus` | `PENDING`, `COMPLETED` |
| `StockReservationStatus` | `RESERVED`, `RELEASED`, `SHIPPED`, `RETURNED` |

## 미구현/예정으로 분리된 항목

- 실제 PG 벤더 키/웹훅/리다이렉트 연동. 현재 결제 승인 API는 `MOCK_PROVIDER` 기반으로 동작한다.
- 완전한 감사 로그 시스템. 현재는 리뷰 숨김/해제/삭제 작업 이력의 최소 기록만 제공한다.
- 고급 BI, 복식부기, 정산 리포트 API는 미구현이다. v0.2.8 기준으로는 `/api/admin/ops-analytics/overview`에서 기초 운영 지표만 제공한다.
- 피킹, 패킹, 출고 자동화 API.
