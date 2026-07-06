# docs 구조 개편 체크리스트

작업 브랜치: `docs/restructure-documentation`

## 목적

문서가 루트 `docs/`에 혼재되어 있던 상태를 목적별 하위 폴더로 정리하고, 포트폴리오 검토자와 개발자가 문서를 쉽게 탐색할 수 있게 한다.

## 수행 항목

- [x] `overview/`, `architecture/`, `plans/`, `ai/`, `operations/` 폴더 생성
- [x] API/DB/백엔드/프론트/권한 문서를 `architecture/`로 이동
- [x] 로드맵/버전 계획/v0.x 계획 문서를 `plans/`로 이동
- [x] 현재 상태/배포/릴리스/문서 운영 문서를 `operations/`로 이동
- [x] AI 전략 문서를 `ai/`로 이동
- [x] 체크리스트를 `v0.1` ~ `v0.6`, `hotfix`로 분류
- [x] `docs/README.md` 재작성
- [x] `docs/INDEX.md` 생성
- [x] overview 핵심 문서 생성
- [x] architecture 보조 문서 생성
- [x] AI 모델/데이터셋 전략 문서 생성
- [x] AWS 1대 서버 배포 계획 문서 생성
- [x] 릴리스 프로세스 문서 생성
- [x] 루트 README 문서 진입 링크 갱신
- [x] 이전 문서 경로와 상대 링크 보정

## 검증

- [x] 로컬 Markdown 링크 존재 여부 점검
- [x] 이전 루트 문서 경로 잔존 여부 검색
- [x] `git diff --check`
- [x] 코드 변경 없음 확인

## 남은 후보

- v0.7 이후 작업이 시작되면 `plans/` 하위에 v0.8/v0.9 세부 계획 문서를 추가한다.
- 실제 배포 파일이 생기면 `operations/AWS_SINGLE_SERVER_DEPLOYMENT_PLAN.md`와 `operations/DEPLOYMENT_ENV.md`를 함께 갱신한다.
