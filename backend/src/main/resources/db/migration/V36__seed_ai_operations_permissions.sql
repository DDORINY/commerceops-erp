INSERT INTO permissions (code, name, domain, action, description, active, created_at, updated_at)
SELECT code, name, domain, action, description, TRUE, NOW(6), NOW(6)
FROM (
    SELECT 'AI_RECOMMENDATION_READ' code, 'AI 상품 추천 조회' name, 'AI' domain, 'RECOMMENDATION_READ' action, '관리자 AI 상품 추천 후보를 조회합니다.' description UNION ALL
    SELECT 'AI_FORECAST_READ', 'AI 수요 예측 조회', 'AI', 'FORECAST_READ', '관리자 AI 수요 예측 결과를 조회합니다.' UNION ALL
    SELECT 'AI_REVIEW_ANALYSIS_READ', 'AI 리뷰 분석 조회', 'AI', 'REVIEW_ANALYSIS_READ', '관리자 AI 리뷰 분석 결과를 조회합니다.' UNION ALL
    SELECT 'AI_ANOMALY_READ', 'AI 이상 주문 조회', 'AI', 'ANOMALY_READ', '관리자 AI 이상 주문 탐지 후보를 조회합니다.' UNION ALL
    SELECT 'AI_RISK_ALERT_READ', 'AI 리스크 알림 조회', 'AI', 'RISK_ALERT_READ', '관리자 AI 재고/정산 리스크 알림을 조회합니다.' UNION ALL
    SELECT 'AI_REPORT_READ', 'AI 리포트 조회', 'AI', 'REPORT_READ', '관리자 AI 운영 리포트와 공통 overview를 조회합니다.' UNION ALL
    SELECT 'AI_OPERATIONS_MANAGE', 'AI 운영 설정 관리', 'AI', 'OPERATIONS_MANAGE', '관리자 AI 운영 설정과 데모 기준을 관리합니다.'
) seed
WHERE NOT EXISTS (SELECT 1 FROM permissions p WHERE p.code = seed.code);

INSERT INTO permission_group_permissions (permission_group_id, permission_id, created_at)
SELECT pg.id, p.id, NOW(6)
FROM permission_groups pg
JOIN permissions p ON p.code IN (
    'AI_DATASET_EXPORT',
    'AI_RECOMMENDATION_READ',
    'AI_FORECAST_READ',
    'AI_REVIEW_ANALYSIS_READ',
    'AI_ANOMALY_READ',
    'AI_RISK_ALERT_READ',
    'AI_REPORT_READ',
    'AI_OPERATIONS_MANAGE'
)
WHERE pg.code IN ('SUPER_ADMIN_GROUP', 'ADMIN_GROUP')
  AND NOT EXISTS (
      SELECT 1 FROM permission_group_permissions pgp
      WHERE pgp.permission_group_id = pg.id AND pgp.permission_id = p.id
  );

INSERT INTO permission_group_permissions (permission_group_id, permission_id, created_at)
SELECT pg.id, p.id, NOW(6)
FROM permission_groups pg
JOIN permissions p ON p.code IN (
    'AI_RECOMMENDATION_READ',
    'AI_FORECAST_READ',
    'AI_REVIEW_ANALYSIS_READ',
    'AI_ANOMALY_READ',
    'AI_RISK_ALERT_READ',
    'AI_REPORT_READ'
)
WHERE pg.code = 'MANAGER_GROUP'
  AND NOT EXISTS (
      SELECT 1 FROM permission_group_permissions pgp
      WHERE pgp.permission_group_id = pg.id AND pgp.permission_id = p.id
  );

INSERT INTO admin_menu_permissions (menu_key, menu_label, menu_path, required_permission_code, visible, sort_order, created_at, updated_at)
SELECT menu_key, menu_label, menu_path, required_permission_code, TRUE, sort_order, NOW(6), NOW(6)
FROM (
    SELECT 'ai-overview' menu_key, 'AI 운영' menu_label, '/admin/ai' menu_path, 'AI_REPORT_READ' required_permission_code, 270 sort_order
) seed
WHERE NOT EXISTS (SELECT 1 FROM admin_menu_permissions amp WHERE amp.menu_key = seed.menu_key);
