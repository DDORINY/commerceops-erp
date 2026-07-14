-- Persistent, connected ERP demonstration data for admin workflow verification.

INSERT INTO users (email, password, name, phone, role, status, created_at, updated_at)
SELECT CONCAT('demo.customer', LPAD(seq.n, 2, '0'), '@commerceops.test'), admin.password,
       CONCAT('데모고객', LPAD(seq.n, 2, '0')), CONCAT('010-9000-', LPAD(seq.n, 4, '0')),
       'USER', 'ACTIVE', TIMESTAMPADD(DAY, -seq.n, NOW(6)), NOW(6)
FROM (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
      UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) seq
JOIN users admin ON admin.email = 'admin@naver.com'
WHERE NOT EXISTS (
    SELECT 1 FROM users u
    WHERE u.email = CONCAT('demo.customer', LPAD(seq.n, 2, '0'), '@commerceops.test')
);

INSERT INTO carriers (code, name, tracking_url_template, active, created_at, updated_at)
SELECT 'DEMO_CARRIER', '커머스택배', 'https://commerceops.ddoriny.com/tracking/{trackingNumber}', TRUE, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM carriers WHERE code = 'DEMO_CARRIER');

INSERT INTO shipping_methods (code, name, carrier_id, default_fee, description, active, created_at, updated_at)
SELECT 'DEMO_STANDARD', '일반 택배', c.id, 3000, '영업일 기준 1~3일 배송', TRUE, NOW(6), NOW(6)
FROM carriers c
WHERE c.code = 'DEMO_CARRIER'
  AND NOT EXISTS (SELECT 1 FROM shipping_methods WHERE code = 'DEMO_STANDARD');

INSERT INTO warehouses (code, name, address, active, created_at, updated_at)
SELECT seed.code, seed.name, seed.address, TRUE, NOW(6), NOW(6)
FROM (
    SELECT 'SEOUL_SUB' code, '서울 보조창고' name, '서울특별시 송파구 물류로 10' address
    UNION ALL SELECT 'BUSAN_SUB', '부산 보조창고', '부산광역시 강서구 유통로 20'
) seed
WHERE NOT EXISTS (SELECT 1 FROM warehouses w WHERE w.code = seed.code);

INSERT INTO orders
    (user_id, order_number, total_price, discount_amount, coupon_code, status,
     receiver_name, receiver_phone, address, detail_address, payment_status, created_at, updated_at)
SELECT u.id, CONCAT('DEMO-202607-', LPAD(seq.n, 4, '0')),
       p.price + 3000, 0, NULL,
       CASE seq.n WHEN 1 THEN 'PAID' WHEN 2 THEN 'PREPARING' WHEN 3 THEN 'SHIPPING'
            WHEN 4 THEN 'COMPLETED' WHEN 5 THEN 'COMPLETED' WHEN 6 THEN 'SHIPPING'
            WHEN 7 THEN 'PREPARING' WHEN 8 THEN 'COMPLETED' WHEN 9 THEN 'CANCELLED' ELSE 'REFUNDED' END,
       u.name, u.phone, CONCAT('서울특별시 강남구 테헤란로 ', 100 + seq.n), CONCAT(seq.n, '01호'),
       CASE seq.n WHEN 9 THEN 'CANCELLED' WHEN 10 THEN 'REFUNDED' ELSE 'PAID' END,
       TIMESTAMPADD(DAY, -seq.n, NOW(6)), TIMESTAMPADD(HOUR, -seq.n, NOW(6))
FROM (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
      UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) seq
JOIN users u ON u.email = CONCAT('demo.customer', LPAD(seq.n, 2, '0'), '@commerceops.test')
JOIN products p ON p.id = MOD(seq.n - 1, 5) + 1
WHERE NOT EXISTS (SELECT 1 FROM orders o WHERE o.order_number = CONCAT('DEMO-202607-', LPAD(seq.n, 4, '0')));

INSERT INTO order_items (order_id, product_id, product_name, price, quantity, selected_options, created_at)
SELECT o.id, p.id, p.name, p.price, 1, NULL, o.created_at
FROM orders o
JOIN products p ON p.id = MOD(CAST(RIGHT(o.order_number, 4) AS UNSIGNED) - 1, 5) + 1
WHERE o.order_number LIKE 'DEMO-202607-%'
  AND NOT EXISTS (SELECT 1 FROM order_items oi WHERE oi.order_id = o.id);

