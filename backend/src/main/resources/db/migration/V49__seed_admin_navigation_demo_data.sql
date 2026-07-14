-- Connected demonstration records for every data-driven admin navigation page.
-- DEMO49 identifiers make the seed deterministic and safe to inspect across domains.

INSERT INTO users (email, password, name, phone, role, status, created_at, updated_at)
SELECT CONCAT('demo49.customer', LPAD(n, 2, '0'), '@commerceops.test'), a.password,
       CONCAT('데모고객', LPAD(n, 2, '0')), CONCAT('010-4900-', LPAD(n, 4, '0')),
       'USER', 'ACTIVE', TIMESTAMPADD(DAY, -n, NOW(6)), NOW(6)
FROM (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) s
JOIN users a ON a.email = 'admin@naver.com'
WHERE NOT EXISTS (SELECT 1 FROM users u WHERE u.email = CONCAT('demo49.customer', LPAD(n, 2, '0'), '@commerceops.test'));

INSERT INTO departments (name, code, parent_id, sort_order, active, created_at, updated_at)
SELECT CONCAT('데모 부서 ', LPAD(n, 2, '0')), CONCAT('DEMO49-DEPT-', LPAD(n, 2, '0')), NULL, n, TRUE, NOW(6), NOW(6)
FROM (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) s
WHERE NOT EXISTS (SELECT 1 FROM departments d WHERE d.code = CONCAT('DEMO49-DEPT-', LPAD(n, 2, '0')));

INSERT INTO positions (name, level, sort_order, active, created_at, updated_at)
SELECT CONCAT('데모 직급 ', LPAD(n, 2, '0')), n, n, TRUE, NOW(6), NOW(6)
FROM (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) s
WHERE NOT EXISTS (SELECT 1 FROM positions p WHERE p.name = CONCAT('데모 직급 ', LPAD(n, 2, '0')));

INSERT INTO users (email, password, name, phone, role, status, created_at, updated_at)
SELECT CONCAT('demo49.staff', LPAD(n, 2, '0'), '@commerceops.test'), a.password,
       CONCAT('데모직원', LPAD(n, 2, '0')), CONCAT('010-4910-', LPAD(n, 4, '0')),
       'MANAGER', 'ACTIVE', TIMESTAMPADD(DAY, -n, NOW(6)), NOW(6)
FROM (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) s
JOIN users a ON a.email = 'admin@naver.com'
WHERE NOT EXISTS (SELECT 1 FROM users u WHERE u.email = CONCAT('demo49.staff', LPAD(n, 2, '0'), '@commerceops.test'));

INSERT INTO staff_profiles (user_id, department_id, position_id, employee_no, employment_status, joined_at, active, created_at, updated_at)
SELECT u.id, d.id, p.id, CONCAT('DEMO49-EMP-', LPAD(s.n, 4, '0')), 'ACTIVE', DATE_SUB(CURDATE(), INTERVAL s.n MONTH), TRUE, NOW(6), NOW(6)
FROM (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) s
JOIN users u ON u.email = CONCAT('demo49.staff', LPAD(s.n, 2, '0'), '@commerceops.test')
JOIN departments d ON d.code = CONCAT('DEMO49-DEPT-', LPAD(s.n, 2, '0'))
JOIN positions p ON p.name = CONCAT('데모 직급 ', LPAD(s.n, 2, '0'))
WHERE NOT EXISTS (SELECT 1 FROM staff_profiles sp WHERE sp.user_id = u.id);

INSERT INTO categories (name, parent_id, depth, sort_order, active, visible_in_nav, slug, created_at, updated_at)
SELECT CONCAT('데모 카테고리 ', LPAD(n, 2, '0')), NULL, 0, 100 + n, TRUE, TRUE,
       CONCAT('demo49-category-', LPAD(n, 2, '0')), NOW(6), NOW(6)
FROM (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) s
WHERE NOT EXISTS (SELECT 1 FROM categories c WHERE c.slug = CONCAT('demo49-category-', LPAD(n, 2, '0')));

INSERT INTO products (category_id, name, description, price, stock_quantity, image_url, status, options,
                      created_at, updated_at, product_code, brand, manufacturer, model_name, origin,
                      original_price, discount_price, purchase_price, search_keywords, tags,
                      delivery_info, seo_title, seo_description, sales_status, display_status, safety_stock_quantity)
SELECT c.id, CONCAT('데모 연결 상품 ', LPAD(s.n, 2, '0')), '관리자 전체 업무 흐름 검증용 연결 상품입니다.',
       20000 + s.n * 3000, 100 + s.n, CONCAT('/demo/products/demo49-', LPAD(s.n, 2, '0'), '.jpg'), 'ACTIVE', NULL,
       TIMESTAMPADD(DAY, -s.n, NOW(6)), NOW(6), CONCAT('DEMO49-P-', LPAD(s.n, 3, '0')),
       'CommerceOps Demo', 'CommerceOps', CONCAT('D49-', LPAD(s.n, 3, '0')), '대한민국',
       25000 + s.n * 3000, 20000 + s.n * 3000, 12000 + s.n * 2000, '데모,연결,관리자', '["demo","connected"]',
       '영업일 기준 1~3일 배송', CONCAT('데모 연결 상품 ', LPAD(s.n, 2, '0')), '관리자 화면 검증용 상품', 'ON_SALE', 'VISIBLE', 10
