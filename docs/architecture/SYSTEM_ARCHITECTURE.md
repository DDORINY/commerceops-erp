# 시스템 아키텍처

## 전체 구조

```text
사용자/관리자 브라우저
  -> Next.js Frontend
  -> Spring Boot Backend
  -> MySQL
```

AI 확장 후보:

```text
Spring Boot Backend
  -> FastAPI AI Server
  -> model.pt/model.pkl
```

## 구성 요소

| 구성 요소 | 역할 |
| --- | --- |
| Next.js Frontend | 사용자 쇼핑몰과 관리자 화면 |
| Spring Boot Backend | 인증, 주문, 결제, 재고, 배송, 권한, 감사 로그 API |
| MySQL | 운영 데이터 저장 |
| FastAPI AI Server 후보 | v0.9 AI 추론 API |
| Nginx 후보 | `/`, `/api`, `/ai` reverse proxy |

## 관리자 요청 흐름

```text
Admin UI -> apiClient -> JWT Bearer -> Spring Security -> PermissionChecker -> Service -> Repository -> MySQL
```

## 쇼핑몰 사용자 요청 흐름

```text
Shop UI -> publicApiClient 또는 apiClient -> Product/Order API -> Service -> MySQL
```

공개 상품/카테고리/배너 조회는 비회원 접근이 가능하고, 보호 API는 인증 실패 시 로그인 흐름으로 이동한다.

## AI 추론 요청 흐름 후보

```text
Admin AI UI -> Spring Boot Backend -> FastAPI AI Server -> model file -> prediction response
```

## AWS 1대 서버 배포 후보

```text
AWS EC2
  Nginx
    /    -> frontend container
    /api -> backend container
    /ai  -> ai container
  MySQL container or managed DB 후보
  volumes:
    mysql-data
    uploads
    ai-models
```
