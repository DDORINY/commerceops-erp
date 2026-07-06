# AWS 1대 서버 배포 계획

## 목표

초기 배포는 AWS EC2 1대와 Docker Compose를 기준으로 한다. 서버는 학습용이 아니라 웹 서비스와 AI 추론용으로 사용한다.

## 배포 구조 후보

```text
AWS EC2 1대
  Nginx
    /    -> frontend
    /api -> backend
    /ai  -> FastAPI AI server

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

## 환경 변수 관리

- `.env`
- `backend/.env`
- `frontend/.env.production`
- `ai/.env`

민감 정보와 운영 환경 변수는 Git에 포함하지 않는다.

## AI 모델 배치

- `model.pt`, `model.pkl`은 Git에 포함하지 않는다.
- 서버의 `ai-models` volume 또는 `/app/ai/models`에 별도 배치한다.
- 학습은 로컬/Colab/임시 GPU 환경에서 수행한다.
- EC2 서버는 추론 API를 제공한다.

## 배포 기준

- `main` 또는 release tag 기준으로 checkout한다.
- 배포 설정은 별도 브랜치에서 검증 후 `main`에 병합한다.
- 운영 배포는 가능한 경우 tag 기준으로 수행한다.

## 제외 범위

- 멀티 서버 오케스트레이션.
- Kubernetes.
- 대형 GPU 학습 서버.
- 실제 CDN/S3 완전 연동.
