INSERT INTO admin_menu_permissions (menu_key, menu_label, menu_path, required_permission_code, visible, sort_order, created_at, updated_at)
SELECT 'ai-risk-alerts', 'AI 리스크 알림', '/admin/ai/risk-alerts', 'AI_RISK_ALERT_READ', TRUE, 275, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM admin_menu_permissions amp WHERE amp.menu_key = 'ai-risk-alerts');