INSERT INTO payments
    (order_id, payment_method, payment_status, paid_amount, transaction_id, idempotency_key, provider, created_at, updated_at)
SELECT o.id,
       CASE MOD(CAST(RIGHT(o.order_number, 4) AS UNSIGNED), 3)
            WHEN 0 THEN 'MOCK_BANK' WHEN 1 THEN 'MOCK_CARD' ELSE 'MOCK_SIMPLE_PAY' END,
       o.payment_status, o.total_price,
       CONCAT('DEMO-TX-', RIGHT(o.order_number, 4)), CONCAT('demo-payment-', o.id), 'COMMERCEOPS_PAY',
       o.created_at, o.updated_at
FROM orders o
WHERE o.order_number LIKE 'DEMO-202607-%'
  AND NOT EXISTS (SELECT 1 FROM payments p WHERE p.order_id = o.id);

INSERT INTO shipments
    (order_id, status, tracking_number, carrier, tracking_number_source, tracking_number_issued_at,
     shipped_at, delivered_at, created_at, updated_at)
SELECT o.id,
       CASE WHEN o.status IN ('COMPLETED', 'REFUNDED') THEN 'DELIVERED'
            WHEN o.status = 'SHIPPING' THEN 'IN_TRANSIT'
            WHEN o.status = 'CANCELLED' THEN 'CANCELLED' ELSE 'READY' END,
       CONCAT('DEMO', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(RIGHT(o.order_number, 4), 4, '0')),
       '커머스택배', 'AUTO', TIMESTAMPADD(HOUR, 2, o.created_at),
       CASE WHEN o.status IN ('SHIPPING', 'COMPLETED', 'REFUNDED') THEN TIMESTAMPADD(DAY, 1, o.created_at) END,
       CASE WHEN o.status IN ('COMPLETED', 'REFUNDED') THEN TIMESTAMPADD(DAY, 2, o.created_at) END,
       o.created_at, o.updated_at
FROM orders o
WHERE o.order_number LIKE 'DEMO-202607-%'
  AND NOT EXISTS (SELECT 1 FROM shipments s WHERE s.order_id = o.id);

INSERT INTO shipment_tracking_events (shipment_id, status, description, event_at, raw_payload, created_at)
SELECT s.id, s.status,
       CASE s.status WHEN 'DELIVERED' THEN '배송이 완료되었습니다.' WHEN 'IN_TRANSIT' THEN '배송지로 이동 중입니다.'
            WHEN 'CANCELLED' THEN '배송이 취소되었습니다.' ELSE '상품 인계를 준비하고 있습니다.' END,
       COALESCE(s.delivered_at, s.shipped_at, s.updated_at), NULL, NOW(6)
FROM shipments s JOIN orders o ON o.id = s.order_id
WHERE o.order_number LIKE 'DEMO-202607-%'
  AND NOT EXISTS (SELECT 1 FROM shipment_tracking_events e WHERE e.shipment_id = s.id);

INSERT INTO outbound_orders
    (outbound_number, order_id, warehouse_id, status, requested_at, picked_at, shipped_at,
     memo, created_by, updated_by, created_at, updated_at)
SELECT CONCAT('DEMO-OUT-', RIGHT(o.order_number, 4)), o.id, w.id,
       CASE WHEN o.status IN ('SHIPPING', 'COMPLETED', 'REFUNDED') THEN 'SHIPPED'
            WHEN o.status = 'CANCELLED' THEN 'CANCELLED' WHEN o.status = 'PREPARING' THEN 'PICKING' ELSE 'REQUESTED' END,
       o.created_at,
       CASE WHEN o.status IN ('SHIPPING', 'COMPLETED', 'REFUNDED') THEN TIMESTAMPADD(HOUR, 12, o.created_at) END,
       CASE WHEN o.status IN ('SHIPPING', 'COMPLETED', 'REFUNDED') THEN TIMESTAMPADD(DAY, 1, o.created_at) END,
       '연결 흐름 검증용 출고 데이터', admin.id, admin.id, o.created_at, o.updated_at
