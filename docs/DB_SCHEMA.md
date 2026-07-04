# DB Schema

> 문서 상태: 초기 설계안 + 현재 구현 보강 중  
> 현재 코드에는 아래 기존 설계 외에 `shipments`, `return_requests`, `inquiries`, `accounting_entries`가 추가되어 있다. 실제 완료 상태는 [CURRENT_STATE.md](./CURRENT_STATE.md)를 확인한다.

CommerceOps ERP의 데이터베이스 설계 문서입니다.

본 문서는 쇼핑몰 사용자 기능과 관리자 ERP 기능을 연결하기 위한 MySQL 기반 테이블 구조를 정의합니다.

---

## 1. 설계 목표

CommerceOps ERP의 DB 설계 목표는 다음과 같습니다.

```text id="h15cuu"
상품 등록
→ 상품 판매
→ 주문 생성
→ 결제 상태 처리
→ 재고 차감
→ 재고 로그 기록
→ 매출 집계
→ 관리자 대시보드 확인
```

핵심은 단순히 상품과 주문만 저장하는 것이 아니라, **주문 발생 시 재고와 매출 데이터가 자동으로 연결되는 구조**를 만드는 것입니다.

---

## 2. 사용 DB

| 항목        | 내용                 |
| --------- | ------------------ |
| DBMS      | MySQL 8.0          |
| Charset   | utf8mb4            |
| Collation | utf8mb4_unicode_ci |
| ORM       | JPA                |
| 초기 개발 환경  | WSL Docker MySQL   |
| 배포 예정 환경  | AWS RDS MySQL      |

---

## 3. 주요 테이블 목록

| 테이블            | 설명          |
| -------------- | ----------- |
| users          | 회원 및 관리자 정보 |
| categories     | 상품 카테고리     |
| products       | 상품 정보       |
| carts          | 장바구니        |
| orders         | 주문 기본 정보    |
| order_items    | 주문 상품 상세    |
| payments       | 결제 정보       |
| inventory_logs | 재고 변경 이력    |

---

## 4. ERD 관계 요약

```text id="n81pj3"
users 1 ─── N carts
users 1 ─── N orders

categories 1 ─── N products

products 1 ─── N carts
products 1 ─── N order_items
products 1 ─── N inventory_logs

orders 1 ─── N order_items
orders 1 ─── 1 payments
```

---

## 5. 테이블 상세 설계

---

# 5-1. users

회원과 관리자 계정을 저장하는 테이블입니다.

## 컬럼

| 컬럼명        | 타입           | 제약조건               | 설명                        |
| ---------- | ------------ | ------------------ | ------------------------- |
| id         | BIGINT       | PK, AUTO_INCREMENT | 회원 ID                     |
| email      | VARCHAR(100) | NOT NULL, UNIQUE   | 이메일                       |
| password   | VARCHAR(255) | NOT NULL           | 암호화된 비밀번호                 |
| name       | VARCHAR(50)  | NOT NULL           | 이름                        |
| phone      | VARCHAR(30)  | NULL               | 연락처                       |
| role       | VARCHAR(30)  | NOT NULL           | USER, ADMIN, SUPER_ADMIN  |
| status     | VARCHAR(30)  | NOT NULL           | ACTIVE, INACTIVE, BLOCKED |
| created_at | DATETIME     | NOT NULL           | 생성일                       |
| updated_at | DATETIME     | NOT NULL           | 수정일                       |

## DDL

```sql id="vl04mv"
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(30),
    role VARCHAR(30) NOT NULL DEFAULT 'USER',
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

---

# 5-2. categories

상품 카테고리를 저장하는 테이블입니다.

## 컬럼

| 컬럼명        | 타입          | 제약조건               | 설명      |
| ---------- | ----------- | ------------------ | ------- |
| id         | BIGINT      | PK, AUTO_INCREMENT | 카테고리 ID |
| name       | VARCHAR(50) | NOT NULL           | 카테고리명   |
| created_at | DATETIME    | NOT NULL           | 생성일     |
| updated_at | DATETIME    | NOT NULL           | 수정일     |

## DDL

```sql id="im5nln"
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

---

# 5-3. products

상품 정보를 저장하는 테이블입니다.

## 컬럼

