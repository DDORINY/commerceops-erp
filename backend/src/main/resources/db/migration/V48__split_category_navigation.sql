UPDATE admin_menu_permissions
SET menu_path = '/admin/categories/navigation',
    updated_at = NOW(6)
WHERE menu_key = 'top-navigation';
