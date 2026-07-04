# Backend Structure

> 문서 상태: 초기 구조 설계안. 현재 코드에는 `shipment`, `returns`, `inquiry`, `accounting`, `warehouse` 도메인이 추가되어 있다. 최신 범위와 검증 상태는 [문서 안내](./README.md)에서 확인한다.

CommerceOps ERP의 백엔드 구조 문서입니다.

본 문서는 Spring Boot 기반으로 쇼핑몰 사용자 기능과 관리자 ERP 기능을 구현하기 위한 패키지 구조, 모듈 설계, 계층 분리, 트랜잭션 처리 기준을 정의합니다.

---

## 1. 백엔드 목표

CommerceOps ERP 백엔드는 다음 흐름을 안정적으로 처리하는 것을 목표로 합니다.

```text
상품 등록
→ 상품 판매
→ 장바구니 담기
→ 주문 생성
→ 결제 상태 처리
→ 재고 차감
→ 재고 로그 기록
→ 매출 집계
→ 관리자 대시보드 조회
```

핵심은 단순 CRUD가 아니라 **주문, 결제, 재고, 매출 데이터가 하나의 운영 흐름으로 연결되는 구조**를 만드는 것입니다.

---

## 2. 기술 스택

| 영역         | 기술                 |
| ---------- | ------------------ |
| Framework  | Spring Boot        |
| Language   | Java               |
| Build Tool | Gradle             |
| Database   | MySQL              |
| ORM        | Spring Data JPA    |
| Auth       | JWT                |
| Security   | Spring Security    |
| Validation | Bean Validation    |
| API 형식     | REST API           |
| 개발 환경      | WSL Ubuntu         |
| 배포 예정      | AWS EC2, RDS MySQL |

---

## 3. 전체 폴더 구조

```text
backend/
├── build.gradle
├── settings.gradle
├── Dockerfile
├── README.md
│
├── src/
│   ├── main/
│   │   ├── java/com/commerceops/erp/
│   │   │   ├── CommerceOpsApplication.java
│   │   │   │
│   │   │   ├── global/
│   │   │   │   ├── config/
│   │   │   │   ├── security/
│   │   │   │   ├── exception/
│   │   │   │   ├── response/
│   │   │   │   └── util/
│   │   │   │
│   │   │   ├── domain/
│   │   │   │   ├── auth/
│   │   │   │   ├── user/
│   │   │   │   ├── category/
│   │   │   │   ├── product/
│   │   │   │   ├── cart/
│   │   │   │   ├── order/
│   │   │   │   ├── payment/
│   │   │   │   ├── inventory/
│   │   │   │   ├── dashboard/
│   │   │   │   └── file/
│   │   │   │
│   │   │   └── admin/
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-local.yml
│   │       ├── application-prod.yml
│   │       └── db/
│   │           └── migration/
│   │
│   └── test/
```

---

## 4. 패키지 역할

| 패키지       | 역할                          |
| --------- | --------------------------- |
| global    | 공통 설정, 보안, 예외, 응답, 유틸       |
| domain    | 실제 비즈니스 도메인                 |
| admin     | 관리자 전용 API 진입점 또는 관리자 전용 기능 |
| resources | 설정 파일, DB 마이그레이션            |
| test      | 테스트 코드                      |

---

## 5. global 구조

```text
global/
├── config/
│   ├── JpaConfig.java
│   ├── WebConfig.java
│   └── CorsConfig.java
│
├── security/
│   ├── SecurityConfig.java
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   ├── CustomUserDetails.java
│   └── CustomUserDetailsService.java
│
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── BusinessException.java
│   └── ErrorCode.java
│
├── response/
│   ├── ApiResponse.java
│   └── ErrorResponse.java
│
└── util/
    ├── DateTimeUtil.java
    └── OrderNumberGenerator.java
```

---

## 6. global 모듈 설명

