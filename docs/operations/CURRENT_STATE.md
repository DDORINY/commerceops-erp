# 현재 구현 현황

기준 버전: `v1.0.0`
기준일: 2026-07-13

이 문서는 v0.1.1 ~ v0.9.9 완료 상태와 v1.0 최종 포트폴리오 릴리스 계획을 함께 기록한다.

문서 정합성 기준: 2026-07-07에 루트 `README.md`, `docs/plans/PRODUCT_ROADMAP.md`, `docs/plans/V0.7_ACCOUNTING_SETTLEMENT_PLAN.md`의 깨진 한글과 오래된 로드맵 단계를 `v0.6.9` 실제 완료 상태 및 `v0.7` 이후 계획 기준으로 재정리했다.

AI 개발/학습/배포 전략 문서: 2026-07-07에 `docs/ai/AI_DEVELOPMENT_DEPLOYMENT_PLAN.md`를 추가해 v0.8/v0.9 AI 기능의 `ai/` 디렉터리 구조, Git 포함/제외 기준, 학습/추론 분리, AWS EC2 1대 서버 배포 전략, LLM 비용 제한 정책을 문서화했다.

v0.7 회계관리 고도화 계획: 2026-07-07에 `docs/plans/V0.7_ACCOUNTING_SETTLEMENT_PLAN.md`를 v0.7.0 기준으로 확정했다. v0.7 기능은 아직 구현 전이며, 회계 원장/거래 유형, 주문 매출 인식, 환불/반품 배송비, 택배비 매입, 정산 배치, 기간 마감, 회계 리포트, 권한/감사 로그, v0.8 AI 데이터셋 이관 후보를 계획으로 정리했다.

## 요약

CommerceOps ERP는 Spring Boot 백엔드와 Next.js 프론트엔드로 구성된 쇼핑몰/관리자 ERP 프로젝트다. 현재 MVP 이후 v0.1.1 ~ v0.1.8 작업을 통해 주요 관리자 운영 화면과 사용자 쇼핑 화면의 mock 데이터를 제거하거나 실제 API 흐름으로 정리했고, v0.1.9에서 계획표/체크리스트/태그/권한/mock 제거 상태를 최종 검증했다. v0.2.0부터는 결제, 인증/보안, DB/배포, 미디어, 운영 고도화 계획을 버전 단위로 진행했다. v0.3.0에서는 상품 마스터, 상세페이지 에디터, 카테고리/네비, 배너 CMS, 상품 판매 상태, 운영 UX, 사용자 화면 반영을 v0.3.1 ~ v0.3.9 작업으로 나누어 계획했다. v0.3.1에서는 상품 마스터 필드를 확장했고, v0.3.2에서는 상품별 상세페이지를 블록 기반 CMS 구조로 확장했다. v0.3.3에서는 쇼핑몰 상단 네비를 관리자 카테고리 설정 기반으로 전환했으며, v0.3.4에서는 메인 배너를 관리자 CMS 데이터 기준으로 렌더링하도록 전환했다. v0.3.5에서는 판매/전시/재고 상태 기준을 분리했고, v0.3.6에서는 관리자 상품 목록 필터, 선택 기반 대량 상태 변경, 운영 메모, 상태 변경 이력 추적을 추가했다. v0.3.7에서는 사용자 메인/상품 목록/상품 상세에 관리자 상품/전시 데이터를 반영하고 구매 불가 사유와 재고 상태 표시를 정리했다.

## 완료된 주요 흐름