FROM (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) s
JOIN categories c ON c.slug = CONCAT('demo49-category-', LPAD(s.n, 2, '0'))
WHERE NOT EXISTS (SELECT 1 FROM products p WHERE p.product_code = CONCAT('DEMO49-P-', LPAD(s.n, 3, '0')));

INSERT INTO product_detail_blocks (product_id, block_type, title, content, image_url, sort_order, visible, created_at, updated_at)
SELECT p.id, 'TEXT', '상품 상세 안내', CONCAT(p.name, '의 연결형 데모 상세 콘텐츠입니다.'), NULL, 1, TRUE, NOW(6), NOW(6)
FROM products p WHERE p.product_code LIKE 'DEMO49-P-%'
AND NOT EXISTS (SELECT 1 FROM product_detail_blocks b WHERE b.product_id = p.id);

INSERT INTO main_banners (title, subtitle, description, image_url, link_url, position, sort_order, active, starts_at, ends_at, created_at, updated_at)
SELECT CONCAT('데모 기획전 ', LPAD(n, 2, '0')), '연결형 운영 데이터', '관리자 배너 관리 검증 데이터',
       CONCAT('/demo/banners/demo49-', LPAD(n, 2, '0'), '.jpg'), CONCAT('/products?demo=49-', LPAD(n, 2, '0')),
       'MAIN', n, TRUE, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 90 DAY), NOW(), NOW()
FROM (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) s
WHERE NOT EXISTS (SELECT 1 FROM main_banners b WHERE b.title = CONCAT('데모 기획전 ', LPAD(n, 2, '0')));

INSERT INTO coupons (code, discount_type, discount_value, min_order_amount, max_usage, used_count, expires_at, active, created_at)
SELECT CONCAT('DEMO49-', LPAD(n, 2, '0')), CASE WHEN MOD(n,2)=0 THEN 'FIXED' ELSE 'PERCENT' END,
       CASE WHEN MOD(n,2)=0 THEN 3000 ELSE 10 END, 20000, 100, n, DATE_ADD(NOW(6), INTERVAL 90 DAY), TRUE, NOW(6)
FROM (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) s
WHERE NOT EXISTS (SELECT 1 FROM coupons c WHERE c.code = CONCAT('DEMO49-', LPAD(n, 2, '0')));

INSERT INTO warehouses (code, name, address, active, created_at, updated_at)
SELECT CONCAT('DEMO49-WH-', LPAD(n, 2, '0')), CONCAT('데모 창고 ', LPAD(n, 2, '0')),
       CONCAT('서울특별시 물류로 ', n), TRUE, NOW(6), NOW(6)
FROM (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) s
WHERE NOT EXISTS (SELECT 1 FROM warehouses w WHERE w.code = CONCAT('DEMO49-WH-', LPAD(n, 2, '0')));

INSERT INTO skus (product_id, option_signature, sku_code, barcode, name, safety_stock_quantity, active, created_at, updated_at)
SELECT p.id, CONCAT('{"color":"DEMO-', LPAD(s.n,2,'0'), '"}'), CONCAT('DEMO49-SKU-', LPAD(s.n,3,'0')),
       CONCAT('490000000', LPAD(s.n,3,'0')), CONCAT(p.name, ' 기본 SKU'), 10 + s.n, TRUE, NOW(6), NOW(6)
FROM (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) s
JOIN products p ON p.product_code = CONCAT('DEMO49-P-', LPAD(s.n,3,'0'))
WHERE NOT EXISTS (SELECT 1 FROM skus k WHERE k.sku_code = CONCAT('DEMO49-SKU-', LPAD(s.n,3,'0')));

INSERT INTO warehouse_stocks (warehouse_id, product_id, quantity, reserved_quantity, version, created_at, updated_at)
SELECT w.id, p.id, 50 + s.n, s.n, 0, NOW(6), NOW(6)
FROM (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) s
JOIN warehouses w ON w.code = CONCAT('DEMO49-WH-', LPAD(s.n,2,'0'))
JOIN products p ON p.product_code = CONCAT('DEMO49-P-', LPAD(s.n,3,'0'))
WHERE NOT EXISTS (SELECT 1 FROM warehouse_stocks x WHERE x.warehouse_id=w.id AND x.product_id=p.id);

