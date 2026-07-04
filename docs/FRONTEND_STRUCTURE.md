# Frontend Structure

> 문서 상태: 초기 구조 설계안. 현재 프론트에는 `/admin/shipments`, `/admin/returns`, `/admin/inquiries`, `/admin/accounting`, `/admin/warehouses` 화면과 관련 서비스가 추가되어 있다. 최신 범위와 검증 상태는 [문서 안내](./README.md)에서 확인한다.

CommerceOps ERP의 프론트엔드 구조 문서입니다.

본 문서는 Next.js App Router 기반으로 사용자 쇼핑몰 화면과 관리자 ERP 화면을 분리하여 개발하기 위한 폴더 구조, 라우팅, 컴포넌트 설계, UI 방향을 정의합니다.

---

## 1. 프론트엔드 목표

CommerceOps ERP 프론트엔드는 두 가지 화면 영역을 가집니다.

```text id="oa6mmp"
1. 사용자 쇼핑몰 화면
   - 상품 조회
   - 상품 상세
   - 장바구니
   - 주문
   - 마이페이지

2. 관리자 ERP 화면
   - 상품 관리
   - 주문 관리
   - 재고 관리
   - 고객 관리
   - 매출 대시보드
```

사용자 화면은 패션 쇼핑몰 스타일의 깔끔한 커머스 UI를 지향하고, 관리자 화면은 실무형 백오피스 ERP UI를 지향합니다.

---

## 2. 기술 스택

| 영역        | 기술                                |
| --------- | --------------------------------- |
| Framework | Next.js                           |
| Language  | TypeScript                        |
| Styling   | Tailwind CSS                      |
| Routing   | Next.js App Router                |
| API 통신    | fetch 또는 axios                    |
| 상태 관리     | 초기 MVP는 React state / Context API |
| 인증 관리     | JWT accessToken                   |
| 개발 환경     | WSL Ubuntu                        |
| 실행 포트     | 3000                              |

---

## 3. 전체 폴더 구조

```text id="ca6ze4"
frontend/
├── package.json
├── next.config.ts
├── tsconfig.json
├── tailwind.config.ts
├── postcss.config.js
├── public/
│   ├── images/
│   │   ├── banners/
│   │   ├── products/
│   │   └── icons/
│   └── favicon.ico
│
└── src/
    ├── app/
    ├── components/
    ├── features/
    ├── lib/
    ├── types/
    ├── constants/
    ├── hooks/
    └── styles/
```

---

## 4. src 폴더 역할

| 폴더         | 역할                      |
| ---------- | ----------------------- |
| app        | 페이지 라우팅                 |
| components | 공통 UI 컴포넌트              |
| features   | 도메인별 기능 모듈              |
| lib        | API 클라이언트, 인증 유틸, 공통 함수 |
| types      | 공통 타입 정의                |
| constants  | 상수 데이터                  |
| hooks      | 커스텀 훅                   |
| styles     | 전역 스타일                  |

---

## 5. App Router 페이지 구조

```text id="lv9hea"
src/app/
├── layout.tsx
├── page.tsx
│
├── login/
│   └── page.tsx
│
├── signup/
│   └── page.tsx
│
├── products/
│   ├── page.tsx
│   └── [id]/
│       └── page.tsx
│
├── cart/
│   └── page.tsx
│
├── orders/
│   ├── page.tsx
│   ├── checkout/
│   │   └── page.tsx
│   └── [id]/
│       └── page.tsx
│
├── mypage/
│   └── page.tsx
│
└── admin/
    ├── layout.tsx
    ├── page.tsx
    ├── products/
    │   ├── page.tsx
    │   ├── new/
    │   │   └── page.tsx
    │   └── [id]/
    │       └── page.tsx
    ├── orders/
    │   └── page.tsx
    ├── inventory/
    │   └── page.tsx
    ├── customers/
    │   └── page.tsx
    └── sales/
        └── page.tsx
```

---

## 6. 사용자 화면 라우팅