| 영역 | 현재 상태 |
| --- | --- |
| 인증 | 회원가입, 로그인, 내 정보 조회, JWT Bearer 인증, refresh token, 로그아웃 |
| 쇼핑몰 공개 접근 | 메인/상품 목록/상품 상세/카테고리/배너/공개 리뷰/상품 문의는 비회원 접근 가능, 공개 API 401은 로그인 리다이렉트 제외 |
| 상품/카테고리 | 사용자 상품 목록/상세, 관리자 상품 목록/상세/등록/수정/삭제, 상품 옵션 저장, 상품 이미지 업로드 |
| 상품 마스터 | 상품코드, 브랜드, 제조사, 모델명, 원산지, 정상가/할인금액/매입가, 검색 키워드, 태그, 판매 기간, 배송/SEO 필드 |
| 상품 상세 CMS | 관리자 상품 수정 화면에서 상세 블록 조회/저장, 사용자 상세에서 visible 블록 정렬 렌더링 |
| 카테고리 네비 | 카테고리 parent/depth/sortOrder/active/visibleInNav/slug, 관리자 관리 화면, 사용자 상단 네비 API 렌더링 |
| 메인 배너 CMS | `main_banners` 테이블, 공개 배너 조회 API, 관리자 배너 등록/수정/비활성화, 사용자 메인 API 렌더링 |
| 상품 운영 UX | 관리자 상품 목록 카테고리/재고/판매기간 필터, 체크박스 선택, 대량 상태 변경, 운영 메모, 상태 변경 이력 |
| 사용자 전시 화면 | 메인 카테고리 바로가기, 상품 카드, 상품 목록, 상품 상세에 공개 상품/카테고리/재고/구매 가능 데이터 반영 |
| 장바구니 | 조회, 추가, 수량 변경, 삭제 |
| 주문 | 주문 생성, 내 주문 목록/상세, 주문 취소, 관리자 주문 목록/상태 변경 |
| 비회원 주문 기반 | `/orders/guest` 비회원 주문조회 진입점과 주문번호/연락처/비밀번호 기반 조회 설계 문서화, 실제 생성/조회 API는 후속 주문 버전으로 이관 |
| 결제 | 결제 승인 API, 하위 호환 모의 결제 완료 API, 결제 취소/환불 API, 멱등성 키 |
| 미디어 | 관리자 상품 이미지 업로드, 로컬 파일 저장, `media_files` 메타데이터, `/uploads/**` 공개 조회 |
| 마이페이지 | 내 정보, 주문, 문의, 반품 요약 API 연결 |
| 위시리스트 | 찜 토글, 목록, 상태 조회 |
| 리뷰 | 사용자 리뷰 작성/조회/삭제, 관리자 리뷰 목록/숨김/해제/삭제, 숨김 리뷰 사용자 미노출 |
| 감사 로그 | 주요 관리자 변경 작업, 권한 실패, 요청 컨텍스트, before/after/metadata JSON 기록과 필터/상세 조회 |
| 알림 | 주문 상태 변경, 문의 답변, 반품 처리 사용자 알림 기록과 읽음 처리 API |
| 운영 분석 | 회계/매출/창고 기초 지표를 모아 제공하는 관리자 overview API |
| DB 마이그레이션 | Flyway `V1__initial_schema.sql`과 DB 마이그레이션 운영 기준 문서 추가 |
| CI/CD/환경 | GitHub Actions lint/build/test 자동 검증, prod profile, env example, CORS origin 설정 분리 |
| 관리자 권한 | `MANAGER`는 운영 조회 중심 GET API 접근, 변경/회계/쿠폰/감사/리뷰 운영은 ADMIN 이상 |
| 문의 | 상품/일반 문의, 내 문의, 관리자 답변/종료 |
| 고객 관리 | 관리자 고객 목록과 주문 수/누적 주문 금액 표시 |
| 대시보드/매출 | 전체/오늘 주문/매출, 상태별 주문 수, 재고부족, 인기 상품, 기간 매출 |
| 회계 | 매출/환불/입고 요약과 회계 내역 목록 |
| 회계 원장/거래 | v0.7.1 기준 `accounting_ledgers`, `accounting_transactions` 모델과 관리자 원장/거래 조회 API, 회계 화면 조회 섹션 구현 |
| 주문 매출 인식 | v0.7.2 기준 결제 완료 시 주문 단위 `SALES` 회계 거래를 중복 방지로 생성하고, 관리자 매출 인식 조회/수동 실행 API를 제공 |
| 재고 | 관리자 재고 목록, 입고, 조정, 로그 조회 |
| SKU/바코드 | SKU/바코드 마스터, 바코드 재발급, 바코드 라벨 미리보기, 출력 이력 기록, 바코드 기반 재고 조회/입고/출고 |
| 재고 실사 | 창고별 실사 세션, SKU 품목 snapshot, 실사 수량 입력, 차이 수량 계산, 완료 시 재고 조정 |
| 창고 위치 | 창고 위치 목록/생성/수정/활성 상태 변경, 위치별 SKU 재고 조회 기반 |
| 안전재고 알림 | SKU 또는 SKU+창고 단위 안전재고 기준, 재고 부족 항목 조회, 관리자 화면 |
| 배송/출고 | 사용자 주문 배송 조회, 관리자 송장 등록/자동 생성/수정/송장 라벨/배송 추적/배송 완료, 주문 기준 출고 지시 목록/상세/생성/수정/피킹/취소, 택배사/배송 방법 관리 |
| 반품 | 사용자 반품 요청/목록, 관리자 승인/거절, 재고/회계 연동 |
| 쿠폰 | 쿠폰 검증, 관리자 쿠폰 목록/생성/삭제 |
| 창고 | 창고 목록/등록, 창고별 재고, 재고 할당, 창고 간 재고 이동 |

## v0.1.1 ~ v0.1.9 변경 이력 요약

