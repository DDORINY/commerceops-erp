# AI 추론 API (v0.9.8)

기존 AI 문서에서 추론 서버는 향후 작업으로 표시되어 있었으나 이번 브랜치에서 FastAPI 컨테이너를 추가했다.

- ai/inference/app.py: checkpoint 자동 로드, /health, /predict, /predict/image
- ai/inference/Dockerfile: CPU 전용 PyTorch와 FastAPI 런타임
- docker-compose.prod.yml: 비root·read-only·no-new-privileges·healthcheck
- deploy/nginx/http.conf, deploy/nginx/https.conf: /ai/ upstream 라우팅
- frontend/src/components/admin/AiImageInferenceCard.tsx: 관리자 이미지 추론 UI

/predict/image는 현재 학습된 이미지 모델이 없는 상태에서 제공하는 baseline 품질 휴리스틱이다. 실제 이미지 모델 도입 시 동일 endpoint 뒤의 구현만 교체한다.

운영에서 health를 UP으로 만들려면 ai/models/checkpoints에 demand/review .pt와 .metadata.json을 배치해야 한다. 대용량 checkpoint는 Git에 저장하지 않는다.
