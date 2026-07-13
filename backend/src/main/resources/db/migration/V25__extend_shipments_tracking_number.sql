ALTER TABLE shipments
    ADD COLUMN tracking_number_source VARCHAR(20) NULL AFTER carrier,
    ADD COLUMN tracking_number_issued_at DATETIME(6) NULL AFTER tracking_number_source;

-- idx_shipments_tracking_number is already created by V1__initial_schema.sql.
CREATE INDEX idx_shipments_tracking_number_issued_at ON shipments (tracking_number_issued_at);

INSERT INTO permissions (code, name, domain, action, description, active, created_at, updated_at)
SELECT code, name, domain, action, description, TRUE, NOW(6), NOW(6)
FROM (
    SELECT 'SHIPMENT_READ' code, '배송 조회' name, 'DISTRIBUTION' domain, 'READ' action, '배송과 송장 정보를 조회합니다.' description UNION ALL
    SELECT 'SHIPMENT_MANAGE', '배송/송장 관리', 'DISTRIBUTION', 'MANAGE', '송장번호 저장, 자동 생성, 배송 완료 처리를 수행합니다.'
) seed
WHERE NOT EXISTS (SELECT 1 FROM permissions p WHERE p.code = seed.code);

INSERT INTO permission_group_permissions (permission_group_id, permission_id, created_at)
SELECT pg.id, p.id, NOW(6)
FROM permission_groups pg
JOIN permissions p ON p.code IN ('SHIPMENT_READ', 'SHIPMENT_MANAGE')
WHERE pg.code IN ('SUPER_ADMIN_GROUP', 'ADMIN_GROUP')
  AND NOT EXISTS (
      SELECT 1 FROM permission_group_permissions pgp
      WHERE pgp.permission_group_id = pg.id AND pgp.permission_id = p.id
  );

INSERT INTO permission_group_permissions (permission_group_id, permission_id, created_at)
SELECT pg.id, p.id, NOW(6)
FROM permission_groups pg
JOIN permissions p ON p.code = 'SHIPMENT_READ'
WHERE pg.code = 'MANAGER_GROUP'
  AND NOT EXISTS (
      SELECT 1 FROM permission_group_permissions pgp
      WHERE pgp.permission_group_id = pg.id AND pgp.permission_id = p.id
  );

UPDATE admin_menu_permissions
SET required_permission_code = 'SHIPMENT_READ', updated_at = NOW(6)
WHERE menu_key = 'shipments';