- `v0.1.1`: 관리자 문의 관리 실제 API 연결.
- `v0.1.2`: 관리자 리뷰 관리 실제 API 연결. 리뷰 상태 변경은 미구현으로 분리하고 삭제 API 중심으로 정리.
- `v0.1.3`: 관리자 회계/매출 화면을 실제 회계 API로 정리.
- `v0.1.4`: 관리자 판매/매출 분석 화면을 대시보드 통계 API 기준으로 정리.
- `v0.1.5`: 관리자 창고 관리 화면과 창고/창고재고/재고이동 API 연결.
- `v0.1.6`: 사용자 쇼핑 화면 mock 사용 여부 점검, 미사용 `features/*/mock.ts` 제거, 주요 사용자 화면 에러/빈 상태 보강.
- `v0.1.7`: API/DB/프론트/백엔드/현재 상태 문서를 실제 코드 기준으로 동기화.
- `v0.1.8`: 공통 API 실패/권한 메시지와 사용자 주요 액션의 화면 내 오류 표시를 보강.
- `v0.1.9`: v0.1 계획 대비 완료 여부, 체크리스트/태그, mock 제거, JWT/권한 흐름, 주요 수동 확인 항목을 릴리스 기준으로 검증.
- `v0.2.0`: v0.1.9에서 이관한 남은 이슈를 인증/결제/미디어/DB/CI/알림/운영 분석 흐름으로 나누어 v0.2 계획을 수립.
- `v0.2.1`: 로그아웃, refresh token 재발급, 401 세션 만료 UX, 관리자 보호 레이아웃 접근 제어를 보강.
- `v0.2.1.1`: 관리자 주문/재고/상품 DataTable 관리 컬럼의 React key 중복 경고를 hotfix로 수정.
- `v0.2.2`: 결제 승인 API, 결제 취소 API, 승인 멱등성 키, checkout 결제 호출 전환을 구현.
- `v0.2.3`: 상품 이미지 업로드 API, 미디어 메타데이터, 관리자 업로드 UI, 이미지 fallback 처리를 구현.
- `v0.2.4`: 리뷰 상태 `VISIBLE/HIDDEN/DELETED`, 관리자 숨김/해제 API, 리뷰 운영 감사 로그를 구현.
- `v0.2.5`: Flyway 의존성, 초기 MySQL DDL, 인덱스/FK 기준, DB 마이그레이션 운영 문서를 추가.
- `v0.2.6`: GitHub Actions CI, `application-prod.yml`, 환경 변수 예시, CORS origin 설정 분리, 배포/환경 문서를 추가.
- `v0.2.7`: 사용자 알림 도메인/API, 알림 DDL, 주문/문의/반품 이벤트 알림 기록, MANAGER 조회 권한 정책을 추가.
- `v0.2.8`: `GET /api/admin/ops-analytics/overview`로 회계/매출/창고 기초 지표를 제공하고, 고급 회계/BI/WMS 후속 범위를 문서화.
- `v0.2.9`: v0.2 계획 대비 체크리스트/태그/mock/JWT 권한 흐름을 최종 검증하고, v0.3.0 ~ v0.9.0 장기 로드맵을 추가.
- `v0.3.0`: 상품/전시/운영관리 고도화 계획을 수립하고 v0.3.1 ~ v0.3.9 작업 순서를 정의.
- `v0.3.1`: 상품 마스터 필드 확장, 관리자 전용 상품 응답 DTO 분리, 상품 마스터 입력/수정 UI, Flyway V3 마이그레이션 추가.
- `v0.3.2`: 상품 상세 블록 테이블/API, 관리자 블록 편집 UI, 사용자 상세 블록 렌더러, Flyway V4 마이그레이션 추가.
- `v0.3.3`: 카테고리 트리/노출/정렬 필드, 관리자 카테고리 관리 화면, 사용자 상단 네비 API 전환, Flyway V5 마이그레이션 추가.
- `v0.3.3.1`: v0.3.4 전 프론트 한글화 hotfix. 관리자 사이드바/카테고리/상품 마스터/상세 블록/이미지 업로드와 사용자 메인/상품 카드/상품 상세의 주요 영어 UI 문구를 한국어로 정리.
- `v0.3.4`: 메인 배너 CMS. `main_banners` 테이블/Flyway V6, 공개 배너 조회 API, 관리자 배너 관리 화면, 사용자 메인 배너 API 렌더링 전환.
- `v0.3.5`: 상품 판매 상태/전시 상태/soft delete/안전재고 기준을 분리하고 구매 가능 여부와 사용자 재고 표시를 반영.
- `v0.3.5.1`: 관리자 사이드바를 업무 그룹형 메뉴와 설정 foundation으로 재구성.
- `v0.3.5.2`: 관리자 사이드바를 단일 openGroupLabel 아코디언으로 보정하고 중복 active 경로를 정리.
- `v0.3.6`: 관리자 상품 목록 필터/선택/대량 상태 변경, 상품 운영 메모, 상품 상태 변경 이력, 상품 운영 감사 로그를 구현.
- `v0.3.7`: 사용자 메인/상품 목록/상품 상세의 깨진 문구를 정리하고, CMS 배너/관리자 카테고리 네비/상품 카드/구매 가능 여부 표시를 공개 API 데이터 기준으로 정리.

## v0.3 계획 요약

- `v0.3.1`: 상품 마스터 고도화.
- `v0.3.2`: 상품 상세페이지 에디터.
- `v0.3.3`: 카테고리/상단 네비 관리자 관리.
- `v0.3.4`: 메인 배너 CMS.
- `v0.3.5`: 상품 판매 상태/품절/일시중지 운영.
- `v0.3.6`: 상품 운영 UX/대량 관리/상태 이력.
- `v0.3.7`: 상품/전시 사용자 화면 반영.
- `v0.3.8`: v0.3 통합 검증.
- `v0.3.9`: v0.4 인사관리/권한관리 이관 정리.

## 검증 기준 이력

최근 각 버전 작업에서 아래 검증을 실행했다.

- frontend `npm.cmd run lint`
- frontend `npm.cmd run build`
- backend `.\gradlew.bat test`

Windows 환경에서 기본 `.next` 디렉터리 EPERM 잠금이 발생하는 경우 `NEXT_DIST_DIR=.next-build-check-v0xx` 형식의 별도 빌드 디렉터리로 검증하고, 완료 후 임시 산출물을 삭제한다.

v0.1.9에서는 아래 항목을 릴리스 검증 기준으로 추가 확인한다.

- `v0.1.1` ~ `v0.1.8` 체크리스트 파일 존재.
- `v0.1.1` ~ `v0.1.8` 태그 존재.
- 관리자/사용자 화면의 기존 mock 데이터 import 제거.
- `SecurityConfig`와 프론트 `apiClient`의 JWT Bearer/401/403 처리 흐름.
- 주문/상품/재고/배송/반품/쿠폰/문의/리뷰/회계/창고 주요 흐름의 문서 기준 확인 항목.

## 현재 명시적 미구현/예정 항목