INSERT INTO warehouse_locations (warehouse_id, code, name, zone, aisle, rack, cell, active, created_at, updated_at)
SELECT w.id, CONCAT('D49-', LPAD(s.n,2,'0'), '-A01'), CONCAT('데모 로케이션 ', LPAD(s.n,2,'0')),
       CONCAT('Z', s.n), 'A01', 'R01', 'C01', TRUE, NOW(6), NOW(6)
FROM (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) s
JOIN warehouses w ON w.code = CONCAT('DEMO49-WH-', LPAD(s.n,2,'0'))
WHERE NOT EXISTS (SELECT 1 FROM warehouse_locations l WHERE l.warehouse_id=w.id AND l.code=CONCAT('D49-',LPAD(s.n,2,'0'),'-A01'));

INSERT INTO warehouse_location_stocks (warehouse_location_id, warehouse_id, sku_id, product_id, quantity, reserved_quantity, created_at, updated_at)
SELECT l.id, w.id, k.id, p.id, 40+s.n, s.n, NOW(6), NOW(6)
FROM (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) s
JOIN warehouses w ON w.code=CONCAT('DEMO49-WH-',LPAD(s.n,2,'0'))
JOIN warehouse_locations l ON l.warehouse_id=w.id AND l.code=CONCAT('D49-',LPAD(s.n,2,'0'),'-A01')
JOIN products p ON p.product_code=CONCAT('DEMO49-P-',LPAD(s.n,3,'0'))
JOIN skus k ON k.sku_code=CONCAT('DEMO49-SKU-',LPAD(s.n,3,'0'))
WHERE NOT EXISTS (SELECT 1 FROM warehouse_location_stocks x WHERE x.warehouse_location_id=l.id AND x.sku_id=k.id);

INSERT INTO inventory_alert_rules (sku_id, warehouse_id, threshold_quantity, active, memo, created_at, updated_at)
SELECT k.id, w.id, 15+s.n, TRUE, 'DEMO49 안전재고 알림', NOW(6), NOW(6)
FROM (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) s
JOIN skus k ON k.sku_code=CONCAT('DEMO49-SKU-',LPAD(s.n,3,'0')) JOIN warehouses w ON w.code=CONCAT('DEMO49-WH-',LPAD(s.n,2,'0'))
WHERE NOT EXISTS (SELECT 1 FROM inventory_alert_rules r WHERE r.sku_id=k.id AND r.warehouse_id=w.id);

INSERT INTO barcode_labels (sku_id, barcode, label_format, print_count, last_printed_at, created_by, created_at, updated_at)
SELECT k.id, k.barcode, 'CODE128', s.n, NOW(6), a.id, NOW(6), NOW(6)
FROM (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) s
JOIN skus k ON k.sku_code=CONCAT('DEMO49-SKU-',LPAD(s.n,3,'0')) JOIN users a ON a.email='admin@naver.com'
WHERE NOT EXISTS (SELECT 1 FROM barcode_labels b WHERE b.sku_id=k.id);

INSERT INTO product_status_histories (product_id, changed_by_user_id, changed_by_email, previous_sales_status, new_sales_status, previous_display_status, new_display_status, reason, created_at)
SELECT p.id,a.id,a.email,'READY','ON_SALE','HIDDEN','VISIBLE','DEMO49 판매·전시 상태 변경',NOW(6)
FROM products p JOIN users a ON a.email='admin@naver.com' WHERE p.product_code LIKE 'DEMO49-P-%'
AND NOT EXISTS (SELECT 1 FROM product_status_histories h WHERE h.product_id=p.id AND h.reason='DEMO49 판매·전시 상태 변경');

INSERT INTO product_operation_notes (product_id, writer_user_id, writer_email, content, created_at, updated_at)
SELECT p.id,a.id,a.email,'DEMO49 관리자 상품 운영 메모',NOW(6),NOW(6)
FROM products p JOIN users a ON a.email='admin@naver.com' WHERE p.product_code LIKE 'DEMO49-P-%'
AND NOT EXISTS (SELECT 1 FROM product_operation_notes n WHERE n.product_id=p.id AND n.content='DEMO49 관리자 상품 운영 메모');

INSERT INTO orders (user_id, order_number, total_price, discount_amount, coupon_code, status, receiver_name, receiver_phone, address, detail_address, payment_status, created_at, updated_at)
SELECT u.id, CONCAT('DEMO49-ORD-',LPAD(s.n,4,'0')), p.price, 0, CONCAT('DEMO49-',LPAD(s.n,2,'0')),
       CASE MOD(s.n,5) WHEN 0 THEN 'COMPLETED' WHEN 1 THEN 'PAID' WHEN 2 THEN 'PREPARING' WHEN 3 THEN 'SHIPPING' ELSE 'COMPLETED' END,
       u.name,u.phone,CONCAT('서울특별시 데모로 ',s.n),CONCAT(s.n,'01호'),'PAID',TIMESTAMPADD(DAY,-s.n,NOW(6)),NOW(6)
