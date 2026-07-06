# 버전 계획표

CommerceOps ERP는 현재 `main`에 올라간 상태를 MVP 기준선으로 둔다. 이후 모든 작업은 버전 브랜치와 태그 단위로 관리한다.

## 운영 원칙

- MVP 이후 첫 작업은 `v0.1.0`부터 시작한다.
- 모든 작업은 버전 브랜치에서 진행한다.
- 브랜치명은 `v{version}-{work}` 형식을 사용한다.
- 각 버전 완료 후 `main` 병합과 태그 생성을 진행한다.
- 모든 버전 작업은 체크리스트로 진행 흐름을 기록한다.
- 세부 규칙은 [버전 작업 규칙](./VERSION_WORKFLOW.md)을 따른다.
- 작업 체크리스트 템플릿은 [버전 작업 체크리스트](./templates/VERSION_TASK_CHECKLIST.md)를 사용한다.

## MVP 기준선

현재 `main`에 push된 상태를 MVP로 본다.

MVP 포함 범위:

- Spring Boot 백엔드 기본 구조
- Next.js 프론트엔드 기본 구조
- JWT 로그인/회원가입/내 정보 조회
- 상품, 옵션, 장바구니, 주문, 결제 mock 흐름
- 마이페이지 실제 API 연결
- 관리자 고객/주문/상품/대시보드/재고/배송/반품/쿠폰 일부 실제 API 연결
- 기본 문서와 API/DB/구조 문서
- frontend lint/build 통과 이력
- backend test 통과 이력
- GitHub 저장소 연결 및 첫 push 완료

MVP 태그는 별도 필요 시 `mvp` 또는 `v0.0.0-mvp`로 생성할 수 있다. 단, 이후 정식 버전은 `v0.1.0`부터 시작한다.

## v0.1 버전 계획

v0.1의 목적은 MVP 이후 남은 관리자/운영 화면을 실제 API 기준으로 정리하고, mock 제거 및 검증 가능한 기본 운영 흐름을 완성하는 것이다.

| 버전 | 브랜치 예시 | 목적 | 완료 조건 |
| --- | --- | --- | --- |
| `v0.1.0` | `v0.1.0-version-plan` | 버전 계획표와 작업 규칙 문서화 | 계획표, 체크리스트, 브랜치/태그 규칙 문서화 완료 |
| `v0.1.1` | `v0.1.1-admin-inquiries-api` | 관리자 문의 관리 실제 API 연결 정리 | 문의 목록/상세/답변 흐름 검증 |
| `v0.1.2` | `v0.1.2-admin-reviews-api` | 관리자 리뷰 관리 실제 API 연결 | 리뷰 목록/숨김/삭제 등 운영 흐름 검증 |
| `v0.1.3` | `v0.1.3-admin-accounting-api` | 관리자 회계/매출 화면 실제 API 정리 | 매출/환불/입고 금액 조회와 CSV 기준 정리 |
| `v0.1.4` | `v0.1.4-admin-sales-api` | 관리자 판매/매출 분석 화면 실제 API 정리 | 기간별 매출/주문/상품 지표 검증 |
| `v0.1.5` | `v0.1.5-admin-warehouses-api` | 창고 관리 화면 실제 API 흐름 정리 | 창고 목록/재고/이동/할당 흐름 검증 |
| `v0.1.6` | `v0.1.6-shop-mock-cleanup` | 사용자 쇼핑 화면 mock 제거 범위 정리 | 상품/카테고리/장바구니/주문 화면 mock 사용 여부 점검 및 교체 |
| `v0.1.7` | `v0.1.7-docs-api-db-sync` | API/DB/프론트 구조 문서 최신화 | 실제 코드 기준 문서 동기화 |
| `v0.1.8` | `v0.1.8-hardening-polish` | 예외 처리, 권한, 빈 상태, UX 정리 | 주요 화면 운영 안정성 보완 |
| `v0.1.9` | `v0.1.9-release-verification` | v0.1 계획 대비 구현 확인 및 테스트 검증 | 전체 체크리스트와 lint/build/test 결과 기록 완료 |

## v0.1 공통 완료 조건

각 버전 작업은 아래 조건을 만족해야 완료로 본다.

- [ ] 작업 브랜치명이 `v{version}-{work}` 규칙을 따른다.
- [ ] 해당 버전의 작업 목표가 이 문서 또는 별도 체크리스트에 기록되어 있다.
- [ ] mock 데이터 제거 여부를 확인했다.
- [ ] JWT/권한이 필요한 API는 기존 인증 흐름을 사용한다.
- [ ] 로딩 상태를 처리했다.
- [ ] 에러 상태를 처리했다.
- [ ] 데이터 없음 상태를 처리했다.
- [ ] TypeScript 타입과 백엔드 DTO 응답이 맞다.
- [ ] 필요한 경우 API/DB/구조 문서를 갱신했다.
- [ ] frontend `npm.cmd run lint`를 실행했다.
- [ ] frontend `npm.cmd run build`를 실행했다.
- [ ] backend `.\gradlew.bat test`를 실행했다.
- [ ] 검증 결과와 남은 이슈를 기록했다.
- [ ] `main` 병합 후 태그를 생성했다.

> PowerShell 명령 기록 시 백엔드 테스트 명령은 `backend` 디렉터리에서 `./gradlew.bat test` 또는 `.\gradlew.bat test`로 실행한다.

## v0.1.0 체크리스트

목적: 버전 계획표 문서화와 작업 규칙 확정.

- [x] MVP 기준선 정의
- [x] v0.1.0 ~ v0.1.9 역할 정의
- [x] v0.2.0 역할 정의
- [x] 브랜치명 규칙 정의
- [x] 태그 규칙 정의
- [x] 버전 작업 체크리스트 템플릿 추가
- [x] 보안/AI 작업 파일 커밋 제외 규칙 확인
- [x] v0.1.0 브랜치 생성
- [x] v0.1.0 커밋
- [x] v0.1.0 원격 브랜치 push
- [x] main 병합
- [x] `v0.1.0` 태그 생성 및 push

## v0.1.1 ~ v0.1.8 개발 체크리스트

각 개발 버전은 아래 흐름으로 진행한다.

- [ ] 버전 브랜치 생성
- [ ] 작업 체크리스트 작성
- [ ] 기존 API/서비스/화면 구조 확인
- [ ] mock 데이터 위치 확인
- [ ] 실제 API 연결 또는 최소 백엔드 API 구현
- [ ] 권한/JWT 흐름 확인
- [ ] 로딩/에러/빈 상태 처리
- [ ] 타입/DTO 정리
- [ ] 문서 갱신
- [ ] frontend lint/build 검증
- [ ] backend test 검증
- [ ] 변경 파일과 검증 결과 요약
- [ ] 커밋 및 push
- [ ] main 병합 후 태그 생성

## v0.1.9 검증 체크리스트

목적: v0.1.0 계획표처럼 구현되었는지 확인하고 테스트 검증한다.

- [ ] v0.1.0 계획 항목 대비 완료 여부 확인
- [ ] v0.1.1 ~ v0.1.8 태그 존재 확인
- [ ] 주요 관리자 화면 mock 제거 여부 확인
- [ ] 주요 사용자 화면 mock 제거 여부 확인
- [ ] JWT/권한 흐름 회귀 검증
- [ ] 주문/상품/재고/배송/반품/쿠폰/문의/회계 주요 흐름 수동 확인
- [ ] frontend `npm.cmd run lint` 통과
- [ ] frontend `npm.cmd run build` 통과
- [ ] backend `.\gradlew.bat test` 통과
- [ ] 임시 빌드 산출물 정리 확인
- [ ] 문서 최신화 확인
- [ ] 남은 이슈를 v0.2.0 계획으로 이관
- [ ] `v0.1.9` 태그 생성 및 push