| 모듈        | 설명                         |
| --------- | -------------------------- |
| config    | JPA, CORS, Web 설정          |
| security  | JWT 인증, Spring Security 설정 |
| exception | 전역 예외 처리                   |
| response  | 공통 응답 포맷                   |
| util      | 주문번호 생성, 날짜 포맷 등 공통 유틸     |

---

## 7. 도메인별 기본 구조

각 도메인은 아래 구조를 기본으로 사용합니다.

```text
domain/product/
├── controller/
│   ├── ProductController.java
│   └── AdminProductController.java
│
├── service/
│   └── ProductService.java
│
├── repository/
│   └── ProductRepository.java
│
├── entity/
│   └── Product.java
│
├── dto/
│   ├── ProductCreateRequest.java
│   ├── ProductUpdateRequest.java
│   ├── ProductResponse.java
│   └── ProductListResponse.java
│
└── enums/
    └── ProductStatus.java
```

---

## 8. 계층별 역할

| 계층         | 역할            |
| ---------- | ------------- |
| Controller | HTTP 요청/응답 처리 |
| Service    | 비즈니스 로직 처리    |
| Repository | DB 접근         |
| Entity     | DB 테이블 매핑     |
| DTO        | 요청/응답 데이터 전달  |
| Enum       | 상태값 정의        |

---

## 9. 주요 도메인 모듈

```text
domain/
├── auth/
├── user/
├── category/
├── product/
├── cart/
├── order/
├── payment/
├── inventory/
├── dashboard/
└── file/
```

---

## 10. auth 모듈

인증과 로그인 처리를 담당합니다.

```text
auth/
├── controller/
│   └── AuthController.java
├── service/
│   └── AuthService.java
├── dto/
│   ├── SignupRequest.java
│   ├── LoginRequest.java
│   ├── LoginResponse.java
│   └── MeResponse.java
```

### 주요 기능

| 기능      | 설명                   |
| ------- | -------------------- |
| 회원가입    | 사용자 계정 생성            |
| 로그인     | 이메일/비밀번호 인증 후 JWT 발급 |
| 내 정보 조회 | 현재 로그인 사용자 정보 반환     |
| 로그아웃    | 클라이언트 토큰 제거 기준 처리    |

---

## 11. user 모듈

회원과 관리자 계정을 관리합니다.

```text
user/
├── controller/
│   └── UserController.java
├── service/
│   └── UserService.java
├── repository/
│   └── UserRepository.java
├── entity/
│   └── User.java
├── dto/
│   ├── UserResponse.java
│   └── CustomerResponse.java
└── enums/
    ├── UserRole.java
    └── UserStatus.java
```

### 주요 기능

| 기능       | 설명                             |
| -------- | ------------------------------ |
| 회원 조회    | 로그인 사용자 정보 조회                  |
| 고객 목록 조회 | 관리자 고객 관리 화면에서 사용              |
| 권한 확인    | USER / ADMIN 구분                |
| 계정 상태 확인 | ACTIVE / INACTIVE / BLOCKED 확인 |

---

## 12. category 모듈

상품 카테고리를 관리합니다.

```text
category/
├── controller/
│   ├── CategoryController.java
│   └── AdminCategoryController.java
├── service/
│   └── CategoryService.java
├── repository/
│   └── CategoryRepository.java
├── entity/
│   └── Category.java
└── dto/
    ├── CategoryCreateRequest.java
    └── CategoryResponse.java
```

### 주요 기능

| 기능         | 설명            |
| ---------- | ------------- |
| 카테고리 목록 조회 | 사용자 상품 필터에 사용 |
| 카테고리 등록    | 관리자 상품 분류 관리  |
| 카테고리 수정    | 관리자 분류명 변경    |
| 카테고리 삭제    | 미사용 카테고리 삭제   |

---

## 13. product 모듈

상품 조회와 관리자 상품 관리를 담당합니다.

```text
product/
├── controller/
│   ├── ProductController.java
│   └── AdminProductController.java
├── service/
│   └── ProductService.java
├── repository/
│   └── ProductRepository.java
├── entity/
│   └── Product.java
├── dto/
│   ├── ProductCreateRequest.java
│   ├── ProductUpdateRequest.java
│   ├── ProductResponse.java
│   └── ProductListResponse.java
└── enums/
    └── ProductStatus.java
```

