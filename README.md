# CommerceOps ERP

쇼핑몰 사용자 기능과 내부 관리자 ERP 기능을 하나로 연결한 **쇼핑몰 운영 통합 관리 시스템**입니다.

상품 등록, 상품 판매, 주문 생성, 재고 차감, 매출 집계, 관리자 확인까지 이어지는 실제 쇼핑몰 운영 흐름을 하나의 웹 서비스로 구현하는 것을 목표로 합니다.

> 최신 구현 현황, 제품 로드맵과 버전별 체크리스트는 [docs/README.md](./docs/README.md)에서 시작합니다.

---

## 1. 프로젝트 개요

| 항목      | 내용                                   |
| ------- | ------------------------------------ |
| 프로젝트명   | CommerceOps ERP                      |
| 프로젝트 형태 | 쇼핑몰 + 내부 관리자 ERP 통합 시스템              |
| 주요 사용자  | 일반 구매자, 쇼핑몰 관리자, 운영 담당자              |
| 핵심 기능   | 상품 구매, 주문 처리, 재고 관리, 매출 통계, 관리자 대시보드 |
| 개발 방향   | 단순 쇼핑몰이 아닌 운영 관리가 가능한 커머스 ERP 시스템    |

---

## 2. 프로젝트 목적

일반적인 쇼핑몰은 상품 판매 기능에 집중되어 있지만, 실제 쇼핑몰 운영에서는 상품 등록, 재고 관리, 주문 확인, 배송 상태 변경, 매출 확인, 고객 관리 등 다양한 운영 업무가 필요합니다.

CommerceOps ERP는 사용자가 상품을 구매하는 쇼핑몰 기능과 관리자가 운영 데이터를 관리하는 ERP 기능을 하나로 연결하여, 실제 쇼핑몰 운영 흐름을 반영한 시스템을 구현합니다.

---

## 3. 핵심 비즈니스 흐름

```text
상품 등록
→ 상품 판매
→ 주문 생성
→ 결제 상태 처리
→ 재고 자동 차감
→ 재고 로그 기록
→ 매출 집계
→ 관리자 대시보드 확인
```

### 사용자 주문 흐름

```text
사용자 로그인
→ 상품 목록 조회
→ 상품 상세 확인
→ 장바구니 담기
→ 주문 생성
→ 결제 대기 또는 결제 완료 처리
→ 재고 자동 차감
→ 주문 내역 확인
```

### 관리자 운영 흐름

```text
관리자 로그인
→ 상품 등록/수정/삭제
→ 주문 확인
→ 주문 상태 변경
→ 재고 입고/조정
→ 매출 및 재고 현황 확인
```

---

## 4. 주요 기능

### 4-1. 사용자 쇼핑몰 기능

| 기능         | 설명                       |
| ---------- | ------------------------ |
| 회원가입 / 로그인 | JWT 기반 사용자 인증            |
| 상품 목록 조회   | 카테고리별 상품 조회, 검색, 정렬      |
| 상품 상세 조회   | 상품 이미지, 가격, 설명, 재고 상태 확인 |
| 장바구니       | 상품 담기, 수량 변경, 삭제         |
| 주문 생성      | 배송지 입력 후 주문 생성           |
| 주문 내역      | 사용자가 본인의 주문 상태 확인        |
| 마이페이지      | 회원 정보 및 주문 이력 확인         |

---

### 4-2. 관리자 ERP 기능

| 기능       | 설명                       |
| -------- | ------------------------ |
| 관리자 로그인  | 일반 사용자와 관리자 권한 분리        |
| 상품 관리    | 상품 등록, 수정, 삭제, 판매 상태 변경  |
| 카테고리 관리  | 상품 분류 관리                 |
| 재고 관리    | 현재 재고 확인, 입고/출고 처리       |
| 주문 관리    | 주문 목록 확인, 주문 상태 변경       |
| 고객 관리    | 회원 목록 및 고객별 주문 내역 확인     |
| 매출 대시보드  | 일별/월별 매출, 주문 수, 인기 상품 확인 |
| 재고 부족 알림 | 일정 수량 이하 상품을 관리자에게 표시    |
| 재고 로그    | 입고, 출고, 주문 차감 이력 기록      |

