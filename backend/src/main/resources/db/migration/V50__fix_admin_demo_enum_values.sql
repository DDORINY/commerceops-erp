-- Correct V49 seed values so every persisted enum matches the application model.
UPDATE products
SET status = 'ON_SALE'
WHERE product_code LIKE 'DEMO49-P-%' AND status = 'ACTIVE';

UPDATE shipments s
JOIN orders o ON o.id = s.order_id
SET s.tracking_number_source = 'SYSTEM'
WHERE o.order_number LIKE 'DEMO49-ORD-%' AND s.tracking_number_source = 'AUTO';

UPDATE notifications n
JOIN orders o ON o.id = n.target_id AND n.target_type = 'ORDER'
SET n.type = 'ORDER_STATUS'
WHERE o.order_number LIKE 'DEMO49-ORD-%' AND n.type = 'ORDER';

UPDATE main_banners
SET position = 'MAIN_TOP'
WHERE link_url LIKE '/products?demo=49-%' AND position = 'MAIN';

UPDATE product_status_histories h
JOIN products p ON p.id = h.product_id
SET h.previous_sales_status = 'DRAFT'
WHERE p.product_code LIKE 'DEMO49-P-%'
  AND h.reason = 'DEMO49 판매·전시 상태 변경'
  AND h.previous_sales_status = 'READY';

UPDATE terms_versions SET type = 'TERMS_OF_SERVICE'
WHERE version LIKE 'demo49.%' AND type = 'TERMS';

UPDATE terms_versions SET type = 'PRIVACY_POLICY'
WHERE version LIKE 'demo49.%' AND type = 'PRIVACY';

UPDATE terms_versions SET type = 'SHIPPING_RETURN_POLICY'
WHERE version LIKE 'demo49.%' AND type = 'RETURN_POLICY';