### 주요 기능

| 기능       | 설명                          |
| -------- | --------------------------- |
| 상품 목록 조회 | 사용자 쇼핑몰 상품 목록               |
| 상품 상세 조회 | 상품 상세 페이지                   |
| 상품 등록    | 관리자 상품 등록                   |
| 상품 수정    | 관리자 상품 수정                   |
| 상품 삭제    | 실제 삭제 또는 DELETED 상태 처리      |
| 판매 상태 변경 | ON_SALE / SOLD_OUT / HIDDEN |

---

## 14. cart 모듈

사용자 장바구니를 담당합니다.

```text
cart/
├── controller/
│   └── CartController.java
├── service/
│   └── CartService.java
├── repository/
│   └── CartRepository.java
├── entity/
│   └── Cart.java
└── dto/
    ├── CartAddRequest.java
    ├── CartUpdateRequest.java
    ├── CartItemResponse.java
    └── CartResponse.java
```

### 주요 기능

| 기능       | 설명               |
| -------- | ---------------- |
| 장바구니 조회  | 로그인 사용자 장바구니 조회  |
| 상품 추가    | 상품 ID와 수량으로 추가   |
| 수량 변경    | 장바구니 상품 수량 수정    |
| 상품 삭제    | 장바구니에서 제거        |
| 중복 상품 처리 | 이미 담긴 상품이면 수량 증가 |

---

## 15. order 모듈

주문 생성과 주문 상태 관리를 담당합니다.

```text
order/
├── controller/
│   ├── OrderController.java
│   └── AdminOrderController.java
├── service/
│   └── OrderService.java
├── repository/
│   ├── OrderRepository.java
│   └── OrderItemRepository.java
├── entity/
│   ├── Order.java
│   └── OrderItem.java
├── dto/
│   ├── OrderCreateRequest.java
│   ├── OrderResponse.java
│   ├── OrderDetailResponse.java
│   ├── AdminOrderResponse.java
│   └── OrderStatusUpdateRequest.java
└── enums/
    └── OrderStatus.java
```

### 주요 기능

| 기능        | 설명                |
| --------- | ----------------- |
| 주문 생성     | 장바구니 상품 기준 주문 생성  |
| 주문 내역 조회  | 사용자 본인 주문 목록      |
| 주문 상세 조회  | 주문 상품, 배송지, 상태 조회 |
| 관리자 주문 목록 | 전체 주문 관리          |
| 주문 상태 변경  | 관리자 배송/완료 상태 변경   |

---

## 16. payment 모듈

결제 상태를 관리합니다.

MVP에서는 실제 PG 연동 전까지 모의 결제를 사용합니다.

```text
payment/
├── controller/
│   └── PaymentController.java
├── service/
│   └── PaymentService.java
├── repository/
│   └── PaymentRepository.java
├── entity/
│   └── Payment.java
├── dto/
│   ├── MockPaymentCompleteRequest.java
│   └── PaymentResponse.java
└── enums/
    ├── PaymentMethod.java
    └── PaymentStatus.java
```

### 주요 기능

| 기능       | 설명                                |
| -------- | --------------------------------- |
| 결제 정보 생성 | 주문 생성 시 READY 상태 결제 정보 생성         |
| 모의 결제 완료 | 결제 완료 처리                          |
| 결제 상태 변경 | READY / PAID / FAILED / CANCELLED |
| 거래 ID 생성 | MOCK 거래 ID 생성                     |

---

## 17. inventory 모듈

재고 관리와 재고 로그를 담당합니다.

```text
inventory/
├── controller/
│   └── AdminInventoryController.java
├── service/
│   └── InventoryService.java
├── repository/
│   └── InventoryLogRepository.java
├── entity/
│   └── InventoryLog.java
├── dto/
│   ├── InventoryResponse.java
│   ├── InventoryInboundRequest.java
│   ├── InventoryAdjustRequest.java
│   └── InventoryLogResponse.java
└── enums/
    └── InventoryLogType.java
```