| 경로                 | 화면     | 설명                        |
| ------------------ | ------ | ------------------------- |
| `/`                | 메인 페이지 | 배너, 카테고리, 신상품, 인기상품       |
| `/products`        | 상품 목록  | 카테고리별 상품 목록, 검색, 정렬       |
| `/products/[id]`   | 상품 상세  | 상품 이미지, 가격, 설명, 재고, 구매 버튼 |
| `/cart`            | 장바구니   | 담긴 상품, 수량 변경, 삭제          |
| `/orders/checkout` | 주문/결제  | 배송지 입력, 모의 결제             |
| `/orders`          | 주문 내역  | 사용자 주문 목록                 |
| `/orders/[id]`     | 주문 상세  | 주문 상품, 배송지, 주문 상태         |
| `/mypage`          | 마이페이지  | 회원 정보, 주문 요약              |
| `/login`           | 로그인    | 이메일/비밀번호 로그인              |
| `/signup`          | 회원가입   | 사용자 회원가입                  |

---

## 7. 관리자 화면 라우팅

| 경로                     | 화면       | 설명                   |
| ---------------------- | -------- | -------------------- |
| `/admin`               | 관리자 대시보드 | 총 매출, 주문 수, 재고 부족 상품 |
| `/admin/products`      | 상품 관리    | 상품 목록, 검색, 수정, 삭제    |
| `/admin/products/new`  | 상품 등록    | 신규 상품 등록             |
| `/admin/products/[id]` | 상품 수정    | 기존 상품 수정             |
| `/admin/orders`        | 주문 관리    | 주문 목록, 주문 상태 변경      |
| `/admin/inventory`     | 재고 관리    | 재고 조회, 입고, 조정, 로그    |
| `/admin/customers`     | 고객 관리    | 회원 목록, 고객별 주문 정보     |
| `/admin/sales`         | 매출 통계    | 일별/월별 매출 통계          |

---

## 8. 컴포넌트 구조

```text id="el02xy"
src/components/
├── common/
│   ├── Button.tsx
│   ├── Input.tsx
│   ├── Select.tsx
│   ├── Modal.tsx
│   ├── Pagination.tsx
│   ├── Badge.tsx
│   ├── EmptyState.tsx
│   └── Loading.tsx
│
├── shop/
│   ├── ShopHeader.tsx
│   ├── ShopFooter.tsx
│   ├── CategoryNav.tsx
│   ├── MainBanner.tsx
│   ├── QuickCategory.tsx
│   ├── ProductCard.tsx
│   ├── ProductGrid.tsx
│   ├── ProductFilter.tsx
│   ├── ProductSort.tsx
│   ├── CartItem.tsx
│   ├── OrderSummary.tsx
│   └── CheckoutForm.tsx
│
└── admin/
    ├── AdminLayout.tsx
    ├── AdminSidebar.tsx
    ├── AdminTopbar.tsx
    ├── StatCard.tsx
    ├── DataTable.tsx
    ├── ProductForm.tsx
    ├── OrderStatusBadge.tsx
    ├── InventoryStatusBadge.tsx
    └── SalesChart.tsx
```

---

## 9. common 컴포넌트

공통 컴포넌트는 사용자 화면과 관리자 화면에서 모두 사용할 수 있는 최소 단위 UI입니다.

| 컴포넌트       | 역할        |
| ---------- | --------- |
| Button     | 버튼        |
| Input      | 텍스트 입력    |
| Select     | 셀렉트 박스    |
| Modal      | 팝업        |
| Pagination | 페이지네이션    |
| Badge      | 상태 표시     |
| EmptyState | 데이터 없음 표시 |
| Loading    | 로딩 표시     |

---

## 10. shop 컴포넌트

사용자 쇼핑몰 화면 전용 컴포넌트입니다.

| 컴포넌트          | 역할          |
| ------------- | ----------- |
| ShopHeader    | 쇼핑몰 상단 헤더   |
| ShopFooter    | 쇼핑몰 푸터      |
| CategoryNav   | 상품 카테고리 메뉴  |
| MainBanner    | 메인 프로모션 배너  |
| QuickCategory | 빠른 카테고리 이동  |
| ProductCard   | 상품 카드       |
| ProductGrid   | 상품 카드 그리드   |
| ProductFilter | 상품 필터       |
| ProductSort   | 상품 정렬       |
| CartItem      | 장바구니 상품     |
| OrderSummary  | 주문 금액 요약    |
| CheckoutForm  | 배송지/결제 입력 폼 |

