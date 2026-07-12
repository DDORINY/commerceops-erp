INSERT INTO permissions (code, name, domain, action, description, active, created_at, updated_at)
SELECT 'AI_DATASET_EXPORT', 'AI 데이터셋 추출', 'AI', 'EXPORT',
       'AI 학습용 데이터셋 카탈로그와 샘플 export 데이터를 조회합니다.', TRUE, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM permissions p WHERE p.code = 'AI_DATASET_EXPORT');

INSERT INTO permission_group_permissions (permission_group_id, permission_id, created_at)
SELECT pg.id, p.id, NOW(6)
FROM permission_groups pg
JOIN permissions p ON p.code = 'AI_DATASET_EXPORT'
WHERE pg.code IN ('SUPER_ADMIN_GROUP', 'ADMIN_GROUP')
  AND NOT EXISTS (
      SELECT 1 FROM permission_group_permissions pgp
      WHERE pgp.permission_group_id = pg.id AND pgp.permission_id = p.id
  );

INSERT INTO admin_menu_permissions (menu_key, menu_label, menu_path, required_permission_code, visible, sort_order, created_at, updated_at)
SELECT 'ai-datasets', 'AI 데이터셋', '/admin/settings?section=ai-datasets', 'AI_DATASET_EXPORT', TRUE, 260, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM admin_menu_permissions amp WHERE amp.menu_key = 'ai-datasets');