| 항목 | 상태 |
| --- | --- |
| 실제 PG 벤더 키/웹훅/리다이렉트 연동 | 미구현. 현재 `/api/payments/approve`는 `MOCK_PROVIDER` 기반 |
| 상품 이미지 업로드 | 구현. 관리자 상품 등록/수정에서 파일 업로드와 URL 직접 입력을 함께 지원 |
| 상품 상세페이지 블록 에디터 | 구현. 관리자 블록 편집과 사용자 visible 블록 렌더링 지원 |
| 메인 배너 CMS | 구현. 활성/노출 기간 기준 공개 배너 조회와 관리자 배너 관리 화면 제공 |
| 상품 판매/전시 상태 분리 | 구현. `salesStatus`, `displayStatus`, soft delete, 안전재고, 구매 가능 여부와 재고 표시 응답 제공 |
| 상품 운영 메모/상태 이력 | 구현. `product_operation_notes`, `product_status_histories` 테이블과 관리자 조회/작성/대량 상태 변경 API 제공 |
| 사용자 상품 전시 반영 | 구현. 상품 카드와 상세에서 브랜드, 태그, 가격/할인, 재고 상태, 구매 불가 사유를 표시 |
| v0.3 통합 검증 | 완료. v0.3.0 계획표 기준으로 v0.3.1 ~ v0.3.7 태그/체크리스트, API, DB, 프론트, 권한/노출 정책을 확인 |
| v0.3 종료 및 v0.4 이관 | 완료. v0.3 남은 이슈를 정리하고 v0.4 인사관리/권한관리 계획, DB/API/권한 후보를 문서화 |
| v0.4 인사관리/권한관리 계획 | 완료. 직원/부서/직급, 권한 그룹, 메뉴/기능 권한, API 접근 제어, 감사 로그 확장, 사업자/약관 설정 저장 범위를 확정 |
| 직원/부서/직급 기본 모델 | 구현. `departments`, `positions`, `staff_profiles` 테이블과 `User` 1:1 직원 프로필 연결, 관리자 HR 조회 API 기반 추가 |
| 직원 관리 API/UI | 구현. `/api/admin/staff` 직원 목록/상세/등록/수정/재직 상태/활성 상태 API와 `/admin/settings/staff` 화면 제공 |
| 권한 그룹/역할 관리 | 구현. `permission_groups`, `user_permission_groups` 기반, 권한 그룹 CRUD, 직원별 권한 그룹 할당, 역할/권한 안내 화면 제공 |
| 메뉴/기능 권한 매트릭스 | 구현. `permissions`, `permission_group_permissions`, `admin_menu_permissions` 기반, 권한 그룹별 기능 권한과 메뉴별 필요 권한 관리 화면/API 제공 |
| 관리자 사이드바 permission 연동 | 구현. 현재 사용자 effective permission과 `admin_menu_permissions.menu_key` 기준으로 사이드바 메뉴와 직접 URL 접근 UX를 제어 |
| API permission 정책 | 구현. `PermissionChecker`로 주요 관리자 API의 세부 실행 권한을 permission code 기준으로 검증하고, role 기반 1차 접근 정책과 병행 |
| 감사 로그 확장 | 구현. `audit_logs`에 요청 컨텍스트와 JSON 필드를 추가하고 주요 관리자 변경 작업 및 `PERMISSION_DENIED` 이력을 기록 |
| 사업자/약관 설정 저장 | 구현. `business_settings`, `terms_versions` 기반으로 사업자 정보 저장과 이용약관/개인정보처리방침/배송반품정책 버전 생성을 제공 |
| v0.4.9 릴리스 검증 | 완료. v0.4.0~v0.4.8 태그/체크리스트, HR/권한/감사 로그/사업자 설정 구현과 문서 정합성을 확인하고 v0.5 재고/생산/바코드 계획으로 이관 |
| v0.5 재고/생산/바코드 계획 | 완료. SKU/바코드, 생산 입고, 재고 movement, 실사/조정, 창고 위치, 안전재고 알림, v0.6 출고/송장 이관 기준을 `V0.5_INVENTORY_BARCODE_PLAN.md`에 확정 |
| SKU/바코드 재고 마스터 | 구현. `skus` 테이블, 관리자 SKU API, `/admin/skus` 화면, `SKU_MANAGE`/`BARCODE_MANAGE` 권한과 audit log 기록을 추가 |
| 생산 입고 흐름 | 구현. `production_orders`, `production_order_items`, `production_receipts` 기반으로 생산 주문과 완료 입고 처리를 추가하고 완료 시 상품/창고 재고와 재고 로그를 갱신 |
| 바코드 라벨 출력 기반 | 구현. `barcode_labels` 테이블, 바코드/SKU 검색 API, 라벨 HTML 미리보기, 출력 이력 기록, `/admin/barcodes` 화면 제공 |
| 바코드 입출고 | 구현. `/api/admin/barcodes/{barcode}/stock|inbound|outbound`와 `/admin/barcode-stock` 화면으로 바코드 기준 SKU/상품/창고 재고 조회와 간단 입고/출고 처리 제공 |
| 재고 실사/조정 | 구현. `stock_count_sessions`, `stock_count_items` 기반으로 재고 실사 세션과 품목을 관리하고 완료 시 차이 수량을 상품/창고 재고와 `InventoryLog(ADJUST)`에 반영 |
| 창고 위치/로케이션 | 구현. `warehouse_locations`, `warehouse_location_stocks` 기반으로 창고 내부 위치와 위치별 SKU 재고 조회 기반을 추가하고 `/admin/warehouse-locations` 화면을 제공 |
| 안전재고 알림 | 구현. `inventory_alert_rules` 기반으로 SKU/창고 안전재고 기준을 관리하고 기준 이하 재고 항목을 `/admin/inventory-alerts`에서 조회 |
| v0.5 UI 검증 | 완료. SKU/바코드/생산/재고/실사/창고 위치/안전재고 관리자 화면 파일과 문서 정합성을 확인하고 lint/build/test 검증 기준으로 정리 |
| v0.5 릴리스 검증/이관 | 완료. v0.5.0 ~ v0.5.8 태그/체크리스트, 재고/생산/바코드 구현, 권한/audit log/문서 정합성을 확인하고 v0.6 유통관리 계획으로 이관 |
| v0.6 유통관리/송장/배송 | 완료. 출고 지시, 택배사/배송 방법, 송장번호, 송장 라벨, 배송 추적, 반품 배송 정보, 바코드 출고 검수, 관리자 UI 검증을 완료하고 v0.7 회계/정산 고도화 후보로 이관 |
| v0.7 회계/정산 이관 | 준비 완료. 배송비, 환불 배송비, 택배비 매입/매출, 출고 완료 기준 매출 확정 후보를 `docs/plans/V0.7_ACCOUNTING_SETTLEMENT_PLAN.md`에 정리 |
| v0.7 회계관리 고도화 계획 | 완료. v0.7.0에서 회계 원장/거래 유형, 매출 인식, 환불/반품 배송비, 택배비 매입, 정산/마감, 리포트, 권한/감사 로그, v0.8 AI 데이터셋 이관 후보를 확정. 기능 구현은 v0.7.1부터 진행 |
| v0.7.1 회계 원장/거래 유형 모델 | 구현. 원장/거래 테이블, enum, 조회 API, 관리자 회계 화면 조회 섹션, `ACCOUNTING_MANAGE` 권한 seed를 추가 |
| v0.7.2 주문 매출 인식 기준 | 구현. 결제 완료 시 주문 매출 회계 거래를 생성하고, 주문별 매출 인식 조회/실행과 매출 이벤트 목록 API를 추가 |
| 송장번호 관리 | 구현. `shipments`에 송장 발급 방식/발급 시각을 저장하고, 관리자 배송 화면에서 수동 입력, 내부 테스트용 자동 생성, 배송중 수정, 배송완료 처리를 제공 |
| 송장 라벨 출력 | 구현. `shipment_labels` 기반으로 송장 라벨 HTML 미리보기와 출력 이력 기록을 제공. 실제 프린터 SDK/PDF 출력은 후속 범위 |
| 배송 상태 추적 | 구현. `shipment_tracking_events` 기반으로 관리자 배송 상태 변경과 수동 추적 이벤트 조회/등록을 제공. 실제 택배사 tracking API와 웹훅 자동 갱신은 후속 범위 |
| 반품 배송 흐름 | 구현. `return_shipment_infos` 기반으로 관리자 반품 화면에서 수거 송장, 수거 상태, 반품 배송비, 배송비 부담 주체를 수동 관리 |
| 출고 바코드 검수 | 구현. `outbound_scan_logs` 기반으로 관리자 출고 화면에서 SKU 바코드 검수 수량과 스캔 이력을 기록 |
| 유통관리 UI 검증 | 완료. `/admin/outbound-orders`, `/admin/carriers`, `/admin/shipping-methods`, `/admin/shipments`, `/admin/returns`와 관련 서비스/문서 정합성을 확인 |
| 쇼핑몰 공개 접근 hotfix | 구현. `publicApiClient`를 분리해 공개 상품/카테고리/배너/리뷰/문의 조회 401이 로그인 리다이렉트를 유발하지 않도록 보정하고 `/orders/guest` 비회원 주문조회 진입점 추가 |
| 관리자 네비게이션/설정 foundation | 구현. 관리자 사이드바를 업무 그룹형 아코디언으로 정리하고 `/admin/settings`, `/admin/settings/audit-logs` 진입점 추가 |
| 관리자 사이드바 아코디언 UX | 개선. 단일 그룹 open 상태와 query string active 판정으로 중복 active/다중 펼침 문제 보정 |
| 리뷰 숨김/상태 변경 | 구현. 사용자 리뷰 목록은 `VISIBLE`만 노출하고 관리자는 숨김 리뷰까지 조회 |
| 고급 회계/복식부기/정산 마감 | 미구현. 현재 단일 `accounting_entries` 기준 요약/내역 |
| 고급 BI/차트 | 미구현. 현재 API 기반 기본 지표 표시 |
| 피킹/패킹/출고 자동화 | 일부 구현. v0.6.1 기준 주문 기반 출고 지시와 피킹 완료 처리를 제공하며, 바코드 검수/재고 차감/패킹 자동화는 후속 범위 |
| 운영 DB 마이그레이션 | 구현 시작. Flyway `V1__initial_schema.sql`과 운영 기준 문서 추가 |
| CI/CD | 구현 시작. GitHub Actions에서 frontend lint/build와 backend test 자동 검증 |
| 감사 로그/관리자 작업 이력 | 구현 확장. 직원/권한/상품/카테고리/배너/주문/결제/재고/창고/쿠폰/문의/리뷰/권한 실패 이력을 기록하고 필터/상세 조회 UI 제공 |
| 알림/이메일 | 알림 DB/API는 구현. 실제 이메일/SMS 발송은 미구현 |
| 운영 분석/BI | 기초 overview API는 구현. 고급 차트/데이터 웨어하우스/정산 리포트는 미구현 |
| S3/CDN/썸네일/이미지 리사이징 | 미구현. 현재 로컬 저장과 공개 URL 제공 |

