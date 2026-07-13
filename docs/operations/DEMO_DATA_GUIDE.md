# 포트폴리오 데모 데이터/계정 가이드

## 목적

CommerceOps ERP를 발표나 면접에서 재현할 때 필요한 데모 계정, 샘플 데이터 범위, AI 데모 데이터와 `.pt` 모델 재생성 기준을 정리한다.

이 문서는 실제 운영 개인정보나 운영 DB 덤프를 포함하지 않는다. 모든 데모 데이터는 로컬 개발 환경에서 직접 생성하거나, 개인정보가 없는 합성 샘플 데이터를 사용한다.

## 데모 계정 기준

실제 비밀번호나 운영 계정은 Git에 커밋하지 않는다. 아래 계정은 로컬 seed 또는 수동 생성 시 사용할 역할 기준이다.

| 역할 | 용도 | 권장 권한 |
| --- | --- | --- |
| 최고관리자 | 전체 관리자 기능 시연 | `SUPER_ADMIN` |
| 운영 관리자 | 상품, 주문, 배송, CS, 회계 조회/운영 시연 | `ADMIN` |
| 매니저 | 조회 중심 관리자 화면과 제한 권한 시연 | `MANAGER` |
| 일반 사용자 | 쇼핑몰 상품 조회, 장바구니, 주문, 마이페이지 시연 | `USER` |
| 비회원 | 공개 쇼핑몰 접근과 비회원 주문조회 진입점 시연 | 인증 없음 |

권장 원칙:
- 데모 계정 이메일과 비밀번호는 `.env`, 로컬 메모, 발표용 별도 문서에서 관리한다.
- 실제 개인 이메일, 전화번호, 주소를 사용하지 않는다.
- 권한 차이를 보여줄 때는 `SUPER_ADMIN`, `ADMIN`, `MANAGER` 계정으로 관리자 메뉴 노출 차이를 확인한다.

## 데모 데이터 범위

### 쇼핑몰/상품

- 카테고리: 상단 네비에 노출되는 활성 카테고리 3~5개
- 상품: 판매중, 품절 임박, 품절, 판매중지, 숨김 상품을 각각 준비
- 상품 상세 블록: 제목, 본문, 이미지, 안내, 스펙표 블록을 포함
- 배너: 현재 기간에 노출되는 메인 배너와 기간 종료/비활성 배너를 함께 준비

### 주문/결제/환불

- 결제 완료 주문
- 배송 준비 주문
- 배송 중/배송 완료 주문
- 취소/환불 후보 주문
- 높은 할인율 또는 고액 주문처럼 AI 이상 탐지 화면에서 확인할 수 있는 주문

### 재고/SKU/바코드

- SKU와 바코드가 연결된 상품
- 안전재고 미만 SKU
- 창고별 재고가 분산된 SKU
- 생산 입고가 완료된 SKU
- 출고 검수에 사용할 바코드 샘플

### 배송/반품

- 출고 지시 생성 전 주문
- 피킹/검수/출고 완료 상태의 출고 지시
- 송장번호가 등록된 배송
- 배송 추적 이벤트가 있는 배송
- 반품 요청/승인/거절 후보

### 회계/정산

- 주문 매출 인식 거래
- 환불 거래
- 배송비/택배비 관련 거래
- 정산 배치 후보
- 기간 마감 후보

## AI 데모 데이터

AI 데모는 실제 운영 고객 데이터를 사용하지 않는다. 아래 합성 샘플 데이터와 config를 사용한다.

| 데모 | 학습 데이터 | 평가 데이터 | 학습 설정 | 평가 설정 |
| --- | --- | --- | --- | --- |
| 수요 예측 | `ai/datasets/samples/demo_demand_train.json` | `ai/datasets/samples/demo_demand_eval.json` | `ai/configs/train_demo_demand.json` | `ai/configs/evaluate_demo_demand.json` |
| 리뷰 감성 | `ai/datasets/samples/demo_review_train.json` | `ai/datasets/samples/demo_review_eval.json` | `ai/configs/train_demo_review.json` | `ai/configs/evaluate_demo_review.json` |

## AI 모델 재생성 명령

```powershell
python ai/scripts/train_baseline.py --config ai/configs/train_demo_demand.json
python ai/scripts/evaluate_baseline.py --config ai/configs/evaluate_demo_demand.json
python ai/scripts/train_baseline.py --config ai/configs/train_demo_review.json
python ai/scripts/evaluate_baseline.py --config ai/configs/evaluate_demo_review.json
```

생성되는 `.pt`, metadata, 평가 리포트는 로컬 산출물이며 Git 추적 대상이 아니다.

## Git 포함/제외 기준

Git에 포함:
- 합성 샘플 데이터
- 학습/평가 config
- 학습/평가 스크립트
- 데모 실행 가이드

Git에서 제외:
- `.env`
- 실제 운영 DB 덤프
- 개인정보 포함 데이터
- `ai/datasets/exports/`
- `ai/datasets/processed/`
- `ai/models/checkpoints/`
- `ai/reports/generated/`
- `*.pt`, `*.pth`, `*.onnx`, `*.safetensors`

## 시연 순서 후보

1. 쇼핑몰 메인에서 배너와 카테고리 네비 확인
2. 상품 목록/상세에서 판매 상태와 재고 상태 확인
3. 장바구니와 주문/결제 흐름 설명
4. 관리자 상품/주문/배송/재고 화면 확인
5. 회계/정산 화면에서 매출 인식과 리포트 확인
6. 직원/권한/감사 로그 화면에서 운영 통제 설명
7. AI 운영 화면에서 추천, 수요 예측, 리뷰 분석, 이상 주문, 리스크 알림, 리포트 확인

## 남은 이슈

- 로컬 DB seed 자동화는 최종 포트폴리오 범위에서 선택 사항으로 유지한다.
- 실제 개인정보 기반 데모 데이터는 사용하지 않는다.
- 대용량 이미지와 모델 체크포인트는 Git에 포함하지 않는다.
