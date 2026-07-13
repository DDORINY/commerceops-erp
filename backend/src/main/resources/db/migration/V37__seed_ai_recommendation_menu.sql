INSERT INTO admin_menu_permissions (menu_key, menu_label, menu_path, required_permission_code, visible, sort_order, created_at, updated_at)
SELECT 'ai-recommendations', 'AI 상품 추천', '/admin/ai/recommendations', 'AI_RECOMMENDATION_READ', TRUE, 271, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM admin_menu_permissions amp WHERE amp.menu_key = 'ai-recommendations');