## v0.3 이후 이관 후보

- 실제 PG 벤더 키/웹훅/리다이렉트 연동과 거래 원장 분리.
- refresh token HttpOnly cookie 저장, 서버 저장소 기반 rotation, 토큰 탈취 감지.
- S3/CDN, 썸네일, 이미지 리사이징, 다중 이미지 갤러리.
- 감사 로그 다운로드/리포트, 장기 보관/아카이빙, 외부 SIEM 연동.
- 운영 DB migration dry-run, rollback 절차, 배포 환경별 DB 계정/권한 분리.
- 실제 배포 자동화, 운영 서버 프로비저닝, 무중단 배포, 환경별 secret 주입 검증.
- CI/CD 자동 검증과 배포 환경 분리.
- 고급 회계/정산/마감, 고급 BI/차트.
- 고급 WMS 피킹/패킹/출고 자동화.
- 관리자 권한 세분화, 감사 로그, 개인정보/보안 정책.
- 알림, 이메일, SMS 연동.
- 상품/전시/운영관리 고도화.
- 상품 상세 SEO metadata 반영, 상세 블록 HTML sanitizer, 배너 전용 이미지 업로드/리사이징, 전시 섹션/기획전 CMS.
- 인사관리와 역할 기반 권한관리.
- 직원/부서/직급 기본 모델, 권한 그룹, 메뉴별/기능별 권한, 관리자 사이드바/API 권한 고도화.
- 권한 실패 audit log 전체 기록, annotation/AOP 기반 permission 프레임워크 고도화.
- 재고관리, 생산 입고, 바코드 자동화.
- v0.5 재고관리/생산 입고/바코드 자동화 계획 후보는 [V0.5_INVENTORY_BARCODE_PLAN.md](../plans/V0.5_INVENTORY_BARCODE_PLAN.md)에 정리했다.
- 택배사 연동, 송장번호 생성/출력 중심 유통관리.
- 매입·매출·정산 중심 회계관리 고도화.
- 상품/리뷰/재고/주문 데이터셋과 `.pt` 모델 학습 구조.
- AI 추천, 수요 예측, 리뷰 분석, 이상 주문 탐지.

