UPDATE admin_menu_permissions
SET menu_label = '알림',
    menu_path = '/admin/notifications',
    required_permission_code = 'DASHBOARD_READ',
    visible = TRUE,
    updated_at = NOW(6)
WHERE menu_key = 'notifications';

INSERT INTO admin_menu_permissions
    (menu_key, menu_label, menu_path, required_permission_code, visible, sort_order, created_at, updated_at)
SELECT 'settings', '설정 저장 관리', '/admin/settings', 'SETTINGS_MANAGE', TRUE, 200, NOW(6), NOW(6)
WHERE NOT EXISTS (
    SELECT 1 FROM admin_menu_permissions WHERE menu_key = 'settings'
);

UPDATE admin_menu_permissions
SET menu_label = '설정 저장 관리',
    menu_path = '/admin/settings',
    required_permission_code = 'SETTINGS_MANAGE',
    visible = TRUE,
    updated_at = NOW(6)
WHERE menu_key = 'settings';