### 주요 기능

| 기능          | 설명             |
| ----------- | -------------- |
| 재고 목록 조회    | 관리자 재고 관리      |
| 입고 처리       | 상품 재고 증가       |
| 재고 조정       | 관리자 직접 재고 변경   |
| 주문 재고 차감    | 결제 완료 시 재고 차감  |
| 주문 취소 재고 복구 | 취소 시 재고 복구     |
| 재고 로그 기록    | 모든 재고 변경 이력 저장 |

---

## 18. dashboard 모듈

관리자 대시보드를 담당합니다.

```text
dashboard/
├── controller/
│   └── AdminDashboardController.java
├── service/
│   └── DashboardService.java
└── dto/
    ├── DashboardSummaryResponse.java
    ├── SalesResponse.java
    ├── LowStockProductResponse.java
    └── TopProductResponse.java
```

### 주요 기능

| 기능       | 설명                |
| -------- | ----------------- |
| 요약 통계    | 총 매출, 오늘 매출, 주문 수 |
| 매출 통계    | 일별/월별 매출          |
| 재고 부족 상품 | 일정 수량 이하 상품 조회    |
| 인기 상품    | 주문 수량 기준 인기 상품    |

---

## 19. file 모듈

상품 이미지 업로드를 담당합니다.

MVP에서는 서버 로컬 저장 방식을 사용하고, 추후 AWS S3로 확장할 수 있습니다.

```text
file/
├── controller/
│   └── AdminFileController.java
├── service/
│   └── FileService.java
└── dto/
    └── FileUploadResponse.java
```

### 주요 기능

| 기능         | 설명                     |
| ---------- | ---------------------- |
| 상품 이미지 업로드 | multipart/form-data 처리 |
| 파일명 생성     | 중복 방지 파일명 생성           |
| 파일 URL 반환  | 상품 등록 시 imageUrl로 사용   |
| S3 확장      | 2차 배포 시 S3 업로드로 전환 가능  |

---

## 20. 도메인 의존 관계

```text
auth
 └── user

product
 └── category

cart
 ├── user
 └── product

order
 ├── user
 ├── cart
 ├── product
 └── payment

payment
 ├── order
 └── inventory

inventory
 └── product

dashboard
 ├── order
 ├── payment
 ├── product
 └── inventory
```

---

## 21. 핵심 트랜잭션

아래 기능은 반드시 `@Transactional`로 처리합니다.

| 기능    | 이유                                                  |
| ----- | --------------------------------------------------- |
| 주문 생성 | orders, order_items, payments, carts 삭제가 함께 처리되어야 함 |
| 결제 완료 | 결제 상태, 주문 상태, 재고 차감, 재고 로그가 함께 처리되어야 함              |
| 주문 취소 | 주문 상태, 결제 상태, 재고 복구, 재고 로그가 함께 처리되어야 함              |
| 입고 처리 | 상품 재고 증가와 재고 로그 저장이 함께 처리되어야 함                      |
| 재고 조정 | 상품 재고 변경과 재고 로그 저장이 함께 처리되어야 함                      |

---

## 22. 주문 생성 처리 흐름

```text
POST /api/orders
→ 로그인 사용자 확인
→ 장바구니 상품 조회
→ 상품 판매 상태 확인
→ 상품 재고 확인
→ 주문 총액 계산
→ orders 저장
→ order_items 저장
→ payments READY 저장
→ 선택된 cart 삭제
→ 주문 생성 응답
```

---

## 23. 결제 완료 처리 흐름

```text
POST /api/payments/mock/complete
→ 주문 조회
→ 결제 정보 조회
→ 주문 상품 조회
→ 상품별 재고 확인
→ 상품별 재고 차감
→ inventory_logs ORDER 저장
→ payments.payment_status = PAID
→ orders.status = PAID
→ orders.payment_status = PAID
→ 결제 완료 응답
```

