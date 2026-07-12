INSERT INTO permissions (code, name, domain, action, description, active, created_at, updated_at)
SELECT 'RETURN_FEE_MANAGE', '반품 배송비 회계 처리', 'ACCOUNTING', 'MANAGE',
       '반품 배송비 부담 주체 기준으로 회계 거래를 생성하고 관리합니다.', TRUE, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM permissions p WHERE p.code = 'RETURN_FEE_MANAGE');

INSERT INTO permission_group_permissions (permission_group_id, permission_id, created_at)
SELECT pg.id, p.id, NOW(6)
FROM permission_groups pg
JOIN permissions p ON p.code = 'RETURN_FEE_MANAGE'
WHERE pg.code IN ('SUPER_ADMIN_GROUP', 'ADMIN_GROUP')
  AND NOT EXISTS (
      SELECT 1 FROM permission_group_permissions pgp
      WHERE pgp.permission_group_id = pg.id AND pgp.permission_id = p.id
  );