| 컬럼명            | 타입           | 제약조건               | 설명                                 |
| -------------- | ------------ | ------------------ | ---------------------------------- |
| id             | BIGINT       | PK, AUTO_INCREMENT | 상품 ID                              |
| category_id    | BIGINT       | FK, NOT NULL       | 카테고리 ID                            |
| name           | VARCHAR(100) | NOT NULL           | 상품명                                |
| description    | TEXT         | NULL               | 상품 설명                              |
| price          | INT          | NOT NULL           | 판매가                                |
| stock_quantity | INT          | NOT NULL           | 현재 재고                              |
| image_url      | VARCHAR(500) | NULL               | 상품 이미지 URL                         |
| status         | VARCHAR(30)  | NOT NULL           | ON_SALE, SOLD_OUT, HIDDEN, DELETED |
| created_at     | DATETIME     | NOT NULL           | 생성일                                |
| updated_at     | DATETIME     | NOT NULL           | 수정일                                |

## DDL

```sql id="fikujb"
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price INT NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    image_url VARCHAR(500),
    status VARCHAR(30) NOT NULL DEFAULT 'ON_SALE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id)
        REFERENCES categories(id)
);
```

## 인덱스

```sql id="y96qz9"
CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_products_name ON products(name);
```

---

# 5-4. carts

회원의 장바구니 상품을 저장하는 테이블입니다.

## 컬럼

| 컬럼명        | 타입       | 제약조건               | 설명      |
| ---------- | -------- | ------------------ | ------- |
| id         | BIGINT   | PK, AUTO_INCREMENT | 장바구니 ID |
| user_id    | BIGINT   | FK, NOT NULL       | 회원 ID   |
| product_id | BIGINT   | FK, NOT NULL       | 상품 ID   |
| quantity   | INT      | NOT NULL           | 수량      |
| created_at | DATETIME | NOT NULL           | 생성일     |
| updated_at | DATETIME | NOT NULL           | 수정일     |

## DDL

```sql id="8ug9bs"
CREATE TABLE carts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_carts_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT fk_carts_product
        FOREIGN KEY (product_id)
        REFERENCES products(id),

    CONSTRAINT uk_carts_user_product
        UNIQUE (user_id, product_id)
);
```

## 인덱스

```sql id="r328ta"
CREATE INDEX idx_carts_user_id ON carts(user_id);
CREATE INDEX idx_carts_product_id ON carts(product_id);
```

---

# 5-5. orders

주문 기본 정보를 저장하는 테이블입니다.

## 컬럼

| 컬럼명            | 타입           | 제약조건               | 설명      |
| -------------- | ------------ | ------------------ | ------- |
| id             | BIGINT       | PK, AUTO_INCREMENT | 주문 ID   |
| user_id        | BIGINT       | FK, NOT NULL       | 주문자 ID  |
| order_number   | VARCHAR(50)  | NOT NULL, UNIQUE   | 주문번호    |
| total_price    | INT          | NOT NULL           | 총 주문 금액 |
| status         | VARCHAR(30)  | NOT NULL           | 주문 상태   |
| receiver_name  | VARCHAR(50)  | NOT NULL           | 수령인     |
| receiver_phone | VARCHAR(30)  | NOT NULL           | 수령인 연락처 |
| address        | VARCHAR(255) | NOT NULL           | 기본 주소   |
| detail_address | VARCHAR(255) | NULL               | 상세 주소   |
| payment_status | VARCHAR(30)  | NOT NULL           | 결제 상태   |
| created_at     | DATETIME     | NOT NULL           | 생성일     |
| updated_at     | DATETIME     | NOT NULL           | 수정일     |

## DDL

```sql id="vegrds"
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    total_price INT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    receiver_name VARCHAR(50) NOT NULL,
    receiver_phone VARCHAR(30) NOT NULL,
    address VARCHAR(255) NOT NULL,
    detail_address VARCHAR(255),
    payment_status VARCHAR(30) NOT NULL DEFAULT 'READY',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
);
```

## 인덱스

```sql id="ecqcx7"
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_payment_status ON orders(payment_status);
CREATE INDEX idx_orders_created_at ON orders(created_at);
```

---

# 5-6. order_items

주문에 포함된 상품 상세 정보를 저장하는 테이블입니다.

주문 이후 상품명이 변경되거나 가격이 변경되어도 주문 당시 정보를 유지하기 위해 `product_name`, `price`를 별도로 저장합니다.

## 컬럼

