# 문서 로드맵 정합성 정리 체크리스트

작업 브랜치: `docs/sync-roadmap-current-standard`

작업 목적: 오래된 로드맵 기준과 깨진 한글 문서를 현재 실제 버전 기준(`v0.6.9`)으로 정리한다.

## 점검 범위

- [x] `README.md` 한글 깨짐 여부 확인
- [x] `docs/plans/PRODUCT_ROADMAP.md` 오래된 v0.4~v1.0 단계표 확인
- [x] `docs/plans/LONG_TERM_ROADMAP.md` 최신 장기 로드맵 기준 확인
- [x] `docs/operations/CURRENT_STATE.md` 현재 기준 버전 확인
- [x] `docs/plans/VERSION_PLAN.md` v0.6.9 완료 및 v0.7 이관 기준 확인
- [x] `docs/plans/V0.4_HR_PERMISSION_PLAN.md` v0.4 완료 기준 확인
- [x] `docs/plans/V0.5_INVENTORY_BARCODE_PLAN.md` v0.5 완료 기준 확인
- [x] `docs/plans/V0.6_DISTRIBUTION_SHIPPING_PLAN.md` v0.6 완료 기준 확인
- [x] `docs/plans/V0.7_ACCOUNTING_SETTLEMENT_PLAN.md` 한글 깨짐 여부 확인

## 수정 내용

- [x] 루트 `README.md`를 UTF-8 한국어 문서로 재작성
- [x] `docs/plans/PRODUCT_ROADMAP.md`의 예전 단계표를 현재 실제 로드맵 기준으로 교체
- [x] `docs/plans/V0.7_ACCOUNTING_SETTLEMENT_PLAN.md`를 UTF-8 한국어 계획 문서로 재작성
- [x] `docs/README.md` 문서 기준일에 로드맵/인코딩 정합성 재정리 이력 추가
- [x] `docs/operations/CURRENT_STATE.md`에 문서 정합성 정리 이력과 v0.7 계획 문서 링크 추가

## 최신 로드맵 기준

- `v0.3.x`: 상품/전시/운영관리
- `v0.4.x`: 인사관리/권한관리
- `v0.5.x`: 재고관리/생산 입고/바코드 자동화
- `v0.6.x`: 유통관리/송장/배송
- `v0.7.x`: 회계관리 고도화
- `v0.8.x`: AI 데이터셋과 모델 학습 구조
- `v0.9.x`: AI 운영 기능과 최종 포트폴리오 릴리스
- `v1.0`: 정식 서비스 후보

## 제외 범위

- [x] 기능 코드 변경 없음
- [x] DB migration 작성 없음
- [x] API 코드 변경 없음
- [x] v0.7 이후 예정 기능을 완료 상태로 표기하지 않음

## 검증 결과

- [x] 깨진 한글/오래된 단계표 재검색
- [x] `git diff --check`

메모: `git diff --check`는 통과했다. Windows 작업트리 특성상 일부 MD 파일의 LF/CRLF 변환 경고만 표시되었고 공백 오류는 없었다.

## 남은 이슈

- v0.7 회계관리 고도화는 계획 문서만 정리된 예정 범위이며 기능 구현은 v0.7.0 이후에 진행한다.
