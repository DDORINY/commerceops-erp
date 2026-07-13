INSERT INTO admin_menu_permissions (menu_key, menu_label, menu_path, required_permission_code, visible, sort_order, created_at, updated_at)
SELECT 'ai-demand-forecast', 'AI 수요 예측', '/admin/ai/demand-forecast', 'AI_FORECAST_READ', TRUE, 272, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM admin_menu_permissions amp WHERE amp.menu_key = 'ai-demand-forecast');