## v0.2.0 계획

v0.2.0은 v0.1 버전이 잘 완료되었을 때 진행하는 고도화 및 추가 기능 계획 문서화 버전이다.

v0.2의 목적은 v0.1에서 실제 API 기반으로 연결한 운영 흐름을 제품 운영 수준으로 고도화하는 것이다. 우선순위는 결제, 인증/보안, DB/배포 안정성처럼 운영 리스크가 큰 항목을 먼저 둔다.

v0.2.0에서 v0.1.9 검증 결과를 반영해 정리한 후보:

- 실제 PG 결제/취소/환불 연동과 결제 멱등성 처리
- 로그아웃/리프레시 토큰 API
- 상품 이미지 업로드와 파일 메타데이터 관리
- 리뷰 숨김/상태 변경 운영 플로우
- 운영 배포 환경 분리
- DB 마이그레이션 체계, 실제 DDL, 인덱스/FK 운영 기준
- CI/CD 자동 검증
- 고급 회계/정산/마감 구조
- 고급 BI/차트
- 고급 WMS 피킹/패킹/출고 자동화
- 알림, 이메일, SMS
- 관리자 권한 세분화
- 감사 로그와 관리자 작업 이력
- 보안 점검과 개인정보 처리 정책

## v0.2 버전 계획

| 버전 | 브랜치 예시 | 목적 | 완료 조건 |
| --- | --- | --- | --- |
| `v0.2.0` | `v0.2.0-version-plan` | v0.2 범위, 제외 범위, 우선순위, 버전별 계획 문서화 | v0.2.1 ~ v0.2.9 계획과 체크리스트 작성 |
| `v0.2.1` | `v0.2.1-auth-session-security` | 인증/세션/보안 안정화 | 로그아웃, 리프레시 토큰, 세션 만료 UX, 기본 보안 점검 완료 |
| `v0.2.2` | `v0.2.2-payment-refund-integration` | 실제 PG 결제/취소/환불 연동 | 모의 결제 의존 제거 방향 확정, 결제 승인/취소/환불 최소 흐름 검증 |
| `v0.2.3` | `v0.2.3-media-image-upload` | 상품 이미지 업로드와 미디어 관리 | 상품 이미지 업로드/조회/수정 흐름과 파일 메타데이터 관리 |
| `v0.2.4` | `v0.2.4-review-moderation-audit` | 리뷰 운영과 관리자 작업 이력 | 리뷰 숨김/상태 변경, 관리자 작업 이력 기초 기록 |
| `v0.2.5` | `v0.2.5-db-migration-schema` | DB 마이그레이션과 운영 스키마 정리 | 마이그레이션 체계, 실제 DDL, 인덱스/FK 기준 문서화 및 적용 |
| `v0.2.6` | `v0.2.6-ci-env-deployment` | CI/CD와 환경 분리 | 자동 lint/build/test, 환경별 설정, 배포 문서 정리 |
| `v0.2.7` | `v0.2.7-notification-admin-roles` | 알림과 관리자 권한 세분화 | 기본 알림/이메일 흐름, 권한 역할 분리 기준 정리 |
| `v0.2.8` | `v0.2.8-ops-analytics-foundation` | 운영 분석/회계/WMS 고도화 기반 | 고급 회계/BI/WMS의 최소 기반과 다음 버전 이관 범위 정리 |
| `v0.2.9` | `v0.2.9-release-verification` | v0.2 계획 대비 구현 확인 및 테스트 검증 | 전체 체크리스트와 lint/build/test 결과 기록 |

## v0.2.1 체크리스트

목적: 인증/세션/보안 안정화.

- [x] 기존 Auth/User/Security/JWT 구조 확인
- [x] 로그아웃 처리 방식 결정 및 최소 구현
- [x] Access Token / Refresh Token 분리 구조 최소 구현
- [x] 401 세션 만료 UX 개선
- [x] 관리자 보호 경로 접근 제어 보강
- [x] CORS와 관리자 API 권한 범위 점검
- [x] 관련 API/구조/현재 상태 문서 갱신
- [x] frontend `npm.cmd run lint` 통과
- [x] frontend `npm.cmd run build` 통과
- [x] backend `.\gradlew.bat test` 통과
- [x] `v0.2.1` 태그 생성 및 push

## v0.2.2 체크리스트

목적: 결제 승인/취소/환불 API 경계 정리와 멱등성 최소 구현.

- [x] 기존 결제/주문 취소/회계/재고 복구 흐름 확인
- [x] `POST /api/payments/approve` 추가
- [x] `POST /api/payments/{paymentId}/cancel` 추가
- [x] 결제 승인 멱등성 키 저장과 재호출 응답 처리 구현
- [x] 기존 `/api/payments/mock/complete` 하위 호환 유지
- [x] checkout 결제 호출을 `/api/payments/approve`로 전환
- [x] 관련 API/DB/구조/현재 상태 문서 갱신
- [x] frontend `npm.cmd run lint` 통과
- [x] frontend `npm.cmd run build` 통과
- [x] backend `.\gradlew.bat test` 통과
- [x] `v0.2.2` 태그 생성 및 push

## v0.2.3 체크리스트

목적: 상품 이미지 업로드와 미디어 파일 메타데이터 관리.

- [x] 기존 상품 이미지 URL 구조 확인
- [x] `POST /api/admin/media/product-images` 추가
- [x] multipart 업로드, 파일명 충돌 방지, 확장자/MIME/용량 검증 구현
- [x] `media_files` 메타데이터 엔티티/저장소 추가
- [x] `/uploads/**` 정적 파일 공개 경로 추가
- [x] 관리자 상품 등록/수정 화면 업로드 UI 연결
- [x] 사용자 상품 목록/상세 이미지 fallback 처리
- [x] 관련 API/DB/구조/현재 상태 문서 갱신
- [x] frontend `npm.cmd run lint` 통과
- [x] frontend `npm.cmd run build` 통과
- [x] backend `.\gradlew.bat test` 통과
- [x] `v0.2.3` 태그 생성 및 push

## v0.2.4 체크리스트

목적: 리뷰 숨김/상태 관리와 관리자 작업 이력 최소 구현.

- [x] 기존 리뷰 작성/조회/삭제와 관리자 리뷰 화면 확인
- [x] `ReviewStatus` 상태 모델 추가
- [x] 사용자 상품 리뷰 목록에서 숨김/삭제 리뷰 미노출 처리
- [x] 관리자 리뷰 목록에서 숨김 리뷰까지 확인 가능하게 처리
- [x] `PATCH /api/admin/reviews/{reviewId}/hide` 추가
- [x] `PATCH /api/admin/reviews/{reviewId}/show` 추가
- [x] 기존 관리자 삭제 API soft delete 유지
- [x] `audit_logs`와 `AuditLogService` 최소 구현
- [x] 리뷰 숨김/해제/삭제 작업 이력 기록
- [x] `GET /api/admin/audit-logs` 최근 작업 이력 조회 추가
- [x] 관리자 리뷰 화면 상태 표시/숨김/해제/최근 감사 로그 연결
- [x] 관련 API/DB/구조/현재 상태 문서 갱신
- [x] frontend `npm.cmd run lint` 통과
- [x] frontend `npm.cmd run build` 통과
- [x] backend `.\gradlew.bat test` 통과
- [x] `v0.2.4` 태그 생성 및 push

