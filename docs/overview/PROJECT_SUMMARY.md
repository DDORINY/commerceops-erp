# 프로젝트 요약

CommerceOps ERP는 Spring Boot와 Next.js 기반의 AI 커머스 ERP 포트폴리오 프로젝트다. 쇼핑몰 사용자 화면과 관리자 ERP 화면을 함께 제공하며, 상품/전시부터 주문/결제, 재고/생산/바코드, 출고/배송, 회계/정산, 권한/감사 로그, AI 운영 화면까지 연결한다.

## 핵심 기능

- 사용자 쇼핑몰: 상품 목록/상세, 카테고리 네비, 배너, 장바구니, 주문, 마이페이지.
- 관리자 운영: 상품, 카테고리, 배너, 주문, 결제/환불, 고객, 리뷰, 문의, 쿠폰.
- 인사/권한: 직원, 부서, 직급, 권한 그룹, 메뉴/기능 권한, API permission 검증.
- 재고/생산: SKU/바코드, 생산 입고, 바코드 라벨, 재고 실사, 창고 위치, 안전재고.
- 유통/배송: 출고 지시, 택배사/배송 방법, 송장번호, 송장 라벨, 배송 추적, 반품 배송, 출고 바코드 검수.
- 회계/정산: 회계 원장, 주문 매출 인식, 환불/배송비/택배비 회계 반영, 정산 배치, 기간 마감, 회계 리포트.
- AI: 데이터셋 export, 개인정보 마스킹, 합성 샘플 데이터, baseline `.pt` 모델, AI 운영 화면.

## 기술스택

- Frontend: Next.js, React, TypeScript
- Backend: Spring Boot, Java 17, JPA, Spring Security, JWT
- Database: MySQL 8, Flyway
- AI 후보: Python, FastAPI, pandas, scikit-learn, PyTorch
- Infra 후보: Docker, Docker Compose, Nginx, AWS EC2

## 아키텍처 요약

```text
Next.js Frontend
  -> Spring Boot Backend
  -> MySQL

Spring Boot Backend
  -> FastAPI AI Server 후보
  -> model.pt/model.pkl 후보
```

## 버전별 진행 요약

- v0.1: 주요 mock 제거와 실제 API 연결.
- v0.2: 인증/결제/미디어/DB/CI/알림/운영 분석 기반.
- v0.3: 상품/전시/운영관리 고도화.
- v0.4: 인사관리/권한관리/감사 로그/사업자 설정.
- v0.5: 재고관리/생산 입고/바코드 자동화.
- v0.6: 유통관리/송장/배송.
- v0.7: 회계관리 고도화.
- v0.8: AI 데이터셋과 모델 학습 구조.
- v0.9: AI 운영 기능과 최종 포트폴리오 릴리스.
- v1.0: 최종 포트폴리오 릴리스 문서/데모/smoke test 정리.

## 포트폴리오 강조 포인트

- 단순 쇼핑몰이 아니라 운영 흐름이 이어지는 ERP형 구조.
- role 기반 권한에서 permission code 기반 세부 권한으로 확장.
- 재고/바코드/출고/배송/회계 데이터를 AI 학습 후보 데이터로 연결.
- 실제 운영 데이터 없이 합성 샘플 데이터와 baseline 모델로 AI 데모를 재현.
- 모든 버전 작업을 브랜치, 체크리스트, 태그, 검증 결과로 관리.
