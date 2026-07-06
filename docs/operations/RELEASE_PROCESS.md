# 릴리스 프로세스

## 기본 원칙

- 모든 작업은 버전 또는 목적별 브랜치에서 진행한다.
- 체크리스트를 먼저 작성하거나 갱신한다.
- 코드 변경이 있으면 frontend lint/build, backend test를 실행한다.
- 문서만 변경하면 `git diff --check`를 실행한다.
- 실패하면 다음 버전으로 넘어가지 않고 즉시 중단해 보고한다.

## 작업 순서

1. main 최신 상태 확인.
2. 작업 브랜치 생성.
3. 체크리스트 작성 또는 갱신.
4. 구현 또는 문서 작업.
5. 관련 문서 갱신.
6. 검증 실행.
7. 변경 파일 확인.
8. 커밋.
9. 원격 브랜치 push.
10. main 병합.
11. 태그 생성.
12. main과 tag push.
13. 작업트리 clean 확인.

## 브랜치 예시

```text
v0.6.7-outbound-barcode-verification
docs/restructure-documentation
hotfix/admin-sales-key
```

## 태그 기준

- 기능 버전: `v0.x.y`
- patch/hotfix: `v0.x.y.z` 또는 명확한 hotfix 태그
- 문서 작업: 필요 시 `docs-*` 태그 사용

## 문서 갱신 기준

- API 변경: `architecture/API_REFERENCE.md`
- DB 변경: `architecture/DATABASE_SCHEMA.md`, `architecture/DB_MIGRATION.md`
- 화면 변경: `architecture/FRONTEND_STRUCTURE.md`
- 백엔드 구조 변경: `architecture/BACKEND_STRUCTURE.md`
- 버전 계획 변경: `plans/VERSION_PLAN.md`
- 현재 상태 변경: `operations/CURRENT_STATE.md`
