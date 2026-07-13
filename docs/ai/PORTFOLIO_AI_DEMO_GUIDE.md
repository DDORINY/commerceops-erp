# 포트폴리오 AI 데모 실행 가이드

## 목적

CommerceOps ERP는 포트폴리오 프로젝트이므로 실제 운영 고객 데이터를 수집하지 않는다. 대신 개인정보가 없는 합성 샘플 데이터로 AI 데이터셋, 학습, 평가, 리포트 흐름을 재현한다.

이 가이드는 실제 서비스 품질의 AI 모델을 만드는 문서가 아니라, AI 커머스 ERP 구조를 포트폴리오에서 설명하고 시연하기 위한 문서다.

## 포함된 데모

### 상품 수요 예측 데모

- 학습 데이터: `ai/datasets/samples/demo_demand_train.json`
- 평가 데이터: `ai/datasets/samples/demo_demand_eval.json`
- 학습 설정: `ai/configs/train_demo_demand.json`
- 평가 설정: `ai/configs/evaluate_demo_demand.json`
- 목표값: `targetDemandQuantity`

### 리뷰 감성 점수 데모

- 학습 데이터: `ai/datasets/samples/demo_review_train.json`
- 평가 데이터: `ai/datasets/samples/demo_review_eval.json`
- 학습 설정: `ai/configs/train_demo_review.json`
- 평가 설정: `ai/configs/evaluate_demo_review.json`
- 목표값: `targetSentimentScore`

## 실행 방법

### 1. 수요 예측 baseline 학습

```powershell
python ai/scripts/train_baseline.py --config ai/configs/train_demo_demand.json
```

PyTorch가 설치되어 있으면 아래 파일이 생성된다.

```text
ai/models/checkpoints/commerceops_demo_demand_baseline.pt
ai/models/checkpoints/commerceops_demo_demand_baseline.metadata.json
```

### 2. 수요 예측 baseline 평가

```powershell
python ai/scripts/evaluate_baseline.py --config ai/configs/evaluate_demo_demand.json
```

평가 리포트는 아래 경로에 생성된다.

```text
ai/reports/generated/
```

### 3. 리뷰 감성 baseline 학습

```powershell
python ai/scripts/train_baseline.py --config ai/configs/train_demo_review.json
```

PyTorch가 설치되어 있으면 아래 파일이 생성된다.

```text
ai/models/checkpoints/commerceops_demo_review_sentiment_baseline.pt
ai/models/checkpoints/commerceops_demo_review_sentiment_baseline.metadata.json
```

### 4. 리뷰 감성 baseline 평가

```powershell
python ai/scripts/evaluate_baseline.py --config ai/configs/evaluate_demo_review.json
```

## Git 관리 기준

아래 파일과 디렉터리는 Git에 커밋하지 않는다.

- `ai/datasets/exports/`
- `ai/datasets/processed/`
- `ai/models/checkpoints/`
- `ai/reports/generated/`
- `*.pt`
- `*.pth`
- `*.onnx`
- `*.safetensors`

샘플 데이터는 개인정보가 없는 합성 데이터이므로 포트폴리오 재현성을 위해 Git에 포함한다.

## 포트폴리오 설명 문구

면접이나 README에서 아래처럼 설명할 수 있다.

> 실제 운영 데이터는 사용하지 않고, 개인정보가 없는 합성 데이터로 상품 수요 예측과 리뷰 감성 분석 baseline 학습 파이프라인을 구성했습니다. 운영 데이터가 생기면 관리자 export API, 마스킹 유틸, 동일한 학습/평가 스크립트 구조로 확장할 수 있도록 설계했습니다.

## 한계

- 현재 모델은 포트폴리오용 baseline 선형 모델이다.
- 실제 추천/예측 정확도를 보장하지 않는다.
- 데이터 양이 매우 작으므로 리포트 수치는 구조 검증용이다.
- 운영 화면 연결은 v0.9 AI 운영 기능 범위로 남겨둔다.