| 컬럼명          | 타입           | 제약조건               | 설명          |
| ------------ | ------------ | ------------------ | ----------- |
| id           | BIGINT       | PK, AUTO_INCREMENT | 주문 상품 ID    |
| order_id     | BIGINT       | FK, NOT NULL       | 주문 ID       |
| product_id   | BIGINT       | FK, NOT NULL       | 상품 ID       |
| product_name | VARCHAR(100) | NOT NULL           | 주문 당시 상품명   |
| price        | INT          | NOT NULL           | 주문 당시 상품 가격 |
| quantity     | INT          | NOT NULL           | 주문 수량       |
| created_at   | DATETIME     | NOT NULL           | 생성일         |

## DDL

```sql id="i6slhl"
CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    price INT NOT NULL,
    quantity INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id)
        REFERENCES orders(id),

    CONSTRAINT fk_order_items_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
);
```

## 인덱스

```sql id="o8z3k4"
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);
```

---

# 5-7. payments

결제 정보를 저장하는 테이블입니다.

MVP에서는 실제 PG 결제 대신 모의 결제 정보를 저장합니다.

## 컬럼

| 컬럼명            | 타입           | 제약조건                 | 설명    |
| -------------- | ------------ | -------------------- | ----- |
| id             | BIGINT       | PK, AUTO_INCREMENT   | 결제 ID |
| order_id       | BIGINT       | FK, NOT NULL, UNIQUE | 주문 ID |
| payment_method | VARCHAR(30)  | NOT NULL             | 결제 수단 |
| payment_status | VARCHAR(30)  | NOT NULL             | 결제 상태 |
| paid_amount    | INT          | NOT NULL             | 결제 금액 |
| transaction_id | VARCHAR(100) | NULL                 | 거래 ID |
| created_at     | DATETIME     | NOT NULL             | 생성일   |
| updated_at     | DATETIME     | NOT NULL             | 수정일   |

## DDL

```sql id="m6ckso"
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    payment_method VARCHAR(30) NOT NULL,
    payment_status VARCHAR(30) NOT NULL DEFAULT 'READY',
    paid_amount INT NOT NULL,
    transaction_id VARCHAR(100),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_payments_order
        FOREIGN KEY (order_id)
        REFERENCES orders(id)
);
```

## 인덱스

```sql id="bv3t0x"
CREATE INDEX idx_payments_status ON payments(payment_status);
CREATE INDEX idx_payments_transaction_id ON payments(transaction_id);
```

---

# 5-8. inventory_logs

재고 변경 이력을 저장하는 테이블입니다.

재고 변경 시 `products.stock_quantity`만 수정하지 않고, 반드시 `inventory_logs`에 변경 이력을 남깁니다.

## 컬럼

| 컬럼명          | 타입           | 제약조건               | 설명                                       |
| ------------ | ------------ | ------------------ | ---------------------------------------- |
| id           | BIGINT       | PK, AUTO_INCREMENT | 재고 로그 ID                                 |
| product_id   | BIGINT       | FK, NOT NULL       | 상품 ID                                    |
| type         | VARCHAR(30)  | NOT NULL           | INBOUND, OUTBOUND, ORDER, CANCEL, ADJUST |
| quantity     | INT          | NOT NULL           | 변경 수량                                    |
| before_stock | INT          | NOT NULL           | 변경 전 재고                                  |
| after_stock  | INT          | NOT NULL           | 변경 후 재고                                  |
| memo         | VARCHAR(255) | NULL               | 메모                                       |
| created_at   | DATETIME     | NOT NULL           | 생성일                                      |

## DDL

```sql id="mslcpl"
CREATE TABLE inventory_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    type VARCHAR(30) NOT NULL,
    quantity INT NOT NULL,
    before_stock INT NOT NULL,
    after_stock INT NOT NULL,
    memo VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_inventory_logs_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
);
```

## 인덱스

```sql id="cq850y"
CREATE INDEX idx_inventory_logs_product_id ON inventory_logs(product_id);
CREATE INDEX idx_inventory_logs_type ON inventory_logs(type);
CREATE INDEX idx_inventory_logs_created_at ON inventory_logs(created_at);
```

---

## 6. Enum 정의

---

# 6-1. UserRole

