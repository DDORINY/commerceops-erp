# API 명세서

> 문서 상태: 초기 설계 명세 + 현재 구현 보강 중  
> 현재 구현/검증 여부는 [CURRENT_STATE.md](./CURRENT_STATE.md), 버전 범위는 [VERSION_PLAN.md](./VERSION_PLAN.md)를 먼저 확인한다.  
> 이 문서에 있으나 코드에 없는 로그아웃·고객 관리·파일 업로드 등은 구현 완료가 아니라 계획 항목이다.

CommerceOps ERP의 백엔드 API 명세서입니다.

본 문서는 사용자 쇼핑몰 기능과 관리자 ERP 기능을 연결하기 위한 REST API 구조를 정의합니다.

---

## 1. 기본 정보

| 항목     | 내용                          |
| ------ | --------------------------- |
| 프로젝트명  | CommerceOps ERP             |
| API 형식 | REST API                    |
| 인증 방식  | JWT Bearer Token            |
| 기본 URL | `http://localhost:8080/api` |
| 데이터 형식 | JSON                        |
| 문자 인코딩 | UTF-8                       |

---

## 2. 공통 요청 헤더

### 인증이 필요 없는 API

```http
Content-Type: application/json
```

### 인증이 필요한 API

```http
Content-Type: application/json
Authorization: Bearer {accessToken}
```

---

## 3. 공통 응답 형식

### 성공 응답

```json
{
  "success": true,
  "statusCode": 200,
  "message": "요청이 성공했습니다.",
  "data": {}
}
```

### 실패 응답

```json
{
  "success": false,
  "statusCode": 400,
  "errorCode": "INVALID_REQUEST",
  "message": "잘못된 요청입니다.",
  "error": "상세 오류 메시지"
}
```

---

## 4. 공통 상태 코드

| HTTP Status               | 설명        |
| ------------------------- | --------- |
| 200 OK                    | 조회, 수정 성공 |
| 201 Created               | 생성 성공     |
| 204 No Content            | 삭제 성공     |
| 400 Bad Request           | 잘못된 요청    |
| 401 Unauthorized          | 인증 실패     |
| 403 Forbidden             | 권한 없음     |
| 404 Not Found             | 데이터 없음    |
| 409 Conflict              | 데이터 충돌    |
| 500 Internal Server Error | 서버 오류     |

---

## 5. 공통 에러 코드

| Error Code            | 설명           |
| --------------------- | ------------ |
| INVALID_REQUEST       | 잘못된 요청       |
| UNAUTHORIZED          | 인증 실패        |
| FORBIDDEN             | 접근 권한 없음     |
| NOT_FOUND             | 리소스를 찾을 수 없음 |
| DUPLICATED_EMAIL      | 이미 가입된 이메일   |
| INVALID_PASSWORD      | 비밀번호 불일치     |
| OUT_OF_STOCK          | 재고 부족        |
| INVALID_ORDER_STATUS  | 잘못된 주문 상태    |
| PAYMENT_FAILED        | 결제 실패        |
| INTERNAL_SERVER_ERROR | 서버 내부 오류     |

---

# 6. 인증 API

## 6-1. 회원가입

```http
POST /api/auth/signup
```

### Request Body

```json
{
  "email": "user@example.com",
  "password": "password1234",
  "name": "홍길동",
  "phone": "010-1234-5678"
}
```

### Response

```json
{
  "success": true,
  "statusCode": 201,
  "message": "회원가입이 완료되었습니다.",
  "data": {
    "userId": 1,
    "email": "user@example.com",
    "name": "홍길동",
    "role": "USER"
  }
}
```

---

## 6-2. 로그인

```http
POST /api/auth/login
```

### Request Body

```json
{
  "email": "user@example.com",
  "password": "password1234"
}
```

### Response

```json
{
  "success": true,
  "statusCode": 200,
  "message": "로그인이 완료되었습니다.",
  "data": {
    "accessToken": "jwt-access-token",
    "tokenType": "Bearer",
    "user": {
      "id": 1,
      "email": "user@example.com",
      "name": "홍길동",
      "role": "USER"
    }
  }
}
```

---

## 6-3. 내 정보 조회

```http
GET /api/auth/me
```

### Headers

```http
Authorization: Bearer {accessToken}
```

### Response

```json
{
  "success": true,
  "statusCode": 200,
  "message": "내 정보 조회가 완료되었습니다.",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "name": "홍길동",
    "phone": "010-1234-5678",
    "role": "USER",
    "status": "ACTIVE"
  }
}
```

