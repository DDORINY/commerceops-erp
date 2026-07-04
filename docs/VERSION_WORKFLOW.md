# 버전 작업 규칙

이 문서는 CommerceOps ERP의 버전 단위 작업, 브랜치, 태그, 문서화, 검증 흐름을 정의한다.

## 기준선

- 현재 `main`의 MVP 상태를 기준선으로 둔다.
- MVP 이후 모든 작업은 `v0.1.0`부터 시작한다.
- `main`은 검증이 끝난 버전만 병합되는 안정 브랜치로 사용한다.

## 버전 흐름

| 버전 | 목적 | 산출물 |
| --- | --- | --- |
| `v0.1.0` | 0.1 버전 계획표 문서화 | 버전 계획, 작업 체크리스트, 검증 기준 |
| `v0.1.1` ~ `v0.1.8` | 계획표에 따른 개발 및 구현 | 기능 구현, 문서 갱신, 검증 기록 |
| `v0.1.9` | 0.1 계획 대비 구현 확인 및 테스트 검증 | 최종 검증 체크리스트, 테스트 결과, 보완 사항 |
| `v0.2.0` | 0.1 안정화 이후 고도화 및 추가 기능 계획 문서화 | 0.2 계획표, 범위, 우선순위 |

## 브랜치 규칙

모든 작업은 버전별 브랜치에서 진행한다.

형식:

```text
v{version}-{work}
```

예시:

```text
v0.1.0-version-plan
v0.1.1-admin-inquiry-api
v0.1.2-admin-review-api
v0.1.9-release-verification
v0.2.0-advanced-plan
```

규칙:

- 버전은 `v0.1.0`처럼 태그와 같은 버전 표기를 사용한다.
- 작업명은 소문자 kebab-case로 작성한다.
- 한 브랜치는 하나의 버전 목표만 다룬다.
- 버전 범위를 벗어나는 변경은 다음 버전 계획으로 넘긴다.

## 태그 규칙

각 버전 작업이 `main`에 병합되고 검증이 끝나면 태그를 생성한다.

형식:

```text
v{version}
```

예시:

```text
v0.1.0
v0.1.1
v0.1.9
v0.2.0
```

태그 생성 예시:

```powershell
git tag -a v0.1.0 -m "v0.1.0 version plan"
git push origin v0.1.0
```

## 작업 체크리스트 규칙

모든 버전 작업은 체크리스트로 진행 상태를 남긴다.

필수 체크리스트:

- [ ] 작업 목표가 문서에 정의됨
- [ ] 브랜치명이 버전 규칙을 따름
- [ ] 구현 범위가 해당 버전 목적을 벗어나지 않음
- [ ] 관련 코드 구현 완료
- [ ] 관련 문서 갱신 완료
- [ ] frontend `npm.cmd run lint` 실행
- [ ] frontend `npm.cmd run build` 실행
- [ ] backend `.\gradlew.bat test` 실행
- [ ] 검증 결과 기록
- [ ] 민감 정보, 로컬 환경 파일, AI 작업 문서가 커밋 대상에서 제외됨
- [ ] `main` 병합 후 버전 태그 생성

## 브랜치별 작업 절차

1. 최신 `main`을 기준으로 작업 브랜치를 만든다.

```powershell
git checkout main
git pull origin main
git checkout -b v0.1.0-version-plan
```

2. 작업 체크리스트를 작성한다.

3. 구현 또는 문서 작업을 진행한다.

4. 검증 명령을 실행한다.

```powershell
cd frontend
npm.cmd run lint
npm.cmd run build

cd ../backend
.\gradlew.bat test
```

5. 검증 결과를 문서 또는 작업 요약에 기록한다.

6. 커밋 후 원격 브랜치로 push한다.

```powershell
git add .
git commit -m "docs: add v0.1.0 version plan"
git push -u origin v0.1.0-version-plan
```

7. 검토 후 `main`에 병합한다.

8. 병합된 `main`에서 태그를 생성하고 push한다.

## 검증 예외 처리

- 기본 `.next` 빌드에서 EPERM 잠금이 발생하면 `NEXT_DIST_DIR`을 별도 경로로 지정해 검증한다.
- 검증 후 임시 빌드 산출물은 정리한다.
- Next가 `tsconfig.json`에 임시 include를 추가하면 원상 복구한다.
- 검증을 실행하지 못한 경우, 실행하지 못한 이유와 남은 리스크를 기록한다.

## 문서 갱신 규칙

작업마다 관련 문서를 함께 갱신한다.

- 버전 범위와 체크리스트: `docs/VERSION_PLAN.md`
- 작업 규칙: `docs/VERSION_WORKFLOW.md`
- 기능 상세: `docs/features/`
- API 변경: `docs/API.md`
- DB 변경: `docs/DB_SCHEMA.md`
- 현재 상태: `docs/CURRENT_STATE.md`

## 금지 사항

- 로컬 환경 파일을 커밋하지 않는다.
- JWT secret, DB password, API key, 개인 토큰을 커밋하지 않는다.
- `.agents/`, `.claude/`, `.codex/`, `AGENTS.md`, `CLAUDE.md` 등 AI 도구별 개인 작업 파일을 커밋하지 않는다.
- 검증 없이 버전 태그를 만들지 않는다.
- 한 브랜치에서 여러 버전 범위를 섞지 않는다.