---

## 11. admin 컴포넌트

관리자 ERP 화면 전용 컴포넌트입니다.

| 컴포넌트                 | 역할          |
| -------------------- | ----------- |
| AdminLayout          | 관리자 공통 레이아웃 |
| AdminSidebar         | 좌측 메뉴       |
| AdminTopbar          | 상단 관리자 바    |
| StatCard             | 대시보드 통계 카드  |
| DataTable            | 관리자 데이터 테이블 |
| ProductForm          | 상품 등록/수정 폼  |
| OrderStatusBadge     | 주문 상태 표시    |
| InventoryStatusBadge | 재고 상태 표시    |
| SalesChart           | 매출 차트       |

---

## 12. features 구조

도메인별 API, 타입, mock 데이터를 분리합니다.

```text id="7ulpm1"
src/features/
├── auth/
│   ├── api.ts
│   ├── types.ts
│   └── mock.ts
│
├── product/
│   ├── api.ts
│   ├── types.ts
│   └── mock.ts
│
├── cart/
│   ├── api.ts
│   ├── types.ts
│   └── mock.ts
│
├── order/
│   ├── api.ts
│   ├── types.ts
│   └── mock.ts
│
├── inventory/
│   ├── api.ts
│   ├── types.ts
│   └── mock.ts
│
└── dashboard/
    ├── api.ts
    ├── types.ts
    └── mock.ts
```

---

## 13. features 폴더 역할

| 폴더        | 역할                   |
| --------- | -------------------- |
| auth      | 로그인, 회원가입, 내 정보      |
| product   | 상품 목록, 상세, 관리자 상품 관리 |
| cart      | 장바구니 조회, 추가, 수정, 삭제  |
| order     | 주문 생성, 주문 내역, 주문 상태  |
| inventory | 재고 조회, 입고, 조정, 재고 로그 |
| dashboard | 관리자 대시보드 통계          |

---

## 14. API 클라이언트 구조

```text id="f4egv0"
src/lib/
├── api.ts
├── auth.ts
├── storage.ts
├── format.ts
└── constants.ts
```

---

## 15. api.ts 예시

```ts id="u7f2cy"
const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080/api";

export async function apiClient<T>(
  path: string,
  options: RequestInit = {}
): Promise<T> {
  const token =
    typeof window !== "undefined"
      ? localStorage.getItem("accessToken")
      : null;

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  });

  if (!response.ok) {
    throw new Error("API 요청에 실패했습니다.");
  }

  return response.json();
}
```

---

## 16. 인증 처리 방식

MVP에서는 JWT accessToken을 localStorage에 저장합니다.

```text id="u1n5jd"
로그인 성공
→ accessToken 저장
→ 사용자 정보 저장
→ USER면 메인 이동
→ ADMIN이면 관리자 대시보드 이동
```

---

## 17. 인증 관련 파일

```text id="k8tqtn"
src/features/auth/
├── api.ts
├── types.ts
└── mock.ts

src/lib/
├── auth.ts
└── storage.ts
```

---

## 18. 권한 처리 기준

| 권한          | 접근 가능 화면                    |
| ----------- | --------------------------- |
| 비로그인        | 메인, 상품 목록, 상품 상세, 로그인, 회원가입 |
| USER        | 장바구니, 주문, 마이페이지             |
| ADMIN       | 관리자 전체 화면                   |
| SUPER_ADMIN | 2차 확장 시 관리자 계정 관리           |

MVP에서는 `USER`, `ADMIN`만 우선 사용합니다.

---

## 19. 관리자 라우트 보호

관리자 화면은 ADMIN 권한만 접근할 수 있어야 합니다.

```text id="foyj7k"
1. accessToken 존재 여부 확인
2. /api/auth/me 호출
3. role이 ADMIN인지 확인
4. ADMIN이 아니면 메인 또는 로그인 페이지로 이동
```

---

## 20. 타입 구조

```text id="awxrsj"
src/types/
├── api.ts
├── user.ts
├── product.ts
├── cart.ts
├── order.ts
├── payment.ts
├── inventory.ts
└── dashboard.ts
```