---

## 6-4. 로그아웃

```http
POST /api/auth/logout
```

### Headers

```http
Authorization: Bearer {accessToken}
```

### Response

```json
{
  "success": true,
  "statusCode": 200,
  "message": "로그아웃이 완료되었습니다.",
  "data": null
}
```

---

# 7. 상품 API

## 7-1. 상품 목록 조회

```http
GET /api/products
```

### Query Parameters

| 이름         | 타입     | 필수 | 설명      |
| ---------- | ------ | -- | ------- |
| categoryId | Long   | N  | 카테고리 ID |
| keyword    | String | N  | 검색어     |
| sort       | String | N  | 정렬 기준   |
| page       | Number | N  | 페이지 번호  |
| size       | Number | N  | 페이지 크기  |

### Example

```http
GET /api/products?categoryId=1&keyword=원피스&sort=new&page=0&size=20
```

### Response

```json
{
  "success": true,
  "statusCode": 200,
  "message": "상품 목록 조회가 완료되었습니다.",
  "data": {
    "content": [
      {
        "id": 1,
        "categoryId": 1,
        "categoryName": "원피스",
        "name": "베이직 셔츠 원피스",
        "price": 39000,
        "stockQuantity": 20,
        "imageUrl": "/images/products/product-1.jpg",
        "status": "ON_SALE",
        "createdAt": "2026-06-16T10:00:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

---

## 7-2. 상품 상세 조회

```http
GET /api/products/{productId}
```

### Response

```json
{
  "success": true,
  "statusCode": 200,
  "message": "상품 상세 조회가 완료되었습니다.",
  "data": {
    "id": 1,
    "categoryId": 1,
    "categoryName": "원피스",
    "name": "베이직 셔츠 원피스",
    "description": "데일리로 착용하기 좋은 셔츠 원피스입니다.",
    "price": 39000,
    "stockQuantity": 20,
    "imageUrl": "/images/products/product-1.jpg",
    "status": "ON_SALE",
    "createdAt": "2026-06-16T10:00:00",
    "updatedAt": "2026-06-16T10:00:00"
  }
}
```

---

# 8. 관리자 상품 API

## 8-1. 상품 등록

```http
POST /api/admin/products
```

### 권한

```text
ADMIN
```

### Request Body

```json
{
  "categoryId": 1,
  "name": "베이직 셔츠 원피스",
  "description": "데일리로 착용하기 좋은 셔츠 원피스입니다.",
  "price": 39000,
  "stockQuantity": 20,
  "imageUrl": "/images/products/product-1.jpg",
  "status": "ON_SALE"
}
```

### Response

```json
{
  "success": true,
  "statusCode": 201,
  "message": "상품이 등록되었습니다.",
  "data": {
    "id": 1,
    "name": "베이직 셔츠 원피스",
    "price": 39000,
    "stockQuantity": 20,
    "status": "ON_SALE"
  }
}
```

---

## 8-2. 상품 수정

```http
PATCH /api/admin/products/{productId}
```

### Request Body

```json
{
  "categoryId": 1,
  "name": "수정된 상품명",
  "description": "수정된 상품 설명",
  "price": 42000,
  "stockQuantity": 30,
  "imageUrl": "/images/products/product-1-updated.jpg",
  "status": "ON_SALE"
}
```

### Response

```json
{
  "success": true,
  "statusCode": 200,
  "message": "상품이 수정되었습니다.",
  "data": {
    "id": 1,
    "name": "수정된 상품명",
    "price": 42000,
    "stockQuantity": 30,
    "status": "ON_SALE"
  }
}
```

---

## 8-3. 상품 삭제

```http
DELETE /api/admin/products/{productId}
```

### Response

```json
{
  "success": true,
  "statusCode": 204,
  "message": "상품이 삭제되었습니다.",
  "data": null
}
```

---

# 9. 카테고리 API

## 9-1. 카테고리 목록 조회

```http
GET /api/categories
```

### Response

```json
{
  "success": true,
  "statusCode": 200,
  "message": "카테고리 목록 조회가 완료되었습니다.",
  "data": [
    {
      "id": 1,
      "name": "원피스"
    },
    {
      "id": 2,
      "name": "블라우스"
    }
  ]
}
```

---

## 9-2. 관리자 카테고리 등록

```http
POST /api/admin/categories
```

### Request Body

```json
{
  "name": "스커트"
}
```

### Response

```json
{
  "success": true,
  "statusCode": 201,
  "message": "카테고리가 등록되었습니다.",
  "data": {
    "id": 3,
    "name": "스커트"
  }
}
```

---

# 10. 장바구니 API

## 10-1. 장바구니 조회

```http
GET /api/cart
```

### Headers

```http
Authorization: Bearer {accessToken}
```

### Response

```json
{
  "success": true,
  "statusCode": 200,
  "message": "장바구니 조회가 완료되었습니다.",
  "data": {
    "items": [
      {
        "cartId": 1,
        "productId": 1,
        "productName": "베이직 셔츠 원피스",
        "price": 39000,
        "quantity": 2,
        "stockQuantity": 20,
        "imageUrl": "/images/products/product-1.jpg",
        "subtotal": 78000
      }
    ],
    "totalPrice": 78000
  }
}
```

---

## 10-2. 장바구니 상품 추가

```http
POST /api/cart
```

### Request Body

```json
{
  "productId": 1,
  "quantity": 2
}
```

### Response

```json
{
  "success": true,
  "statusCode": 201,
  "message": "장바구니에 상품이 추가되었습니다.",
  "data": {
    "cartId": 1,
    "productId": 1,
    "quantity": 2
  }
}
```

---

## 10-3. 장바구니 수량 변경

```http
PATCH /api/cart/{cartId}
```

### Request Body

```json
{
  "quantity": 3
}
```

### Response

```json
{
  "success": true,
  "statusCode": 200,
  "message": "장바구니 수량이 변경되었습니다.",
  "data": {
    "cartId": 1,
    "quantity": 3
  }
}
```

---

## 10-4. 장바구니 상품 삭제

```http
DELETE /api/cart/{cartId}
```

### Response

```json
{
  "success": true,
  "statusCode": 204,
  "message": "장바구니 상품이 삭제되었습니다.",
  "data": null
}
```

---

# 11. 주문 API

## 11-1. 주문 생성

```http
POST /api/orders
```

### 설명

장바구니 상품을 기준으로 주문을 생성합니다.
MVP에서는 실제 결제 연동 전까지 주문 상태를 `PENDING`으로 생성합니다.

### Request Body

```json
{
  "receiverName": "홍길동",
  "receiverPhone": "010-1234-5678",
  "address": "서울특별시 강남구 테헤란로 123",
  "detailAddress": "101동 1001호",
  "paymentMethod": "MOCK_CARD",
  "cartItemIds": [1, 2]
}
```

### Response

```json
{
  "success": true,
  "statusCode": 201,
  "message": "주문이 생성되었습니다.",
  "data": {
    "orderId": 1,
    "orderNumber": "ORD-20260616-000001",
    "totalPrice": 78000,
    "status": "PENDING",
    "paymentStatus": "READY"
  }
}
```

---

## 11-2. 주문 내역 조회

```http
GET /api/orders
```

### Response

```json
{
  "success": true,
  "statusCode": 200,
  "message": "주문 내역 조회가 완료되었습니다.",
  "data": [
    {
      "orderId": 1,
      "orderNumber": "ORD-20260616-000001",
      "totalPrice": 78000,
      "status": "PENDING",
      "paymentStatus": "READY",
      "createdAt": "2026-06-16T10:00:00"
    }
  ]
}
```

---

## 11-3. 주문 상세 조회

```http
GET /api/orders/{orderId}
```

### Response

```json
{
  "success": true,
  "statusCode": 200,
  "message": "주문 상세 조회가 완료되었습니다.",
  "data": {
    "orderId": 1,
    "orderNumber": "ORD-20260616-000001",
    "receiverName": "홍길동",
    "receiverPhone": "010-1234-5678",
    "address": "서울특별시 강남구 테헤란로 123",
    "totalPrice": 78000,
    "status": "PENDING",
    "paymentStatus": "READY",
    "items": [
      {
        "productId": 1,
        "productName": "베이직 셔츠 원피스",
        "price": 39000,
        "quantity": 2,
        "subtotal": 78000
      }
    ],
    "createdAt": "2026-06-16T10:00:00"
  }
}
```

## 11-4. 사용자 주문 취소

```http
PATCH /api/orders/{orderId}/cancel
```

- 본인 주문만 취소할 수 있다.
- `PENDING`, `PAID`, `PREPARING` 상태에서만 허용한다.
- 결제된 주문은 상품 가용재고, 창고 예약, 결제·회계 기록을 하나의 트랜잭션으로 복구한다.
- `SHIPPING` 이후에는 반품 흐름을 사용한다.

---

# 12. 관리자 주문 API

## 12-1. 관리자 주문 목록 조회

```http
GET /api/admin/orders
```

### Query Parameters

| 이름      | 타입     | 필수 | 설명          |
| ------- | ------ | -- | ----------- |
| status  | String | N  | 주문 상태       |
| keyword | String | N  | 주문번호 또는 고객명 |
| page    | Number | N  | 페이지 번호      |
| size    | Number | N  | 페이지 크기      |

### Response

```json
{
  "success": true,
  "statusCode": 200,
  "message": "관리자 주문 목록 조회가 완료되었습니다.",
  "data": {
    "content": [
      {
        "orderId": 1,
        "orderNumber": "ORD-20260616-000001",
        "userName": "홍길동",
        "totalPrice": 78000,
        "status": "PENDING",
        "paymentStatus": "READY",
        "createdAt": "2026-06-16T10:00:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

---

## 12-2. 주문 상태 변경

```http
PATCH /api/admin/orders/{orderId}/status
```

### Request Body

```json
{
  "status": "SHIPPING"
}
```

### Response

```json
{
  "success": true,
  "statusCode": 200,
  "message": "주문 상태가 변경되었습니다.",
  "data": {
    "orderId": 1,
    "status": "SHIPPING"
  }
}
```

---

# 13. 결제 API

## 13-1. 모의 결제 완료 처리

```http
POST /api/payments/mock/complete
```

### 설명

MVP에서는 실제 PG 연동 전까지 모의 결제로 결제 완료 처리를 합니다.
결제 완료 시 주문 상태는 `PAID`, 결제 상태는 `PAID`로 변경됩니다.
재고는 결제 완료 시점에 차감합니다.

### Request Body

```json
{
  "orderId": 1,
  "paymentMethod": "MOCK_CARD"
}
```

### Response

```json
{
  "success": true,
  "statusCode": 200,
  "message": "모의 결제가 완료되었습니다.",
  "data": {
    "paymentId": 1,
    "orderId": 1,
    "paymentStatus": "PAID",
    "paidAmount": 78000,
    "transactionId": "MOCK-20260616-000001"
  }
}
```

---

# 14. 재고 API

## 14-1. 관리자 재고 목록 조회

```http
GET /api/admin/inventory
```

### Query Parameters

| 이름           | 타입      | 필수 | 설명           |
| ------------ | ------- | -- | ------------ |
| keyword      | String  | N  | 상품명 검색       |
| lowStockOnly | Boolean | N  | 재고 부족 상품만 조회 |
| page         | Number  | N  | 페이지 번호       |
| size         | Number  | N  | 페이지 크기       |

### Response

```json
{
  "success": true,
  "statusCode": 200,
  "message": "재고 목록 조회가 완료되었습니다.",
  "data": {
    "content": [
      {
        "productId": 1,
        "productName": "베이직 셔츠 원피스",
        "stockQuantity": 20,
        "lowStockThreshold": 5,
        "status": "NORMAL"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

---

## 14-2. 상품 입고 처리

```http
POST /api/admin/inventory/inbound
```

### Request Body

```json
{
  "warehouseId": 1,
  "productId": 1,
  "quantity": 10,
  "memo": "초기 입고"
}
```

### Response

```json
{
  "success": true,
  "statusCode": 201,
  "message": "입고 처리가 완료되었습니다.",
  "data": {
    "productId": 1,
    "beforeStock": 20,
    "afterStock": 30,
    "quantity": 10,
    "type": "INBOUND"
  }
}
```

---

## 14-3. 재고 조정

```http
POST /api/admin/inventory/adjust
```

### Request Body

```json
{
  "warehouseId": 1,
  "productId": 1,
  "quantity": 25,
  "memo": "실사 재고 반영"
}
```

### Response

```json
{
  "success": true,
  "statusCode": 200,
  "message": "재고 조정이 완료되었습니다.",
  "data": {
    "productId": 1,
    "beforeStock": 30,
    "afterStock": 25,
    "type": "ADJUST"
  }
}
```

---

## 14-4. 재고 로그 조회

```http
GET /api/admin/inventory/logs
```

### Query Parameters

| 이름        | 타입     | 필수 | 설명     |
| --------- | ------ | -- | ------ |
| productId | Long   | N  | 상품 ID  |
| type      | String | N  | 로그 유형  |
| page      | Number | N  | 페이지 번호 |
| size      | Number | N  | 페이지 크기 |

### Response

```json
{
  "success": true,
  "statusCode": 200,
  "message": "재고 로그 조회가 완료되었습니다.",
  "data": {
    "content": [
      {
        "id": 1,
        "productId": 1,
        "productName": "베이직 셔츠 원피스",
        "type": "INBOUND",
        "quantity": 10,
        "beforeStock": 20,
        "afterStock": 30,
        "memo": "초기 입고",
        "createdAt": "2026-06-16T10:00:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

---

# 15. 관리자 대시보드 API

## 15-1. 대시보드 요약 조회

```http
GET /api/admin/dashboard/summary
```

### Response

```json
{
  "success": true,
  "statusCode": 200,
  "message": "대시보드 요약 조회가 완료되었습니다.",
  "data": {
    "totalSales": 1250000,
    "todaySales": 150000,
    "totalOrders": 42,
    "todayOrders": 5,
    "lowStockProductCount": 3,
    "pendingOrderCount": 7
  }
}
```

---

## 15-2. 매출 통계 조회

```http
GET /api/admin/dashboard/sales
```

### Query Parameters

| 이름        | 타입     | 필수 | 설명              |
| --------- | ------ | -- | --------------- |
| period    | String | N  | DAILY / MONTHLY |
| startDate | String | N  | 시작일, `yyyy-MM-dd` |
| endDate   | String | N  | 종료일, `yyyy-MM-dd` |

`period=DAILY`는 일별 `yyyy-MM-dd`, `period=MONTHLY`는 월별 `yyyy-MM` 라벨로 응답한다. `salesAmount`와 `orderCount`는 `payments.payment_status = PAID` 기준으로 집계한다.

### Response

```json
{
  "success": true,
  "statusCode": 200,
  "message": "매출 통계 조회가 완료되었습니다.",
  "data": [
    {
      "date": "2026-06-16",
      "salesAmount": 150000,
      "orderCount": 5
    }
  ]
}
```

---

## 15-3. 재고 부족 상품 조회

```http
GET /api/admin/dashboard/low-stock
```

### Response

```json
{
  "success": true,
  "statusCode": 200,
  "message": "재고 부족 상품 조회가 완료되었습니다.",
  "data": [
    {
      "productId": 1,
      "productName": "베이직 셔츠 원피스",
      "stockQuantity": 3,
      "lowStockThreshold": 5
    }
  ]
}
```

---

## 15-4. 인기 상품 조회

```http
GET /api/admin/dashboard/top-products
```

### Query Parameters

| 이름    | 타입     | 필수 | 설명    |
| ----- | ------ | -- | ----- |
| limit | Number | N  | 조회 개수 |

### Response

```json
{
  "success": true,
  "statusCode": 200,
  "message": "인기 상품 조회가 완료되었습니다.",
  "data": [
    {
      "productId": 1,
      "productName": "베이직 셔츠 원피스",
      "orderCount": 12,
      "salesAmount": 468000
    }
  ]
}
```

현재 인기 상품 조회는 주문 상태가 `PAID`, `PREPARING`, `SHIPPING`, `COMPLETED`인 주문 상품을 기준으로 판매 수량과 판매 금액을 집계한다. 기간 조건은 아직 없으며, 기간별 상품 성과 분석은 v0.2 이후 고급 BI 범위에서 확장한다.

---

# 16. 고객 관리 API

## 16-1. 관리자 고객 목록 조회

```http
GET /api/admin/customers
```

### Query Parameters

| 이름      | 타입     | 필수 | 설명         |
| ------- | ------ | -- | ---------- |
| keyword | String | N  | 이름, 이메일 검색 |
| page    | Number | N  | 페이지 번호     |
| size    | Number | N  | 페이지 크기     |

### Response

```json
{
  "success": true,
  "statusCode": 200,
  "message": "고객 목록 조회가 완료되었습니다.",
  "data": {
    "content": [
      {
        "id": 1,
        "email": "user@example.com",
        "name": "홍길동",
        "phone": "010-1234-5678",
        "role": "USER",
        "status": "ACTIVE",
        "orderCount": 3,
        "totalOrderAmount": 180000,
        "createdAt": "2026-06-16T10:00:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

---

# 17. 파일 업로드 API

## 17-1. 상품 이미지 업로드

```http
POST /api/admin/files/products
```

### Content-Type

```http
multipart/form-data
```

### Request

| 이름   | 타입            | 필수 | 설명        |
| ---- | ------------- | -- | --------- |
| file | MultipartFile | Y  | 상품 이미지 파일 |

### Response

```json
{
  "success": true,
  "statusCode": 201,
  "message": "파일 업로드가 완료되었습니다.",
  "data": {
    "fileName": "product-1.jpg",
    "fileUrl": "/uploads/products/product-1.jpg"
  }
}
```

---

# 18. Enum 정의

## 18-1. UserRole

```text
USER
ADMIN
SUPER_ADMIN
```

---

## 18-2. UserStatus

```text
ACTIVE
INACTIVE
BLOCKED
```

---

## 18-3. ProductStatus

```text
ON_SALE
SOLD_OUT
HIDDEN
DELETED
```

---

## 18-4. OrderStatus

```text
PENDING
PAID
PREPARING
SHIPPING
COMPLETED
CANCELLED
REFUNDED
```

---

## 18-5. PaymentStatus

```text
READY
PAID
FAILED
CANCELLED
REFUNDED
```

---

## 18-6. InventoryLogType

```text
INBOUND
OUTBOUND
ORDER
CANCEL
ADJUST
```

---

# 19. 권한 정책

| API 영역      | USER | ADMIN |
| ----------- | ---: | ----: |
| 상품 조회       |    O |     O |
| 장바구니        |    O |     O |
| 주문 생성       |    O |     O |
| 본인 주문 조회    |    O |     O |
| 상품 등록/수정/삭제 |    X |     O |
| 주문 상태 변경    |    X |     O |
| 재고 관리       |    X |     O |
| 고객 관리       |    X |     O |
| 매출 대시보드     |    X |     O |

---

# 20. 재고 처리 정책

## 결제 완료 시 재고 차감

```text
1. 주문 ID 조회
2. 주문 상품 목록 조회
3. 상품별 현재 재고 확인
4. 재고 부족 시 결제 실패 처리
5. 재고 충분 시 창고별 가용 재고 예약
6. products.stock_quantity 차감
7. inventory_logs에 ORDER 타입 로그 저장
8. 주문 상태와 결제 상태 PAID 변경
9. 송장 등록/SHIPPING 전환 시 예약 창고에서 실재고 출고
```

---

## 주문 취소 시 재고 복구

```text
1. 주문 ID 조회
2. 주문 상태 확인
3. 취소 가능한 상태인지 검증
4. 주문 상품 수량만큼 재고 복구
5. inventory_logs에 CANCEL 타입 로그 저장
6. 주문 상태 CANCELLED 변경
7. 결제 상태 CANCELLED 또는 REFUNDED 변경
```

---

# 21. API 개발 우선순위

## 1순위

```text
POST /api/auth/signup
POST /api/auth/login
GET  /api/auth/me

GET    /api/products
GET    /api/products/{productId}
POST   /api/admin/products
PATCH  /api/admin/products/{productId}
DELETE /api/admin/products/{productId}
```

## 2순위

```text
GET    /api/cart
POST   /api/cart
PATCH  /api/cart/{cartId}
DELETE /api/cart/{cartId}

POST /api/orders
GET  /api/orders
GET  /api/orders/{orderId}
```

## 3순위

```text
POST /api/payments/mock/complete

GET  /api/admin/inventory
POST /api/admin/inventory/inbound
POST /api/admin/inventory/adjust
GET  /api/admin/inventory/logs
```

## 4순위

```text
GET /api/admin/dashboard/summary
GET /api/admin/dashboard/sales
GET /api/admin/dashboard/low-stock
GET /api/admin/dashboard/top-products
```

---

# 22. 테스트 계정 예시

## 일반 사용자

```json
{
  "email": "user@example.com",
  "password": "password1234",
  "role": "USER"
}
```

## 관리자

```json
{
  "email": "admin@example.com",
  "password": "admin1234",
  "role": "ADMIN"
}
```

---

# 23. 추가 운영 API — 현재 코드 기준

> 아래 API는 Claude 로드맵 이후 구현되었고 2026-07-04 기준 백엔드 컴파일/테스트를 통과했다. 실제 DB API 통합 테스트와 E2E는 추가 보강 대상이다.

## 23-1. 배송

| Method | Path | 권한 | 설명 |
| --- | --- | --- | --- |
| GET | `/api/admin/shipments` | ADMIN | 상태·키워드·페이지 조건 배송 목록 |
| GET | `/api/admin/shipments/{id}` | ADMIN | 배송 상세 |
| PATCH | `/api/admin/shipments/{id}/tracking` | ADMIN | 송장번호와 택배사 등록, 배송 중 전환 |
| PATCH | `/api/admin/shipments/{id}/deliver` | ADMIN | 배송 완료 처리 |
| GET | `/api/orders/{orderId}/shipment` | 주문 소유자 | 주문의 배송 정보 |

배송 상태는 `READY`, `IN_TRANSIT`, `DELIVERED`, `CANCELLED`를 사용한다. 준비 중 주문을 취소하면 배송 건도 `CANCELLED`가 되며 이후 송장 등록은 거부한다.

## 23-2. 반품

| Method | Path | 권한 | 설명 |
| --- | --- | --- | --- |
| POST | `/api/orders/{orderId}/returns` | 주문 소유자 | 반품 신청 |
| GET | `/api/returns` | USER | 내 반품 목록 |
| GET | `/api/admin/returns` | ADMIN | 반품 관리 목록 |
| PATCH | `/api/admin/returns/{id}/approve` | ADMIN | 반품 승인, 현재 구현은 재고 복구·회계 환불 기록 포함 |
| PATCH | `/api/admin/returns/{id}/reject` | ADMIN | 반품 거절 |

- 반품 사유: `CHANGE_OF_MIND`, `DEFECTIVE`, `WRONG_DELIVERY`
- 반품 상태: `REQUESTED`, `APPROVED`, `REJECTED`
- 실제 PG 환불, 수거/검수, 부분 반품 상태는 아직 구현 범위가 아니다.

## 23-3. 문의

| Method | Path | 권한 | 설명 |
| --- | --- | --- | --- |
| POST | `/api/products/{productId}/inquiries` | USER | 상품 문의 작성 |
| GET | `/api/products/{productId}/inquiries` | 공개 정책 검토 필요 | 상품 문의 목록 |
| POST | `/api/inquiries` | USER | 주문/일반 문의 작성 |
| GET | `/api/my/inquiries` | USER | 내 문의 목록 |
| GET | `/api/admin/inquiries` | ADMIN | 문의 관리 목록 |
| PATCH | `/api/admin/inquiries/{id}/answer` | ADMIN | 답변 등록 |
| PATCH | `/api/admin/inquiries/{id}/close` | ADMIN | 문의 종료 |

- 문의 유형: `PRODUCT`, `ORDER`, `OTHER`
- 문의 상태: `WAITING`, `ANSWERED`, `CLOSED`

## 23-4. 기초 회계

| Method | Path | 권한 | 설명 |
| --- | --- | --- | --- |
| GET | `/api/admin/accounting/summary` | ADMIN | 매출·환불·입고·순매출 요약 |
| GET | `/api/admin/accounting/entries` | ADMIN | 유형별 기초 회계 기록 목록 |

`GET /api/admin/accounting/summary` 응답 `data`:

| 필드 | 의미 |
| --- | --- |
| `totalSales` | `SALE` 합계 |
| `totalRefunds` | `REFUND` 합계 |
| `totalInbound` | `INBOUND` 합계 |
| `netSales` | `totalSales - totalRefunds` |

`GET /api/admin/accounting/entries` query:

| 이름 | 필수 | 설명 |
| --- | --- | --- |
| `type` | 아니오 | `SALE`, `REFUND`, `INBOUND` 중 하나 |
| `page` | 아니오 | 0부터 시작하는 페이지 번호 |
| `size` | 아니오 | 페이지 크기 |

응답 `content` 항목은 `entryId`, `type`, `amount`, `description`, `referenceId`, `createdAt`을 포함한다.

현재 유형은 `SALE`, `REFUND`, `INBOUND`이다. 관리자 화면 CSV 다운로드는 별도 서버 API가 아니라 현재 조회된 회계 내역 페이지를 기준으로 `ID`, `구분`, `금액`, `설명`, `참조ID`, `일시` 컬럼을 클라이언트에서 생성한다. 이는 정식 복식부기 전표가 아니며 계정과목, 차변/대변 라인, 마감, 반제는 [v0.4 계획](./VERSION_PLAN.md#v04--회계정산-기반)에서 구현한다.

---

# 24. 창고 관리 API

> v0.1.5 기준 관리자 창고 화면에서 사용한다. 모든 API는 JWT 인증과 `ADMIN` 또는 `SUPER_ADMIN` 권한이 필요하다.

| Method | Path | 설명 |
| --- | --- | --- |
| GET | `/api/admin/warehouses` | 창고 목록 |
| POST | `/api/admin/warehouses` | 창고 등록 |
| GET | `/api/admin/warehouse-stocks` | 창고·상품명 조건 창고별 재고 목록 |
| POST | `/api/admin/warehouse-stocks/allocate` | 상품 총재고의 미배정 수량을 창고에 초기 배치 |
| GET | `/api/admin/stock-transfers` | 상태 조건 재고 이동 목록 |
| POST | `/api/admin/stock-transfers` | 단일 상품 재고 이동 요청 |
| PATCH | `/api/admin/stock-transfers/{id}/complete` | 출발 재고 차감과 도착 재고 증가 |

재고 이동 상태는 `PENDING`, `COMPLETED`다. 이동 완료는 동일 트랜잭션에서 창고 재고를 잠그고 처리하며 상품의 전체 재고 수량은 변경하지 않는다. 상세 정합성 규칙은 [창고 관리 기능 명세](./features/warehouse-management.md)를 따른다.

`GET /api/admin/warehouse-stocks` query:

| 이름 | 필수 | 설명 |
| --- | --- | --- |
| `warehouseId` | 아니오 | 특정 창고 재고만 조회 |
| `keyword` | 아니오 | 상품명 검색어 |
| `page` | 아니오 | 0부터 시작하는 페이지 번호 |
| `size` | 아니오 | 페이지 크기 |

`GET /api/admin/stock-transfers` query:

| 이름 | 필수 | 설명 |
| --- | --- | --- |
| `status` | 아니오 | `PENDING`, `COMPLETED` |
| `page` | 아니오 | 0부터 시작하는 페이지 번호 |
| `size` | 아니오 | 페이지 크기 |

## 24-1. 창고 재고 수량 의미

창고별 재고 응답은 다음 수량을 구분한다.

| 필드 | 의미 |
| --- | --- |
| `quantity` | 창고에 실제로 존재하는 수량 |
| `reservedQuantity` | 결제 완료 후 출고 대기 중인 수량 |
| `availableQuantity` | `quantity - reservedQuantity` |
| `totalProductStock` | 전체 창고를 합친 상품 가용 총재고 |

`POST /api/admin/inventory/inbound`와 `/adjust` 요청에는 `warehouseId`가 필수다. 입고는 상품 총재고와 목적 창고 실재고를 함께 증가시키고, 조정은 선택 창고의 목표 실재고와 상품 총재고의 차이를 함께 반영한다.

주문 결제 완료 시 창고별 가용 재고는 `reservedQuantity`로 예약된다. 배송 출고 시 `quantity`와 `reservedQuantity`가 함께 감소하고, 주문 취소는 예약을 해제하며, 반품 승인은 출고된 원창고 재고를 복구한다. 피킹/패킹/출고 자동화와 복잡한 WMS 정책은 v0.2 이후 범위다.

---

## v0.1.2 관리자 리뷰 API

관리자 리뷰 API는 JWT 인증이 필요하며 `ADMIN`, `SUPER_ADMIN` 권한만 접근할 수 있다.

### 관리자 리뷰 목록 조회

```http
GET /api/admin/reviews?rating={rating}&keyword={keyword}&page={page}&size={size}
```

Query Parameters:

| 이름 | 필수 | 설명 |
| --- | --- | --- |
| `rating` | 아니오 | 1~5 평점 필터 |
| `keyword` | 아니오 | 상품명, 작성자명, 리뷰 내용 검색어 |
| `page` | 아니오 | 0부터 시작하는 페이지 번호. 기본값 0 |
| `size` | 아니오 | 페이지 크기. 기본값 15 |

Response `data`:

```json
{
  "content": [
    {
      "reviewId": 1,
      "productId": 10,
      "productName": "Sample Product",
      "userName": "홍길동",
      "orderItemId": 100,
      "rating": 5,
      "content": "좋아요.",
      "createdAt": "2026-07-04T12:00:00"
    }
  ],
  "page": 0,
  "size": 15,
  "totalElements": 1,
  "totalPages": 1
}
```

### 관리자 리뷰 삭제

```http
DELETE /api/admin/reviews/{reviewId}
```

Response:

```json
{
  "success": true,
  "statusCode": 200,
  "message": "리뷰가 삭제되었습니다.",
  "data": null
}
```