FROM (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) s
JOIN users u ON u.email=CONCAT('demo49.customer',LPAD(s.n,2,'0'),'@commerceops.test') JOIN products p ON p.product_code=CONCAT('DEMO49-P-',LPAD(s.n,3,'0'))
WHERE NOT EXISTS (SELECT 1 FROM orders o WHERE o.order_number=CONCAT('DEMO49-ORD-',LPAD(s.n,4,'0')));

INSERT INTO order_items (order_id,product_id,product_name,price,quantity,selected_options,created_at)
SELECT o.id,p.id,p.name,p.price,1,NULL,o.created_at FROM orders o
JOIN products p ON p.product_code=CONCAT('DEMO49-P-',RIGHT(o.order_number,3))
WHERE o.order_number LIKE 'DEMO49-ORD-%' AND NOT EXISTS (SELECT 1 FROM order_items i WHERE i.order_id=o.id);

INSERT INTO payments (order_id,payment_method,payment_status,paid_amount,transaction_id,idempotency_key,provider,created_at,updated_at)
SELECT o.id,CASE MOD(o.id,3) WHEN 0 THEN 'MOCK_CARD' WHEN 1 THEN 'MOCK_BANK' ELSE 'MOCK_SIMPLE_PAY' END,'PAID',o.total_price,
       CONCAT('DEMO49-TX-',RIGHT(o.order_number,4)),CONCAT('demo49-pay-',o.id),'MOCK_PROVIDER',o.created_at,o.updated_at
FROM orders o WHERE o.order_number LIKE 'DEMO49-ORD-%' AND NOT EXISTS (SELECT 1 FROM payments p WHERE p.order_id=o.id);

INSERT INTO shipments (order_id,status,tracking_number,carrier,tracking_number_source,tracking_number_issued_at,shipped_at,delivered_at,created_at,updated_at)
SELECT o.id,CASE WHEN o.status='COMPLETED' THEN 'DELIVERED' ELSE 'IN_TRANSIT' END,CONCAT('D49TRACK',RIGHT(o.order_number,4)),'데모택배','AUTO',o.created_at,
       TIMESTAMPADD(HOUR,12,o.created_at),CASE WHEN o.status='COMPLETED' THEN TIMESTAMPADD(DAY,2,o.created_at) END,o.created_at,o.updated_at
FROM orders o WHERE o.order_number LIKE 'DEMO49-ORD-%' AND NOT EXISTS (SELECT 1 FROM shipments x WHERE x.order_id=o.id);

INSERT INTO shipment_tracking_events (shipment_id,status,description,event_at,created_at)
SELECT s.id,s.status,CASE WHEN s.status='DELIVERED' THEN '배송이 완료되었습니다.' ELSE '배송지로 이동 중입니다.' END,COALESCE(s.delivered_at,s.shipped_at),NOW(6)
FROM shipments s JOIN orders o ON o.id=s.order_id WHERE o.order_number LIKE 'DEMO49-ORD-%' AND NOT EXISTS (SELECT 1 FROM shipment_tracking_events e WHERE e.shipment_id=s.id);

INSERT INTO return_requests (order_id,user_id,reason,reason_detail,status,admin_note,created_at,updated_at)
SELECT o.id,o.user_id,CASE MOD(o.id,3) WHEN 0 THEN 'DEFECTIVE' WHEN 1 THEN 'CHANGE_OF_MIND' ELSE 'WRONG_DELIVERY' END,
       'DEMO49 연결 반품 요청',CASE MOD(o.id,3) WHEN 0 THEN 'REQUESTED' WHEN 1 THEN 'APPROVED' ELSE 'REJECTED' END,'관리자 검수 메모',TIMESTAMPADD(DAY,3,o.created_at),NOW(6)
FROM orders o WHERE o.order_number LIKE 'DEMO49-ORD-%' AND NOT EXISTS (SELECT 1 FROM return_requests r WHERE r.order_id=o.id);

INSERT INTO return_shipment_infos (return_request_id,carrier,tracking_number,status,shipping_fee,fee_payer,memo,created_at,updated_at)
SELECT r.id,'데모택배',CONCAT('D49RET',LPAD(r.id,6,'0')),'COLLECTION_REQUESTED',3000,'CUSTOMER','DEMO49 반품 회수',NOW(6),NOW(6)
FROM return_requests r JOIN orders o ON o.id=r.order_id WHERE o.order_number LIKE 'DEMO49-ORD-%' AND NOT EXISTS (SELECT 1 FROM return_shipment_infos x WHERE x.return_request_id=r.id);

INSERT INTO reviews (product_id,user_id,order_item_id,rating,content,status,created_at)
SELECT i.product_id,o.user_id,i.id,MOD(o.id,5)+1,CONCAT(o.order_number,' 상품 사용 후기입니다.'),'VISIBLE',TIMESTAMPADD(DAY,4,o.created_at)
FROM orders o JOIN order_items i ON i.order_id=o.id WHERE o.order_number LIKE 'DEMO49-ORD-%' AND NOT EXISTS (SELECT 1 FROM reviews r WHERE r.order_item_id=i.id);