## v0.2.5 체크리스트

목적: DB 마이그레이션 체계와 운영 스키마 기준 정리.

- [x] 기존 JPA Entity와 DB 문서 확인
- [x] Flyway 의존성과 기본 설정 추가
- [x] 테스트 프로파일 Flyway 비활성화 기준 정리
- [x] MySQL 8.0 기준 초기 DDL `V1__initial_schema.sql` 추가
- [x] 주요 FK, unique, 조회 인덱스 기준 반영
- [x] `docs/DB_SCHEMA.md`를 실제 DDL 기준으로 갱신
- [x] `docs/DB_MIGRATION.md` 운영 기준 추가
- [x] 관련 구조/현재 상태/API 문서 갱신
- [x] frontend `npm.cmd run lint` 통과
- [x] frontend `npm.cmd run build` 통과
- [x] backend `.\gradlew.bat test` 통과
- [x] `v0.2.5` 태그 생성 및 push

## v0.2.6 체크리스트

목적: CI 자동 검증과 환경 분리 기준 정리.

- [x] 기존 CI workflow 존재 여부 확인
- [x] GitHub Actions workflow 추가
- [x] frontend `npm ci`, lint, build 자동 검증 구성
- [x] backend Java 17, Gradle test 자동 검증 구성
- [x] 운영 backend profile `application-prod.yml` 추가
- [x] CORS allowed origins 설정값 분리
- [x] `.env.example`, `frontend/.env.example` 추가
- [x] 배포/환경 분리 문서 추가
- [x] 관련 구조/현재 상태 문서 갱신
- [x] frontend `npm.cmd run lint` 통과
- [x] frontend `npm.cmd run build` 통과
- [x] backend `.\gradlew.bat test` 통과
- [x] `v0.2.6` 태그 생성 및 push

## v0.2.7 체크리스트

목적: 알림 기초와 관리자 권한 세분화.

- [x] 기존 알림/이메일 코드 존재 여부 확인
- [x] `notifications` 도메인과 DDL 추가
- [x] 사용자 알림 목록/읽음/미읽음 수 API 추가
- [x] 관리자 최근 알림 조회 API 추가
- [x] 주문 상태 변경 알림 기록
- [x] 문의 답변 알림 기록
- [x] 반품 승인/거절 알림 기록
- [x] `MANAGER` 조회 권한 정책 정리
- [x] 관련 API/DB/구조/현재 상태 문서 갱신
- [x] frontend `npm.cmd run lint` 통과
- [x] frontend `npm.cmd run build` 통과
- [x] backend `.\gradlew.bat test` 통과
- [x] `v0.2.7` 태그 생성 및 push

## v0.2.8 체크리스트

목적: 운영 분석/회계/WMS 고도화의 최소 기반과 후속 이관 범위 정리.

- [x] 기존 대시보드/회계/창고/주문/결제 구조 확인
- [x] 회계 기초 지표 집계 기준 정리
- [x] 매출/주문 기초 지표 집계 기준 정리
- [x] 창고/WMS 기초 지표 집계 기준 정리
- [x] 관리자 운영 분석 overview API 추가
- [x] 프론트 운영 분석 service 타입 추가
- [x] 신규 DB 테이블 없이 기존 테이블 읽기 전용 집계로 구현
- [x] 관련 API/DB/구조/현재 상태 문서 갱신
- [x] frontend `npm.cmd run lint` 통과
- [x] frontend `npm.cmd run build` 통과
- [x] backend `.\gradlew.bat test` 통과
- [x] `v0.2.8` 태그 생성 및 push

## v0.2.9 체크리스트

목적: v0.2.0 계획 대비 v0.2.1 ~ v0.2.8 완료 여부를 검증하고, v0.3 이후 장기 로드맵을 정리한다.

- [x] `v0.2.0` ~ `v0.2.8` 체크리스트 파일 존재 확인
- [x] `v0.2.0` ~ `v0.2.8` 태그 존재 확인
- [x] 주요 관리자 화면 mock 제거 여부 확인
- [x] 주요 사용자 화면 mock 제거 여부 확인
- [x] JWT/권한 흐름 회귀 점검
- [x] v0.3.0 이후 장기 로드맵 문서화
- [x] v0.2 계획표의 완료 상태 정리
- [x] frontend `npm.cmd run lint` 통과
- [x] frontend `npm.cmd run build` 통과
- [x] backend `.\gradlew.bat test` 통과
- [x] `v0.2.9` 태그 생성 및 push

## v0.3 이후 장기 로드맵

v0.3.0 이후 장기 로드맵은 실무 ERP의 핵심 흐름을 축소 구현한 AI 커머스 ERP 포트폴리오 완성을 목표로 한다. 상세 문서는 [LONG_TERM_ROADMAP.md](./LONG_TERM_ROADMAP.md)를 기준으로 한다.

| 버전 | 목표 | 핵심 범위 |
| --- | --- | --- |
| `v0.3.0` | 상품/전시/운영관리 고도화 계획 | 상품 전시, 카테고리/옵션, 상품 상태, 관리자 상품 운영 UX 고도화 계획 |
| `v0.3.x` | 상품/전시/운영관리 구현 | v0.3.0 계획 기준 개발 및 검증 |
| `v0.4.0` | 인사관리와 권한관리 계획 | 직원/부서/직무, 역할 기반 권한, 관리자 권한 세분화 계획 |
| `v0.4.x` | 인사관리와 권한관리 구현 | 인사/권한/감사 로그 확장 개발 및 검증 |
| `v0.5.0` | 재고관리, 생산 입고, 바코드 자동화 계획 | 재고 실사, 생산/입고, 바코드 생성/스캔 기준 계획 |
| `v0.5.x` | 재고관리, 생산 입고, 바코드 자동화 구현 | 창고/입고/바코드 운영 흐름 개발 및 검증 |
| `v0.6.0` | 유통관리 계획 | 택배사 연동, 송장번호 생성/출력, 배송 처리 계획 |
| `v0.6.x` | 유통관리 구현 | 택배사 연동 인터페이스, 송장 출력, 출고/배송 흐름 개발 및 검증 |
| `v0.7.0` | 회계관리 고도화 계획 | 매입, 매출, 환불, 정산, 마감, 회계 리포트 계획 |
| `v0.7.x` | 회계관리 고도화 구현 | 정산/마감/회계 리포트 개발 및 검증 |
| `v0.8.0` | AI 데이터셋과 모델 학습 구조 계획 | 상품, 리뷰, 재고, 주문 데이터셋과 `.pt` 모델 학습 구조 계획 |
| `v0.8.x` | AI 데이터셋과 모델 학습 구조 구현 | 데이터셋 생성, 전처리, 학습, 평가, 모델 산출 개발 및 검증 |
| `v0.9.0` | AI 기능과 최종 포트폴리오 릴리스 계획 | AI 추천, 수요 예측, 리뷰 분석, 이상 주문 탐지, 관리자 화면 연결 계획 |
| `v0.9.x` | AI 기능과 최종 포트폴리오 릴리스 | AI 운영 기능 연결, 데모 흐름, 최종 릴리스 검증 |

## v0.3 버전 계획

v0.3의 목적은 상품/전시/운영관리를 실무 ERP 포트폴리오 수준으로 고도화하는 것이다. v0.3.0은 계획 수립 버전이며, 실제 구현은 v0.3.1부터 진행한다. 상세 계획은 [V0.3_COMMERCE_OPS_PLAN.md](./V0.3_COMMERCE_OPS_PLAN.md)를 기준으로 한다.