---

## 5. MVP 범위

### 1차 MVP 필수 기능

| 구분   | 기능                     |
| ---- | ---------------------- |
| 인증   | 회원가입, 로그인, 관리자 권한      |
| 상품   | 상품 등록, 수정, 삭제, 목록, 상세  |
| 장바구니 | 상품 담기, 수량 변경, 삭제       |
| 주문   | 주문 생성, 주문 내역, 주문 상태 관리 |
| 재고   | 주문 시 재고 차감, 관리자 재고 수정  |
| 관리자  | 상품 관리, 주문 관리, 재고 관리    |
| 대시보드 | 총 매출, 주문 수, 재고 부족 상품   |

---

### 2차 확장 기능

| 기능       | 설명                     |
| -------- | ---------------------- |
| 실제 결제 연동 | 토스페이먼츠 또는 포트원 연동       |
| 쿠폰 / 할인  | 쿠폰 발급, 주문 할인           |
| 리뷰       | 상품 리뷰 작성               |
| 배송 관리    | 송장번호, 배송 상태 관리         |
| 엑셀 다운로드  | 주문/매출/재고 데이터 다운로드      |
| 알림 기능    | 재고 부족, 신규 주문 알림        |
| 권한 세분화   | 최고관리자, 상품관리자, 주문관리자 분리 |

---

## 6. 기술 스택

| 영역       | 기술                                |
| -------- | --------------------------------- |
| Frontend | Next.js, TypeScript, Tailwind CSS |
| Backend  | Spring Boot                       |
| Database | MySQL                             |
| ORM      | JPA                               |
| 인증       | JWT                               |
| 개발 환경    | WSL Ubuntu                        |
| 배포 예정    | AWS EC2, RDS MySQL                |
| 결제       | 1차 모의 결제, 2차 실제 결제 연동             |

---

## 7. 프로젝트 폴더 구조

```text
commerceops-erp/
├── frontend/                 # Next.js 사용자/관리자 화면
├── backend/                  # Spring Boot API 서버
├── database/                 # ERD, SQL, 초기 데이터
├── infra/                    # Docker, Nginx, 배포 설정
├── docs/                     # 기획서, API 명세, 화면 설계
└── README.md
```

---

## 8. Frontend 구조

```text
frontend/
├── src/
│   ├── app/
│   │   ├── page.tsx
│   │   ├── products/
│   │   │   ├── page.tsx
│   │   │   └── [id]/page.tsx
│   │   ├── cart/page.tsx
│   │   ├── orders/
│   │   │   ├── page.tsx
│   │   │   └── checkout/page.tsx
│   │   ├── mypage/page.tsx
│   │   ├── login/page.tsx
│   │   ├── signup/page.tsx
│   │   └── admin/
│   │       ├── page.tsx
│   │       ├── products/page.tsx
│   │       ├── products/new/page.tsx
│   │       ├── orders/page.tsx
│   │       ├── inventory/page.tsx
│   │       ├── customers/page.tsx
│   │       └── sales/page.tsx
│   │
│   ├── components/
│   │   ├── shop/
│   │   ├── admin/
│   │   └── common/
│   │
│   ├── features/
│   │   ├── auth/
│   │   ├── product/
│   │   ├── cart/
│   │   ├── order/
│   │   ├── inventory/
│   │   └── dashboard/
│   │
│   ├── lib/
│   ├── types/
│   └── styles/
```

---

## 9. Backend 구조

