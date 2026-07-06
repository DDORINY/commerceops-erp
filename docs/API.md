# API 명세

기준 버전: `v0.5.3`
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
| `GET /api/banners/**`, `GET /api/categories/**`, `GET /api/products/**`, `GET /api/settings/**` | 공개 |
| `/api/admin/users/**` | `ADMIN`, `SUPER_ADMIN` |
| `/api/admin/orders/**` | `ADMIN`, `SUPER_ADMIN` |
| `/api/admin/dashboard/**` | `ADMIN`, `SUPER_ADMIN` |
| `/api/admin/inventory/**` | `ADMIN`, `SUPER_ADMIN` |
| `/api/admin/shipments/**` | `ADMIN`, `SUPER_ADMIN` |
| `/api/admin/returns/**` | `ADMIN`, `SUPER_ADMIN` |
| `/api/admin/inquiries/**` | `ADMIN`, `SUPER_ADMIN` |
| `/api/admin/reviews/**` | `ADMIN`, `SUPER_ADMIN` |
| `/api/admin/audit-logs/**` | `ADMIN`, `SUPER_ADMIN` |
| `GET /api/admin/hr/**` | `ADMIN`, `SUPER_ADMIN` |
| `GET /api/admin/staff/**` | `ADMIN`, `SUPER_ADMIN` |
| 변경성 `/api/admin/staff/**` | `SUPER_ADMIN` |
| `GET /api/admin/permission-groups/**`, `GET /api/admin/users/{userId}/permission-groups` | `ADMIN`, `SUPER_ADMIN` |
| 변경성 `/api/admin/permission-groups/**`, `PUT /api/admin/users/{userId}/permission-groups` | `SUPER_ADMIN` |
| `GET /api/admin/permissions`, `GET /api/admin/permission-groups/{groupId}/permissions`, `GET /api/admin/users/{userId}/permissions` | `ADMIN`, `SUPER_ADMIN` |
| `GET /api/admin/users/me/permissions`, `GET /api/admin/menu-permissions` | `MANAGER`, `ADMIN`, `SUPER_ADMIN` |
| `PUT /api/admin/permission-groups/{groupId}/permissions`, `PUT /api/admin/menu-permissions` | `SUPER_ADMIN` |
| `GET /api/admin/dashboard/**`, `GET /api/admin/orders/**`, `GET /api/admin/inventory/**`, `GET /api/admin/shipments/**`, `GET /api/admin/returns/**`, `GET /api/admin/inquiries/**`, `GET /api/admin/warehouses/**`, `GET /api/admin/warehouse-stocks/**`, `GET /api/admin/stock-transfers/**`, `GET /api/admin/products/**`, `GET /api/admin/categories/**`, `GET /api/admin/notifications/**`, `GET /api/admin/ops-analytics/**` | `MANAGER`, `ADMIN`, `SUPER_ADMIN` |
| 변경성 `/api/admin/warehouses/**`, `/api/admin/warehouse-stocks/**`, `/api/admin/stock-transfers/**` | `ADMIN`, `SUPER_ADMIN` |
| 변경성 `/api/admin/products/**`, `/api/admin/categories/**`, `/api/admin/banners/**`, `/api/admin/coupons/**`, `/api/admin/accounting/**`, `/api/admin/media/**` | `ADMIN`, `SUPER_ADMIN` |
| `/uploads/**` | 공개 정적 파일 |
| 기타 인증 API | 로그인 사용자 |

## v0.4.6 관리자 Permission 정책

`/api/admin/**`는 기존 role 기반 1차 접근 정책을 유지한다. `MANAGER`, `ADMIN`, `SUPER_ADMIN`만 관리자 API에 접근할 수 있고, 주요 실행 권한은 Controller method 시작부에서 `PermissionChecker.require(...)`로 세부 permission code를 검증한다. `SUPER_ADMIN`은 모든 활성 permission을 보유한 것으로 간주한다.

권한이 없으면 403과 함께 “해당 작업을 수행할 권한이 없습니다. 관리자에게 권한을 요청하세요.” 메시지를 반환한다.