| 버전 | 브랜치 예시 | 목적 | 완료 조건 |
| --- | --- | --- | --- |
| `v0.3.0` | `v0.3.0-commerce-ops-plan` | 상품/전시/운영관리 고도화 계획 | v0.3.1 ~ v0.3.9 작업 순서와 제외/이관 범위 문서화 |
  | `v0.3.1` | `v0.3.1-product-catalog-master` | 상품 마스터 고도화 | 상품코드/브랜드/제조사/가격/태그/SEO 등 마스터 필드 저장/조회 |
  | `v0.3.2` | `v0.3.2-product-detail-editor` | 상품 상세페이지 에디터 | 블록 기반 상세페이지 저장/정렬/노출과 사용자 상세 렌더링 |
  | `v0.3.3` | `v0.3.3-category-nav-admin` | 카테고리/상단 네비 관리자 관리 | 카테고리 트리와 상단 네비를 관리자 설정 기준으로 렌더링 |
  | `v0.3.3.1` | `v0.3.3.1-frontend-korean-copy` | 프론트 한글화 hotfix | v0.3.4 전 주요 영어 UI 문구를 한국어로 정리 |
  | `v0.3.4` | `v0.3.4-main-banner-cms` | 메인 배너 CMS | 관리자 배너 등록/수정/활성 기간/정렬과 사용자 메인 노출 |
| `v0.3.5` | `v0.3.5-product-sales-status` | 상품 판매 상태/품절/일시중지 운영 | 상품/전시/판매 상태 분리와 사용자 구매 가능 여부 반영 |
| `v0.3.6` | `v0.3.6-product-admin-operations` | 상품 운영 UX/대량 관리/상태 이력 | 필터, 대량 상태 변경, 운영 메모, 상태 변경 이력 |
| `v0.3.7` | `v0.3.7-commerce-display-frontend` | 상품/전시 사용자 화면 반영 | 상품/네비/배너/재고 상태가 사용자 화면에 반영 |
| `v0.3.8` | `v0.3.8-release-verification` | v0.3 통합 검증 | v0.3 계획 대비 완료 여부, lint/build/test, 수동 확인 기록 |
| `v0.3.9` | `v0.3.9-v04-handover` | v0.4 인사관리/권한관리 이관 정리 | v0.3 남은 이슈와 v0.4 인사/권한 계획 후보 정리 |

## v0.3 제외 범위

- 네이버쇼핑 실제 API 연동
- 쿠팡/스마트스토어/11번가 실제 마켓 연동
- 택배사 실제 API 연동
- 실제 PG 상용 결제 심사
- 인사/권한 전체 구현
- 생산/바코드 전체 구현
- 매입/정산/복식부기 전체 구현
- AI 모델 학습/추론 구현
- 대규모 SaaS/멀티테넌트 구조

## v0.3.0 체크리스트

목적: 상품/전시/운영관리 고도화 계획 수립.

- [x] main 최신 상태 확인
- [x] v0.3.0 브랜치 생성
- [x] v0.2.9 완료 상태 확인
- [x] `LONG_TERM_ROADMAP.md` 기준 v0.3 범위 확인
- [x] 상품/전시/운영관리 고도화 범위 정의
- [x] v0.3.1 ~ v0.3.9 작업 순서 정의
- [x] v0.4.0 이후 이관 범위 정의
- [x] 제외 범위 정의
- [x] 관련 문서 갱신
- [x] 코드 변경 없음. lint/build/test 생략
- [x] `v0.3.0` 태그 생성 및 push

## v0.3.1 체크리스트

목적: 상품 마스터 필드를 실무 커머스 운영 기준으로 확장.

- [x] main 최신 상태 확인
- [x] v0.3.1 브랜치 생성
- [x] 기존 상품 Entity/DTO/Service/Controller 확인
- [x] 기존 관리자/사용자 상품 화면 확인
- [x] 상품코드/브랜드/제조사/모델명/원산지 필드 추가
- [x] 정상가/판매가/할인금액/매입가와 계산 마진율 기준 정리
- [x] 검색 키워드/태그/판매 기간/배송/SEO 필드 추가
- [x] Flyway V3 마이그레이션 추가
- [x] 관리자 응답 DTO와 사용자 응답 DTO 분리
- [x] 관리자 상품 등록/수정/목록 화면 반영
- [x] 사용자 상품 API에서 매입가/마진율 비노출 확인
- [x] 관련 API/DB/구조/현재 상태 문서 갱신
- [x] frontend `npm.cmd run lint` 통과
- [x] frontend `npm.cmd run build` 통과
- [x] backend `.\gradlew.bat test` 통과
- [x] `v0.3.1` 태그 생성 및 push

## v0.3.2 체크리스트

목적: 상품 상세페이지를 블록 기반 CMS 구조로 확장.

- [x] main 최신 상태 확인
- [x] v0.3.2 브랜치 생성
- [x] 기존 상품 상세 구조와 v0.3.1 상품 마스터 필드 영향 확인
- [x] `ProductDetailBlock` 엔티티와 `ProductDetailBlockType` enum 추가
- [x] `ProductDetailBlockRepository` 추가
- [x] Flyway V4 마이그레이션 추가
- [x] 관리자 상세 블록 조회/전체 교체 저장 API 추가
- [x] 사용자 상품 상세 응답에 visible 상세 블록 포함
- [x] 관리자 상품 수정 화면에 블록 편집 UI 추가
- [x] 사용자 상품 상세 화면에 블록 타입별 렌더러 추가
- [x] HTML 블록 XSS 위험과 내부 CMS 신뢰 범위 문서화
- [x] 관련 API/DB/구조/현재 상태 문서 갱신
- [x] frontend `npm.cmd run lint` 통과
- [x] frontend `npm.cmd run build` 통과
- [x] backend `.\gradlew.bat test` 통과
- [x] `v0.3.2` 태그 생성 및 push

## v0.3.3 체크리스트

목적: 카테고리 트리와 쇼핑몰 상단 네비를 관리자 설정 기반으로 전환.

- [x] main 최신 상태 확인
- [x] v0.3.3 브랜치 생성
- [x] 기존 Category Entity/Controller/Service/Repository 확인
- [x] 기존 상품 등록/수정 카테고리 선택과 사용자 상품 목록 필터 확인
- [x] 쇼핑몰 상단 네비 하드코딩 구조 확인
- [x] Category parent/depth/sortOrder/active/visibleInNav/slug 필드 추가
- [x] Flyway V5 마이그레이션 추가
- [x] 공개 navigation API 추가
- [x] 관리자 category tree/create/update API 추가
- [x] 순환 참조와 자기 parent 지정 방지
- [x] 관리자 카테고리 관리 화면 추가
- [x] 쇼핑몰 상단 네비 API 기반 전환
- [x] 상품 목록 category query 필터 연결 보강
- [x] 관련 API/DB/구조/현재 상태 문서 갱신
- [x] frontend `npm.cmd run lint` 통과
- [x] frontend `npm.cmd run build` 통과
- [x] backend `.\gradlew.bat test` 통과
- [x] `v0.3.3` 태그 생성 및 push

## v0.3.3.1 hotfix 체크리스트

목적: v0.3.4 진입 전 프론트 화면에 남은 주요 영어 UI 문구를 한국어로 정리.

- [x] main 최신 상태 확인
- [x] v0.3.3.1 hotfix 브랜치 생성
- [x] 사용자/관리자 프론트 영어 문구 검색
- [x] 관리자 사이드바/카테고리/상품/상세 블록 문구 한국어 정리
- [x] 사용자 메인/상품 카드/상품 상세 문구 한국어 정리
- [x] enum/API/DTO/변수명은 변경하지 않음
- [x] frontend `npm.cmd run lint` 통과
- [x] frontend `npm.cmd run build` 통과
- [x] `v0.3.3.1` 태그 생성 및 push