| 값           | 설명      |
| ----------- | ------- |
| USER        | 일반 구매자  |
| ADMIN       | 쇼핑몰 관리자 |
| SUPER_ADMIN | 최고 관리자  |

MVP에서는 `USER`, `ADMIN`만 우선 사용합니다.

---

# 6-2. UserStatus

| 값        | 설명     |
| -------- | ------ |
| ACTIVE   | 정상 계정  |
| INACTIVE | 비활성 계정 |
| BLOCKED  | 차단 계정  |

---

# 6-3. ProductStatus

| 값        | 설명    |
| -------- | ----- |
| ON_SALE  | 판매 중  |
| SOLD_OUT | 품절    |
| HIDDEN   | 숨김    |
| DELETED  | 삭제 처리 |

---

# 6-4. OrderStatus

| 값         | 설명           |
| --------- | ------------ |
| PENDING   | 주문 생성, 결제 대기 |
| PAID      | 결제 완료        |
| PREPARING | 상품 준비 중      |
| SHIPPING  | 배송 중         |
| COMPLETED | 배송 완료        |
| CANCELLED | 주문 취소        |
| REFUNDED  | 환불 완료        |

MVP에서는 아래 4개 상태를 우선 사용합니다.

```text id="s5v4l8"
PENDING
PAID
SHIPPING
COMPLETED
```

---

# 6-5. PaymentStatus

| 값         | 설명    |
| --------- | ----- |
| READY     | 결제 대기 |
| PAID      | 결제 완료 |
| FAILED    | 결제 실패 |
| CANCELLED | 결제 취소 |
| REFUNDED  | 환불 완료 |

---

# 6-6. InventoryLogType

| 값        | 설명              |
| -------- | --------------- |
| INBOUND  | 입고              |
| OUTBOUND | 출고              |
| ORDER    | 주문으로 인한 재고 차감   |
| CANCEL   | 주문 취소로 인한 재고 복구 |
| ADJUST   | 관리자 직접 조정       |

---

## 7. 핵심 제약 조건

---

# 7-1. 이메일 중복 방지

```sql id="y9rabc"
email VARCHAR(100) NOT NULL UNIQUE
```

동일한 이메일로 중복 가입할 수 없습니다.

---

# 7-2. 장바구니 중복 상품 방지

```sql id="ob77hz"
CONSTRAINT uk_carts_user_product UNIQUE (user_id, product_id)
```

같은 사용자가 같은 상품을 장바구니에 여러 줄로 담지 않도록 합니다.
이미 담긴 상품을 다시 담으면 수량만 증가시키는 방식으로 처리합니다.

---

# 7-3. 주문번호 중복 방지

```sql id="wz1jjh"
order_number VARCHAR(50) NOT NULL UNIQUE
```

주문번호는 유일해야 합니다.

예시:

```text id="n1d9gu"
ORD-20260616-000001
```

---

# 7-4. 주문과 결제 1:1 관계

```sql id="zrqzb5"
order_id BIGINT NOT NULL UNIQUE
```

하나의 주문은 하나의 결제 정보를 가집니다.

---

## 8. 재고 처리 정책

---

# 8-1. 결제 완료 시 재고 차감

```text id="mjiibw"
1. 주문 조회
2. 주문 상품 목록 조회
3. 상품별 현재 재고 확인
4. 재고 부족 시 결제 실패 처리
5. 재고 충분 시 products.stock_quantity 차감
6. inventory_logs에 ORDER 타입 로그 저장
7. orders.status = PAID 변경
8. orders.payment_status = PAID 변경
9. payments.payment_status = PAID 변경
```

---

# 8-2. 주문 취소 시 재고 복구

```text id="8ll6gc"
1. 주문 조회
2. 주문 상태 확인
3. 취소 가능한 상태인지 검증
4. 주문 상품 수량만큼 products.stock_quantity 복구
5. inventory_logs에 CANCEL 타입 로그 저장
6. orders.status = CANCELLED 변경
7. payments.payment_status = CANCELLED 또는 REFUNDED 변경
```

---

# 8-3. 관리자 입고 처리

```text id="crhqn8"
1. 상품 조회
2. 기존 재고 확인
3. 입고 수량만큼 products.stock_quantity 증가
4. inventory_logs에 INBOUND 타입 로그 저장
```

---

# 8-4. 관리자 재고 조정