---

## 24. 재고 입고 처리 흐름

```text
POST /api/admin/inventory/inbound
→ 관리자 권한 확인
→ 상품 조회
→ 기존 재고 확인
→ 입고 수량만큼 재고 증가
→ inventory_logs INBOUND 저장
→ 입고 처리 응답
```

---

## 25. 재고 조정 처리 흐름

```text
POST /api/admin/inventory/adjust
→ 관리자 권한 확인
→ 상품 조회
→ 기존 재고 확인
→ 입력한 수량으로 재고 변경
→ inventory_logs ADJUST 저장
→ 재고 조정 응답
```

---

## 26. 공통 응답 형식

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

## 27. 공통 에러 코드

| Error Code            | 설명        |
| --------------------- | --------- |
| INVALID_REQUEST       | 잘못된 요청    |
| UNAUTHORIZED          | 인증 실패     |
| FORBIDDEN             | 접근 권한 없음  |
| NOT_FOUND             | 리소스 없음    |
| DUPLICATED_EMAIL      | 이메일 중복    |
| INVALID_PASSWORD      | 비밀번호 불일치  |
| OUT_OF_STOCK          | 재고 부족     |
| INVALID_ORDER_STATUS  | 잘못된 주문 상태 |
| PAYMENT_FAILED        | 결제 실패     |
| INTERNAL_SERVER_ERROR | 서버 오류     |

---

## 28. 예외 처리 구조

```text
exception/
├── GlobalExceptionHandler.java
├── BusinessException.java
└── ErrorCode.java
```

### BusinessException 예시

```java
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
```

---

## 29. ErrorCode 예시

```java
public enum ErrorCode {

    INVALID_REQUEST(400, "INVALID_REQUEST", "잘못된 요청입니다."),
    UNAUTHORIZED(401, "UNAUTHORIZED", "인증이 필요합니다."),
    FORBIDDEN(403, "FORBIDDEN", "접근 권한이 없습니다."),
    NOT_FOUND(404, "NOT_FOUND", "리소스를 찾을 수 없습니다."),
    OUT_OF_STOCK(409, "OUT_OF_STOCK", "재고가 부족합니다."),
    INTERNAL_SERVER_ERROR(500, "INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다.");

    private final int statusCode;
    private final String code;
    private final String message;
}
```

---

## 30. 보안 구조

```text
security/
├── SecurityConfig.java
├── JwtTokenProvider.java
├── JwtAuthenticationFilter.java
├── CustomUserDetails.java
└── CustomUserDetailsService.java
```

---

## 31. 인증 정책

| API      | 인증          |
| -------- | ----------- |
| 상품 목록 조회 | 불필요         |
| 상품 상세 조회 | 불필요         |
| 회원가입     | 불필요         |
| 로그인      | 불필요         |
| 장바구니     | 필요          |
| 주문       | 필요          |
| 마이페이지    | 필요          |
| 관리자 API  | ADMIN 권한 필요 |

---

## 32. 권한 정책

| 권한          | 설명                   |
| ----------- | -------------------- |
| USER        | 상품 구매 가능             |
| ADMIN       | 상품, 주문, 재고, 매출 관리 가능 |
| SUPER_ADMIN | 관리자 계정 관리 가능, 2차 확장  |

MVP에서는 `USER`, `ADMIN`만 우선 구현합니다.

---

## 33. 환경 설정 파일

```text
resources/
├── application.yml
├── application-local.yml
└── application-prod.yml
```

---

## 34. application-local.yml 예시

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/commerceops
    username: commerce
    password: commerce1234
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: true
    show-sql: true

jwt:
  secret: local-development-secret-key
  access-token-expiration: 3600000

file:
  upload-dir: ./uploads
```

---

## 35. application-prod.yml 예시

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: false
    show-sql: false

jwt:
  secret: ${JWT_SECRET}
  access-token-expiration: 3600000

file:
  upload-dir: ${UPLOAD_DIR}
```

---

## 36. 환경변수 예시