## v0.3.4 체크리스트

목적: 메인 배너를 관리자 CMS 데이터 기준으로 관리하고 사용자 메인 화면에 노출.

- [x] main 최신 상태 확인
- [x] `v0.3.4-main-banner-cms` 브랜치 생성
- [x] 기존 `MainBanner.tsx` 하드코딩 배너 구조 확인
- [x] `main_banners` 테이블/Flyway V6 추가
- [x] 공개 `GET /api/banners` 구현
- [x] 관리자 `/api/admin/banners` 조회/상세/등록/수정/비활성화 API 구현
- [x] `SecurityConfig` 공개/관리자 배너 권한 반영
- [x] 관리자 `/admin/banners` 화면 추가
- [x] 사용자 메인 배너 API 기반 렌더링 전환
- [x] 배너 없음/API 실패 fallback 처리
- [x] 문서와 체크리스트 갱신
- [x] frontend `npm.cmd run lint` 통과
- [x] frontend `npm.cmd run build` 통과
- [x] backend `.\gradlew.bat test` 통과
- [x] `v0.3.4` 태그 생성 및 push

## v0.3.5 체크리스트

목적: 상품 판매 상태, 전시 상태, 품절/일시중지 운영 기준을 분리하고 사용자 구매 가능 여부를 일관화.

- [x] main 최신 상태 확인
- [x] `v0.3.5-product-sales-status` 브랜치 생성
- [x] 기존 상품/장바구니/주문 상태 검증 흐름 확인
- [x] `ProductSalesStatus`, `ProductDisplayStatus`, `StockDisplayStatus` 추가
- [x] Flyway V7 마이그레이션 추가
- [x] 사용자 상품 목록/상세 노출 기준 정리
- [x] `purchasable`, `stockDisplayStatus`, `stockDisplayText` 응답 추가
- [x] 장바구니/주문 생성에서 비구매 가능 상품 차단
- [x] 관리자 상품 등록/수정/목록에 판매/전시 상태와 안전재고 반영
- [x] 사용자 상품 카드/상세에서 재고 표시와 구매 버튼 비활성화 반영
- [x] 문서와 체크리스트 갱신
- [x] frontend `npm.cmd run lint` 통과
- [x] frontend `npm.cmd run build` 통과
- [x] backend `.\gradlew.bat test` 통과

## v0.3.5.1 hotfix/foundation 체크리스트

목적: 길어진 관리자 사이드바를 업무 영역별 그룹 구조로 재구성하고 설정/감사 로그/인사 권한관리 진입점을 정리.

- [x] main 최신 상태 확인
- [x] `v0.3.5.1-admin-navigation-settings-foundation` 브랜치 생성
- [x] 기존 관리자 사이드바와 권한 유틸 확인
- [x] 관리자 메뉴를 업무 영역별 그룹으로 재구성
- [x] 그룹 접기/펼치기와 active 그룹 기본 펼침 처리
- [x] `SUPER_ADMIN`, `ADMIN`, `MANAGER` 기준 메뉴 노출 최소 적용
- [x] `/admin/settings` 설정 메인 페이지 추가
- [x] `/admin/settings/audit-logs` 관리자 작업 이력 페이지 추가
- [x] 기존 audit log API/service 재사용
- [x] 사업자/약관/정책/직원 권한 관리 진입점 추가
- [x] 상세 인사/권한 DB 구현은 v0.4.0으로 이관
- [x] frontend `npm.cmd run lint` 통과
- [x] frontend `npm.cmd run build` 통과
- [x] backend test 생략: 백엔드 변경 없음

## v0.3.5.2 hotfix 체크리스트

목적: 관리자 사이드바 그룹 메뉴를 단일 openGroupLabel 기반 아코디언으로 보정.

- [x] main 최신 상태 확인
- [x] `v0.3.5.2-admin-sidebar-accordion-fix` 브랜치 생성
- [x] 중복 href와 query string active 판정 정리
- [x] 한 그룹만 열리는 아코디언 상태 관리로 변경
- [x] `/admin/sales`, `/admin/settings?section=company` 등 중복 active 케이스 보정
- [x] frontend `npm.cmd run lint` 통과
- [x] frontend `npm.cmd run build` 통과
- [x] backend test 생략: 백엔드 변경 없음

## v0.3.6 체크리스트

목적: 관리자 상품 목록 운영 UX를 강화하고 대량 상태 변경, 운영 메모, 상태 변경 이력을 구현.

- [x] main 최신 상태 확인
- [x] `v0.3.6-product-admin-operations` 브랜치 생성
- [x] 기존 상품 상태/전시 상태/감사 로그 구조 확인
- [x] 관리자 상품 목록 필터에 카테고리, 재고 상태, 안전재고 이하, 판매 기간 상태 추가
- [x] 관리자 상품 목록 체크박스 선택과 현재 페이지 전체 선택 구현
- [x] `PATCH /api/admin/products/bulk-status` 대량 상태 변경 API 추가
- [x] `product_status_histories` 상태 변경 이력 테이블/Flyway 추가
- [x] `product_operation_notes` 운영 메모 테이블/Flyway 추가
- [x] 상태 변경/대량 변경/운영 메모 작성 감사 로그 기록
- [x] 관리자 상품 수정 화면에 운영 메모와 상태 변경 이력 패널 추가
- [x] DataTable render 전용 컬럼 key를 고유 key로 정리
- [x] 관련 API/DB/현재 상태 문서 갱신
- [x] frontend `npm.cmd run lint` 통과
- [x] frontend `npm.cmd run build` 통과
- [x] backend `.\gradlew.bat test` 통과

## v0.3.7 체크리스트

목적: v0.3 상품/전시/상태 데이터를 사용자 쇼핑 화면에 안정적으로 반영.

- [x] main 최신 상태 확인
- [x] `v0.3.7-commerce-display-frontend` 브랜치 생성
- [x] 사용자 메인/상품 목록/상품 상세/배너/카테고리 네비 구조 확인
- [x] 메인 카테고리 바로가기를 관리자 네비 카테고리 API 기반으로 전환
- [x] 카테고리 네비를 `/api/categories/navigation` 기반으로 정리
- [x] 관리자 버튼을 MANAGER/ADMIN/SUPER_ADMIN 사용자에게만 표시
- [x] 상품 목록 URL category/keyword query 반영과 필터 문구 정리
- [x] 상품 카드에 브랜드, 태그, 가격/할인, 재고/판매 상태 표시
- [x] 상품 상세에 브랜드, 제조사, 원산지, 배송 정보, 태그, 상세 블록 표시
- [x] 구매 불가 상품의 버튼 비활성화와 사유 표시
- [x] 사용자 화면 깨진 한글 문구 정리
- [x] 내부 운영 필드 비노출 확인
- [x] 문서와 체크리스트 갱신
- [x] frontend `npm.cmd run lint` 통과
- [x] frontend `npm.cmd run build` 통과
- [x] backend test 생략: 백엔드 변경 없음

## v0.3.8 체크리스트

목적: v0.3.0 계획표 기준으로 v0.3.1 ~ v0.3.7 구현 결과를 통합 검증하고 남은 이슈를 후속 버전으로 이관.