```text id="1qzoeu"
1. 상품 조회
2. 기존 재고 확인
3. 입력한 재고 수량으로 products.stock_quantity 변경
4. inventory_logs에 ADJUST 타입 로그 저장
```

---

## 9. 트랜잭션 처리 대상

아래 작업은 반드시 트랜잭션으로 처리해야 합니다.

| 작업        | 이유                                              |
| --------- | ----------------------------------------------- |
| 주문 생성     | orders, order_items, payments 생성이 함께 처리되어야 함    |
| 결제 완료     | 결제 상태 변경, 주문 상태 변경, 재고 차감, 재고 로그 저장이 함께 처리되어야 함 |
| 주문 취소     | 주문 상태 변경, 결제 상태 변경, 재고 복구, 재고 로그 저장이 함께 처리되어야 함 |
| 관리자 입고    | 상품 재고 증가와 재고 로그 저장이 함께 처리되어야 함                  |
| 관리자 재고 조정 | 상품 재고 변경과 재고 로그 저장이 함께 처리되어야 함                  |

---

## 10. 주문 생성 트랜잭션 예시

```text id="y3m7k0"
주문 생성 요청
→ 사용자 확인
→ 장바구니 상품 조회
→ 상품 판매 상태 확인
→ 주문 총액 계산
→ orders 생성
→ order_items 생성
→ payments 생성
→ 선택한 cart 삭제
→ 주문 생성 완료
```

---

## 11. 결제 완료 트랜잭션 예시

```text id="4b0rva"
결제 완료 요청
→ 주문 조회
→ 결제 정보 조회
→ 주문 상품 목록 조회
→ 상품별 재고 확인
→ 상품별 재고 차감
→ inventory_logs 저장
→ payments 상태 PAID 변경
→ orders 상태 PAID 변경
→ 결제 완료
```

---

## 12. 대시보드 조회 기준

---

# 12-1. 총 매출

```sql id="et73zy"
SELECT SUM(paid_amount)
FROM payments
WHERE payment_status = 'PAID';
```

---

# 12-2. 오늘 매출

```sql id="w53rlx"
SELECT SUM(paid_amount)
FROM payments
WHERE payment_status = 'PAID'
  AND DATE(created_at) = CURDATE();
```

---

# 12-3. 주문 수

```sql id="6if4lo"
SELECT COUNT(*)
FROM orders;
```

---

# 12-4. 오늘 주문 수

```sql id="y5elt4"
SELECT COUNT(*)
FROM orders
WHERE DATE(created_at) = CURDATE();
```

---

# 12-5. 재고 부족 상품 수

MVP에서는 재고 기준을 5개 이하로 설정합니다.

```sql id="pm7bvt"
SELECT COUNT(*)
FROM products
WHERE stock_quantity <= 5
  AND status = 'ON_SALE';
```

---

# 12-6. 인기 상품

```sql id="o35k2r"
SELECT
    oi.product_id,
    oi.product_name,
    SUM(oi.quantity) AS total_quantity,
    SUM(oi.price * oi.quantity) AS total_sales
FROM order_items oi
JOIN orders o ON oi.order_id = o.id
WHERE o.status IN ('PAID', 'SHIPPING', 'COMPLETED')
GROUP BY oi.product_id, oi.product_name
ORDER BY total_quantity DESC
LIMIT 10;
```

---

## 13. 초기 데이터 예시

---

# 13-1. 카테고리 초기 데이터

```sql id="n613wg"
INSERT INTO categories (name) VALUES
('BEST'),
('NEW'),
('원피스'),
('블라우스'),
('아우터'),
('니트'),
('티셔츠'),
('스커트'),
('팬츠'),
('SALE');
```

---

# 13-2. 관리자 계정 초기 데이터

비밀번호는 실제 개발 시 BCrypt로 암호화된 값을 저장해야 합니다.

```sql id="rviqsu"
INSERT INTO users (
    email,
    password,
    name,
    phone,
    role,
    status
) VALUES (
    'admin@example.com',
    '{bcrypt-password}',
    '관리자',
    '010-0000-0000',
    'ADMIN',
    'ACTIVE'
);
```

---

# 13-3. 테스트 사용자 초기 데이터

```sql id="dbj9ou"
INSERT INTO users (
    email,
    password,
    name,
    phone,
    role,
    status
) VALUES (
    'user@example.com',
    '{bcrypt-password}',
    '테스트사용자',
    '010-1111-2222',
    'USER',
    'ACTIVE'
);
```