## 문서 기준

- [API_REFERENCE.md](../architecture/API_REFERENCE.md): 실제 Controller 기준 API 목록.
- [DATABASE_SCHEMA.md](../architecture/DATABASE_SCHEMA.md): 실제 Entity 기준 논리 스키마.
- [DB_MIGRATION.md](../architecture/DB_MIGRATION.md): Flyway migration 파일 규칙과 운영 반영 기준.
- [DEPLOYMENT_ENV.md](./DEPLOYMENT_ENV.md): CI와 환경 변수, 운영 profile 기준.
- [FRONTEND_STRUCTURE.md](../architecture/FRONTEND_STRUCTURE.md): 실제 Next route와 service 구조.
- [BACKEND_STRUCTURE.md](../architecture/BACKEND_STRUCTURE.md): 실제 domain/controller/service/repository 구조.
- [VERSION_PLAN.md](../plans/VERSION_PLAN.md): 버전 작업 계획과 완료 조건.
- [LONG_TERM_ROADMAP.md](../plans/LONG_TERM_ROADMAP.md): v0.3 이후 AI 커머스 ERP 장기 로드맵.
- [AI_DEVELOPMENT_DEPLOYMENT_PLAN.md](../ai/AI_DEVELOPMENT_DEPLOYMENT_PLAN.md): v0.8/v0.9 AI 개발/학습/추론/배포 전략.
- [V0.3_COMMERCE_OPS_PLAN.md](../plans/V0.3_COMMERCE_OPS_PLAN.md): v0.3 상품/전시/운영관리 고도화 계획.
- [V0.4_HR_PERMISSION_PLAN.md](../plans/V0.4_HR_PERMISSION_PLAN.md): v0.4 인사관리/권한관리 계획.
- [V0.5_INVENTORY_BARCODE_PLAN.md](../plans/V0.5_INVENTORY_BARCODE_PLAN.md): v0.5 재고관리/생산 입고/바코드 자동화 계획과 구현 기준.
- [V0.6_DISTRIBUTION_SHIPPING_PLAN.md](../plans/V0.6_DISTRIBUTION_SHIPPING_PLAN.md): v0.6 유통관리/송장/배송 계획과 구현 기준.
- [V0.7_ACCOUNTING_SETTLEMENT_PLAN.md](../plans/V0.7_ACCOUNTING_SETTLEMENT_PLAN.md): v0.7 회계관리 고도화 예정 범위.



## v0.7.3 환불/반품 배송비 회계 처리

- 결제 환불, 반품 승인 환불, 반품 배송비 부담 주체를 신규 회계 거래로 반영하는 기반을 추가했다.
- 반품 배송비 회계 실행 권한으로 `RETURN_FEE_MANAGE`를 추가했다.
- `backend/.gradle-cache/`는 로컬 Gradle 캐시 전용 경로로 `.gitignore`에 추가했다.

## v0.7.4 택배비 매입/배송비 정산 진행 상태

- 배송 상태가 배송중 또는 배송완료가 될 때 배송 방법 기본비 기준으로 택배비 비용 후보를 생성한다.
- `SHIPPING_COST` 회계 거래를 `referenceType=SHIPMENT` 기준으로 중복 없이 생성한다.
- 주문에 고객 청구 배송비 컬럼이 없어 `chargedAmount`는 0으로 보수 처리하고, 배송비 매출 분리는 후속 설계로 이관한다.

## v0.7.5 정산 배치/마감 진행 상태

- 기간별 회계 거래를 정산 배치와 정산 항목으로 묶는 기반을 추가했다.
- 정산 배치 생성, 조회, 마감 API와 중복 생성/중복 마감 방지 기준을 추가했다.
- 마감 후 회계 잠금과 외부 정산 파일 연동은 후속 버전으로 이관한다.

## v0.7.6 회계 리포트 UI 진행 상태

- `/admin/accounting` 화면에서 회계 요약, 원장, 거래, 택배비 비용, 정산 배치를 확인할 수 있게 정리했다.
- 기존 깨진 한글 문구를 회계 화면 범위에서 정상 한국어로 교체했다.
## v0.7.7 회계 권한/감사 로그 연동

- 회계/정산 관리자 API의 permission code 매핑과 감사 로그 기록 기준을 점검했다.
- 회계 실행 권한은 `ACCOUNTING_READ`, `ACCOUNTING_MANAGE`, `PAYMENT_REFUND`, `RETURN_FEE_MANAGE`, `SHIPPING_COST_MANAGE`, `SETTLEMENT_MANAGE`, `ACCOUNTING_CLOSE` 기준으로 정리했다.
- DB에 표시되는 회계 권한/메뉴 라벨은 `V34__normalize_accounting_permission_audit_labels.sql`에서 정상 한국어로 보정한다.

## v0.7.8 회계 검증 데이터 정합성

- 관리자 회계 API에 `GET /api/admin/accounting/consistency-report`를 추가했다.
- 주문/결제/반품/배송 원천 데이터 기준으로 매출, 환불, 반품 배송비, 택배비 회계 거래 누락 후보를 조회한다.
- 관리자 회계 화면에 정합성 점검 섹션을 추가하고 깨진 한국어 문구를 정상화했다.

## v0.7.9 회계/정산 릴리스 검증

- v0.7.0 ~ v0.7.8 태그와 체크리스트 존재 여부를 확인했다.
- 회계/정산 고도화 범위는 원장/거래, 매출 인식, 환불/반품비, 택배비, 정산 배치, 리포트 UI, 권한/감사 로그, 정합성 점검까지 완료된 상태다.
- v0.8 AI 데이터셋 구축 후보를 `docs/plans/V0.8_AI_DATASET_PLAN.md`에 이관했다.
## v0.8.0 AI 데이터셋/학습 계획