---

## 21. 공통 API 응답 타입

```ts id="gzuox9"
export interface ApiResponse<T> {
  success: boolean;
  statusCode: number;
  message: string;
  data: T;
}

export interface ApiErrorResponse {
  success: false;
  statusCode: number;
  errorCode: string;
  message: string;
  error?: string;
}
```

---

## 22. 상품 타입 예시

```ts id="1tikt6"
export type ProductStatus = "ON_SALE" | "SOLD_OUT" | "HIDDEN" | "DELETED";

export interface Product {
  id: number;
  categoryId: number;
  categoryName: string;
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
  imageUrl: string;
  status: ProductStatus;
  createdAt: string;
  updatedAt: string;
}

export interface ProductListItem {
  id: number;
  categoryId: number;
  categoryName: string;
  name: string;
  price: number;
  stockQuantity: number;
  imageUrl: string;
  status: ProductStatus;
}
```

---

## 23. 주문 타입 예시

```ts id="2o6rz9"
export type OrderStatus =
  | "PENDING"
  | "PAID"
  | "PREPARING"
  | "SHIPPING"
  | "COMPLETED"
  | "CANCELLED"
  | "REFUNDED";

export type PaymentStatus =
  | "READY"
  | "PAID"
  | "FAILED"
  | "CANCELLED"
  | "REFUNDED";

export interface Order {
  orderId: number;
  orderNumber: string;
  totalPrice: number;
  status: OrderStatus;
  paymentStatus: PaymentStatus;
  createdAt: string;
}
```

---

## 24. 디자인 방향

### 사용자 쇼핑몰 UI

사용자 화면은 패션 쇼핑몰형 UI를 기준으로 합니다.

```text id="hdo5qo"
- 흰색 배경
- 얇은 회색 라인
- 넓은 여백
- 중앙 정렬형 레이아웃
- 상품 이미지 중심
- BEST / NEW / SALE 섹션
- 깔끔한 상단 카테고리 메뉴
```

### 관리자 ERP UI

관리자 화면은 쇼핑몰과 분리된 백오피스 UI를 기준으로 합니다.

```text id="p9y2mg"
- 좌측 사이드바
- 상단 관리자 바
- 카드형 통계
- 테이블 중심 데이터 관리
- 상태 배지
- 필터와 검색 중심
```

---

## 25. 디자인 토큰

```ts id="e8c5ku"
export const colors = {
  background: "#ffffff",
  text: "#222222",
  subText: "#777777",
  border: "#e5e5e5",
  muted: "#f7f7f7",
  primary: "#f3a6b8",
  primaryDark: "#d97b93",
  danger: "#d94f4f",
};

export const layout = {
  maxWidth: "1200px",
  headerHeight: "72px",
  adminSidebarWidth: "240px",
};
```

---

## 26. 사용자 메인 화면 구조

```text id="mnd4a1"
/
├── ShopHeader
├── MainBanner
├── QuickCategory
├── NewArrivals
├── BestProducts
├── TodaySale
├── ThemeLook
└── ShopFooter
```

---

## 27. 상품 목록 화면 구조

```text id="c8lp1r"
/products
├── ShopHeader
├── PageTitle
├── CategoryTabs
├── ProductFilter
├── ProductSort
├── ProductGrid
├── Pagination
└── ShopFooter
```

---

## 28. 상품 상세 화면 구조

```text id="o7gt52"
/products/[id]
├── ShopHeader
├── ProductImageGallery
├── ProductInfo
│   ├── 상품명
│   ├── 가격
│   ├── 재고 상태
│   ├── 수량 선택
│   ├── 장바구니 버튼
│   └── 바로 구매 버튼
├── ProductDetailTabs
└── ShopFooter
```

---

## 29. 장바구니 화면 구조

```text id="o5i55r"
/cart
├── ShopHeader
├── CartItemList
├── OrderSummary
└── ShopFooter
```

---

## 30. 주문 화면 구조

```text id="ic8yi2"
/orders/checkout
├── ShopHeader
├── CheckoutProductList
├── ReceiverForm
├── PaymentMethodSelector
├── OrderSummary
└── SubmitOrderButton
```

---