---

# 13-4. 상품 초기 데이터

```sql id="v2g9u9"
INSERT INTO products (
    category_id,
    name,
    description,
    price,
    stock_quantity,
    image_url,
    status
) VALUES
(3, '베이직 셔츠 원피스', '데일리로 착용하기 좋은 셔츠 원피스입니다.', 39000, 20, '/uploads/products/product-1.jpg', 'ON_SALE'),
(4, '소프트 블라우스', '깔끔한 분위기의 데일리 블라우스입니다.', 29000, 15, '/uploads/products/product-2.jpg', 'ON_SALE'),
(8, 'A라인 미디 스커트', '출근룩과 데일리룩에 어울리는 스커트입니다.', 34000, 12, '/uploads/products/product-3.jpg', 'ON_SALE');
```

---

## 14. 전체 DDL 실행 순서

외래키 관계 때문에 아래 순서대로 테이블을 생성합니다.

```text id="axvguz"
1. users
2. categories
3. products
4. carts
5. orders
6. order_items
7. payments
8. inventory_logs
```

---

## 15. 전체 DROP 순서

삭제할 때는 생성 순서의 반대로 삭제합니다.

```sql id="ojl3yh"
DROP TABLE IF EXISTS inventory_logs;
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS carts;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;
```

---

## 16. 확장 예정 테이블

2차 기능에서 아래 테이블을 추가할 수 있습니다.

| 테이블             | 설명            |
| --------------- | ------------- |
| reviews         | 상품 리뷰         |
| coupons         | 쿠폰            |
| coupon_issues   | 사용자별 쿠폰 발급 이력 |
| deliveries      | 배송 및 송장 정보    |
| product_images  | 상품 이미지 다중 관리  |
| product_options | 색상, 사이즈 옵션    |
| notifications   | 관리자 알림        |
| admin_logs      | 관리자 작업 로그     |

---

## 17. 2차 확장 ERD 방향

```text id="lymbov"
products 1 ─── N product_images
products 1 ─── N product_options
products 1 ─── N reviews

users 1 ─── N reviews
users 1 ─── N coupon_issues

coupons 1 ─── N coupon_issues

orders 1 ─── 1 deliveries
orders 1 ─── N admin_logs
```

---

## 18. DB 설계 핵심 정리

```text id="jmik30"
1. users는 USER와 ADMIN을 함께 관리한다.
2. products는 현재 재고를 stock_quantity로 가진다.
3. orders는 주문 기본 정보를 가진다.
4. order_items는 주문 당시 상품명과 가격을 별도로 저장한다.
5. payments는 주문별 결제 상태를 저장한다.
6. inventory_logs는 모든 재고 변경 이력을 저장한다.
7. 결제 완료, 주문 취소, 재고 입고, 재고 조정은 반드시 트랜잭션 처리한다.
8. 관리자 대시보드는 orders, payments, products, order_items를 집계해서 구성한다.
```

---

## 19. 현재 코드에 추가된 테이블

> 아래 내용은 JPA 엔티티 기준 요약이다. 정식 운영 전 Flyway/Liquibase 같은 마이그레이션 도구로 DDL과 인덱스를 고정해야 한다.

### shipments

| 주요 컬럼 | 의미 |
| --- | --- |
| id | 배송 식별자 |
| order_id | 주문과의 관계 |
| status | `READY`, `IN_TRANSIT`, `DELIVERED` |
| tracking_number / carrier | 송장번호 / 택배사 |
| shipped_at / delivered_at | 발송 / 배송 완료 시각 |
| created_at / updated_at | 생성 / 수정 시각 |

권장 제약: 주문당 배송 한 건을 유지한다면 `order_id UNIQUE`. 분할 배송을 지원할 예정이면 이 제약을 두지 말고 배송 품목 테이블을 추가한다.

### return_requests

| 주요 컬럼 | 의미 |
| --- | --- |
| id | 반품 요청 식별자 |
| order_id / user_id | 대상 주문 / 요청 사용자 |
| reason / reason_detail | 사유 enum / 상세 사유 |
| status | `REQUESTED`, `APPROVED`, `REJECTED` |
| admin_note | 관리자 처리 메모 |
| created_at / updated_at | 생성 / 수정 시각 |