INSERT INTO inquiries (user_id,product_id,type,subject,content,answer,status,created_at,updated_at)
SELECT o.user_id,i.product_id,'ORDER',CONCAT('[DEMO49] 주문 문의 ',RIGHT(o.order_number,4)),'배송 일정을 확인해주세요.',
       CASE WHEN MOD(o.id,2)=0 THEN '확인 후 안내드립니다.' END,CASE WHEN MOD(o.id,2)=0 THEN 'ANSWERED' ELSE 'WAITING' END,o.created_at,NOW(6)
FROM orders o JOIN order_items i ON i.order_id=o.id WHERE o.order_number LIKE 'DEMO49-ORD-%' AND NOT EXISTS (SELECT 1 FROM inquiries q WHERE q.subject=CONCAT('[DEMO49] 주문 문의 ',RIGHT(o.order_number,4)));

INSERT INTO notifications (user_id,type,title,message,target_type,target_id,created_at)
SELECT o.user_id,'ORDER',CONCAT('[DEMO49] 주문 상태 ',RIGHT(o.order_number,4)),CONCAT(o.order_number,' 처리 상태가 변경되었습니다.'),'ORDER',o.id,NOW(6)
FROM orders o WHERE o.order_number LIKE 'DEMO49-ORD-%' AND NOT EXISTS (SELECT 1 FROM notifications n WHERE n.target_type='ORDER' AND n.target_id=o.id);

INSERT INTO carriers (code,name,tracking_url_template,active,created_at,updated_at)
SELECT CONCAT('DEMO49-CAR-',LPAD(n,2,'0')),CONCAT('데모 택배사 ',LPAD(n,2,'0')),'https://commerceops.ddoriny.com/tracking/{trackingNumber}',TRUE,NOW(6),NOW(6)
FROM (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) s
WHERE NOT EXISTS (SELECT 1 FROM carriers c WHERE c.code=CONCAT('DEMO49-CAR-',LPAD(n,2,'0')));

INSERT INTO shipping_methods (code,name,carrier_id,default_fee,description,active,created_at,updated_at)
SELECT CONCAT('DEMO49-SHIP-',LPAD(s.n,2,'0')),CONCAT('데모 배송 방법 ',LPAD(s.n,2,'0')),c.id,2500+s.n*100,'DEMO49 배송 정책',TRUE,NOW(6),NOW(6)
FROM (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) s
JOIN carriers c ON c.code=CONCAT('DEMO49-CAR-',LPAD(s.n,2,'0')) WHERE NOT EXISTS (SELECT 1 FROM shipping_methods m WHERE m.code=CONCAT('DEMO49-SHIP-',LPAD(s.n,2,'0')));

INSERT INTO outbound_orders (outbound_number,order_id,warehouse_id,status,requested_at,picked_at,shipped_at,memo,created_by,updated_by,created_at,updated_at)
SELECT CONCAT('DEMO49-OUT-',RIGHT(o.order_number,4)),o.id,w.id,'SHIPPED',o.created_at,TIMESTAMPADD(HOUR,6,o.created_at),TIMESTAMPADD(HOUR,12,o.created_at),'DEMO49 연결 출고',a.id,a.id,o.created_at,NOW(6)
FROM orders o JOIN warehouses w ON w.code=CONCAT('DEMO49-WH-',RIGHT(o.order_number,2)) JOIN users a ON a.email='admin@naver.com'
WHERE o.order_number LIKE 'DEMO49-ORD-%' AND NOT EXISTS (SELECT 1 FROM outbound_orders x WHERE x.order_id=o.id);

INSERT INTO outbound_order_items (outbound_order_id,order_item_id,sku_id,product_id,quantity,picked_quantity,scanned_quantity,created_at,updated_at)
SELECT x.id,i.id,k.id,i.product_id,i.quantity,i.quantity,i.quantity,x.created_at,NOW(6) FROM outbound_orders x JOIN orders o ON o.id=x.order_id JOIN order_items i ON i.order_id=o.id
JOIN skus k ON k.product_id=i.product_id WHERE o.order_number LIKE 'DEMO49-ORD-%' AND NOT EXISTS (SELECT 1 FROM outbound_order_items z WHERE z.outbound_order_id=x.id);

INSERT INTO outbound_scan_logs (outbound_order_id,outbound_order_item_id,sku_id,barcode,quantity,scanned_by,created_at)
SELECT x.id,i.id,i.sku_id,k.barcode,i.quantity,a.id,NOW(6) FROM outbound_orders x JOIN outbound_order_items i ON i.outbound_order_id=x.id
JOIN skus k ON k.id=i.sku_id JOIN users a ON a.email='admin@naver.com' WHERE x.outbound_number LIKE 'DEMO49-OUT-%'
AND NOT EXISTS (SELECT 1 FROM outbound_scan_logs l WHERE l.outbound_order_item_id=i.id);