```env
DB_HOST=localhost
DB_PORT=3306
DB_NAME=commerceops
DB_USERNAME=commerce
DB_PASSWORD=commerce1234
JWT_SECRET=change-this-secret-key
UPLOAD_DIR=./uploads
```

---

## 37. Gradle 의존성 예시

```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'

    runtimeOnly 'com.mysql:mysql-connector-j'

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

JWT 라이브러리는 구현 방식에 따라 추가합니다.

---

## 38. 실행 방법

```bash
cd backend
./gradlew bootRun
```

기본 실행 주소:

```text
http://localhost:8080
```

---

## 39. 빌드 방법

```bash
cd backend
./gradlew clean build
```

빌드 결과:

```text
backend/build/libs/*.jar
```

---

## 40. JAR 실행

```bash
java -jar build/libs/commerceops-erp-0.0.1-SNAPSHOT.jar
```

운영 프로필 실행:

```bash
java -jar build/libs/commerceops-erp-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

---

## 41. API 개발 우선순위

### 1순위: 인증 + 상품

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

---

### 2순위: 장바구니 + 주문

```text
GET    /api/cart
POST   /api/cart
PATCH  /api/cart/{cartId}
DELETE /api/cart/{cartId}

POST /api/orders
GET  /api/orders
GET  /api/orders/{orderId}
```

---

### 3순위: 결제 + 재고

```text
POST /api/payments/mock/complete

GET  /api/admin/inventory
POST /api/admin/inventory/inbound
POST /api/admin/inventory/adjust
GET  /api/admin/inventory/logs
```

---

### 4순위: 관리자 대시보드

```text
GET /api/admin/dashboard/summary
GET /api/admin/dashboard/sales
GET /api/admin/dashboard/low-stock
GET /api/admin/dashboard/top-products
```

---

## 42. 테스트 전략

### 단위 테스트

| 대상               | 설명             |
| ---------------- | -------------- |
| AuthService      | 회원가입, 로그인 검증   |
| ProductService   | 상품 등록, 수정, 삭제  |
| CartService      | 장바구니 추가, 수량 변경 |
| OrderService     | 주문 생성          |
| PaymentService   | 결제 완료 처리       |
| InventoryService | 재고 차감, 입고, 조정  |

---

### 통합 테스트

| 대상        | 설명                                  |
| --------- | ----------------------------------- |
| 주문 생성     | orders, order_items, payments 생성 확인 |
| 결제 완료     | 재고 차감, 재고 로그, 결제 상태 변경 확인           |
| 주문 취소     | 재고 복구, 주문 상태 변경 확인                  |
| 관리자 상품 등록 | ADMIN 권한 확인                         |
| 관리자 대시보드  | 매출/주문/재고 집계 확인                      |

---

## 43. 배포 구조

### 로컬 개발

```text
WSL Ubuntu
├── Spring Boot :8080
└── MySQL Docker :3306
```

---

### 운영 배포 예정

```text
AWS EC2
└── Spring Boot :8080

AWS RDS
└── MySQL :3306
```

Nginx를 사용할 경우:

```text
사용자
→ Nginx
→ /api
→ Spring Boot
→ RDS MySQL
```

---

## 44. 백엔드 개발 핵심 정리

```text
1. Spring Boot 단일 서버로 시작한다.
2. 도메인별 패키지를 명확히 분리한다.
3. Controller, Service, Repository, Entity, DTO 계층을 유지한다.
4. 주문, 결제, 재고 처리는 반드시 트랜잭션으로 묶는다.
5. 재고 변경 시 inventory_logs에 반드시 이력을 남긴다.
6. 사용자 API와 관리자 API를 분리한다.
7. 관리자 API는 ADMIN 권한을 필수로 검사한다.
8. MVP에서는 모의 결제를 사용한다.
9. 실제 배포 시 application-prod.yml과 환경변수로 DB 정보를 분리한다.
10. 나중에 AWS RDS, S3, 실제 PG 연동으로 확장한다.
```