| 영역 | 조회 권한 | 변경/실행 권한 |
| --- | --- | --- |
| 대시보드/운영 분석 | `DASHBOARD_READ` | - |
| 상품 | `PRODUCT_READ` | `PRODUCT_WRITE`, `PRODUCT_STATUS_CHANGE`, `PRODUCT_BULK_UPDATE` |
| 카테고리 | `CATEGORY_MANAGE` | `CATEGORY_MANAGE` |
| 배너 | `BANNER_MANAGE` | `BANNER_MANAGE` |
| 주문/배송/반품 | `ORDER_READ` | `ORDER_STATUS_CHANGE` |
| 결제/환불 | - | `PAYMENT_REFUND` |
| 재고 | `INVENTORY_READ` | `INVENTORY_WRITE` |
| 창고/재고 이동 | `INVENTORY_READ` | `WAREHOUSE_MANAGE`, `INVENTORY_WRITE` |
| 회계/매출 | `ACCOUNTING_READ` | `ACCOUNTING_CLOSE` 후보 |
| 쿠폰 | `COUPON_MANAGE` | `COUPON_MANAGE` |
| 리뷰 | `REVIEW_MODERATE` | `REVIEW_MODERATE` |
| 문의 | `INQUIRY_REPLY` | `INQUIRY_REPLY` |
| 직원/HR | `STAFF_MANAGE` | `STAFF_MANAGE` |
| 권한 그룹/권한 매트릭스 | `ROLE_MANAGE` | `ROLE_MANAGE` |
| 감사 로그 | `AUDIT_LOG_READ` | - |
| 사업자/약관 설정 | `SETTINGS_MANAGE` | `SETTINGS_MANAGE` |

## 실제 엔드포인트

