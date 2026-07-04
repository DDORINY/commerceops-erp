# 백엔드 구조 문서

기준 버전: `v0.2.3`
기준 코드: `backend/src/main/java/com/commerceops/erp`

## 기술 스택

- Spring Boot 3.5.0
- Java 17
- Spring Security + JWT
- Spring Data JPA
- MySQL 운영 기준, 테스트는 H2 프로파일 사용

## 패키지 구조

```text
com.commerceops.erp
├── CommerceOpsErpApplication.java
├── domain
│   ├── accounting
│   ├── auth
│   ├── cart
│   ├── category
│   ├── coupon
│   ├── dashboard
│   ├── inquiry
│   ├── inventory
│   ├── media
│   ├── order
│   ├── payment
│   ├── product
│   ├── returns
│   ├── review
│   ├── shipment
│   ├── user
│   ├── warehouse
│   └── wishlist
└── global
    ├── config
    ├── exception
    ├── health
    ├── response
    └── security
```

## 도메인별 실제 구성

| 도메인 | Controller | Service | Repository | Entity |
| --- | --- | --- | --- | --- |
| accounting | `AdminAccountingController` | `AccountingService` | `AccountingEntryRepository` | `AccountingEntry` |
| auth | `AuthController` | `AuthService` | `UserRepository` 사용 | `User` |
| cart | `CartController` | `CartService` | `CartRepository` | `Cart` |
| category | `CategoryController`, `AdminCategoryController` | `CategoryService` | `CategoryRepository` | `Category` |
| coupon | `CouponController`, `AdminCouponController` | `CouponService` | `CouponRepository` | `Coupon` |
| dashboard | `AdminDashboardController` | `DashboardService` | 주문/결제/상품/회원 repository 사용 | - |
| inquiry | `InquiryController`, `AdminInquiryController` | `InquiryService` | `InquiryRepository` | `Inquiry` |
| inventory | `AdminInventoryController` | `InventoryService` | `InventoryLogRepository` | `InventoryLog` |
| media | `AdminMediaController` | `MediaStorageService` | `MediaFileRepository` | `MediaFile` |
| order | `OrderController`, `AdminOrderController` | `OrderService`, `OrderCancellationService` | `OrderRepository`, `OrderItemRepository` | `Order`, `OrderItem` |
| payment | `PaymentController` | `PaymentService` | `PaymentRepository` | `Payment` |
| product | `ProductController`, `AdminProductController` | `ProductService` | `ProductRepository` | `Product` |
| returns | `ReturnController`, `AdminReturnController` | `ReturnService` | `ReturnRequestRepository` | `ReturnRequest` |
| review | `ReviewController`, `AdminReviewController` | `ReviewService` | `ReviewRepository` | `Review` |
| shipment | `ShipmentController`, `AdminShipmentController` | `ShipmentService` | `ShipmentRepository` | `Shipment` |
| user | `AdminUserController` | `AdminUserService` | `UserRepository` | `User` |
| warehouse | `AdminWarehouseController` | `WarehouseService`, `WarehouseFulfillmentService` | `WarehouseRepository`, `WarehouseStockRepository`, `StockReservationRepository`, `StockTransferRepository` | `Warehouse`, `WarehouseStock`, `StockReservation`, `StockTransfer` |
| wishlist | `WishlistController` | `WishlistService` | `WishlistRepository` | `Wishlist` |

## 글로벌 구성

- `SecurityConfig`: JWT stateless 인증, 공개/관리자 권한 라우팅 설정.
- `JwtAuthenticationFilter`: Bearer access token 파싱 후 SecurityContext 설정. refresh token은 인증 토큰으로 사용하지 않는다.
- `CustomUserDetailsService`: 사용자 로딩.
- `JwtTokenProvider`: access token, refresh token 생성/검증.
- `ApiResponse<T>`: 성공 응답 래퍼.
- `PageResponse<T>`: 프론트와 맞춘 페이지 응답 DTO.
- `GlobalExceptionHandler`, `BusinessException`, `ErrorCode`: 공통 예외 처리.
- `HealthController`: `GET /api/health`.
- `MediaWebConfig`: `COMMERCEOPS_MEDIA_UPLOAD_DIR` 기준 로컬 업로드 디렉터리를 `/uploads/**` 정적 리소스로 제공.

## v0.1.1 ~ v0.1.6 반영 상태

- 관리자 문의: `GET /api/admin/inquiries`, 답변, 종료 API 실제 연결.
- 관리자 리뷰: `GET /api/admin/reviews`, `DELETE /api/admin/reviews/{reviewId}` 실제 연결.
- 관리자 회계: `GET /api/admin/accounting/summary`, `GET /api/admin/accounting/entries` 실제 연결.
- 관리자 판매/매출 분석: `GET /api/admin/dashboard/summary`, `/sales`, `/top-products` 사용.
- 관리자 창고: 창고 목록/등록, 창고별 재고, 재고 할당, 재고 이동 실제 API 구현.
- 사용자 쇼핑 화면: mock 파일 제거, 상품/카테고리/장바구니/주문/위시리스트/문의/리뷰 service 기반 연결.

## 명시적 미구현

- 실제 PG 벤더 키/웹훅/리다이렉트 연동. 현재 `PaymentController`는 `/api/payments/approve`, `/api/payments/{paymentId}/cancel`, 하위 호환 `/api/payments/mock/complete`를 제공하며 `MOCK_PROVIDER` 기반으로 동작한다.
- 리뷰 숨김/상태 변경. 현재 관리자 리뷰 운영은 조회와 삭제다.
- S3/CDN, 이미지 리사이징, 썸네일, 다중 이미지 갤러리.
- 고급 WMS 피킹/패킹/출고 자동화.
- 복식부기 기반 정산/마감 리포트.
