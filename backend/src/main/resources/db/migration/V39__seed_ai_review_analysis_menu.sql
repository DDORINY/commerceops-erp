INSERT INTO admin_menu_permissions (menu_key, menu_label, menu_path, required_permission_code, visible, sort_order, created_at, updated_at)
SELECT 'ai-review-analysis', 'AI 리뷰 분석', '/admin/ai/review-analysis', 'AI_REVIEW_ANALYSIS_READ', TRUE, 273, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM admin_menu_permissions amp WHERE amp.menu_key = 'ai-review-analysis');
