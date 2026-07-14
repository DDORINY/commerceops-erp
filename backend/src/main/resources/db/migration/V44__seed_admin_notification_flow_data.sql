INSERT INTO notifications (user_id, type, title, message, target_type, target_id, read_at, created_at)
SELECT u.id, seed.type, seed.title, seed.message, seed.target_type, seed.target_id, NULL,
       TIMESTAMPADD(MINUTE, -seed.minutes_ago, NOW(6))
FROM users u
JOIN (
    SELECT 'SYSTEM' type, '[운영 확인] 안전재고 점검 필요' title,
           '와이드 데님 팬츠의 가용 재고가 안전재고 기준에 근접했습니다. 재고 현황을 확인해주세요.' message,
           'PRODUCT' target_type, 5 target_id, 1 minutes_ago
    UNION ALL
    SELECT 'ORDER_STATUS', '[운영 확인] 신규 주문 접수',
           '신규 주문이 결제 완료 상태로 접수되었습니다. 상품 준비 상태를 확인해주세요.',
           'ORDER', 10001, 3
    UNION ALL
    SELECT 'INQUIRY_ANSWERED', '[운영 확인] 배송 문의 답변 대기',
           '배송 일정에 대한 고객 문의가 등록되었습니다. 담당자 답변이 필요합니다.',
           'INQUIRY', 20001, 5
    UNION ALL
    SELECT 'RETURN_PROCESSED', '[운영 확인] 반품 요청 접수',
           '단순 변심 사유의 반품 요청이 접수되었습니다. 회수 가능 여부를 확인해주세요.',
           'RETURN', 30001, 7
    UNION ALL
    SELECT 'SYSTEM', '[운영 확인] 상품 노출 상태 점검',
           '베이직 셔츠 원피스의 판매 기간과 전시 상태를 점검해주세요.',
           'PRODUCT', 1, 9
    UNION ALL
    SELECT 'ORDER_STATUS', '[운영 확인] 출고 준비 지연',
           '상품 준비 단계가 예상 처리 시간을 초과한 주문이 있습니다. 출고 일정을 확인해주세요.',
           'ORDER', 10002, 11
    UNION ALL
    SELECT 'INQUIRY_ANSWERED', '[운영 확인] 상품 문의 답변 대기',
           '소재와 세탁 방법에 대한 상품 문의가 등록되었습니다. 상품 정보를 확인해 답변해주세요.',
           'INQUIRY', 20002, 13
    UNION ALL
    SELECT 'RETURN_PROCESSED', '[운영 확인] 반품 상품 검수 필요',
           '회수 완료된 반품 상품의 상태 검수와 환불 가능 여부 확인이 필요합니다.',
           'RETURN', 30002, 15
    UNION ALL
    SELECT 'SYSTEM', '[운영 확인] 상품 이미지 검수 완료',
           '봄 가을 트렌치 코트 상품 이미지의 공개 경로와 전시 상태를 확인했습니다.',
           'PRODUCT', 3, 17
    UNION ALL
    SELECT 'SYSTEM', '[운영 확인] 운영 알림 연결 점검',
           '관리자 알림 API와 상단 미리보기 드롭다운 연결을 확인하기 위한 운영 알림입니다.',
           'SYSTEM', NULL, 19
) seed
WHERE u.email = 'admin@naver.com'
  AND u.status = 'ACTIVE'
  AND NOT EXISTS (
      SELECT 1 FROM notifications n
      WHERE n.user_id = u.id AND n.title = seed.title
  );
