# CommerceOps ERP AI 학습 파이프라인

이 디렉터리는 v0.8 AI 데이터셋과 모델 학습 기반을 관리한다.

## 원칙

- 운영 DB 원본은 Git에 저장하지 않는다.
- export 데이터는 `ai/datasets/exports/`에 두며 Git 추적 대상에서 제외한다.
- 전처리 산출물은 `ai/datasets/processed/`에 두며 Git 추적 대상에서 제외한다.
- 모델 산출물은 `ai/models/checkpoints/`에 두며 Git 추적 대상에서 제외한다.
- `.pt`, `.pth`, `.onnx`, `.safetensors` 파일은 Git에 커밋하지 않는다.
- 학습 로그와 리포트에는 개인정보 원문을 남기지 않는다.

## v0.8.7 최소 학습 흐름

1. 관리자 AI 데이터셋 export API에서 JSON 데이터를 내려받는다.
2. 필요한 경우 rows 배열만 JSONL 또는 CSV로 변환한다.
3. `ai/configs/train_baseline.json`의 경로와 target 설정을 맞춘다.
4. `python ai/scripts/train_baseline.py --config ai/configs/train_baseline.json`을 실행한다.
5. PyTorch가 설치된 환경이면 `.pt` 체크포인트를 생성한다.
6. PyTorch가 없는 환경이면 모델 대신 학습 메타데이터 JSON만 생성해 파이프라인 입력 검증을 수행한다.

## 후속 범위

- 데이터셋별 전처리 스크립트 분리
- 상품 추천, 수요 예측, 리뷰 분석, 이상 주문 탐지별 모델 분리
- 학습/평가 리포트 자동 생성
- 운영 화면 연동은 v0.9로 이관
