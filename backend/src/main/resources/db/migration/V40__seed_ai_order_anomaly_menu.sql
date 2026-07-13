INSERT INTO admin_menu_permissions (menu_key, menu_label, menu_path, required_permission_code, visible, sort_order, created_at, updated_at)
SELECT 'ai-order-anomaly', 'AI 이상 주문 탐지', '/admin/ai/order-anomalies', 'AI_ANOMALY_READ', TRUE, 274, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM admin_menu_permissions amp WHERE amp.menu_key = 'ai-order-anomaly');