| Domain | Method | Path | Request/Query | Response | 권한 |
| --- | --- | --- | --- | --- | --- |
| Health | GET | `/api/health` | - | health payload | 공개 |
| Auth | POST | `/api/auth/signup` | `SignupRequest` | `SignupResponse` | 공개 |
| Auth | POST | `/api/auth/login` | `LoginRequest` | `LoginResponse` | 공개 |
| Auth | POST | `/api/auth/refresh` | `RefreshTokenRequest` | `RefreshTokenResponse` | 공개 |
| Auth | POST | `/api/auth/logout` | - | `null` | 공개 |
| Auth | GET | `/api/auth/me` | - | `MeResponse` | 인증 |
| HR Admin | GET | `/api/admin/hr/departments` | - | `List<DepartmentResponse>` | ADMIN/SUPER_ADMIN |
| HR Admin | GET | `/api/admin/hr/positions` | - | `List<PositionResponse>` | ADMIN/SUPER_ADMIN |
| HR Admin | GET | `/api/admin/hr/staff-profiles` | - | `List<StaffProfileResponse>` | ADMIN/SUPER_ADMIN |
| Staff Admin | GET | `/api/admin/staff` | `keyword`, `departmentId`, `positionId`, `employmentStatus`, `active`, `role`, `page`, `size` | `PageResponse<StaffProfileResponse>` | ADMIN/SUPER_ADMIN |
| Staff Admin | GET | `/api/admin/staff/{staffId}` | - | `StaffProfileResponse` | ADMIN/SUPER_ADMIN |
| Staff Admin | POST | `/api/admin/staff` | `StaffCreateRequest` | `StaffProfileResponse` | SUPER_ADMIN |
| Staff Admin | PATCH | `/api/admin/staff/{staffId}` | `StaffUpdateRequest` | `StaffProfileResponse` | SUPER_ADMIN |
| Staff Admin | PATCH | `/api/admin/staff/{staffId}/status` | `StaffStatusUpdateRequest` | `StaffProfileResponse` | SUPER_ADMIN |
| Staff Admin | PATCH | `/api/admin/staff/{staffId}/active` | `StaffActiveUpdateRequest` | `StaffProfileResponse` | SUPER_ADMIN |
| Permission Admin | GET | `/api/admin/permission-groups` | - | `List<PermissionGroupResponse>` | ADMIN/SUPER_ADMIN |
| Permission Admin | GET | `/api/admin/permission-groups/{groupId}` | - | `PermissionGroupResponse` | ADMIN/SUPER_ADMIN |
| Permission Admin | POST | `/api/admin/permission-groups` | `PermissionGroupCreateRequest` | `PermissionGroupResponse` | SUPER_ADMIN |
| Permission Admin | PATCH | `/api/admin/permission-groups/{groupId}` | `PermissionGroupUpdateRequest` | `PermissionGroupResponse` | SUPER_ADMIN |
| Permission Admin | PATCH | `/api/admin/permission-groups/{groupId}/active` | `PermissionGroupActiveUpdateRequest` | `PermissionGroupResponse` | SUPER_ADMIN |
| Permission Admin | GET | `/api/admin/users/{userId}/permission-groups` | - | `List<UserPermissionGroupResponse>` | ADMIN/SUPER_ADMIN |
| Permission Admin | PUT | `/api/admin/users/{userId}/permission-groups` | `UserPermissionGroupUpdateRequest` | `List<UserPermissionGroupResponse>` | SUPER_ADMIN |
| Permission Matrix Admin | GET | `/api/admin/permissions` | - | `List<PermissionResponse>` | ADMIN/SUPER_ADMIN |
| Permission Matrix Admin | GET | `/api/admin/permission-groups/{groupId}/permissions` | - | `List<PermissionResponse>` | ADMIN/SUPER_ADMIN |
| Permission Matrix Admin | PUT | `/api/admin/permission-groups/{groupId}/permissions` | `PermissionGroupPermissionUpdateRequest` | `List<PermissionResponse>` | SUPER_ADMIN |
| Permission Matrix Admin | GET | `/api/admin/users/{userId}/permissions` | - | `EffectivePermissionResponse` | ADMIN/SUPER_ADMIN |
| Permission Matrix Admin | GET | `/api/admin/users/me/permissions` | - | `EffectivePermissionResponse` | MANAGER/ADMIN/SUPER_ADMIN |
| Permission Matrix Admin | GET | `/api/admin/menu-permissions` | - | `List<AdminMenuPermissionResponse>` | MANAGER/ADMIN/SUPER_ADMIN |
| Permission Matrix Admin | PUT | `/api/admin/menu-permissions` | `AdminMenuPermissionUpdateRequest` | `List<AdminMenuPermissionResponse>` | SUPER_ADMIN |
| Category | GET | `/api/categories` | - | `List<CategoryResponse>` | 공개 |
| Category | GET | `/api/categories/navigation` | - | `List<CategoryTreeResponse>` | 공개 |
| Category Admin | GET | `/api/admin/categories/tree` | - | `List<CategoryTreeResponse>` | 관리자 |
| Category | POST | `/api/admin/categories` | `CategoryCreateRequest` | `CategoryResponse` | 관리자 |
| Category Admin | PATCH | `/api/admin/categories/{categoryId}` | `CategoryUpdateRequest` | `CategoryResponse` | 관리자 |
| Banner | GET | `/api/banners` | - | `List<MainBannerResponse>` | 공개 |
| Banner Admin | GET | `/api/admin/banners` | - | `List<MainBannerResponse>` | 관리자 |
| Banner Admin | GET | `/api/admin/banners/{bannerId}` | - | `MainBannerResponse` | 관리자 |
| Banner Admin | POST | `/api/admin/banners` | `MainBannerRequest` | `MainBannerResponse` | 관리자 |
| Banner Admin | PATCH | `/api/admin/banners/{bannerId}` | `MainBannerRequest` | `MainBannerResponse` | 관리자 |
| Banner Admin | DELETE | `/api/admin/banners/{bannerId}` | - | `null` | 관리자, active=false 비활성화 |
| Product | GET | `/api/products` | `categoryId`, `keyword`, `sort`, `minPrice`, `maxPrice`, `inStock`, `page`, `size` | `PageResponse<ProductListResponse>` | 공개 |
| Product | GET | `/api/products/{productId}` | - | `ProductResponse` | 공개 |
| Product | GET | `/api/admin/products` | `status`, `salesStatus`, `displayStatus`, `categoryId`, `stockStatus`, `lowStockOnly`, `salePeriodStatus`, `keyword`, `page`, `size` | `PageResponse<AdminProductListResponse>` | 관리자 |
| Product | GET | `/api/admin/products/{productId}` | - | `AdminProductResponse` | 관리자 |
| Product Detail Block Admin | GET | `/api/admin/products/{productId}/detail-blocks` | - | `List<ProductDetailBlockResponse>` | 관리자 |
| Product Detail Block Admin | PUT | `/api/admin/products/{productId}/detail-blocks` | `List<ProductDetailBlockRequest>` | `List<ProductDetailBlockResponse>` | 관리자 |
| Product | POST | `/api/admin/products` | `ProductCreateRequest` | `AdminProductResponse` | 관리자 |
| Product | PATCH | `/api/admin/products/{productId}` | `ProductUpdateRequest` | `AdminProductResponse` | 관리자 |
| Product | PATCH | `/api/admin/products/{productId}/status` | `ProductStatusUpdateRequest` | `AdminProductResponse` | 관리자 |
| Product | PATCH | `/api/admin/products/bulk-status` | `ProductBulkStatusUpdateRequest` | `ProductBulkStatusUpdateResponse` | 관리자 |
| Product | GET | `/api/admin/products/{productId}/status-history` | `limit` | `List<ProductStatusHistoryResponse>` | 관리자/매니저 |
| Product | GET | `/api/admin/products/{productId}/operation-notes` | `limit` | `List<ProductOperationNoteResponse>` | 관리자/매니저 |
| Product | POST | `/api/admin/products/{productId}/operation-notes` | `ProductOperationNoteRequest` | `ProductOperationNoteResponse` | 관리자 |
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
| Audit Admin | GET | `/api/admin/audit-logs` | `actorKeyword`, `actionType`, `targetType`, `targetId`, `dateFrom`, `dateTo`, `page`, `size` | `PageResponse<AuditLogResponse>` | `AUDIT_LOG_READ` |
| Audit Admin | GET | `/api/admin/audit-logs/{auditLogId}` | - | `AuditLogResponse` | `AUDIT_LOG_READ` |
| Settings Admin | GET | `/api/admin/settings/company` | - | `BusinessSettingsResponse` | `SETTINGS_MANAGE` |
| Settings Admin | PUT | `/api/admin/settings/company` | `BusinessSettingsUpdateRequest` | `BusinessSettingsResponse` | `SETTINGS_MANAGE` |
| Settings Admin | GET | `/api/admin/settings/terms` | - | `List<TermsVersionResponse>` | `SETTINGS_MANAGE` |
| Settings Admin | POST | `/api/admin/settings/terms` | `TermsVersionCreateRequest` | `TermsVersionResponse` | `SETTINGS_MANAGE` |
| Settings Admin | GET | `/api/admin/settings/terms/{type}/latest` | - | `TermsVersionResponse` | `SETTINGS_MANAGE` |
| Settings Admin | GET | `/api/admin/settings/terms/{type}/versions` | - | `List<TermsVersionResponse>` | `SETTINGS_MANAGE` |
| Settings Admin | GET | `/api/admin/settings/terms/{type}/versions/{versionId}` | - | `TermsVersionResponse` | `SETTINGS_MANAGE` |
| Settings Public | GET | `/api/settings/company/public` | - | `PublicBusinessSettingsResponse` | 공개 |
| Settings Public | GET | `/api/settings/terms/{type}/latest` | - | `PublicTermsVersionResponse` | 공개 |
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
| SKU Admin | GET | `/api/admin/skus` | `keyword`, `productId`, `active`, `hasBarcode`, `page`, `size` | `PageResponse<SkuListResponse>` | `INVENTORY_READ` |
| SKU Admin | GET | `/api/admin/skus/{skuId}` | - | `SkuResponse` | `INVENTORY_READ` |
| SKU Admin | GET | `/api/admin/products/{productId}/skus` | - | `List<SkuResponse>` | `INVENTORY_READ` |
| SKU Admin | POST | `/api/admin/skus` | `SkuCreateRequest` | `SkuResponse` | `SKU_MANAGE` |
| SKU Admin | PATCH | `/api/admin/skus/{skuId}` | `SkuUpdateRequest` | `SkuResponse` | `SKU_MANAGE` |
| SKU Admin | PATCH | `/api/admin/skus/{skuId}/active` | `SkuActiveUpdateRequest` | `SkuResponse` | `SKU_MANAGE` |
| SKU Admin | POST | `/api/admin/skus/{skuId}/barcode/regenerate` | - | `SkuResponse` | `BARCODE_MANAGE` |
| Barcode Admin | GET | `/api/admin/barcodes` | `keyword`, `page`, `size` | `PageResponse<BarcodeSkuResponse>` | `INVENTORY_READ` |
| Barcode Admin | GET | `/api/admin/barcodes/{barcode}` | - | `BarcodeSkuResponse` | `INVENTORY_READ` |
| Barcode Label Admin | GET | `/api/admin/barcode-labels` | `keyword`, `page`, `size` | `PageResponse<BarcodeLabelResponse>` | `INVENTORY_READ` |
| Barcode Label Admin | POST | `/api/admin/barcodes/{skuId}/labels` | `BarcodeLabelRequest` | `BarcodeLabelPreviewResponse` | `BARCODE_MANAGE` |
| Barcode Label Admin | POST | `/api/admin/barcode-labels/{labelId}/print` | - | `BarcodeLabelPreviewResponse` | `BARCODE_MANAGE` |
| Barcode Stock Admin | GET | `/api/admin/barcodes/{barcode}/stock` | - | `BarcodeStockResponse` | `INVENTORY_READ` |
| Barcode Stock Admin | POST | `/api/admin/barcodes/{barcode}/inbound` | `BarcodeStockChangeRequest` | `BarcodeStockChangeResponse` | `INVENTORY_WRITE` |
| Barcode Stock Admin | POST | `/api/admin/barcodes/{barcode}/outbound` | `BarcodeStockChangeRequest` | `BarcodeStockChangeResponse` | `INVENTORY_WRITE` |
| Outbound Admin | GET | `/api/admin/outbound-orders` | `status`, `warehouseId`, `orderId`, `keyword`, `page`, `size` | `PageResponse<OutboundOrderResponse>` | `OUTBOUND_READ` |
| Outbound Admin | GET | `/api/admin/outbound-orders/{outboundOrderId}` | - | `OutboundOrderResponse` | `OUTBOUND_READ` |
| Outbound Admin | POST | `/api/admin/outbound-orders` | `OutboundOrderCreateRequest` | `OutboundOrderResponse` | `OUTBOUND_MANAGE` |
| Outbound Admin | PATCH | `/api/admin/outbound-orders/{outboundOrderId}` | `OutboundOrderUpdateRequest` | `OutboundOrderResponse` | `OUTBOUND_MANAGE` |
| Outbound Admin | PATCH | `/api/admin/outbound-orders/{outboundOrderId}/pick` | - | `OutboundOrderResponse` | `OUTBOUND_MANAGE` |
| Outbound Admin | PATCH | `/api/admin/outbound-orders/{outboundOrderId}/cancel` | - | `OutboundOrderResponse` | `OUTBOUND_MANAGE` |
| Carrier Admin | GET | `/api/admin/carriers` | `keyword`, `active`, `page`, `size` | `PageResponse<CarrierResponse>` | `CARRIER_MANAGE` |
| Carrier Admin | POST | `/api/admin/carriers` | `CarrierRequest` | `CarrierResponse` | `CARRIER_MANAGE` |
| Carrier Admin | PATCH | `/api/admin/carriers/{carrierId}` | `CarrierRequest` | `CarrierResponse` | `CARRIER_MANAGE` |
| Carrier Admin | PATCH | `/api/admin/carriers/{carrierId}/active` | `CarrierActiveUpdateRequest` | `CarrierResponse` | `CARRIER_MANAGE` |
| Shipping Method Admin | GET | `/api/admin/shipping-methods` | `keyword`, `carrierId`, `active`, `page`, `size` | `PageResponse<ShippingMethodResponse>` | `CARRIER_MANAGE` |
| Shipping Method Admin | POST | `/api/admin/shipping-methods` | `ShippingMethodRequest` | `ShippingMethodResponse` | `CARRIER_MANAGE` |
| Shipping Method Admin | PATCH | `/api/admin/shipping-methods/{shippingMethodId}` | `ShippingMethodRequest` | `ShippingMethodResponse` | `CARRIER_MANAGE` |
| Shipping Method Admin | PATCH | `/api/admin/shipping-methods/{shippingMethodId}/active` | `ShippingMethodActiveUpdateRequest` | `ShippingMethodResponse` | `CARRIER_MANAGE` |
| Stock Count Admin | GET | `/api/admin/stock-counts` | `status`, `page`, `size` | `PageResponse<StockCountResponse>` | `INVENTORY_READ` |
| Stock Count Admin | GET | `/api/admin/stock-counts/{stockCountId}` | - | `StockCountResponse` | `INVENTORY_READ` |
| Stock Count Admin | POST | `/api/admin/stock-counts` | `StockCountCreateRequest` | `StockCountResponse` | `STOCK_COUNT_MANAGE` |
| Stock Count Admin | PATCH | `/api/admin/stock-counts/{stockCountId}/items` | `StockCountItemsUpdateRequest` | `StockCountResponse` | `STOCK_COUNT_MANAGE` |
| Stock Count Admin | PATCH | `/api/admin/stock-counts/{stockCountId}/start` | - | `StockCountResponse` | `STOCK_COUNT_MANAGE` |
| Stock Count Admin | PATCH | `/api/admin/stock-counts/{stockCountId}/complete` | - | `StockCountResponse` | `STOCK_COUNT_MANAGE` |
| Stock Count Admin | PATCH | `/api/admin/stock-counts/{stockCountId}/cancel` | - | `StockCountResponse` | `STOCK_COUNT_MANAGE` |
| Inventory Alert Admin | GET | `/api/admin/inventory-alert-rules` | `warehouseId`, `active`, `keyword`, `page`, `size` | `PageResponse<InventoryAlertRuleResponse>` | `INVENTORY_READ` |
| Inventory Alert Admin | POST | `/api/admin/inventory-alert-rules` | `InventoryAlertRuleRequest` | `InventoryAlertRuleResponse` | `INVENTORY_WRITE` |
| Inventory Alert Admin | PATCH | `/api/admin/inventory-alert-rules/{ruleId}` | `InventoryAlertRuleRequest` | `InventoryAlertRuleResponse` | `INVENTORY_WRITE` |
| Inventory Alert Admin | PATCH | `/api/admin/inventory-alert-rules/{ruleId}/active` | `InventoryAlertRuleActiveRequest` | `InventoryAlertRuleResponse` | `INVENTORY_WRITE` |
| Inventory Alert Admin | GET | `/api/admin/inventory-alerts/low-stock` | `warehouseId` | `List<LowStockAlertResponse>` | `INVENTORY_READ` |
| Warehouse Location Admin | GET | `/api/admin/warehouse-locations` | `warehouseId`, `active`, `keyword`, `page`, `size` | `PageResponse<WarehouseLocationResponse>` | `INVENTORY_READ` |
| Warehouse Location Admin | POST | `/api/admin/warehouse-locations` | `WarehouseLocationCreateRequest` | `WarehouseLocationResponse` | `WAREHOUSE_MANAGE` |
| Warehouse Location Admin | PATCH | `/api/admin/warehouse-locations/{locationId}` | `WarehouseLocationUpdateRequest` | `WarehouseLocationResponse` | `WAREHOUSE_MANAGE` |
| Warehouse Location Admin | PATCH | `/api/admin/warehouse-locations/{locationId}/active` | `WarehouseLocationActiveRequest` | `WarehouseLocationResponse` | `WAREHOUSE_MANAGE` |
| Warehouse Location Admin | GET | `/api/admin/warehouse-locations/{locationId}/stocks` | `page`, `size` | `PageResponse<WarehouseLocationStockResponse>` | `INVENTORY_READ` |
| Production Admin | GET | `/api/admin/production-orders` | `status`, `warehouseId`, `skuId`, `keyword`, `dateFrom`, `dateTo`, `page`, `size` | `PageResponse<ProductionOrderListResponse>` | `INVENTORY_READ` |
| Production Admin | GET | `/api/admin/production-orders/{productionOrderId}` | - | `ProductionOrderResponse` | `INVENTORY_READ` |
| Production Admin | POST | `/api/admin/production-orders` | `ProductionOrderCreateRequest` | `ProductionOrderResponse` | `PRODUCTION_MANAGE` |
| Production Admin | PATCH | `/api/admin/production-orders/{productionOrderId}` | `ProductionOrderUpdateRequest` | `ProductionOrderResponse` | `PRODUCTION_MANAGE` |
| Production Admin | PATCH | `/api/admin/production-orders/{productionOrderId}/start` | `ProductionOrderStartRequest` | `ProductionOrderResponse` | `PRODUCTION_MANAGE` |
| Production Admin | PATCH | `/api/admin/production-orders/{productionOrderId}/complete` | `ProductionOrderCompleteRequest` | `ProductionOrderResponse` | `PRODUCTION_MANAGE` |
| Production Admin | PATCH | `/api/admin/production-orders/{productionOrderId}/cancel` | `ProductionOrderCancelRequest` | `ProductionOrderResponse` | `PRODUCTION_MANAGE` |
| Production Admin | GET | `/api/admin/production-receipts` | `productionOrderId`, `skuId`, `warehouseId`, `page`, `size` | `PageResponse<ProductionReceiptResponse>` | `INVENTORY_READ` |
| Production Admin | GET | `/api/admin/production-receipts/{receiptId}` | - | `ProductionReceiptResponse` | `INVENTORY_READ` |
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