- [x] main 최신 상태 확인
- [x] `v0.3.8-release-verification` 브랜치 생성
- [x] v0.3.0 ~ v0.3.7 태그 존재 확인
- [x] v0.3.0 ~ v0.3.7 체크리스트 존재 확인
- [x] v0.3.5.2는 태그와 `VERSION_PLAN.md` hotfix 체크리스트로 확인
- [x] V3 ~ V9 Flyway migration 존재 확인
- [x] API 문서와 실제 컨트롤러/서비스 호출 기준 확인
- [x] DB 문서와 migration 기준 확인
- [x] 프론트 구조 문서와 실제 화면/서비스 기준 확인
- [x] 관리자 권한과 사용자 노출 정책 확인
- [x] 사용자 상품 전시/구매 가능 정책 확인
- [x] 남은 이슈와 v0.4 이후 이관 범위 정리
- [x] frontend `npm.cmd run lint` 통과
- [x] frontend `npm.cmd run build` 통과: 기본 `.next` EPERM 후 `NEXT_DIST_DIR=.next-build-check-v038` 권한 상승 재실행으로 통과
- [x] backend `.\gradlew.bat test` 통과: Gradle wrapper lock 접근 거부 후 권한 상승 재실행으로 통과

## v0.3.9 체크리스트

목적: v0.3 상품/전시/운영관리 고도화 작업을 종료 상태로 정리하고 v0.4 인사관리/권한관리 계획으로 이관.

- [x] main 최신 상태 확인
- [x] `v0.3.9-v04-handover` 브랜치 생성
- [x] v0.3.0 ~ v0.3.8 완료 상태 요약
- [x] v0.3 남은 이슈와 후속 이관 범위 정리
- [x] v0.4.0 ~ v0.4.9 세부 버전 계획 작성
- [x] v0.4 DB 테이블 후보 정리
- [x] v0.4 권한 코드 후보 정리
- [x] v0.4 API 후보 정리
- [x] v0.4 프론트 화면 후보 정리
- [x] v0.4 제외 범위 정리
- [x] `docs/V0.4_HR_PERMISSION_PLAN.md` 생성
- [x] `docs/checklists/v0.3.9-v04-handover.md` 생성
- [x] 문서 작업만 수행하여 frontend lint/build와 backend test 생략

## v0.4 계획표

| 버전 | 목표 | 핵심 범위 |
| --- | --- | --- |
| `v0.4.0` | 인사관리/권한관리 계획 수립 | v0.4 전체 범위, DB/API/프론트/권한 설계 |
| `v0.4.1` | 직원/부서/직급 기본 모델 | departments, positions, staff_profiles 기반 모델 |
| `v0.4.2` | 직원 관리 화면/API | 직원 목록/상세/생성/수정, 활성/비활성, 재직 상태 |
| `v0.4.3` | 권한 그룹/역할 관리 | permission_groups, 사용자 권한 그룹 할당 |
| `v0.4.4` | 메뉴별/기능별 권한 매트릭스 | permissions, menu permissions, group-permission 매핑 |
| `v0.4.5` | 관리자 사이드바 권한 연동 고도화 | 메뉴별 권한 기준 노출과 비인가 UX |
| `v0.4.6` | API 권한 정책 세분화 | read/write/manage 권한 분리와 SecurityConfig 고도화 |
| `v0.4.7` | 관리자 작업 이력/감사 로그 확장 | 주요 관리자 변경 작업 감사 로그 기록 |
| `v0.4.8` | 사업자/약관 설정 저장과 버전관리 | 사업자 정보, 약관, 개인정보처리방침, 배송/반품 정책 저장 |
| `v0.4.9` | v0.4 통합 검증 및 v0.5 이관 | v0.4 검증, v0.5 재고/생산/바코드 이관 |

## v0.4.0 체크리스트

목적: 인사관리/권한관리 전체 범위, 제외 범위, DB/API/프론트/권한 설계, 세부 작업 순서를 확정.

- [x] main 최신 상태 확인
- [x] `v0.4.0-hr-permission-plan` 브랜치 생성
- [x] v0.3.0 ~ v0.3.9 태그 존재 확인
- [x] v0.3 체크리스트 존재 확인
- [x] `docs/V0.4_HR_PERMISSION_PLAN.md` 확인 및 보강
- [x] 직원/부서/직급/직무/역할/권한 그룹 범위 확정
- [x] 메뉴별/기능별 권한과 관리자 사이드바/API 접근 제어 범위 확정
- [x] 관리자 작업 이력/감사 로그 확장 범위 확정
- [x] 사업자/약관 설정 저장과 버전관리 범위 확정
- [x] v0.4.0 ~ v0.4.9 세부 작업 순서 확정
- [x] DB 테이블 후보, 인덱스 후보, active/soft delete 정책 정리
- [x] 권한 코드 후보와 role/permission group 병행 정책 정리
- [x] v0.4 제외 범위 정리
- [x] v0.5 재고/생산/바코드 이관 후보 정리
- [x] `docs/checklists/v0.4.0-hr-permission-plan.md` 생성
- [x] 문서 작업만 수행하여 frontend lint/build와 backend test 생략

## v0.4.1 체크리스트

목적: 인사/권한관리의 최소 DB 기반으로 부서, 직급, 직원 프로필 모델을 추가.

- [x] main 최신 상태 확인
- [x] `v0.4.1-staff-organization-model` 브랜치 생성
- [x] 기존 `User`, `UserRole`, `SecurityConfig` 확인
- [x] `Department`, `Position`, `StaffProfile`, `EmploymentStatus` 추가
- [x] `DepartmentRepository`, `PositionRepository`, `StaffProfileRepository` 추가
- [x] `DepartmentService`, `PositionService`, `StaffProfileService` 추가
- [x] `GET /api/admin/hr/departments` 조회 API 추가
- [x] `GET /api/admin/hr/positions` 조회 API 추가
- [x] `GET /api/admin/hr/staff-profiles` 조회 API 추가
- [x] HR 조회 API를 `ADMIN`, `SUPER_ADMIN` 전용으로 제한
- [x] `V10__create_hr_staff_base.sql` Flyway migration 추가
- [x] API/DB/백엔드/현재 상태/v0.4 계획 문서 갱신
- [x] backend `.\gradlew.bat test` 통과: Gradle wrapper lock 접근 거부 후 권한 상승 재실행으로 통과
- [x] frontend lint/build 생략: 프론트 변경 없음

## v0.4.2 체크리스트

목적: 직원 관리 API와 관리자 직원 관리 화면을 구현.

- [x] v0.4.1 태그/체크리스트 완료 상태 확인
- [x] `v0.4.2-staff-management-api-ui` 브랜치 생성
- [x] 직원 목록/상세/등록/수정 API 구현
- [x] 재직 상태 변경 API 구현
- [x] 직원 프로필 활성/비활성 변경 API 구현
- [x] 직원 생성/수정/상태 변경 감사 로그 기록
- [x] `/admin/settings/staff` 직원 관리 화면 구현
- [x] `staffService.ts` 타입과 API 호출 추가
- [x] 설정 메인/사이드바 직원 관리 링크 연결
- [x] API/백엔드/프론트/현재 상태/v0.4 계획 문서 갱신
- [x] backend `.\gradlew.bat test` 통과
- [x] frontend `npm.cmd run lint` 통과
- [x] frontend `npm.cmd run build` 통과: 기본 `.next` EPERM 후 `NEXT_DIST_DIR=.next-build-check-v042` 권한 상승 재실행으로 통과

## v0.4.2.1 hotfix 체크리스트

목적: 쇼핑몰 공개 화면 비회원 접근과 비회원 주문 기반을 정리.

