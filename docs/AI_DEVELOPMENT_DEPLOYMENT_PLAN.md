# AI 개발/학습/배포 전략

기준 버전: `v0.6.9`

문서 상태: v0.8 AI 데이터셋과 모델 학습 구조, v0.9 AI 운영 기능 연결을 준비하기 위한 전략 문서다. 이 문서는 코드나 인프라 파일을 생성하지 않고, 향후 `ai/` 디렉터리와 AWS 배포 구조를 어떻게 운영할지 기준만 정리한다.

## 1. 목적

- CommerceOps ERP에서 AI 기능을 어떤 구조로 개발, 학습, 추론, 배포할지 정리한다.
- 로컬 개발, AI 학습, FastAPI 추론 서버, AWS EC2 1대 서버 배포 기준을 명확히 한다.
- Git에 포함할 파일과 제외할 파일 기준을 정리한다.
- v0.8 데이터셋/모델 학습 구조와 v0.9 관리자 AI 운영 기능 연결의 선행 기준으로 사용한다.

## 2. 전체 구조

향후 프로젝트 구조는 아래 방향을 기준으로 한다.

```text
commerceops-erp/
  backend/
  frontend/
  ai/
  infra/
  docs/
```

| 폴더 | 역할 |
| --- | --- |
| `backend/` | Spring Boot 기반 ERP API 서버 |
| `frontend/` | Next.js 사용자/관리자 화면 |
| `ai/` | 데이터셋 생성, 전처리, 모델 학습, FastAPI 추론 서버 후보 |
| `infra/` | Docker Compose, Nginx, 배포 스크립트 후보 |
| `docs/` | 로드맵, API, DB, 배포, AI 전략 문서 |

## 3. `ai/` 권장 구조

v0.8 이후 실제 AI 작업을 시작할 때 아래 구조를 후보로 사용한다.

```text
ai/
  README.md
  requirements.txt

  datasets/
    sample/
    raw/
      .gitkeep
    processed/
      .gitkeep

  scripts/
    generate_synthetic_erp_data.py
    export_dataset_from_db.py

  preprocessing/
    demand_forecast_preprocess.py
    anomaly_detection_preprocess.py
    review_sentiment_preprocess.py

  training/
    train_demand_forecast.py
    train_anomaly_detection.py
    train_review_sentiment.py

  inference/
    app/
      main.py
      routers/
      services/
      schemas/

  models/
    .gitkeep

  metrics/
    demand_forecast_metrics.json
    anomaly_detection_metrics.json
    review_sentiment_metrics.json
```

## 4. Git에 포함할 파일

Git에는 재현 가능한 코드와 작은 샘플만 포함한다.

- AI 학습 코드
- 전처리 코드
- FastAPI 추론 서버 코드
- synthetic dataset 생성 스크립트
- sample CSV 또는 작은 sample JSON
- metrics JSON
- AI README
- `requirements.txt`
- Dockerfile 후보
- inference API schema

## 5. Git에서 제외할 파일

대용량 파일, 민감 데이터, 학습 산출물은 Git에 포함하지 않는다.

- 대용량 raw dataset
- processed dataset
- `model.pt`
- `model.pkl`
- checkpoint
- training logs
- cache
- `ai/.venv`
- `__pycache__`

`.gitignore` 후보:

```gitignore
# AI datasets
ai/datasets/raw/*
!ai/datasets/raw/.gitkeep

ai/datasets/processed/*
!ai/datasets/processed/.gitkeep

# AI models
ai/models/*
!ai/models/.gitkeep

# AI training outputs
ai/runs/
ai/checkpoints/
ai/logs/
ai/**/__pycache__/
ai/.venv/
```

## 6. AI 학습과 배포 분리 원칙

- 학습은 로컬, Colab, 임시 GPU 환경에서 수행한다.
- AWS EC2 1대 서버는 학습용이 아니라 추론 서버로 사용한다.
- 학습 완료 후 생성된 `model.pt` 또는 `model.pkl`만 AWS 서버에 별도 배치한다.
- AWS 서버에서 대규모 학습을 반복 실행하지 않는다.
- 대형 LLM을 AWS 1대 서버에 직접 호스팅하지 않는다.

## 7. AWS EC2 1대 서버 배포 구조

초기 배포는 Docker Compose 기반 단일 EC2 서버를 후보로 한다.

```text
AWS EC2 1대
  Nginx
    /       -> frontend
    /api    -> backend
    /ai     -> FastAPI AI server

  containers:
    frontend
    backend
    ai
    mysql

  volumes:
    mysql-data
    uploads
    ai-models
```

배포 후보 파일:

- `infra/docker/docker-compose.prod.yml`
- `infra/docker/nginx.conf`
- `infra/scripts/deploy.sh`
- `infra/scripts/backup-db.sh`

이 문서에서는 위 파일을 생성하지 않는다. 실제 인프라 파일은 별도 배포 버전에서 작성한다.

## 8. AWS 서버에서 Git 사용 방식

권장 방식:

- AWS 서버는 `main` 또는 release tag 기준으로 clone/checkout한다.
- 배포 설정은 별도 브랜치에서 작업 후 검증되면 `main`에 병합한다.
- `deploy` 브랜치를 `main`과 장기간 분리해 운영하지 않는다.
- 실제 배포는 가능한 경우 release tag 기준으로 수행한다.

예시:

```bash
git clone https://github.com/DDORINY/commerceops-erp.git
cd commerceops-erp
git checkout v0.9.0
```

서버에 별도로 배치할 파일:

- `.env`
- `backend/.env`
- `frontend/.env.production`
- `ai/.env`
- `ai/models/*.pt`
- `ai/models/*.pkl`

## 9. 배포 브랜치 전략

| 브랜치 | 용도 |
| --- | --- |
| `main` | 최신 안정 버전, 배포 가능한 상태 |
| `v0.x.x-feature-branch` | 버전별 기능 작업 |
| `deploy/aws-ec2-compose` | AWS Docker Compose 배포 설정 작업용 임시 브랜치 |
| `hotfix/*` | 긴급 수정 |

배포 브랜치는 설정 검증을 위한 임시 작업 브랜치로 사용하고, 검증 후 `main`에 병합한다. AWS 배포는 `main` 또는 release tag 기준으로 수행한다.

## 10. Windows 로컬 개발과 Linux 배포 주의사항

- 코드와 문서에 Windows 절대 경로를 고정하지 않는다.
- `C:\...` 경로는 문서 예시나 로컬 검증 기록에만 제한적으로 사용한다.
- 파일명 대소문자를 일관되게 사용한다.
- 배포 스크립트는 bash/Linux 기준으로 작성한다.
- `npm.cmd`는 Windows 로컬 검증 문서에서만 사용한다.
- 서버 파일 경로는 `/app/uploads`, `/app/ai/models` 같은 Linux 경로를 사용한다.
- 환경 변수는 `.env` 기준으로 주입한다.
- 실제 배포 전 Docker 기반 Linux 환경 검증을 권장한다.

## 11. AI 모델 우선순위

### 1. SKU 수요 예측

- 목적: SKU별 단기 수요와 재고 부족 위험을 예측한다.
- 후보 기술: pandas, scikit-learn, PyTorch.
- 산출물: `model.pt` 또는 `model.pkl`.

### 2. 이상 주문/재고 탐지

- 목적: 비정상 주문, 급격한 재고 변동, 반복 취소/환불 후보를 탐지한다.
- 후보 기술: rule-based baseline, Isolation Forest, AutoEncoder.

### 3. 리뷰 감성 분석

- 목적: 리뷰 텍스트를 긍정/부정/중립 또는 이슈 카테고리로 분류한다.
- 후보 기술: TF-IDF + Logistic Regression을 우선 검토하고, KoBERT/KoELECTRA는 후속 후보로 둔다.

### 4. 상품 추천

- 목적: 인기 상품, 같이 구매한 상품, 운영자가 지정한 추천 기준을 조합한다.
- 초기 구현은 복잡한 추천 모델보다 통계 기반 또는 작업 필터 기반 추천을 우선한다.

## 12. LLM 비용 제한 정책

LLM은 핵심 모델이 아니라 관리자 보조 기능으로 제한한다.

- 자동 호출을 금지한다.
- 관리자 버튼 클릭 시에만 호출한다.
- 사용자별 또는 관리자별 일일 사용량 제한을 둔다.
- 같은 입력은 캐싱한다.
- 사용 로그와 비용 추정 로그를 남긴다.
- 적용 범위는 리뷰 답변 초안, 운영 리포트 요약, 이상 탐지 설명 생성처럼 보조 기능으로 제한한다.

테이블 후보:

- `llm_usage_logs`
- `llm_usage_limits`
- `llm_response_cache`

## 13. NVIDIA 모델/도구 사용 기준

| 도구 | 사용 기준 |
| --- | --- |
| Nemotron/NIM | 제한적인 LLM 보조 기능 후보. AWS 1대 서버 직접 호스팅은 비추천 |
| RAPIDS/cuML | 데이터 전처리와 이상 탐지 가속 후보 |
| Merlin | 추천 시스템 고도화 후보 |
| Cosmos/GR00T/BioNeMo | CommerceOps ERP 범위에서는 제외 |

대형 NVIDIA 모델을 직접 호스팅하는 방식은 AWS EC2 1대 서버 기준으로 비추천한다. 필요할 경우 외부 API 또는 별도 GPU 환경을 검토한다.

## 14. v0.8/v0.9 적용 계획

### v0.8 AI 데이터셋과 모델 학습 구조

- synthetic ERP dataset 생성.
- 운영 DB에서 학습 데이터셋 export 후보 구현.
- 수요 예측 전처리.
- 이상 탐지 전처리.
- 리뷰 감성 분석 전처리.
- 수요 예측 모델 학습.
- 이상 탐지 모델 학습.
- 리뷰 감성 분석 모델 학습.
- FastAPI AI server 후보 구조 작성.
- `model.pt` 또는 `model.pkl` 산출 기준 정리.

### v0.9 AI 운영 기능과 최종 포트폴리오 릴리스

- Spring Boot와 FastAPI 연동.
- 관리자 AI 대시보드.
- AI 수요 예측 화면.
- 이상 탐지 알림.
- 리뷰 분석 화면.
- LLM 보조 기능.
- 사용량 제한, 캐싱, 로그.
- 최종 포트폴리오 데모 흐름.

## 15. 결론

- AI 코드는 같은 repository의 `ai/` 폴더에 둔다.
- 대용량 dataset과 model 파일은 Git에 포함하지 않는다.
- AWS EC2 1대 서버는 학습용이 아니라 추론용으로 사용한다.
- 배포 설정은 별도 브랜치에서 작업 후 `main`에 병합한다.
- AWS 배포는 `main` 또는 release tag 기준으로 수행한다.
- 핵심 AI는 자체 학습 모델 중심으로 구성하고, LLM은 제한적인 보조 기능으로 설계한다.