```text
backend/
├── src/
│   ├── main/
│   │   ├── java/com/commerceops/erp/
│   │   │   ├── CommerceOpsApplication.java
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
│   │       └── application-prod.yml
│   │
│   └── test/
```

---

## 10. 백엔드 주요 모듈

| 모듈        | 역할                       |
| --------- | ------------------------ |
| auth      | 회원가입, 로그인, JWT 발급, 인증 확인 |
| user      | 회원 정보, 권한 관리             |
| category  | 상품 카테고리 관리               |
| product   | 상품 등록, 수정, 삭제, 조회        |
| cart      | 장바구니 담기, 수량 변경, 삭제       |
| order     | 주문 생성, 주문 내역, 주문 상태 관리   |
| payment   | 모의 결제, 결제 상태 관리          |
| inventory | 재고 차감, 입고, 조정, 재고 로그     |
| dashboard | 매출, 주문, 재고 부족 상품 통계      |
| file      | 상품 이미지 업로드               |
| admin     | 관리자 전용 API 관리            |

---

## 11. 데이터베이스 설계 초안

### 주요 테이블

```text
users
products
categories
carts
orders
order_items
inventory_logs
payments
```

### users

| 컬럼         | 설명                         |
| ---------- | -------------------------- |
| id         | 회원 ID                      |
| email      | 이메일                        |
| password   | 암호화된 비밀번호                  |
| name       | 이름                         |
| phone      | 연락처                        |
| role       | USER / ADMIN / SUPER_ADMIN |
| status     | 계정 상태                      |
| created_at | 생성일                        |
| updated_at | 수정일                        |

### products

| 컬럼             | 설명      |
| -------------- | ------- |
| id             | 상품 ID   |
| category_id    | 카테고리 ID |
| name           | 상품명     |
| description    | 상품 설명   |
| price          | 판매가     |
| stock_quantity | 현재 재고   |
| image_url      | 상품 이미지  |
| status         | 판매 상태   |
| created_at     | 생성일     |
| updated_at     | 수정일     |

### orders

| 컬럼             | 설명      |
| -------------- | ------- |
| id             | 주문 ID   |
| user_id        | 주문자 ID  |
| order_number   | 주문번호    |
| total_price    | 총 주문 금액 |
| status         | 주문 상태   |
| receiver_name  | 수령인     |
| receiver_phone | 수령인 연락처 |
| address        | 배송지     |
| payment_status | 결제 상태   |
| created_at     | 생성일     |
| updated_at     | 수정일     |

### inventory_logs

| 컬럼           | 설명                                           |
| ------------ | -------------------------------------------- |
| id           | 재고 로그 ID                                     |
| product_id   | 상품 ID                                        |
| type         | INBOUND / OUTBOUND / ORDER / CANCEL / ADJUST |
| quantity     | 변경 수량                                        |
| before_stock | 변경 전 재고                                      |
| after_stock  | 변경 후 재고                                      |
| memo         | 메모                                           |
| created_at   | 생성일                                          |

---

## 12. 주문 상태

| 상태        | 설명           |
| --------- | ------------ |
| PENDING   | 주문 생성, 결제 대기 |
| PAID      | 결제 완료        |
| PREPARING | 상품 준비 중      |
| SHIPPING  | 배송 중         |
| COMPLETED | 배송 완료        |
| CANCELLED | 주문 취소        |
| REFUNDED  | 환불 완료        |

MVP에서는 아래 상태를 우선 사용합니다.

```text
PENDING
PAID
SHIPPING
COMPLETED
```

---

## 13. 재고 처리 규칙

| 상황       | 처리           |
| -------- | ------------ |
| 상품 주문 생성 | 재고 수량 확인     |
| 결제 완료    | 재고 차감        |
| 주문 취소    | 재고 복구        |
| 관리자 입고   | 재고 증가        |
| 관리자 조정   | 재고 수량 직접 변경  |
| 재고 부족    | 관리자 대시보드에 표시 |