- [x] `v0.4.2.1-public-shop-guest-order-foundation` 브랜치 생성
- [x] 공개 API용 `publicApiClient` 추가
- [x] 공개 API 401에서는 세션 만료 처리와 로그인 리다이렉트 제외
- [x] 보호 API는 기존 `apiClient` 401/refresh/login 흐름 유지
- [x] 상품 목록/상세, 카테고리/네비, 배너, 공개 리뷰/문의 조회를 공개 클라이언트로 전환
- [x] ShopHeader 주문조회 링크를 `/orders/guest`로 분리
- [x] `/orders/guest` 비회원 주문조회 기반 안내 페이지 추가
- [x] 비회원 주문 구조와 후속 구현 범위 문서화
- [x] frontend `npm.cmd run lint` 통과
- [x] frontend `npm.cmd run build` 통과: 기본 `.next`와 별도 dist 일반 실행 EPERM 후 `NEXT_DIST_DIR=.next-build-check-public-shop` 권한 상승 재실행으로 통과

## v0.4.3 체크리스트

목적: 권한 그룹과 사용자 권한 그룹 할당 기반을 구현하고 기존 role과 병행 운영한다.

- [x] main 최신 상태 확인
- [x] `v0.4.3-permission-group-role-management` 브랜치 생성
- [x] 기존 `User`, `UserRole`, `StaffProfile`, `SecurityConfig`, audit log 구조 확인
- [x] `PermissionGroup`, `UserPermissionGroup` 모델 추가
- [x] `V11__create_permission_groups.sql` Flyway migration 추가
- [x] `SUPER_ADMIN_GROUP`, `ADMIN_GROUP`, `MANAGER_GROUP` 시스템 그룹 seed 추가
- [x] 권한 그룹 목록/상세/생성/수정/활성 변경 API 구현
- [x] 사용자 권한 그룹 조회/할당 API 구현
- [x] 기존 role 기반 접근 제어 유지, 조회 `ADMIN/SUPER_ADMIN`, 변경 `SUPER_ADMIN` 정책 적용
- [x] 권한 그룹 생성/수정/활성 변경/사용자 할당 변경 audit log 기록
- [x] `/admin/settings/permission-groups` 권한 그룹 관리 화면 구현
- [x] `/admin/settings/roles` 역할/권한 병행 운영 안내 화면 구현
- [x] API/DB/백엔드/프론트/현재 상태/v0.4 계획 문서 갱신
- [x] backend `.\gradlew.bat test` 통과: 기본 Gradle wrapper lock 접근 거부 후 권한 상승 재실행으로 통과
- [x] frontend `npm.cmd run lint` 통과
- [x] frontend `npm.cmd run build` 통과: 기본 `.next`와 별도 dist 일반 실행 EPERM 후 `NEXT_DIST_DIR=.next-build-check-v043` 권한 상승 재실행으로 통과

## v0.4.4 체크리스트

목적: 메뉴별/기능별 권한 매트릭스를 구현하고 권한 그룹별 기능 권한, 메뉴별 필요 권한, 사용자 유효 권한 조회 기반을 마련한다.

- [x] main 최신 상태 확인
- [x] `v0.4.4-menu-feature-permission-matrix` 브랜치 생성
- [x] v0.4.3 권한 그룹/사용자 권한 그룹 구조 확인
- [x] `Permission`, `PermissionGroupPermission`, `AdminMenuPermission` 모델 추가
- [x] `V12__create_permission_matrix.sql` Flyway migration 추가
- [x] 권한 코드 seed와 시스템 그룹 기본 권한 seed 추가
- [x] 관리자 메뉴별 필요 권한 seed 추가
- [x] 권한 목록 조회 API 구현
- [x] 권한 그룹별 기능 권한 조회/교체 저장 API 구현
- [x] 사용자 유효 권한 조회 API 구현
- [x] 관리자 메뉴 권한 조회/수정 API 구현
- [x] 조회 `ADMIN/SUPER_ADMIN`, 변경 `SUPER_ADMIN` role 정책 적용
- [x] 권한 매트릭스/메뉴 권한 변경 audit log 기록
- [x] `/admin/settings/menu-permissions` 권한 매트릭스 화면 구현
- [x] 관리자 설정 홈/사이드바에 메뉴/기능 권한 진입점 추가
- [x] API/DB/백엔드/프론트/현재 상태/v0.4 계획 문서 갱신
- [x] backend `.\gradlew.bat test` 통과: 기본 Gradle wrapper lock 접근 거부 후 권한 상승 재실행으로 통과
- [x] frontend `npm.cmd run lint` 통과
- [x] frontend `npm.cmd run build` 통과: 기본 `.next`와 별도 dist 일반 실행 EPERM 후 `NEXT_DIST_DIR=.next-build-check-v044` 권한 상승 재실행으로 통과

## v0.4.5 체크리스트

목적: 관리자 사이드바와 관리자 layout guard를 effective permission 및 `admin_menu_permissions` 기준으로 연동한다.

- [x] main 최신 상태 확인
- [x] `v0.4.5-admin-sidebar-permission-integration` 브랜치 생성
- [x] v0.4.4 권한 매트릭스 구조 확인
- [x] 현재 사용자 effective permission 조회 API 추가
- [x] `GET /api/admin/menu-permissions` MANAGER 조회 허용
- [x] `V13__seed_admin_sidebar_menu_permissions.sql`로 query 기반 menuKey seed 보강
- [x] `frontend/src/lib/adminMenu.ts` 공용 메뉴 정의 추가
- [x] `AdminSidebarV2` permission code 기반 메뉴 필터링 전환
- [x] `AdminLayout` 직접 URL 접근 권한 확인과 403 안내 UX 보강
- [x] 권한 API 실패 시 기존 role 기반 fallback 유지
- [x] query string active 판정과 단일 open group 아코디언 UX 유지
- [x] 관련 문서와 체크리스트 갱신
- [x] frontend `npm.cmd run lint` 통과
- [x] frontend `npm.cmd run build` 통과: 기본 `.next`와 별도 dist 일반 실행 EPERM 후 `NEXT_DIST_DIR=.next-build-check-v045` 권한 상승 재실행으로 통과
- [x] backend `.\gradlew.bat test` 통과: 기본 Gradle wrapper lock 접근 거부 후 권한 상승 재실행으로 통과

## v0.4.6 체크리스트

목적: 주요 관리자 API를 role 기반 1차 접근과 permission code 기반 세부 실행 권한으로 분리한다.

- [x] main 최신 상태 확인
- [x] `v0.4.6-api-permission-policy` 브랜치 생성
- [x] 기존 SecurityConfig, JWT, UserRole, PermissionMatrixService 구조 확인
- [x] `PermissionCodes` 상수와 `PermissionChecker` 유틸 구현
- [x] `SUPER_ADMIN` 전체 허용, 일반 관리자는 effective permission code 기준 검증
- [x] 상품 조회/수정/상태 변경/대량 변경 권한 분리
- [x] 카테고리, 배너, 주문, 배송, 반품, 결제/환불 API permission 적용
- [x] 재고, 창고, 회계, 쿠폰, 리뷰, 문의 API permission 적용
- [x] 직원/HR, 권한 그룹/권한 매트릭스, 감사 로그 API permission 적용
- [x] 권한 없음 403 응답에 한국어 안내 메시지 반영
- [x] 관련 문서와 체크리스트 갱신
- [x] backend `.\gradlew.bat test` 통과
- [x] frontend 변경 없음. frontend lint/build 생략

## v0.4.7 체크리스트

