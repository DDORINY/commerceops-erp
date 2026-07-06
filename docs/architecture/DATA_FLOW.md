# 데이터 흐름

## 상품 등록 -> SKU 생성 -> 재고 입고

```text
관리자 상품 등록
  -> 상품 마스터 저장
  -> SKU/바코드 생성
  -> 생산 입고 또는 바코드 입고
  -> 상품/창고 재고 증가
  -> InventoryLog/AuditLog 기록
```

## 주문 -> 결제 -> 출고 -> 배송 -> 완료

```text
주문 생성
  -> 결제 승인
  -> 주문 상태 변경
  -> 출고 지시 생성
  -> 바코드 검수
  -> 송장번호 생성/입력
  -> 배송 상태 추적
  -> 배송 완료
```

## 반품/환불 흐름

```text
반품 요청
  -> 관리자 승인/거절
  -> 반품 배송 정보 저장
  -> 환불/재고 복구 후보
  -> 회계 반영 후보
```

## 생산 주문 -> 생산 완료 -> 생산 입고 -> 재고 증가

```text
생산 주문 생성
  -> 생산 시작
  -> 생산 완료 수량 입력
  -> ProductionReceipt 생성
  -> SKU/상품/창고 재고 증가
  -> InventoryLog(PRODUCTION_RECEIPT)
```

## 바코드 기반 입고/출고

```text
바코드 스캔
  -> SKU 조회
  -> 창고 선택
  -> 입고/출고 수량 입력
  -> 재고 변경
  -> InventoryLog/AuditLog 기록
```

## 감사 로그 기록 흐름

```text
관리자 변경 요청
  -> PermissionChecker 통과
  -> Service 변경 처리
  -> before/after/metadata JSON 기록
  -> AuditLog 저장
```

## AI dataset export 후보 흐름

```text
운영 DB
  -> export script
  -> raw dataset
  -> preprocessing
  -> processed dataset
  -> training
  -> model artifact
```