재고 변경 시 `products.stock_quantity`만 수정하지 않고, 반드시 `inventory_logs`에 이력을 남깁니다.

---

## 14. API 설계 초안

### 인증 API

```http
POST /api/auth/signup
POST /api/auth/login
GET  /api/auth/me
POST /api/auth/logout
```

### 상품 API

```http
GET    /api/products
GET    /api/products/{productId}
POST   /api/admin/products
PATCH  /api/admin/products/{productId}
DELETE /api/admin/products/{productId}
```

### 장바구니 API

```http
GET    /api/cart
POST   /api/cart
PATCH  /api/cart/{cartId}
DELETE /api/cart/{cartId}
```

### 주문 API

```http
POST  /api/orders
GET   /api/orders
GET   /api/orders/{orderId}
PATCH /api/admin/orders/{orderId}/status
```

### 재고 API

```http
GET  /api/admin/inventory
POST /api/admin/inventory/inbound
POST /api/admin/inventory/adjust
GET  /api/admin/inventory/logs
```

### 대시보드 API

```http
GET /api/admin/dashboard/summary
GET /api/admin/dashboard/sales
GET /api/admin/dashboard/low-stock
GET /api/admin/dashboard/top-products
```

---

## 15. 화면 구성

### 사용자 화면

| 경로               | 화면     |
| ---------------- | ------ |
| `/`              | 메인 페이지 |
| `/products`      | 상품 목록  |
| `/products/[id]` | 상품 상세  |
| `/cart`          | 장바구니   |
| `/orders`        | 주문 내역  |
| `/orders/[id]`   | 주문 상세  |
| `/mypage`        | 마이페이지  |

### 관리자 화면

| 경로                    | 화면       |
| --------------------- | -------- |
| `/admin`              | 관리자 대시보드 |
| `/admin/products`     | 상품 관리    |
| `/admin/products/new` | 상품 등록    |
| `/admin/orders`       | 주문 관리    |
| `/admin/inventory`    | 재고 관리    |
| `/admin/customers`    | 고객 관리    |
| `/admin/sales`        | 매출 통계    |

---

## 16. UI 방향

### 사용자 쇼핑몰 UI

사용자 화면은 패션 쇼핑몰 스타일의 깔끔한 커머스 UI를 지향합니다.

* 흰 배경 중심
* 얇은 회색 라인
* 넓은 여백
* 상품 이미지 중심 그리드
* BEST / NEW / SALE 카테고리 구성
* 장바구니와 주문 흐름이 명확한 구조

### 관리자 ERP UI

관리자 화면은 실무형 백오피스 UI를 지향합니다.

* 좌측 사이드바
* 상단 관리자 바
* 카드형 통계 영역
* 테이블 중심 데이터 관리
* 상품, 주문, 재고, 매출 메뉴 분리

---

## 17. 로컬 개발 환경

이 프로젝트는 우선 WSL Ubuntu 환경에서 개발합니다.

```text
Windows
└── WSL Ubuntu
    ├── frontend - Next.js
    ├── backend - Spring Boot
    └── database - MySQL Docker
```

---

## 18. 실행 방법

### 18-1. 프로젝트 클론

```bash
git clone https://github.com/your-name/commerceops-erp.git
cd commerceops-erp
```

---

### 18-2. MySQL 실행

```bash
cd infra
docker compose -f docker-compose.local.yml up -d
```

---

### 18-3. Backend 실행

```bash
cd backend
./gradlew bootRun
```

백엔드 기본 주소:

```text
http://localhost:8080
```

---

### 18-4. Frontend 실행

```bash
cd frontend
npm install
npm run dev
```

프론트엔드 기본 주소:

```text
http://localhost:3000
```

---

## 19. 환경변수 예시

### Backend

```env
DB_HOST=localhost
DB_PORT=3306
DB_NAME=commerceops
DB_USERNAME=commerce
DB_PASSWORD=commerce1234
JWT_SECRET=change-this-secret-key
UPLOAD_DIR=./uploads
```