목적: 주요 관리자 변경 작업과 권한 실패를 감사 로그로 추적하고, 감사 로그 조회 API/UI를 필터와 상세 보기 중심으로 확장한다.

- [x] main 최신 상태 확인
- [x] `v0.4.7-admin-audit-log-expansion` 브랜치 생성
- [x] 기존 `AuditLog`, `AuditActionType`, `AuditLogService`, `/admin/settings/audit-logs` 구조 확인
- [x] `audit_logs` 기존 테이블 확장 결정
- [x] `V14__extend_audit_logs_context.sql` 추가
- [x] `ipAddress`, `userAgent`, `requestMethod`, `requestPath`, `beforeJson`, `afterJson`, `metadataJson` 필드 추가
- [x] 주요 `AuditActionType` 확장
- [x] 상품/카테고리/배너/주문/결제/재고/창고/쿠폰/문의/리뷰/직원/권한 작업 audit log 범위 확장
- [x] `PermissionChecker`에서 `PERMISSION_DENIED` 기록
- [x] `GET /api/admin/audit-logs` 필터 보강
- [x] `GET /api/admin/audit-logs/{auditLogId}` 상세 조회 추가
- [x] `/admin/settings/audit-logs` 필터/상세 UI 보강
- [x] 민감정보 마스킹 기준 적용 및 문서화
- [x] 관련 문서와 체크리스트 갱신
- [x] backend `.\gradlew.bat test` 통과
- [x] frontend `npm.cmd run lint` 통과
- [x] frontend `npm.cmd run build` 통과: 기본 `.next` EPERM 후 `NEXT_DIST_DIR=.next-build-check-v047` 권한 상승 재실행으로 통과

## v0.4.8 체크리스트

목적: 사업자 설정, 약관, 개인정보처리방침, 배송/반품 정책 저장과 버전관리를 구현.

- [x] main 최신 상태 확인
- [x] `v0.4.8-business-terms-settings` 브랜치 생성
- [x] 기존 `/admin/settings`, `SETTINGS_MANAGE`, `PermissionChecker`, `AuditLogService` 구조 확인
- [x] `BusinessSettings` 단일 row 설정 모델 구현
- [x] `TermsVersion`, `TermsType` 버전 모델 구현
- [x] `V15__create_business_terms_settings.sql` Flyway migration 추가
- [x] 관리자 사업자 설정 조회/저장 API 구현
- [x] 관리자 약관/정책 버전 생성/최신/이력/상세 조회 API 구현
- [x] 공개 사업자 정보와 최신 약관/정책 조회 API 구현
- [x] 공개 settings GET API 인증 제외 처리
- [x] `SETTINGS_MANAGE` permission 검증 적용
- [x] 사업자 설정 변경과 약관/정책 버전 생성 audit log 기록
- [x] `/admin/settings?section=company|terms|privacy|policies` 저장 UI 구현
- [x] `settingsService.ts` 타입과 API 호출 추가
- [x] 관련 문서와 체크리스트 갱신
- [x] backend `.\gradlew.bat test` 통과
- [x] frontend `npm.cmd run lint` 통과
- [x] frontend `npm.cmd run build` 통과: 기본 `.next` EPERM 후 `NEXT_DIST_DIR=.next-build-check-v048c` 권한 상승 재실행으로 통과

## v0.4.9 체크리스트

목적: v0.4 인사관리/권한관리 범위를 통합 검증하고 v0.5 재고/생산/바코드 자동화로 이관.

- [x] main 최신 상태 확인
- [x] `v0.4.9-hr-permission-release-verification` 브랜치 최신 main 기준 재정리
- [x] v0.4.0 ~ v0.4.8 태그 확인
- [x] v0.4.0 ~ v0.4.8 체크리스트 확인
- [x] HR/직원/권한 그룹/권한 매트릭스/사이드바/API 권한/감사 로그 구현 범위 확인
- [x] 사업자/약관 설정 저장 구현 확인
- [x] API/DB/프론트/권한/감사 로그 문서 정합성 확인
- [x] v0.4 남은 이슈 정리
- [x] v0.5 재고/생산/바코드 이관 계획 후보 정리
- [x] frontend lint/build 통과
- [x] backend test 통과
- [x] main 병합 및 `v0.4.9` 태그 생성

## v0.5 계획

목적: v0.4 인사관리/권한관리 기반 위에서 재고관리, 생산 입고, 바코드 자동화 범위를 계획하고 구현한다. v0.5.0은 계획 수립 버전이며 기능 코드와 DB migration은 작성하지 않는다.

- `v0.5.0`: 재고/생산/바코드 계획 수립 완료
- `v0.5.1`: SKU/바코드 재고 마스터 고도화
- `v0.5.2`: 생산 입고 모델과 생산 완료 처리
- `v0.5.3`: 바코드 생성/라벨 출력
- `v0.5.4`: 바코드 기반 입고/출고/재고 조회
- `v0.5.5`: 재고 실사/조정 워크플로우
- `v0.5.6`: 창고 위치/로케이션 관리
- `v0.5.7`: 재고 자동 알림/안전재고 처리
- `v0.5.8`: 재고/생산/바코드 사용자/관리자 화면 검증
- `v0.5.9`: v0.5 통합 검증 및 v0.6 유통관리 이관

상세 계획은 `docs/V0.5_INVENTORY_BARCODE_PLAN.md`에 기록한다.

### v0.5.0 체크리스트

- [x] main 최신 상태 확인
- [x] `v0.5.0-inventory-production-barcode-plan` 브랜치 생성
- [x] v0.4.9 완료 상태와 태그 확인
- [x] 기존 상품/재고/창고/주문/배송 구조 확인
- [x] v0.5 전체 범위와 제외 범위 확정
- [x] v0.5.0 ~ v0.5.9 작업 순서 확정
- [x] DB/API/프론트 화면 후보 정리
- [x] SKU/바코드 정책 정리
- [x] 생산 입고 정책 정리
- [x] 재고 이동/실사/조정 정책 정리
- [x] 권한/감사 로그 기준 정리
- [x] v0.6 유통관리 이관 후보 정리
- [x] 문서 작업만 수행하여 frontend lint/build와 backend test 생략
- [ ] main 병합 및 `v0.5.0` 태그 생성

## v0.2 우선순위

| 우선순위 | 항목 | 이유 |
| --- | --- | --- |
| P0 | 인증/세션/보안, 결제/환불, DB 마이그레이션, CI/CD | 운영 안정성과 데이터 정합성에 직접 영향 |
| P1 | 이미지 업로드, 리뷰 운영, 알림, 관리자 권한 | 관리자/사용자 운영 품질에 직접 영향 |
| P2 | 고급 회계, BI, WMS 자동화 | 범위가 크므로 최소 기반부터 정리 |

## v0.2 제외 범위

v0.2에서는 아래 항목을 완성형으로 구현하지 않고, 필요한 경우 기반 설계나 최소 흐름만 다룬다.

- 완전한 복식부기 회계 시스템
- 복잡한 정산 마감 자동화
- 대규모 BI 대시보드/데이터 웨어하우스
- 고급 WMS 자동 피킹/패킹 최적화
- 멀티 테넌트 SaaS 구조
- 모바일 앱
- 외부 ERP/택배사/마켓플레이스 대규모 연동

v0.2.0 완료 조건:

- [x] v0.1.9 검증 결과를 반영했다.
- [x] v0.2 범위와 제외 범위를 정의했다.
- [x] v0.2 우선순위를 정리했다.
- [x] v0.2 버전별 작업 계획을 작성했다.
- [x] `v0.2.0` 태그를 생성했다.
