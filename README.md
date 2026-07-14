# CommerceOps ERP

CommerceOps ERP는 쇼핑몰 사용자 기능과 관리자 ERP 기능을 하나의 데이터 흐름으로 연결하는 AI 커머스 ERP 포트폴리오 프로젝트입니다.

> 본 프로젝트의 토스페이먼츠 연동은 포트폴리오 시연을 위한 테스트 환경입니다. 실제 결제와 정산은 발생하지 않으며, 라이브 결제를 사용하려면 사업자등록, 토스페이먼츠 계약 및 카드사 심사가 필요합니다.

결제위젯 SDK v2의 문서용 테스트 키 세트를 사용합니다. `NEXT_PUBLIC_TOSS_CLIENT_KEY`에 `test_gck`로 시작하는 클라이언트 키를, `TOSS_SECRET_KEY`에 같은 세트의 `test_gsk`로 시작하는 시크릿 키를 주입해야 합니다. 키 값은 소스코드와 `.env.example`에 포함하지 않습니다. 라이브 키는 애플리케이션에서 거부되며, 라이브 결제 전환은 현재 구현 범위에 포함되지 않습니다.

상품 전시, 주문, 결제, 재고, 생산 입고, 바코드, 출고, 송장, 배송, 반품, 회계, 권한, 감사 로그, AI 운영 화면까지 실제 운영에서 이어지는 흐름을 축소 구현하는 것을 목표로 합니다. 현재 기준 구현 상태와 로드맵은 `v0.9.9` 완료 및 `v1.0` 최종 포트폴리오 릴리스 계획 기준으로 정리되어 있습니다.

> 문서 구조는 [docs/README.md](./docs/README.md)에서 시작하고, 포트폴리오 제출용 요약은 [docs/overview/PROJECT_SUMMARY.md](./docs/overview/PROJECT_SUMMARY.md)를 확인하세요.

## 프로젝트 개요

| 항목 | 내용 |
| --- | --- |
| 프로젝트명 | CommerceOps ERP |
| 형태 | 쇼핑몰 + 관리자 ERP 통합 시스템 |
| 주요 사용자 | 일반 구매자, 쇼핑몰 운영자, 관리자, 물류/재고 담당자 |
| 핵심 기능 | 상품/전시, 주문/결제, 재고/생산/바코드, 출고/배송, 고객/CS, 권한/감사 로그 |
| 기술 스택 | Spring Boot, Java 17, MySQL 8, Next.js, TypeScript |

## 현재 구현 요약

| 영역 | 상태 |
| --- | --- |
| 인증/세션 | JWT 로그인, refresh token, 로그아웃, 공개 API와 보호 API 401 처리 분리 |
| 상품/전시 | 상품 마스터, 상세 블록 CMS, 카테고리 네비, 메인 배너 CMS, 상품 판매/전시 상태 |
| 주문/결제 | 주문 생성, 내 주문 조회, 관리자 주문 상태 변경, 결제 승인/취소/환불 |
| 리뷰/문의/알림 | 리뷰 숨김/해제, 문의 답변, 사용자 알림 |
| 인사/권한 | 직원/부서/직급, 권한 그룹, 메뉴/기능 권한, API permission 정책 |
| 감사 로그 | 주요 관리자 변경 작업, 권한 실패, 요청 컨텍스트와 JSON 변경 이력 |
| 재고/생산/바코드 | SKU/바코드, 생산 입고, 바코드 라벨, 입출고, 재고 실사, 창고 위치, 안전재고 |
| 유통/배송 | 출고 지시, 택배사/배송 방법, 송장번호, 송장 라벨, 배송 추적, 반품 배송, 출고 바코드 검수 |
| 회계/정산 | 회계 원장, 주문 매출 인식, 환불/배송비/택배비 회계 반영, 정산 배치, 기간 마감, 회계 리포트 |
| AI | 데이터셋 export, 개인정보 마스킹, 합성 샘플 데이터, baseline `.pt` 모델, AI 운영 화면 |

## 장기 로드맵

| 버전 | 목표 |
| --- | --- |
| `v0.3.x` | 상품/전시/운영관리 고도화 |
| `v0.4.x` | 인사관리/권한관리 |
| `v0.5.x` | 재고관리/생산 입고/바코드 자동화 |
| `v0.6.x` | 유통관리/송장/배송 |
| `v0.7.x` | 회계관리 고도화 |
| `v0.8.x` | AI 데이터셋과 모델 학습 구조 |
| `v0.9.x` | AI 운영 기능과 최종 포트폴리오 릴리스 |
| `v1.0` | 최종 포트폴리오 릴리스 |

상세 로드맵은 [docs/plans/PRODUCT_ROADMAP.md](./docs/plans/PRODUCT_ROADMAP.md), [docs/plans/LONG_TERM_ROADMAP.md](./docs/plans/LONG_TERM_ROADMAP.md), [docs/plans/VERSION_PLAN.md](./docs/plans/VERSION_PLAN.md)를 기준으로 합니다.

## 실행 구조

```text
CommerceOps ERP/
├─ frontend/   # Next.js 사용자/관리자 화면
├─ backend/    # Spring Boot API 서버
├─ docs/       # 현재 상태, API, DB, 로드맵, 체크리스트
└─ README.md
```

## 검증 명령

프론트엔드:

```powershell
cd frontend
npm.cmd run lint
npm.cmd run build
```

백엔드:

```powershell
cd backend
.\gradlew.bat test
```

문서만 수정하는 작업은 코드 검증 대신 `git diff --check`로 공백 오류를 확인합니다.

## 문서 시작점

- [문서 안내](./docs/README.md)
- [문서 전체 인덱스](./docs/INDEX.md)
- [포트폴리오 요약](./docs/overview/PROJECT_SUMMARY.md)
- [기능명세서](./docs/overview/FEATURE_SPECIFICATION.md)
- [현재 구현 현황](./docs/operations/CURRENT_STATE.md)
- [버전 계획표](./docs/plans/VERSION_PLAN.md)
- [제품 로드맵](./docs/plans/PRODUCT_ROADMAP.md)
- [장기 로드맵](./docs/plans/LONG_TERM_ROADMAP.md)
- [API 명세](./docs/architecture/API_REFERENCE.md)
- [DB 스키마](./docs/architecture/DATABASE_SCHEMA.md)