- `ProductCreateRequest`, `ProductUpdateRequest`: `categoryId`, `name`, `description`, `price`, `productCode`, `brand`, `manufacturer`, `modelName`, `origin`, `originalPrice`, `discountPrice`, `purchasePrice`, `searchKeywords`, `tags`, `saleStartAt`, `saleEndAt`, `deliveryInfo`, `seoTitle`, `seoDescription`, `seoKeywords`, `salesStatus`, `displayStatus`, `safetyStockQuantity`, `stockQuantity`, `imageUrl`, `status`, `options`.
- `ProductStatusUpdateRequest`: `salesStatus`, `displayStatus`, `safetyStockQuantity`, `reason`.
- `ProductBulkStatusUpdateRequest`: `productIds`, `salesStatus`, `displayStatus`, `reason`.
- `ProductBulkStatusUpdateResponse`: `updatedCount`, `products`.
- `ProductStatusHistoryResponse`: `productId`, `changedByUserId`, `changedByEmail`, `previousSalesStatus`, `newSalesStatus`, `previousDisplayStatus`, `newDisplayStatus`, `reason`, `createdAt`.
- `ProductOperationNoteRequest`: `content`.
- `ProductOperationNoteResponse`: `productId`, `writerUserId`, `writerEmail`, `content`, `createdAt`, `updatedAt`.
- `CategoryResponse`: `id`, `name`, `parentId`, `depth`, `sortOrder`, `active`, `visibleInNav`, `slug`.
- `CategoryTreeResponse`: `CategoryResponse` 계열 필드와 `children`.
- `CategoryCreateRequest`, `CategoryUpdateRequest`: `name`, `parentId`, `sortOrder`, `active`, `visibleInNav`, `slug`.
- `ProductResponse`, `ProductListResponse`: 사용자 공개 상품 필드. `ProductResponse`는 visible 상세 블록 `detailBlocks`를 포함한다. `purchasable`, `stockDisplayStatus`, `stockDisplayText`, `remainingStockQuantity`를 포함하며 `purchasePrice`, `marginRate`, `displayStatus`, `safetyStockQuantity`는 포함하지 않는다.
- `AdminProductResponse`, `AdminProductListResponse`: 관리자 상품 필드. `purchasePrice`, 계산 필드 `marginRate`, `salesStatus`, `displayStatus`, `safetyStockQuantity`, `purchasable`, `stockDisplayStatus`, `stockDisplayText`를 포함한다.
- `ProductDetailBlockRequest`, `ProductDetailBlockResponse`: `blockType`, `title`, `content`, `imageUrl`, `specJson`, `sortOrder`, `visible`. `blockType`은 `HEADING`, `TEXT`, `IMAGE`, `NOTICE`, `SPEC_TABLE`, `HTML`.
- `MediaFileResponse`: `id`, `originalFilename`, `storedFilename`, `url`, `contentType`, `size`, `mediaType`, `createdAt`.
- `ReviewResponse`: `reviewId`, `productId`, `productName`, `userName`, `orderItemId`, `rating`, `content`, `status`, `createdAt`.
- `AuditLogResponse`: `id`, `actorId`, `actorEmail`, `actorName`, `actionType`, `targetType`, `targetId`, `beforeStatus`, `afterStatus`, `summary`, `createdAt`.
- `NotificationResponse`: `id`, `userId`, `type`, `title`, `message`, `targetType`, `targetId`, `read`, `readAt`, `createdAt`.
- `WarehouseLocationResponse`: `locationId`, `warehouseId`, `warehouseCode`, `warehouseName`, `code`, `name`, `zone`, `aisle`, `rack`, `cell`, `active`, `createdAt`, `updatedAt`.
- `WarehouseLocationStockResponse`: `stockId`, `locationId`, `locationCode`, `locationName`, `warehouseId`, `warehouseName`, `skuId`, `skuCode`, `barcode`, `productId`, `productName`, `quantity`, `reservedQuantity`, `availableQuantity`, `updatedAt`.
- `InventoryAlertRuleResponse`: `ruleId`, `skuId`, `skuCode`, `barcode`, `productId`, `productName`, `warehouseId`, `warehouseName`, `thresholdQuantity`, `active`, `memo`, `createdAt`, `updatedAt`.
- `LowStockAlertResponse`: `ruleId`, `skuId`, `skuCode`, `barcode`, `productId`, `productName`, `warehouseId`, `warehouseName`, `currentQuantity`, `thresholdQuantity`, `shortageQuantity`, `memo`.
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
- `BarcodeSkuResponse`: `skuId`, `skuCode`, `barcode`, `skuName`, `optionSignature`, `productId`, `productName`, `productCode`, `stockQuantity`, `safetyStockQuantity`, `active`.
- `BarcodeLabelRequest`: `labelFormat`. 기본값은 `SKU_60X40`.
- `BarcodeLabelResponse`: `id`, SKU/상품 요약, `barcode`, `labelFormat`, `printCount`, `lastPrintedAt`, `createdBy`, `createdAt`.
- `BarcodeLabelPreviewResponse`: `labelId`, `labelFormat`, `barcode`, `skuCode`, `productName`, `skuName`, `html`, `createdAt`.
- `BarcodeStockResponse`: SKU/상품 요약, `productStockQuantity`, `safetyStockQuantity`, `warehouseStocks`.
- `BarcodeStockChangeRequest`: `warehouseId`, `quantity`, `memo`.
- `BarcodeStockChangeResponse`: 바코드/SKU/상품/창고 요약, 처리 수량, 상품/창고 재고 변경 전후, `type`.
- `OutboundOrderCreateRequest`: `orderId`, `warehouseId`, `memo`.
- `OutboundOrderUpdateRequest`: `warehouseId`, `memo`.
- `OutboundOrderResponse`: 출고 지시 ID/번호, 주문/주문자/창고 요약, 상태, 요청/피킹/배송 일시, 메모, 수량 합계, 출고 품목 목록.
- `OutboundOrderItemResponse`: 주문 품목, SKU/바코드, 상품, 지시 수량, 피킹 수량, 스캔 수량.
- `CarrierRequest`: `code`, `name`, `trackingUrlTemplate`, `active`.
- `CarrierResponse`: 택배사 ID, 코드, 이름, 배송 추적 URL 템플릿, 활성 상태, 생성/수정일.
- `ShippingMethodRequest`: `code`, `name`, `carrierId`, `defaultFee`, `description`, `active`.
- `ShippingMethodResponse`: 배송 방법 ID, 코드, 이름, 연결 택배사, 기본 배송비, 설명, 활성 상태, 생성/수정일.
- `StockCountCreateRequest`: `warehouseId`, `memo`.
- `StockCountItemsUpdateRequest`: `items[{ skuId, countedQuantity, memo }]`.
- `StockCountResponse`: 실사 세션 요약, 상태, 창고, 시작/완료 시각, 실사 품목 목록.
- `UserSummaryResponse`: `id`, `name`, `email`, `phone`, `role`, `status`, `createdAt`, `orderCount`, `totalOrderAmount`.
- `DashboardSummaryResponse`: 전체/오늘 주문, 전체/오늘 매출, 고객/상품/품절/재고부족 수, 상태별 주문 수.
- `WarehouseStockResponse`: 창고, 상품, 수량, 예약 수량, 가용 수량, 상품 총 재고.
- `SkuCreateRequest`: `productId`, `optionSignature`, 선택 `skuCode`, 선택 `barcode`, 선택 `name`, `safetyStockQuantity`, `active`.
- `SkuUpdateRequest`: `optionSignature`, 선택 `skuCode`, 선택 `barcode`, 선택 `name`, `safetyStockQuantity`.
- `SkuActiveUpdateRequest`: `active`.
- `SkuResponse`, `SkuListResponse`: `id`, `productId`, `productName`, `productCode`, `optionSignature`, `skuCode`, `barcode`, `name`, `safetyStockQuantity`, `active`, timestamps.
- `ProductionOrderCreateRequest`, `ProductionOrderUpdateRequest`: `warehouseId`, `memo`, `items[{skuId, plannedQuantity}]`.
- `ProductionOrderCompleteRequest`: `items[{skuId, completedQuantity}]`, `memo`.
- `ProductionOrderResponse`: 생산번호, 상태, 창고, 예정/완료 수량, 시작/완료일, 메모, 품목 목록.
- `ProductionReceiptResponse`: 생산 주문, SKU, 상품, 창고, 입고 수량, 연결 `inventoryLogId`, 생성일.