INSERT INTO shipment_labels (shipment_id,tracking_number,carrier,label_format,print_count,last_printed_at,created_by,created_at,updated_at)
SELECT s.id,s.tracking_number,s.carrier,'PDF',1,NOW(6),a.id,NOW(6),NOW(6) FROM shipments s JOIN orders o ON o.id=s.order_id JOIN users a ON a.email='admin@naver.com'
WHERE o.order_number LIKE 'DEMO49-ORD-%' AND NOT EXISTS (SELECT 1 FROM shipment_labels l WHERE l.shipment_id=s.id);

INSERT INTO inventory_logs (product_id,type,quantity,before_stock,after_stock,memo,created_at)
SELECT i.product_id,'ORDER',i.quantity,101,100,CONCAT(o.order_number,' 연결 재고 이력'),o.created_at FROM orders o JOIN order_items i ON i.order_id=o.id
WHERE o.order_number LIKE 'DEMO49-ORD-%' AND NOT EXISTS (SELECT 1 FROM inventory_logs l WHERE l.memo=CONCAT(o.order_number,' 연결 재고 이력'));

INSERT INTO stock_reservations (order_id,order_item_id,warehouse_stock_id,quantity,status,created_at,updated_at)
SELECT o.id,i.id,ws.id,i.quantity,'SHIPPED',o.created_at,NOW(6) FROM orders o JOIN order_items i ON i.order_id=o.id JOIN outbound_orders x ON x.order_id=o.id
JOIN warehouse_stocks ws ON ws.warehouse_id=x.warehouse_id AND ws.product_id=i.product_id WHERE o.order_number LIKE 'DEMO49-ORD-%' AND NOT EXISTS (SELECT 1 FROM stock_reservations r WHERE r.order_item_id=i.id);

INSERT INTO production_orders (production_number,status,warehouse_id,planned_quantity,completed_quantity,started_at,completed_at,memo,created_by,updated_by,created_at,updated_at)
SELECT CONCAT('DEMO49-PROD-',LPAD(s.n,3,'0')),'COMPLETED',w.id,20+s.n,20+s.n,TIMESTAMPADD(DAY,-s.n,NOW(6)),NOW(6),'DEMO49 생산 입고',a.id,a.id,NOW(6),NOW(6)
FROM (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) s JOIN warehouses w ON w.code=CONCAT('DEMO49-WH-',LPAD(s.n,2,'0')) JOIN users a ON a.email='admin@naver.com'
WHERE NOT EXISTS (SELECT 1 FROM production_orders p WHERE p.production_number=CONCAT('DEMO49-PROD-',LPAD(s.n,3,'0')));

INSERT INTO production_order_items (production_order_id,sku_id,product_id,planned_quantity,completed_quantity)
SELECT po.id,k.id,k.product_id,20+s.n,20+s.n FROM (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) s
JOIN production_orders po ON po.production_number=CONCAT('DEMO49-PROD-',LPAD(s.n,3,'0')) JOIN skus k ON k.sku_code=CONCAT('DEMO49-SKU-',LPAD(s.n,3,'0'))
WHERE NOT EXISTS (SELECT 1 FROM production_order_items i WHERE i.production_order_id=po.id);

INSERT INTO production_receipts (production_order_id,sku_id,product_id,warehouse_id,quantity,inventory_log_id,created_by,created_at)
SELECT po.id,k.id,k.product_id,po.warehouse_id,po.completed_quantity,NULL,a.id,NOW(6) FROM production_orders po JOIN production_order_items i ON i.production_order_id=po.id JOIN skus k ON k.id=i.sku_id JOIN users a ON a.email='admin@naver.com'
WHERE po.production_number LIKE 'DEMO49-PROD-%' AND NOT EXISTS (SELECT 1 FROM production_receipts r WHERE r.production_order_id=po.id);

INSERT INTO stock_count_sessions (count_number,warehouse_id,status,memo,started_by,completed_by,started_at,completed_at,created_at,updated_at)
SELECT CONCAT('DEMO49-COUNT-',LPAD(s.n,3,'0')),w.id,'COMPLETED','DEMO49 재고 실사',a.id,a.id,TIMESTAMPADD(DAY,-s.n,NOW(6)),NOW(6),NOW(6),NOW(6)
FROM (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) s JOIN warehouses w ON w.code=CONCAT('DEMO49-WH-',LPAD(s.n,2,'0')) JOIN users a ON a.email='admin@naver.com'
WHERE NOT EXISTS (SELECT 1 FROM stock_count_sessions c WHERE c.count_number=CONCAT('DEMO49-COUNT-',LPAD(s.n,3,'0')));