- v0.8은 상품, 주문, 재고, 배송, 리뷰, 문의, 회계/정산 데이터를 AI 학습 데이터셋으로 정리하는 단계로 확정했다.
- v0.8.0 ~ v0.8.9 작업 순서, 데이터셋 후보, 개인정보/보안 기준, 학습 파이프라인 후보 구조를 `docs/plans/V0.8_AI_DATASET_PLAN.md`에 정리했다.
- 실제 데이터 export API, 마스킹 유틸, `.pt` 모델 학습 코드는 v0.8.1 이후 구현한다.

## v0.8.1 AI 데이터셋 추출 기반

- `GET /api/admin/ai/datasets`와 `GET /api/admin/ai/datasets/{key}/export` API를 추가했다.
- 상품, 주문, 리뷰, 회계 거래 데이터셋 샘플 rows를 JSON 형태로 조회할 수 있다.
- `AI_DATASET_EXPORT` 권한과 관리자 메뉴 seed를 추가했다.

## v0.8.2 AI 데이터셋 개인정보 마스킹

- AI 데이터셋 export 응답에 `privacyMasked` 플래그를 추가했다.
- 리뷰 본문 export 과정에 이메일, 휴대폰 번호, 주민등록번호 형태, 토큰/비밀번호 형태, 주소 패턴 힌트 마스킹을 적용했다.
- 고급 개인정보 탐지와 파일 보관 정책은 후속 범위로 유지한다.

## v0.8.3 상품/리뷰 학습 데이터셋

- `PRODUCT_REVIEWS` dataset key를 추가했다.
- 상품 마스터 속성과 리뷰 평점/본문/상태를 결합한 export rows를 제공한다.
- 리뷰 본문은 v0.8.2의 개인정보 마스킹을 거친다.

## v0.8.4 주문/수요 예측 데이터셋

- `ORDER_DEMAND` dataset key를 추가했다.
- 주문일, 시간, 요일, 총액, 할인액, 순금액, 주문 상태, 결제 상태를 수요 예측 피처로 제공한다.

## v0.8.5 재고/배송 데이터셋

- `INVENTORY_SHIPPING` dataset key를 추가했다.
- `SHIPPING_LEADTIME` dataset key를 추가했다.
- SKU/창고 재고 피처와 배송 리드타임 피처를 export할 수 있다.

## v0.8.6 회계/정산 데이터셋

- `SETTLEMENT_BATCHES` dataset key를 추가했다.
- `ACCOUNTING_CONSISTENCY_ISSUES` dataset key를 추가했다.
- 정산 배치 피처와 회계 정합성 이슈 피처를 AI 학습 후보로 export할 수 있다.
- AI 데이터셋 카탈로그의 깨진 한글 표시 라벨을 정상 한국어로 정리했다.

## v0.8.7 `.pt` 모델 학습 파이프라인

- `ai/` 디렉터리에 데이터셋, 모델, 설정, 학습 스크립트 기준 구조를 추가했다.
- `train_baseline.py`는 JSON/JSONL/CSV 입력을 읽어 baseline 학습 메타데이터를 생성한다.
- PyTorch가 설치된 환경에서는 `.pt` 체크포인트를 생성하고, 미설치 환경에서는 메타데이터 저장으로 입력 검증을 수행한다.
- export 데이터, 전처리 데이터, 모델 체크포인트, `.pt`/`.pth`/`.onnx` 산출물은 Git 추적 대상에서 제외했다.

## v0.8.8 모델 평가/리포트

- `evaluate_baseline.py`를 추가해 검증 데이터셋과 체크포인트를 기준으로 baseline 평가를 수행할 수 있게 했다.
- PyTorch checkpoint가 있으면 MAE/RMSE를 계산하고, 없거나 평가할 수 없으면 사유를 리포트에 기록한다.
- 평가 결과는 JSON/Markdown 형태로 `ai/reports/generated/`에 저장하며 Git 추적 대상에서 제외한다.

## v0.8.9 AI 데이터셋/학습 기반 통합 검증

- v0.8.0 ~ v0.8.8 태그와 체크리스트 존재를 확인했다.
- AI 데이터셋 export, 개인정보 마스킹, 도메인별 데이터셋, 학습 스크립트, 평가 리포트 구조를 통합 검증했다.
- v0.9 AI 운영 기능 계획 후보를 `docs/plans/V0.9_AI_OPERATIONS_PLAN.md`로 이관했다.

## v0.8.10 포트폴리오 AI 데모 데이터

- 실제 운영 데이터가 없는 포트폴리오 환경을 위해 개인정보 없는 합성 샘플 데이터셋을 추가했다.
- 수요 예측과 리뷰 감성 baseline 학습/평가 config를 추가했다.
- `train_baseline.py`, `evaluate_baseline.py`의 깨진 한글 메시지를 정상 한국어로 정리했다.
- 데모 실행 방법은 `docs/ai/PORTFOLIO_AI_DEMO_GUIDE.md`에 정리했다.

## v0.9.0 AI 운영 기능 계획

- v0.9는 v0.8에서 만든 데이터셋/export/마스킹/학습/평가 기반을 관리자 운영 화면에 연결하는 단계로 확정했다.
- 실제 운영 데이터가 없는 포트폴리오 환경이므로 합성 샘플 데이터와 baseline `.pt` 모델을 기준으로 데모 가능한 AI 운영 흐름을 구성한다.
- v0.9.1 ~ v0.9.9 작업 순서, AI 결과 공통 응답 구조, 권한 후보, 제외 범위를 `docs/plans/V0.9_AI_OPERATIONS_PLAN.md`에 정상 한국어로 정리했다.
- v0.9 구현은 AI 운영 공통 기반, 상품 추천, 수요 예측, 리뷰 분석, 이상 주문 탐지, 재고/정산 리스크 알림, AI 리포트, 최종 데모 정리 순서로 진행한다.

## v0.9.1 AI 운영 공통 기반

