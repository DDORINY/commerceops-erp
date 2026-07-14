UPDATE admin_menu_permissions
SET menu_label = 'AI 데이터셋',
    menu_path = '/admin/ai/datasets',
    required_permission_code = 'AI_DATASET_EXPORT',
    visible = TRUE,
    updated_at = NOW(6)
WHERE menu_key = 'ai-datasets';

UPDATE admin_menu_permissions
SET visible = FALSE, updated_at = NOW(6)
WHERE menu_key IN ('events', 'promotions', 'departments', 'positions', 'settings-environment');
