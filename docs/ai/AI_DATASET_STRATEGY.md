# AI 데이터셋 전략

## 기본 방향

현재 실제 운영 데이터가 없으므로 v0.8에서는 synthetic ERP dataset을 생성해 학습 구조를 먼저 만든다. Git에는 sample dataset만 포함하고, 대용량 raw/processed dataset은 제외한다.

## Git 포함 기준

- 작은 sample CSV/JSON.
- synthetic dataset 생성 스크립트.
- 전처리 코드.
- metrics JSON.

## Git 제외 기준

- 대용량 raw dataset.
- processed dataset.
- `model.pt`, `model.pkl`.
- checkpoint, runs, logs, cache.

## dataset 후보

### 수요 예측 dataset

- SKU
- 날짜
- 판매 수량
- 재고 수량
- 가격/할인
- 카테고리
- 시즌/요일 후보

### 이상 탐지 dataset

- 주문 ID
- 주문 금액
- 주문 품목 수
- 취소/환불 여부
- 재고 변동량
- 사용자/배송지 후보 feature

### 리뷰 감성 분석 dataset

- 리뷰 텍스트
- 평점
- 상품 카테고리
- 감성 label 후보

### 추천 dataset

- 사용자 또는 세션 후보
- 상품 ID
- 주문/조회/찜 이벤트
- 카테고리/태그

## v0.8 구현 범위 후보

- synthetic ERP dataset 생성.
- DB export 스크립트 후보.
- 전처리 파이프라인.
- 학습/평가 스크립트.
- metrics 저장.
