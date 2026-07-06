INSERT INTO admin_menu_permissions (menu_key, menu_label, menu_path, required_permission_code, visible, sort_order, created_at, updated_at)
SELECT 'barcode-stock', '바코드 입출고', '/admin/barcode-stock', 'INVENTORY_READ', TRUE, 103, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM admin_menu_permissions amp WHERE amp.menu_key = 'barcode-stock');
