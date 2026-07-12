# AI 평가 리포트

모델 평가 리포트의 저장 기준을 기록한다.

## 저장 위치

- 생성 리포트: `ai/reports/generated/`
- Git 추적 여부: 제외

## v0.8.8 평가 기준

- 입력 데이터 row 수
- feature column 목록
- target column
- checkpoint 또는 metadata 경로
- PyTorch checkpoint 평가 가능 여부
- 가능한 경우 MAE, RMSE
- 평가 실행 시각

## 주의

- 평가 리포트에는 개인정보 원문을 포함하지 않는다.
- 운영 의사결정 자동 반영은 v0.9 이후 범위다.
- 모델 파일과 리포트 산출물은 Git이 아니라 외부 스토리지 또는 릴리스 아티팩트로 관리한다.
