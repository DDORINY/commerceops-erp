INSERT INTO admin_menu_permissions (menu_key, menu_label, menu_path, required_permission_code, visible, sort_order, created_at, updated_at)
SELECT menu_key, menu_label, menu_path, required_permission_code, TRUE, sort_order, NOW(6), NOW(6)
FROM (
    SELECT 'notifications' menu_key, '알림' menu_label, '/admin/settings?section=notifications' menu_path, 'DASHBOARD_READ' required_permission_code, 15 sort_order UNION ALL
    SELECT 'top-navigation', '상단 네비 관리', '/admin/categories?focus=navigation', 'CATEGORY_MANAGE', 35 UNION ALL
    SELECT 'product-create', '상품 등록', '/admin/products/new', 'PRODUCT_WRITE', 21 UNION ALL
    SELECT 'product-status', '상품 상태 관리', '/admin/products?focus=status', 'PRODUCT_STATUS_CHANGE', 22 UNION ALL
    SELECT 'refunds', '환불 관리', '/admin/returns?focus=refunds', 'PAYMENT_REFUND', 75 UNION ALL
    SELECT 'inbound', '입고 관리', '/admin/warehouses?focus=inbound', 'INVENTORY_WRITE', 111 UNION ALL
    SELECT 'stock-transfers', '재고 이동', '/admin/warehouses?focus=transfers', 'WAREHOUSE_MANAGE', 112 UNION ALL
    SELECT 'payments', '결제 내역', '/admin/accounting?focus=payments', 'ACCOUNTING_READ', 131 UNION ALL
    SELECT 'refund-history', '환불 내역', '/admin/accounting?focus=refunds', 'PAYMENT_REFUND', 132 UNION ALL
    SELECT 'settlements', '정산 관리', '/admin/accounting?focus=settlements', 'ACCOUNTING_CLOSE', 133 UNION ALL
    SELECT 'events', '이벤트 관리', '/admin/settings?section=events', 'COUPON_MANAGE', 141 UNION ALL
    SELECT 'promotions', '프로모션 관리', '/admin/settings?section=promotions', 'COUPON_MANAGE', 142 UNION ALL
    SELECT 'departments', '부서 관리', '/admin/settings?section=departments', 'STAFF_MANAGE', 151 UNION ALL
    SELECT 'positions', '직급 관리', '/admin/settings?section=positions', 'STAFF_MANAGE', 152 UNION ALL
    SELECT 'settings-company', '사업자 설정', '/admin/settings?section=company', 'SETTINGS_MANAGE', 201 UNION ALL
    SELECT 'settings-terms', '약관 설정', '/admin/settings?section=terms', 'SETTINGS_MANAGE', 202 UNION ALL
    SELECT 'settings-privacy', '개인정보처리방침 설정', '/admin/settings?section=privacy', 'SETTINGS_MANAGE', 203 UNION ALL
    SELECT 'settings-policies', '배송/반품 정책 설정', '/admin/settings?section=policies', 'SETTINGS_MANAGE', 204 UNION ALL
    SELECT 'settings-environment', '환경 설정', '/admin/settings?section=environment', 'SETTINGS_MANAGE', 205
) seed
WHERE NOT EXISTS (SELECT 1 FROM admin_menu_permissions amp WHERE amp.menu_key = seed.menu_key);