FROM orders o JOIN warehouses w ON w.code = 'DEFAULT' JOIN users admin ON admin.email = 'admin@naver.com'
WHERE o.order_number LIKE 'DEMO-202607-%'
  AND NOT EXISTS (SELECT 1 FROM outbound_orders oo WHERE oo.order_id = o.id);

INSERT INTO outbound_order_items
    (outbound_order_id, order_item_id, sku_id, product_id, quantity, picked_quantity, scanned_quantity, created_at, updated_at)
SELECT oo.id, oi.id, NULL, oi.product_id, oi.quantity,
       CASE WHEN oo.status IN ('SHIPPED', 'PICKED') THEN oi.quantity ELSE 0 END,
       CASE WHEN oo.status = 'SHIPPED' THEN oi.quantity ELSE 0 END, oo.created_at, oo.updated_at
FROM outbound_orders oo JOIN orders o ON o.id = oo.order_id JOIN order_items oi ON oi.order_id = o.id
WHERE o.order_number LIKE 'DEMO-202607-%'
  AND NOT EXISTS (SELECT 1 FROM outbound_order_items x WHERE x.outbound_order_id = oo.id AND x.order_item_id = oi.id);

INSERT INTO inquiries (user_id, product_id, type, subject, content, answer, status, created_at, updated_at)
SELECT o.user_id, oi.product_id,
       CASE MOD(CAST(RIGHT(o.order_number, 4) AS UNSIGNED), 3) WHEN 0 THEN 'PRODUCT' WHEN 1 THEN 'ORDER' ELSE 'OTHER' END,
       CONCAT('[데모] 주문 문의 ', RIGHT(o.order_number, 4)),
       CONCAT(o.order_number, ' 주문의 상품 준비 및 배송 일정을 확인하고 싶습니다.'),
       CASE WHEN MOD(CAST(RIGHT(o.order_number, 4) AS UNSIGNED), 2) = 0 THEN '담당자가 주문 상태를 확인했으며 예정 일정에 맞춰 처리하겠습니다.' END,
       CASE WHEN MOD(CAST(RIGHT(o.order_number, 4) AS UNSIGNED), 2) = 0 THEN 'ANSWERED' ELSE 'WAITING' END,
       TIMESTAMPADD(HOUR, 3, o.created_at), TIMESTAMPADD(HOUR, 4, o.created_at)
FROM orders o JOIN order_items oi ON oi.order_id = o.id
WHERE o.order_number LIKE 'DEMO-202607-%'
  AND NOT EXISTS (SELECT 1 FROM inquiries i WHERE i.subject = CONCAT('[데모] 주문 문의 ', RIGHT(o.order_number, 4)));

INSERT INTO return_requests (order_id, user_id, reason, reason_detail, status, admin_note, created_at, updated_at)
SELECT o.id, o.user_id,
       CASE MOD(CAST(RIGHT(o.order_number, 4) AS UNSIGNED), 3)
            WHEN 0 THEN 'WRONG_DELIVERY' WHEN 1 THEN 'CHANGE_OF_MIND' ELSE 'DEFECTIVE' END,
       CONCAT(o.order_number, ' 반품 처리 흐름 확인용 요청입니다.'),
       CASE MOD(CAST(RIGHT(o.order_number, 4) AS UNSIGNED), 3)
            WHEN 0 THEN 'REQUESTED' WHEN 1 THEN 'APPROVED' ELSE 'REJECTED' END,
       '검수 결과에 따라 반품 상태를 처리합니다.', TIMESTAMPADD(DAY, 3, o.created_at), NOW(6)
FROM orders o
WHERE o.order_number LIKE 'DEMO-202607-%'
  AND NOT EXISTS (SELECT 1 FROM return_requests r WHERE r.order_id = o.id);

INSERT INTO warehouse_stocks (warehouse_id, product_id, quantity, reserved_quantity, version, created_at, updated_at)
SELECT w.id, p.id, 20 + p.id * 3, 0, 0, NOW(6), NOW(6)
FROM warehouses w JOIN products p ON p.id BETWEEN 1 AND 5
WHERE w.code IN ('SEOUL_SUB', 'BUSAN_SUB')
  AND NOT EXISTS (SELECT 1 FROM warehouse_stocks ws WHERE ws.warehouse_id = w.id AND ws.product_id = p.id);