## 31. 관리자 대시보드 구조

```text id="kpenme"
/admin
├── AdminLayout
│   ├── AdminSidebar
│   ├── AdminTopbar
│   └── Content
│       ├── StatCard
│       ├── SalesSummary
│       ├── LowStockList
│       └── RecentOrders
```

---

## 32. 관리자 상품 관리 구조

```text id="667fl5"
/admin/products
├── AdminLayout
│   └── Content
│       ├── PageHeader
│       ├── SearchFilter
│       ├── ProductTable
│       └── Pagination
```

---

## 33. 관리자 주문 관리 구조

```text id="xhexzz"
/admin/orders
├── AdminLayout
│   └── Content
│       ├── PageHeader
│       ├── OrderStatusFilter
│       ├── OrderTable
│       └── Pagination
```

---

## 34. 관리자 재고 관리 구조

```text id="l6lxx5"
/admin/inventory
├── AdminLayout
│   └── Content
│       ├── PageHeader
│       ├── InventorySummary
│       ├── InventoryTable
│       ├── InboundModal
│       └── AdjustModal
```

---

## 35. 환경변수

```env id="tkfgiw"
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/api
```

운영 배포 시에는 API 서버 주소로 변경합니다.

```env id="3qbcz5"
NEXT_PUBLIC_API_BASE_URL=https://your-domain.com/api
```

---

## 36. 실행 방법

```bash id="becw5k"
cd frontend
npm install
npm run dev
```

접속 주소:

```text id="8b1t90"
http://localhost:3000
```

---

## 37. 빌드 방법

```bash id="z24g0q"
npm run build
npm run start
```

---

## 38. 개발 우선순위

### 1순위

```text id="h52rqa"
1. 프로젝트 초기 세팅
2. 공통 레이아웃
3. ShopHeader / ShopFooter
4. 메인 페이지
5. 상품 목록
6. 상품 상세
```

### 2순위

```text id="ya5mkc"
1. 로그인
2. 회원가입
3. 장바구니
4. 주문 생성
5. 주문 내역
```

### 3순위

```text id="84we5h"
1. 관리자 레이아웃
2. 관리자 상품 관리
3. 관리자 주문 관리
4. 관리자 재고 관리
5. 관리자 대시보드
```

### 4순위

```text id="lc3qg4"
1. API 연동
2. 오류 처리
3. 로딩 처리
4. 반응형 정리
5. 배포 설정
```

---

## 39. 네이밍 규칙

### 파일명

| 대상    | 규칙         | 예시              |
| ----- | ---------- | --------------- |
| 컴포넌트  | PascalCase | ProductCard.tsx |
| 훅     | camelCase  | useAuth.ts      |
| 유틸    | camelCase  | formatPrice.ts  |
| 타입 파일 | camelCase  | product.ts      |

### 컴포넌트명

```text id="h3u86m"
ProductCard
ShopHeader
AdminLayout
OrderSummary
```

### API 함수명

```text id="13pue4"
getProducts
getProductDetail
createOrder
updateOrderStatus
getInventoryList
```

---

## 40. Mock 데이터 운영 기준

API 연동 전에는 features 폴더에 mock 데이터를 둡니다.

```text id="onjj0r"
src/features/product/mock.ts
src/features/order/mock.ts
src/features/inventory/mock.ts
src/features/dashboard/mock.ts
```

API 연동 후에도 mock 데이터는 삭제하지 않고, 화면 테스트용으로 유지할 수 있습니다.

---

## 41. 프론트엔드 개발 핵심 정리

```text id="gikmlx"
1. 사용자 쇼핑몰과 관리자 ERP 화면을 명확히 분리한다.
2. app 폴더는 라우팅만 담당한다.
3. components는 UI 단위 컴포넌트를 담당한다.
4. features는 도메인별 API, 타입, mock 데이터를 담당한다.
5. lib는 공통 API 클라이언트와 유틸 함수를 담당한다.
6. 관리자 화면은 /admin 하위 라우트로 묶는다.
7. API 연동 전에는 mock 데이터로 화면을 먼저 완성한다.
8. MVP에서는 복잡한 상태 관리 라이브러리 없이 React state와 Context API로 시작한다.
```
