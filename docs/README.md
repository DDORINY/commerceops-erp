# CommerceOps ERP 문서 안내

CommerceOps ERP 문서는 현재 구현 상태, 버전 계획, 작업 규칙, API/DB 구조를 함께 관리한다.

## 먼저 읽을 문서

1. [현재 구현 현황](./CURRENT_STATE.md): 현재 코드 기준으로 가능한 기능과 남은 이슈를 확인한다.
2. [버전 계획표](./VERSION_PLAN.md): MVP 이후 `v0.1.0`부터의 버전별 작업 계획과 체크리스트를 확인한다.
3. [버전 작업 규칙](./VERSION_WORKFLOW.md): 브랜치명, 태그, 검증, 문서 갱신 규칙을 확인한다.
4. [제품 로드맵](./PRODUCT_ROADMAP.md): 제품이 어떤 방향으로 확장되는지 확인한다.
5. [문서 운영 기준](./DOCUMENTATION_GUIDE.md): 문서를 언제 어떻게 갱신해야 하는지 확인한다.

## 버전 운영 요약

현재 `main`에 올라간 상태를 MVP 기준선으로 둔다.

이후 작업은 다음 규칙을 따른다.

- 모든 작업은 버전 브랜치에서 진행한다.
- 브랜치명은 `v{version}-{work}` 형식을 사용한다.
- 정식 버전은 `v0.1.0`부터 시작한다.
- `v0.1.0`은 버전 계획표 문서화 버전이다.
- `v0.1.1` ~ `v0.1.8`은 개발 및 구현 버전이다.
- `v0.1.9`는 계획 대비 구현 확인과 테스트 검증 버전이다.
- `v0.2.0`은 v0.1 완료 이후 고도화 및 추가 기능 계획 문서화 버전이다.
- 각 버전 완료 후 `main` 병합과 태그 생성을 진행한다.

예시:

```text
v0.1.0-version-plan
v0.1.1-admin-inquiries-api
v0.1.9-release-verification
v0.2.0-advanced-plan
```

## 체크리스트 운영

모든 작업은 체크리스트로 진행 흐름을 남긴다.

- 버전별 전체 계획: [VERSION_PLAN.md](./VERSION_PLAN.md)
- 버전 작업 규칙: [VERSION_WORKFLOW.md](./VERSION_WORKFLOW.md)
- 작업 체크리스트 템플릿: [VERSION_TASK_CHECKLIST.md](./templates/VERSION_TASK_CHECKLIST.md)
- 릴리스 체크리스트: [RELEASE_CHECKLIST.md](./templates/RELEASE_CHECKLIST.md)

## 상세 기술 문서

| 문서 | 역할 | 갱신 시점 |
| --- | --- | --- |
| [API 명세](./API.md) | 요청, 응답, 권한, 오류 계약 | API 추가 또는 변경 |
| [DB 스키마](./DB_SCHEMA.md) | 테이블, 관계, 제약, 데이터 정책 | 엔티티 또는 마이그레이션 변경 |
| [백엔드 구조](./BACKEND_STRUCTURE.md) | 패키지, 계층, 트랜잭션 구조 | 백엔드 구조 변경 |
| [프론트엔드 구조](./FRONTEND_STRUCTURE.md) | 라우팅, 컴포넌트, 상태 관리 | 화면 또는 구조 변경 |

## 작업 시 필수 검증

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

주의:

- 기본 `.next`에서 EPERM 잠금이 발생하면 `NEXT_DIST_DIR`을 별도로 지정해 빌드 검증한다.
- 임시 빌드 산출물은 검증 후 정리한다.
- Next가 `tsconfig.json`에 임시 include를 추가하면 원상 복구한다.
- 검증하지 못한 항목은 이유와 남은 리스크를 기록한다.

## 커밋 제외 원칙

다음 항목은 커밋하지 않는다.

- `.env`, `.env.local`, `application-local.yml`
- JWT secret, DB password, API key, 개인 토큰
- `.agents/`, `.claude/`, `.codex/`, `AGENTS.md`, `CLAUDE.md`
- `node_modules/`, `.next/`, `.gradle/`, `build/`

## 상태 표기

| 표기 | 의미 |
| --- | --- |
| `[ ]` | 미완료 |
| `[x]` | 완료 |
| `Blocked` | 외부 입력 또는 선행 작업 없이는 진행 불가 |
| `Deferred` | 이번 버전 범위에서 제외하고 이후 버전으로 이관 |

## 문서 기준일

- 최초 정리: 2026-07-04
- 버전 운영 규칙 도입: 2026-07-04