INSERT INTO stock_reservations
    (order_id, order_item_id, warehouse_stock_id, quantity, status, created_at, updated_at)
SELECT o.id, oi.id, ws.id, oi.quantity,
       CASE WHEN o.status IN ('SHIPPING', 'COMPLETED', 'REFUNDED') THEN 'SHIPPED'
            WHEN o.status = 'CANCELLED' THEN 'RELEASED' ELSE 'RESERVED' END,
       o.created_at, o.updated_at
FROM orders o JOIN order_items oi ON oi.order_id = o.id
JOIN warehouses w ON w.code = 'DEFAULT'
JOIN warehouse_stocks ws ON ws.warehouse_id = w.id AND ws.product_id = oi.product_id
WHERE o.order_number LIKE 'DEMO-202607-%'
  AND NOT EXISTS (SELECT 1 FROM stock_reservations sr WHERE sr.order_item_id = oi.id);

INSERT INTO inventory_logs (product_id, type, quantity, before_stock, after_stock, memo, created_at)
SELECT oi.product_id,
       CASE WHEN o.status = 'CANCELLED' THEN 'CANCEL' WHEN o.status = 'REFUNDED' THEN 'RETURN_RESTOCK' ELSE 'ORDER' END,
       1, p.stock_quantity + 1, p.stock_quantity,
       CONCAT(o.order_number, ' 연결 재고 이력'), o.created_at
FROM orders o JOIN order_items oi ON oi.order_id = o.id JOIN products p ON p.id = oi.product_id
WHERE o.order_number LIKE 'DEMO-202607-%'
  AND NOT EXISTS (SELECT 1 FROM inventory_logs il WHERE il.memo = CONCAT(o.order_number, ' 연결 재고 이력'));

INSERT INTO stock_transfers
    (transfer_number, from_warehouse_id, to_warehouse_id, product_id, quantity, status, requested_at, completed_at)
SELECT CONCAT('DEMO-TR-', LPAD(seq.n, 4, '0')), source.id, target.id, MOD(seq.n - 1, 5) + 1,
       seq.n + 1, CASE WHEN MOD(seq.n, 2) = 0 THEN 'COMPLETED' ELSE 'REQUESTED' END,
       TIMESTAMPADD(DAY, -seq.n, NOW(6)),
       CASE WHEN MOD(seq.n, 2) = 0 THEN TIMESTAMPADD(HOUR, 6, TIMESTAMPADD(DAY, -seq.n, NOW(6))) END
FROM (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
      UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) seq
JOIN warehouses source ON source.code = CASE WHEN seq.n <= 5 THEN 'DEFAULT' ELSE 'SEOUL_SUB' END
JOIN warehouses target ON target.code = CASE WHEN seq.n <= 5 THEN 'SEOUL_SUB' ELSE 'BUSAN_SUB' END
WHERE NOT EXISTS (SELECT 1 FROM stock_transfers st WHERE st.transfer_number = CONCAT('DEMO-TR-', LPAD(seq.n, 4, '0')));

INSERT INTO accounting_ledgers (ledger_number, period, status, closed_at, closed_by, created_at, updated_at)
SELECT 'DEMO-LEDGER-2026-07', '2026-07', 'OPEN', NULL, NULL, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM accounting_ledgers WHERE ledger_number = 'DEMO-LEDGER-2026-07');

INSERT INTO accounting_entries (type, amount, description, reference_id, created_at)
SELECT CASE WHEN o.status = 'REFUNDED' THEN 'REFUND' ELSE 'SALE' END,
       o.total_price, CONCAT(o.order_number, ' 매출/환불 회계 항목'), o.order_number, o.created_at
FROM orders o
WHERE o.order_number LIKE 'DEMO-202607-%'
  AND NOT EXISTS (SELECT 1 FROM accounting_entries ae WHERE ae.reference_id = o.order_number);

INSERT INTO accounting_transactions
    (ledger_id, transaction_number, type, direction, amount, reference_type, reference_id,
     occurred_at, memo, created_by, created_at, updated_at)
SELECT l.id, CONCAT('DEMO-ACCT-', RIGHT(o.order_number, 4)),
       CASE WHEN o.status = 'REFUNDED' THEN 'REFUND' ELSE 'SALES' END,
       CASE WHEN o.status = 'REFUNDED' THEN 'EXPENSE' ELSE 'INCOME' END,
       o.total_price, 'ORDER', o.id, o.created_at,
       CONCAT(o.order_number, ' 주문 연계 회계 거래'), admin.id, NOW(6), NOW(6)