INSERT INTO stock_count_items (session_id,sku_id,product_id,system_quantity,counted_quantity,difference_quantity,memo,created_at,updated_at)
SELECT c.id,k.id,k.product_id,50+s.n,49+s.n,-1,'DEMO49 실사 차이',NOW(6),NOW(6) FROM (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) s
JOIN stock_count_sessions c ON c.count_number=CONCAT('DEMO49-COUNT-',LPAD(s.n,3,'0')) JOIN skus k ON k.sku_code=CONCAT('DEMO49-SKU-',LPAD(s.n,3,'0')) WHERE NOT EXISTS (SELECT 1 FROM stock_count_items i WHERE i.session_id=c.id AND i.sku_id=k.id);

INSERT INTO stock_transfers (transfer_number,from_warehouse_id,to_warehouse_id,product_id,quantity,status,requested_at,completed_at)
SELECT CONCAT('DEMO49-TR-',LPAD(s.n,3,'0')),w.id,d.id,p.id,5+s.n,'COMPLETED',TIMESTAMPADD(DAY,-s.n,NOW(6)),NOW(6)
FROM (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) s JOIN warehouses w ON w.code='DEFAULT' JOIN warehouses d ON d.code=CONCAT('DEMO49-WH-',LPAD(s.n,2,'0')) JOIN products p ON p.product_code=CONCAT('DEMO49-P-',LPAD(s.n,3,'0'))
WHERE NOT EXISTS (SELECT 1 FROM stock_transfers t WHERE t.transfer_number=CONCAT('DEMO49-TR-',LPAD(s.n,3,'0')));

INSERT INTO accounting_ledgers (ledger_number,period,status,closed_at,closed_by,created_at,updated_at)
SELECT CONCAT('DEMO49-LEDGER-',LPAD(n,2,'0')),DATE_FORMAT(DATE_SUB(CURDATE(),INTERVAL n MONTH),'%Y-%m'),CASE WHEN MOD(n,2)=0 THEN 'CLOSED' ELSE 'OPEN' END,
       CASE WHEN MOD(n,2)=0 THEN NOW(6) END,CASE WHEN MOD(n,2)=0 THEN a.id END,NOW(6),NOW(6)
FROM (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) s JOIN users a ON a.email='admin@naver.com'
WHERE NOT EXISTS (SELECT 1 FROM accounting_ledgers l WHERE l.ledger_number=CONCAT('DEMO49-LEDGER-',LPAD(n,2,'0')));

INSERT INTO accounting_entries (type,amount,description,reference_id,created_at)
SELECT 'SALE',o.total_price,CONCAT(o.order_number,' 매출 회계 항목'),o.order_number,o.created_at FROM orders o WHERE o.order_number LIKE 'DEMO49-ORD-%' AND NOT EXISTS (SELECT 1 FROM accounting_entries e WHERE e.reference_id=o.order_number);

INSERT INTO accounting_transactions (ledger_id,transaction_number,type,direction,amount,reference_type,reference_id,occurred_at,memo,created_by,created_at,updated_at)
SELECT l.id,CONCAT('DEMO49-ACCT-',RIGHT(o.order_number,4)),'SALES','INCOME',o.total_price,'ORDER',o.id,o.created_at,CONCAT(o.order_number,' 연결 회계 거래'),a.id,NOW(6),NOW(6)
FROM orders o JOIN accounting_ledgers l ON l.ledger_number=CONCAT('DEMO49-LEDGER-',RIGHT(o.order_number,2)) JOIN users a ON a.email='admin@naver.com'
WHERE o.order_number LIKE 'DEMO49-ORD-%' AND NOT EXISTS (SELECT 1 FROM accounting_transactions t WHERE t.transaction_number=CONCAT('DEMO49-ACCT-',RIGHT(o.order_number,4)));

INSERT INTO shipping_cost_entries (shipment_id,carrier_id,shipping_method_id,cost_amount,charged_amount,occurred_at,settlement_status,memo,created_at,updated_at)
SELECT sh.id,c.id,m.id,2500,3000,sh.shipped_at,'PENDING',CONCAT(o.order_number,' 배송비 정산'),NOW(6),NOW(6) FROM orders o JOIN shipments sh ON sh.order_id=o.id
JOIN carriers c ON c.code=CONCAT('DEMO49-CAR-',RIGHT(o.order_number,2)) JOIN shipping_methods m ON m.code=CONCAT('DEMO49-SHIP-',RIGHT(o.order_number,2))
WHERE o.order_number LIKE 'DEMO49-ORD-%' AND NOT EXISTS (SELECT 1 FROM shipping_cost_entries e WHERE e.shipment_id=sh.id);

