# v1.0 릴리스 Smoke Test 결과

## 목적

최종 포트폴리오 릴리스 전 핵심 빌드/테스트 명령과 주요 화면 수동 확인 후보를 정리한다.

## 자동 검증 결과

| 항목 | 명령 | 결과 | 비고 |
| --- | --- | --- | --- |
| backend test | `.\gradlew.bat test --no-daemon` | 통과 | `GRADLE_USER_HOME=backend/.gradle-cache` 사용 |
| frontend lint | `npm.cmd run lint` | 통과 | ESLint 오류 없음 |
| frontend build | `npm.cmd run build` | 조건부 통과 | 기본 `.next`는 Windows `spawn EPERM` 발생 |
| frontend build 재검증 | `NEXT_DIST_DIR=.next-build-check-v103 npm.cmd run build` | 통과 | 임시 산출물 삭제 완료 |
| 문서 공백 검증 | `git diff --check` | 통과 | 공백 오류 없음 |

## 수동 확인 후보

v1.0.3에서는 자동 검증을 우선 수행하고, 브라우저 수동 확인은 최종 제출 전 후보로 문서화한다.

| 영역 | 확인 항목 |
| --- | --- |
| 인증 | 로그인, 로그아웃, refresh token, 세션 만료 안내 |
| 쇼핑몰 | 메인, 배너, 카테고리 네비, 상품 목록, 상품 상세 |
| 장바구니/주문 | 장바구니 추가, 주문/체크아웃, 주문 내역, 비회원 주문조회 진입점 |
| 관리자 상품 | 상품 목록, 상품 등록/수정, 상세 블록, 상태 변경, 운영 메모 |
| 주문/배송 | 주문 목록, 주문 상태 변경, 출고 지시, 송장, 배송 추적 |
| 재고/바코드 | SKU, 바코드, 생산 입고, 재고 실사, 창고 위치, 안전재고 알림 |
| 회계 | 원장, 거래, 매출 인식, 정산 배치, 기간 마감, 회계 리포트 |
| 권한/감사 | 직원, 권한 그룹, 메뉴 권한, API 권한, 감사 로그 |
| AI 운영 | AI overview, 추천, 수요 예측, 리뷰 분석, 이상 주문, 리스크 알림, 리포트 |

## Known Issue

- Windows 환경에서 기본 `.next` 빌드는 파일 잠금 또는 프로세스 spawn 제한으로 `EPERM`이 발생할 수 있다.
- 검증 시 `NEXT_DIST_DIR`을 별도로 지정하면 빌드가 통과한다.
- 임시 dist 디렉터리는 검증 후 삭제하고, Next가 `tsconfig.json`에 추가한 임시 include는 제거해야 한다.
- 실제 운영 데이터 기반 AI 성능 검증과 실시간 추론 서버 운영은 포트폴리오 범위에서 제외한다.