## enum

| Enum | 값 |
| --- | --- |
| `UserRole` | `USER`, `MANAGER`, `ADMIN`, `SUPER_ADMIN` |
| `UserStatus` | `ACTIVE`, `INACTIVE`, `BLOCKED` |
| `ProductStatus` | `ON_SALE`, `SOLD_OUT`, `HIDDEN`, `DELETED` |
| `ProductSalesStatus` | `DRAFT`, `ON_SALE`, `PAUSED`, `SOLD_OUT`, `DISCONTINUED` |
| `ProductDisplayStatus` | `VISIBLE`, `HIDDEN` |
| `StockDisplayStatus` | `IN_STOCK`, `LOW_STOCK`, `SOLD_OUT` |
| `ReviewStatus` | `VISIBLE`, `HIDDEN`, `DELETED` |
| `AuditActionType` | `PRODUCT_CREATED`, `PRODUCT_UPDATED`, `PRODUCT_DELETED`, `PRODUCT_STATUS_CHANGED`, `PRODUCT_BULK_STATUS_CHANGED`, `PRODUCT_OPERATION_NOTE_CREATED`, `CATEGORY_CREATED`, `CATEGORY_UPDATED`, `CATEGORY_ACTIVE_CHANGED`, `BANNER_CREATED`, `BANNER_UPDATED`, `BANNER_ACTIVE_CHANGED`, `ORDER_STATUS_CHANGED`, `PAYMENT_CANCELLED`, `REFUND_PROCESSED`, `INVENTORY_ADJUSTED`, `INVENTORY_INBOUNDED`, `SKU_CREATED`, `SKU_UPDATED`, `SKU_ACTIVE_CHANGED`, `SKU_BARCODE_REGENERATED`, `WAREHOUSE_CREATED`, `WAREHOUSE_UPDATED`, `STOCK_TRANSFERRED`, `COUPON_CREATED`, `COUPON_UPDATED`, `COUPON_DELETED`, `REVIEW_HIDDEN`, `REVIEW_SHOWN`, `REVIEW_DELETED`, `INQUIRY_ANSWERED`, `INQUIRY_CLOSED`, `STAFF_CREATED`, `STAFF_UPDATED`, `STAFF_STATUS_CHANGED`, `STAFF_ACTIVE_CHANGED`, `PERMISSION_GROUP_CREATED`, `PERMISSION_GROUP_UPDATED`, `PERMISSION_GROUP_ACTIVE_CHANGED`, `USER_PERMISSION_GROUPS_UPDATED`, `PERMISSION_MATRIX_UPDATED`, `MENU_PERMISSION_UPDATED`, `SETTINGS_UPDATED`, `BUSINESS_SETTINGS_UPDATED`, `TERMS_VERSION_CREATED`, `POLICY_VERSION_CREATED`, `PERMISSION_DENIED` |
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
| `ProductionOrderStatus` | `PLANNED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED` |
| `StockReservationStatus` | `RESERVED`, `RELEASED`, `SHIPPED`, `RETURNED` |

## 미구현/예정으로 분리된 항목

- 실제 PG 벤더 키/웹훅/리다이렉트 연동. 현재 결제 승인 API는 `MOCK_PROVIDER` 기반으로 동작한다.
- 완전한 감사 로그 시스템. 현재는 리뷰 숨김/해제/삭제와 상품 상태 변경/대량 변경/운영 메모 작성 이력을 최소 기록한다.
- 고급 BI, 복식부기, 정산 리포트 API는 미구현이다. v0.2.8 기준으로는 `/api/admin/ops-analytics/overview`에서 기초 운영 지표만 제공한다.
- 피킹, 패킹, 출고 자동화 API.