- 관리자 AI 운영 overview/health API를 추가했다.
- `AiInsightResponse` 공통 응답 구조와 `LOW`, `MEDIUM`, `HIGH` risk level 기준을 추가했다.
- `AI_RECOMMENDATION_READ`, `AI_FORECAST_READ`, `AI_REVIEW_ANALYSIS_READ`, `AI_ANOMALY_READ`, `AI_RISK_ALERT_READ`, `AI_REPORT_READ`, `AI_OPERATIONS_MANAGE` permission code를 추가했다.
- 관리자 사이드바에 `AI 운영` 그룹과 `/admin/ai` 화면을 추가했다.
- v0.9.1 기준 AI 결과는 자동 처리 기능이 아니라 포트폴리오 데모 기반 관리자 참고 지표로 제공한다.

## v0.9.2 AI 상품 추천 후보

- 관리자 AI 운영 메뉴에 `/admin/ai/recommendations` 화면을 추가했다.
- `GET /api/admin/ai/recommendations/products` API로 상품 추천 후보를 조회한다.
- 추천 후보는 상품 노출/판매 상태, 재고, 태그, 검색 키워드, 이미지 여부, 판매가를 기준으로 rule-based 점수를 계산한다.
- 추천 결과는 자동 전시나 개인화 추천에 적용하지 않고 관리자 검토용 참고 지표로만 표시한다.

## v0.9.3 AI 수요 예측

- 관리자 AI 운영 메뉴에 `/admin/ai/demand-forecast` 화면을 추가했다.
- `GET /api/admin/ai/forecasts/demand` API로 상품별 수요 예측 후보를 조회한다.
- 현재 재고, 안전재고, 태그/검색 키워드 기반 데모 수요지수, 예상 재고일을 표시한다.
- 예측 결과는 자동 발주나 생산 지시에 반영하지 않고 관리자 보충 판단용 참고 지표로만 표시한다.

## v0.9.4 AI 리뷰 분석

- 관리자 AI 운영 메뉴에 `/admin/ai/review-analysis` 화면을 추가했다.
- `GET /api/admin/ai/reviews/analysis` API로 리뷰 감성 분석 후보를 조회한다.
- 리뷰 평점과 마스킹된 본문을 기준으로 긍정/중립/부정 확인 후보를 표시한다.
- 분석 결과는 자동 숨김이나 제재에 반영하지 않고 관리자 검토용 참고 지표로만 표시한다.

## v0.9.5 AI 이상 주문 탐지

- 관리자 AI 운영 메뉴에 `/admin/ai/order-anomalies` 화면을 추가했다.
- `GET /api/admin/ai/anomalies/orders` API로 이상 주문 후보를 조회한다.
- 고액 주문, 높은 할인율, 주문/결제 상태 불일치 후보를 기준으로 위험 점수를 표시한다.
- 탐지 결과는 자동 주문 차단에 반영하지 않고 관리자 검토용 참고 지표로만 표시한다.

## v0.9.6 AI 리스크 알림

- 관리자 AI 운영 메뉴에 `/admin/ai/risk-alerts` 화면을 추가했다.
- `GET /api/admin/ai/risks/inventory` API로 재고 부족 리스크 후보를 조회한다.
- `GET /api/admin/ai/risks/settlement` API로 정산 확인 필요 리스크 후보를 조회한다.
- 리스크 알림은 자동 입고나 정산 마감에 반영하지 않고 관리자 확인용 참고 지표로만 표시한다.

## v0.9.7 AI 리포트/근거 설명

- 관리자 AI 운영 메뉴에 `/admin/ai/reports` 화면을 추가했다.
- `GET /api/admin/ai/reports` API로 AI 운영 모듈별 리포트와 해석 기준을 조회한다.
- 리포트는 근거 데이터, 모델명, 해석 기준, 한계점을 카드 형태로 표시한다.
- 고급 설명 모델이나 외부 LLM 요약은 제외하고 포트폴리오 데모 설명용 구조로 유지한다.

## v0.9.8 포트폴리오 데모 정리

- `docs/ai/AI_OPERATIONS_DEMO_SCRIPT.md`를 추가해 관리자 AI 운영 화면 시연 순서와 발표용 설명 문구를 정리했다.
- 포트폴리오 환경에서는 실제 고객 데이터를 수집하지 않고 합성 샘플 데이터, baseline `.pt` 모델, rule-based 운영 지표를 조합해 AI 커머스 ERP 흐름을 설명한다.
- `docs/ai/PORTFOLIO_AI_DEMO_GUIDE.md`에서 데모 시나리오 문서를 연결했다.

## v0.9.9 최종 통합 검증

- v0.9.0 ~ v0.9.8 로컬 태그와 체크리스트 존재를 확인했다.
- v0.9 AI 운영 기능은 공통 기반, 상품 추천, 수요 예측, 리뷰 분석, 이상 주문 탐지, 재고/정산 리스크 알림, AI 리포트, 포트폴리오 데모 정리까지 연결되었다.
- backend test, frontend lint, frontend build, `git diff --check`를 통과했다. 기본 `.next` 빌드는 Windows EPERM으로 실패했으나, `NEXT_DIST_DIR=.next-build-check-v099`로 재실행해 통과했고 임시 산출물은 삭제했다.
- 실제 운영 데이터 기반 모델 성능 검증, 실시간 추론 서버, 완전한 MLOps 플랫폼, 외부 LLM 자동 업무 처리는 포트폴리오 범위에서 제외한다.

## v1.0.0 최종 포트폴리오 릴리스 계획

- v1.0은 신규 대형 기능 구현이 아니라 포트폴리오 제출 품질과 재현성을 마감하는 단계로 정리했다.
- `docs/plans/V1.0_FINAL_PORTFOLIO_RELEASE_PLAN.md`를 추가해 v1.0.0 ~ v1.0.4 작업 순서, 제외 범위, 검증 기준을 문서화했다.
- v1.0.1에서는 README와 핵심 문서 polish, v1.0.2에서는 데모 계정/데이터, v1.0.3에서는 smoke test, v1.0.4에서는 최종 태그 릴리스를 진행한다.
