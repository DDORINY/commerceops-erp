# CommerceOps ERP 문서 안내

기준 버전: `v1.0.1`

CommerceOps ERP 문서는 포트폴리오 검토, 개발 인수인계, 버전 운영, 배포/AI 전략을 빠르게 찾을 수 있도록 목적별 폴더로 정리한다.

## 문서 폴더 구조

```text
docs/
  README.md
  INDEX.md
  overview/       # 프로젝트 개요, 기능명세, 기술스택, 포트폴리오 요약
  architecture/   # 시스템 구조, API, DB, 백엔드/프론트 구조, 보안/권한
  plans/          # 제품 로드맵, 버전 계획, v0.x 계획 문서
  ai/             # AI 개발, 데이터셋, 모델, LLM 사용 전략
  operations/     # 현재 상태, 배포/릴리스/문서 운영 기준
  checklists/     # 버전별 작업 체크리스트
  adr/            # 아키텍처 의사결정 기록
  features/       # 기능별 보충 문서
  templates/      # 문서 템플릿
```

## 처음 읽을 문서 순서

1. [프로젝트 요약](./overview/PROJECT_SUMMARY.md)
2. [기능명세서](./overview/FEATURE_SPECIFICATION.md)
3. [시스템 아키텍처](./architecture/SYSTEM_ARCHITECTURE.md)
4. [버전 계획표](./plans/VERSION_PLAN.md)
5. [현재 구현 현황](./operations/CURRENT_STATE.md)
6. [AI 개발/학습/배포 전략](./ai/AI_DEVELOPMENT_DEPLOYMENT_PLAN.md)

## 포트폴리오 검토자 추천 문서

- [프로젝트 개요](./overview/PROJECT_OVERVIEW.md)
- [프로젝트 요약](./overview/PROJECT_SUMMARY.md)
- [기능명세서](./overview/FEATURE_SPECIFICATION.md)
- [기술스택](./overview/TECH_STACK.md)
- [제품 로드맵](./plans/PRODUCT_ROADMAP.md)
- [최종 포트폴리오 요약](./overview/PORTFOLIO_SUMMARY.md)

## 개발자 추천 문서

- [API Reference](./architecture/API_REFERENCE.md)
- [Database Schema](./architecture/DATABASE_SCHEMA.md)
- [Backend Structure](./architecture/BACKEND_STRUCTURE.md)
- [Frontend Structure](./architecture/FRONTEND_STRUCTURE.md)
- [Security Permission Policy](./architecture/SECURITY_PERMISSION_POLICY.md)
- [Data Flow](./architecture/DATA_FLOW.md)
- [Algorithm Structure](./architecture/ALGORITHM_STRUCTURE.md)

## 배포/운영/AI 문서

- [현재 구현 현황](./operations/CURRENT_STATE.md)
- [배포/환경 분리](./operations/DEPLOYMENT_ENV.md)
- [AWS 1대 서버 배포 계획](./operations/AWS_SINGLE_SERVER_DEPLOYMENT_PLAN.md)
- [릴리스 프로세스](./operations/RELEASE_PROCESS.md)
- [AI 개발/학습/배포 전략](./ai/AI_DEVELOPMENT_DEPLOYMENT_PLAN.md)
- [포트폴리오 AI 데모 실행 가이드](./ai/PORTFOLIO_AI_DEMO_GUIDE.md)
- [AI 운영 화면 데모 시나리오](./ai/AI_OPERATIONS_DEMO_SCRIPT.md)
- [AI 모델 전략](./ai/AI_MODEL_STRATEGY.md)
- [AI 데이터셋 전략](./ai/AI_DATASET_STRATEGY.md)

## 체크리스트

- [v0.1 체크리스트](./checklists/v0.1/)
- [v0.2 체크리스트](./checklists/v0.2/)
- [v0.3 체크리스트](./checklists/v0.3/)
- [v0.4 체크리스트](./checklists/v0.4/)
- [v0.5 체크리스트](./checklists/v0.5/)
- [v0.6 체크리스트](./checklists/v0.6/)
- [v0.7 체크리스트](./checklists/v0.7/)
- [v0.8 체크리스트](./checklists/v0.8/)
- [v0.9 체크리스트](./checklists/v0.9/)
- [v1.0 체크리스트](./checklists/v1.0/)
- [hotfix/checklist](./checklists/hotfix/)

## 전체 인덱스

전체 문서 목록은 [INDEX.md](./INDEX.md)를 기준으로 한다.