INSERT INTO settlement_batches (batch_number,period_start,period_end,status,total_sales,total_refunds,total_shipping_fee,total_shipping_cost,closed_at,closed_by,created_at,updated_at)
SELECT CONCAT('DEMO49-SET-',LPAD(s.n,2,'0')),DATE_SUB(CURDATE(),INTERVAL s.n MONTH),LAST_DAY(DATE_SUB(CURDATE(),INTERVAL s.n MONTH)),
       CASE WHEN MOD(s.n,2)=0 THEN 'CLOSED' ELSE 'DRAFT' END,100000+s.n*10000,s.n*1000,3000,2500,CASE WHEN MOD(s.n,2)=0 THEN NOW(6) END,CASE WHEN MOD(s.n,2)=0 THEN a.id END,NOW(6),NOW(6)
FROM (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) s JOIN users a ON a.email='admin@naver.com'
WHERE NOT EXISTS (SELECT 1 FROM settlement_batches b WHERE b.batch_number=CONCAT('DEMO49-SET-',LPAD(s.n,2,'0')));

INSERT INTO settlement_batch_items (settlement_batch_id,reference_type,reference_id,item_type,amount,memo,status,created_at)
SELECT b.id,'ORDER',o.id,'SALES',o.total_price,CONCAT(o.order_number,' 정산 항목'),'INCLUDED',NOW(6) FROM orders o
JOIN settlement_batches b ON b.batch_number=CONCAT('DEMO49-SET-',RIGHT(o.order_number,2)) WHERE o.order_number LIKE 'DEMO49-ORD-%' AND NOT EXISTS (SELECT 1 FROM settlement_batch_items i WHERE i.settlement_batch_id=b.id AND i.reference_id=o.id);

INSERT INTO audit_logs (actor_id,actor_email,actor_name,action_type,target_type,target_id,summary,created_at,ip_address,request_method,request_path)
SELECT a.id,a.email,a.name,'PRODUCT_UPDATED','PRODUCT',p.id,CONCAT(p.name,' 데모 감사 이력'),NOW(6),'127.0.0.1','PATCH',CONCAT('/api/admin/products/',p.id)
FROM products p JOIN users a ON a.email='admin@naver.com' WHERE p.product_code LIKE 'DEMO49-P-%' AND NOT EXISTS (SELECT 1 FROM audit_logs l WHERE l.target_type='PRODUCT' AND l.target_id=p.id AND l.summary LIKE '%데모 감사 이력');

INSERT INTO permission_groups (name,code,description,system_group,active,created_at,updated_at)
SELECT CONCAT('데모 권한 그룹 ',LPAD(n,2,'0')),CONCAT('DEMO49-GROUP-',LPAD(n,2,'0')),'관리자 권한 화면 검증 그룹',FALSE,TRUE,NOW(6),NOW(6)
FROM (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) s
WHERE NOT EXISTS (SELECT 1 FROM permission_groups g WHERE g.code=CONCAT('DEMO49-GROUP-',LPAD(n,2,'0')));

INSERT INTO user_permission_groups (user_id,permission_group_id,created_by,created_at)
SELECT u.id,g.id,a.id,NOW(6) FROM users u JOIN permission_groups g ON g.code=CONCAT('DEMO49-GROUP-',SUBSTRING(u.email,13,2)) JOIN users a ON a.email='admin@naver.com'
WHERE u.email LIKE 'demo49.staff%@commerceops.test' AND NOT EXISTS (SELECT 1 FROM user_permission_groups x WHERE x.user_id=u.id AND x.permission_group_id=g.id);

INSERT INTO terms_versions (type,title,content,version,effective_from,active,created_by,created_at)
SELECT CASE MOD(n,3) WHEN 0 THEN 'PRIVACY' WHEN 1 THEN 'TERMS' ELSE 'RETURN_POLICY' END,
       CONCAT('DEMO49 운영 정책 ',LPAD(n,2,'0')),'관리자 설정 화면 검증용 약관 및 정책 본문입니다.',CONCAT('demo49.',n),
       DATE_SUB(NOW(6),INTERVAL n DAY),TRUE,a.id,NOW(6)
FROM (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) s JOIN users a ON a.email='admin@naver.com'
WHERE NOT EXISTS (SELECT 1 FROM terms_versions t WHERE t.version=CONCAT('demo49.',n) AND t.type=CASE MOD(n,3) WHEN 0 THEN 'PRIVACY' WHEN 1 THEN 'TERMS' ELSE 'RETURN_POLICY' END);

INSERT INTO business_settings (company_name,representative_name,business_registration_number,mail_order_business_number,address,customer_service_phone,customer_service_email,brand_name,updated_by,created_at,updated_at)
SELECT 'CommerceOps Demo','데모 대표','490-00-00049','제2026-서울-0049호','서울특별시 데모구 커머스로 49','02-490-0049','demo49@commerceops.test','CommerceOps Demo',a.id,NOW(6),NOW(6)
FROM users a WHERE a.email='admin@naver.com' AND NOT EXISTS (SELECT 1 FROM business_settings);