### Frontend

```env
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/api
```

---

## 20. Docker Compose 예시

```yaml
services:
  mysql:
    image: mysql:8.0
    container_name: commerceops-mysql
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: root1234
      MYSQL_DATABASE: commerceops
      MYSQL_USER: commerce
      MYSQL_PASSWORD: commerce1234
    volumes:
      - mysql_data:/var/lib/mysql

volumes:
  mysql_data:
```

---

## 21. 배포 계획

### 1차 개발/시연

```text
WSL Ubuntu
├── Next.js
├── Spring Boot
└── MySQL Docker
```

### 최종 배포 예정

```text
AWS EC2
├── Nginx
├── Next.js
└── Spring Boot

AWS RDS
└── MySQL
```

### 배포 구조

```text
사용자
→ Nginx
→ Frontend
→ Backend API
→ RDS MySQL
```

---

## 22. 개발 일정

### 4주 MVP 기준

| 주차  | 작업                           |
| --- | ---------------------------- |
| 1주차 | 기획 확정, DB 설계, 프로젝트 세팅, 인증 구현 |
| 2주차 | 상품 CRUD, 상품 목록/상세, 관리자 상품 관리 |
| 3주차 | 장바구니, 주문 생성, 주문 내역, 주문 상태 관리 |
| 4주차 | 재고 관리, 대시보드, 통계, UI 정리, 배포   |

### 6주 안정화 기준

| 주차  | 작업                      |
| --- | ----------------------- |
| 1주차 | 요구사항 정의, 화면 설계, DB 설계   |
| 2주차 | 인증, 권한, 사용자/관리자 레이아웃    |
| 3주차 | 상품, 카테고리, 이미지 관리        |
| 4주차 | 장바구니, 주문, 결제 상태 처리      |
| 5주차 | 재고, 주문 관리, 매출 통계        |
| 6주차 | 테스트, 오류 처리, 배포, 발표자료 정리 |

---

## 23. 차별화 포인트

| 차별화 요소      | 설명                        |
| ----------- | ------------------------- |
| 주문-재고 자동 연동 | 주문 발생 시 재고 자동 차감          |
| 재고 이력 관리    | 입고, 출고, 주문 차감 로그 저장       |
| 관리자 대시보드    | 매출, 주문, 재고 상태 시각화         |
| 운영 중심 구조    | 실제 쇼핑몰 운영자가 쓰는 ERP 흐름 반영  |
| 데이터 기반 관리   | 인기 상품, 매출 추이, 재고 부족 상품 확인 |
| 권한 분리       | 사용자와 관리자의 접근 권한 분리        |

---

## 24. 기대 효과

이 프로젝트를 통해 다음 역량을 보여줄 수 있습니다.

| 역량       | 설명                        |
| -------- | ------------------------- |
| 서비스 기획   | 쇼핑몰 운영 흐름 기반 기능 설계        |
| DB 설계    | 주문, 상품, 재고, 결제 관계 설계      |
| 백엔드 개발   | 인증, 권한, 주문 처리, 재고 트랜잭션 구현 |
| 프론트엔드 개발 | 사용자 쇼핑몰과 관리자 ERP 화면 구현    |
| 데이터 처리   | 매출, 주문, 재고 데이터를 집계하고 시각화  |
| 실무 이해도   | 실제 쇼핑몰 운영 업무를 반영한 시스템 구현  |

---

## 25. 향후 개선 사항

* 실제 결제 연동
* 쿠폰 및 할인 정책
* 상품 리뷰
* 배송 송장 관리
* 관리자 권한 세분화
* 주문/매출/재고 엑셀 다운로드
* 상품 이미지 S3 업로드
* AWS EC2 + RDS 기반 배포
* 관리자 알림 기능
* 테스트 코드 작성