현재는 주문 전체 반품 모델이다. 부분 반품을 지원할 때는 `return_items`와 품목별 수량·금액·검수 결과가 필요하다.

### inquiries

| 주요 컬럼 | 의미 |
| --- | --- |
| id | 문의 식별자 |
| user_id / product_id | 작성자 / 선택적 대상 상품 |
| type | `PRODUCT`, `ORDER`, `OTHER` |
| subject / content | 제목 / 본문 |
| answer | 관리자 답변 |
| status | `WAITING`, `ANSWERED`, `CLOSED` |
| created_at / updated_at | 생성 / 수정 시각 |

주문 문의를 명확히 연결하려면 향후 `order_id` 또는 별도 문의 대상 관계를 추가한다.

### accounting_entries

| 주요 컬럼 | 의미 |
| --- | --- |
| id | 기록 식별자 |
| type | `SALE`, `REFUND`, `INBOUND` |
| amount | 금액 |
| description | 설명 |
| reference_id | 주문번호 또는 상품 참조 문자열 |
| created_at | 생성 시각 |

현재 테이블은 업무 이벤트 요약 기록이다. v0.4에서 `account_codes`와 `accounting_entry_lines`를 추가하고 차변/대변 균형, 원거래 참조, 확정/반제, 회계 기간을 정의한다.

## 20. 운영 DB로 가기 전 필수 보강

- 모든 FK, unique, check 제약과 인덱스를 마이그레이션 파일로 명시
- 금액 타입과 통화, 세금, 반올림 정책 정의
- 재고 동시성 제어용 낙관/비관 잠금 또는 원자적 갱신 결정
- 개인정보 컬럼 분류, 암호화, 접근, 보존/파기 정책
- 상태 변경 및 관리자 작업 감사 로그
- created/updated/deleted 시각과 soft delete 정책 통일
- 백업/복원, 데이터 정합성 검사, 무중단 마이그레이션 절차

## 21. 다중 창고 단계 1 테이블

### warehouses

| 주요 컬럼 | 의미 |
| --- | --- |
| id | 창고 식별자 |
| code | 대소문자를 정규화한 고유 창고 코드 |
| name / address | 창고명 / 주소 |
| active | 사용 가능 여부 |
| created_at / updated_at | 생성 / 수정 시각 |

### warehouse_stocks

| 주요 컬럼 | 의미 |
| --- | --- |
| id | 창고 재고 식별자 |
| warehouse_id / product_id | 창고 / 상품 FK |
| quantity | 해당 창고 실재고 수량 |
| reserved_quantity | 결제 완료 후 출고 대기 예약 수량 |
| version | 낙관적 잠금 버전 |
| created_at / updated_at | 생성 / 수정 시각 |

`(warehouse_id, product_id)`는 유일해야 한다. 가용수량은 `quantity - reserved_quantity`다. 초기 배치 시 상품 행을 잠그고 `products.stock_quantity - SUM(가용수량)` 범위 안에서만 증가시킨다.

### stock_transfers

| 주요 컬럼 | 의미 |
| --- | --- |
| id / transfer_number | 식별자 / 고유 이동번호 |
| from_warehouse_id / to_warehouse_id | 출발 / 도착 창고 FK |
| product_id / quantity | 이동 상품 / 수량 |
| status | `PENDING`, `COMPLETED` |
| requested_at / completed_at | 요청 / 완료 시각 |

현재는 이동 한 건당 상품 하나를 지원한다. 다품목 이동으로 확장할 때 헤더 `stock_transfers`와 라인 `stock_transfer_items`로 분리한다.

### stock_reservations

| 주요 컬럼 | 의미 |
| --- | --- |
| id | 예약 원장 식별자 |
| order_id / order_item_id | 주문 / 주문 품목 FK |
| warehouse_stock_id | 예약한 창고 재고 FK |
| quantity | 해당 창고에서 예약/출고한 수량 |
| status | `RESERVED`, `RELEASED`, `SHIPPED`, `RETURNED` |
| created_at / updated_at | 생성 / 수정 시각 |

한 주문 품목이 여러 창고에 나뉘어 예약될 수 있다. `RELEASED`는 배송 전 주문 취소로 예약이 해제된 상태다. 상태 전이로 결제 재호출, 취소/배송 중복 처리, 반품 중복 복구가 수량에 두 번 반영되는 것을 막는다.