FROM orders o JOIN accounting_ledgers l ON l.ledger_number = 'DEMO-LEDGER-2026-07'
JOIN users admin ON admin.email = 'admin@naver.com'
WHERE o.order_number LIKE 'DEMO-202607-%'
  AND NOT EXISTS (SELECT 1 FROM accounting_transactions atx WHERE atx.transaction_number = CONCAT('DEMO-ACCT-', RIGHT(o.order_number, 4)));

INSERT INTO shipping_cost_entries
    (shipment_id, carrier_id, shipping_method_id, cost_amount, charged_amount, occurred_at,
     settlement_status, memo, created_at, updated_at)
SELECT s.id, c.id, sm.id, 2500, 3000, COALESCE(s.shipped_at, s.created_at),
       CASE WHEN s.status = 'DELIVERED' THEN 'SETTLED' WHEN s.status = 'CANCELLED' THEN 'EXCLUDED' ELSE 'PENDING' END,
       CONCAT(o.order_number, ' 배송비 정산'), NOW(6), NOW(6)
FROM shipments s JOIN orders o ON o.id = s.order_id
JOIN carriers c ON c.code = 'DEMO_CARRIER' JOIN shipping_methods sm ON sm.code = 'DEMO_STANDARD'
WHERE o.order_number LIKE 'DEMO-202607-%'
  AND NOT EXISTS (SELECT 1 FROM shipping_cost_entries sce WHERE sce.shipment_id = s.id);

INSERT INTO settlement_batches
    (batch_number, period_start, period_end, status, total_sales, total_refunds,
     total_shipping_fee, total_shipping_cost, closed_at, closed_by, created_at, updated_at)
SELECT 'DEMO-SETTLEMENT-2026-07', '2026-07-01', '2026-07-31', 'DRAFT',
       SUM(CASE WHEN o.status <> 'REFUNDED' THEN o.total_price ELSE 0 END),
       SUM(CASE WHEN o.status = 'REFUNDED' THEN o.total_price ELSE 0 END),
       COUNT(*) * 3000, COUNT(*) * 2500, NULL, NULL, NOW(6), NOW(6)
FROM orders o
WHERE o.order_number LIKE 'DEMO-202607-%'
  AND NOT EXISTS (SELECT 1 FROM settlement_batches WHERE batch_number = 'DEMO-SETTLEMENT-2026-07');

INSERT INTO settlement_batch_items
    (settlement_batch_id, reference_type, reference_id, item_type, amount, memo, status, created_at)
SELECT sb.id, 'ORDER', o.id,
       CASE WHEN o.status = 'REFUNDED' THEN 'REFUND' ELSE 'SALES' END,
       o.total_price, CONCAT(o.order_number, ' 정산 항목'), 'INCLUDED', NOW(6)
FROM settlement_batches sb JOIN orders o ON o.order_number LIKE 'DEMO-202607-%'
WHERE sb.batch_number = 'DEMO-SETTLEMENT-2026-07'
  AND NOT EXISTS (
      SELECT 1 FROM settlement_batch_items sbi
      WHERE sbi.settlement_batch_id = sb.id AND sbi.reference_type = 'ORDER' AND sbi.reference_id = o.id
  );

-- Point the earlier notification preview records at real connected demo entities.
UPDATE notifications n
JOIN orders o ON o.order_number = 'DEMO-202607-0001'
SET n.target_id = o.id
WHERE n.title = '[운영 확인] 신규 주문 접수' AND n.target_type = 'ORDER';

UPDATE notifications n
JOIN inquiries i ON i.subject = '[데모] 주문 문의 0001'
SET n.target_id = i.id
WHERE n.title = '[운영 확인] 배송 문의 답변 대기' AND n.target_type = 'INQUIRY';

UPDATE notifications n
JOIN return_requests r JOIN orders o ON o.id = r.order_id AND o.order_number = 'DEMO-202607-0001'
SET n.target_id = r.id
WHERE n.title = '[운영 확인] 반품 요청 접수' AND n.target_type = 'RETURN';
