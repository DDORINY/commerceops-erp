CREATE TABLE IF NOT EXISTS permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    domain VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    description VARCHAR(500) NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_permissions_code UNIQUE (code),
    INDEX idx_permissions_domain_action (domain, action),
    INDEX idx_permissions_active (active)
);

CREATE TABLE IF NOT EXISTS permission_group_permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    permission_group_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_permission_group_permission UNIQUE (permission_group_id, permission_id),
    CONSTRAINT fk_permission_group_permissions_group FOREIGN KEY (permission_group_id) REFERENCES permission_groups (id),
    CONSTRAINT fk_permission_group_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions (id),
    INDEX idx_permission_group_permissions_group (permission_group_id),
    INDEX idx_permission_group_permissions_permission (permission_id)
);

CREATE TABLE IF NOT EXISTS admin_menu_permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    menu_key VARCHAR(100) NOT NULL,
    menu_label VARCHAR(100) NOT NULL,
    menu_path VARCHAR(200) NOT NULL,
    required_permission_code VARCHAR(100) NOT NULL,
    visible BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_admin_menu_permissions_key UNIQUE (menu_key),
    INDEX idx_admin_menu_permissions_visible_sort (visible, sort_order),
    INDEX idx_admin_menu_permissions_code (required_permission_code)
);

INSERT INTO permissions (code, name, domain, action, description, active, created_at, updated_at)
SELECT code, name, domain, action, description, TRUE, NOW(6), NOW(6)
FROM (
    SELECT 'DASHBOARD_READ' code, '대시보드 조회' name, 'DASHBOARD' domain, 'READ' action, '관리자 대시보드와 운영 요약을 조회합니다.' description UNION ALL
    SELECT 'PRODUCT_READ', '상품 조회', 'PRODUCT', 'READ', '관리자 상품 목록과 상세를 조회합니다.' UNION ALL
    SELECT 'PRODUCT_WRITE', '상품 등록/수정', 'PRODUCT', 'WRITE', '상품 기본 정보와 상세 정보를 등록/수정합니다.' UNION ALL
    SELECT 'PRODUCT_STATUS_CHANGE', '상품 상태 변경', 'PRODUCT', 'STATUS_CHANGE', '상품 판매/전시 상태를 변경합니다.' UNION ALL
    SELECT 'PRODUCT_BULK_UPDATE', '상품 대량 변경', 'PRODUCT', 'BULK_UPDATE', '상품 상태를 대량으로 변경합니다.' UNION ALL
    SELECT 'CATEGORY_MANAGE', '카테고리 관리', 'CATEGORY', 'MANAGE', '카테고리와 상단 네비를 관리합니다.' UNION ALL
    SELECT 'BANNER_MANAGE', '배너 관리', 'BANNER', 'MANAGE', '메인 배너를 관리합니다.' UNION ALL
    SELECT 'ORDER_READ', '주문 조회', 'ORDER', 'READ', '주문 목록과 상세를 조회합니다.' UNION ALL
    SELECT 'ORDER_STATUS_CHANGE', '주문 상태 변경', 'ORDER', 'STATUS_CHANGE', '주문 상태를 변경합니다.' UNION ALL
    SELECT 'PAYMENT_REFUND', '결제/환불 처리', 'PAYMENT', 'REFUND', '결제 취소와 환불 처리를 수행합니다.' UNION ALL
    SELECT 'INVENTORY_READ', '재고 조회', 'INVENTORY', 'READ', '재고와 창고 현황을 조회합니다.' UNION ALL
    SELECT 'INVENTORY_WRITE', '재고 변경', 'INVENTORY', 'WRITE', '재고 입고와 조정을 처리합니다.' UNION ALL
    SELECT 'WAREHOUSE_MANAGE', '창고 관리', 'WAREHOUSE', 'MANAGE', '창고와 재고 이동을 관리합니다.' UNION ALL
    SELECT 'ACCOUNTING_READ', '회계 조회', 'ACCOUNTING', 'READ', '매출/회계 내역을 조회합니다.' UNION ALL
    SELECT 'ACCOUNTING_CLOSE', '회계 마감', 'ACCOUNTING', 'CLOSE', '회계 마감과 정산 기준 작업을 수행합니다.' UNION ALL
    SELECT 'COUPON_MANAGE', '쿠폰 관리', 'COUPON', 'MANAGE', '쿠폰을 생성/삭제/관리합니다.' UNION ALL
    SELECT 'REVIEW_MODERATE', '리뷰 운영', 'REVIEW', 'MODERATE', '리뷰 숨김/해제 등 운영 처리를 수행합니다.' UNION ALL
    SELECT 'INQUIRY_REPLY', '문의 답변', 'INQUIRY', 'REPLY', '고객 문의에 답변하거나 종료합니다.' UNION ALL
    SELECT 'SETTINGS_MANAGE', '시스템 설정', 'SETTINGS', 'MANAGE', '사업자/약관/환경 설정을 관리합니다.' UNION ALL
    SELECT 'STAFF_MANAGE', '직원 관리', 'HR', 'STAFF_MANAGE', '직원 계정과 인사 프로필을 관리합니다.' UNION ALL
    SELECT 'ROLE_MANAGE', '역할/권한 관리', 'HR', 'ROLE_MANAGE', '권한 그룹과 권한 매트릭스를 관리합니다.' UNION ALL
    SELECT 'AUDIT_LOG_READ', '감사 로그 조회', 'AUDIT', 'READ', '관리자 작업 이력을 조회합니다.'
) seed
WHERE NOT EXISTS (SELECT 1 FROM permissions p WHERE p.code = seed.code);

INSERT INTO permission_group_permissions (permission_group_id, permission_id, created_at)
SELECT pg.id, p.id, NOW(6)
FROM permission_groups pg
JOIN permissions p ON p.active = TRUE
WHERE pg.code = 'SUPER_ADMIN_GROUP'
  AND NOT EXISTS (
      SELECT 1 FROM permission_group_permissions pgp
      WHERE pgp.permission_group_id = pg.id AND pgp.permission_id = p.id
  );

INSERT INTO permission_group_permissions (permission_group_id, permission_id, created_at)
SELECT pg.id, p.id, NOW(6)
FROM permission_groups pg
JOIN permissions p ON p.code IN (
    'DASHBOARD_READ', 'PRODUCT_READ', 'PRODUCT_WRITE', 'PRODUCT_STATUS_CHANGE', 'PRODUCT_BULK_UPDATE',
    'CATEGORY_MANAGE', 'BANNER_MANAGE', 'ORDER_READ', 'ORDER_STATUS_CHANGE', 'PAYMENT_REFUND',
    'INVENTORY_READ', 'INVENTORY_WRITE', 'WAREHOUSE_MANAGE', 'ACCOUNTING_READ', 'COUPON_MANAGE',
    'REVIEW_MODERATE', 'INQUIRY_REPLY', 'AUDIT_LOG_READ'
)
WHERE pg.code = 'ADMIN_GROUP'
  AND NOT EXISTS (
      SELECT 1 FROM permission_group_permissions pgp
      WHERE pgp.permission_group_id = pg.id AND pgp.permission_id = p.id
  );

INSERT INTO permission_group_permissions (permission_group_id, permission_id, created_at)
SELECT pg.id, p.id, NOW(6)
FROM permission_groups pg
JOIN permissions p ON p.code IN ('DASHBOARD_READ', 'PRODUCT_READ', 'ORDER_READ', 'INVENTORY_READ')
WHERE pg.code = 'MANAGER_GROUP'
  AND NOT EXISTS (
      SELECT 1 FROM permission_group_permissions pgp
      WHERE pgp.permission_group_id = pg.id AND pgp.permission_id = p.id
  );

INSERT INTO admin_menu_permissions (menu_key, menu_label, menu_path, required_permission_code, visible, sort_order, created_at, updated_at)
SELECT menu_key, menu_label, menu_path, required_permission_code, TRUE, sort_order, NOW(6), NOW(6)
FROM (
    SELECT 'dashboard' menu_key, '대시보드' menu_label, '/admin' menu_path, 'DASHBOARD_READ' required_permission_code, 10 sort_order UNION ALL
    SELECT 'products', '상품 관리', '/admin/products', 'PRODUCT_READ', 20 UNION ALL
    SELECT 'categories', '카테고리 관리', '/admin/categories', 'CATEGORY_MANAGE', 30 UNION ALL
    SELECT 'banners', '배너 관리', '/admin/banners', 'BANNER_MANAGE', 40 UNION ALL
    SELECT 'orders', '주문 관리', '/admin/orders', 'ORDER_READ', 50 UNION ALL
    SELECT 'shipments', '배송 관리', '/admin/shipments', 'ORDER_READ', 60 UNION ALL
    SELECT 'returns', '반품 관리', '/admin/returns', 'ORDER_STATUS_CHANGE', 70 UNION ALL
    SELECT 'inquiries', '문의 관리', '/admin/inquiries', 'INQUIRY_REPLY', 80 UNION ALL
    SELECT 'reviews', '리뷰 관리', '/admin/reviews', 'REVIEW_MODERATE', 90 UNION ALL
    SELECT 'inventory', '재고 관리', '/admin/inventory', 'INVENTORY_READ', 100 UNION ALL
    SELECT 'warehouses', '창고 관리', '/admin/warehouses', 'WAREHOUSE_MANAGE', 110 UNION ALL
    SELECT 'sales', '매출 통계', '/admin/sales', 'ACCOUNTING_READ', 120 UNION ALL
    SELECT 'accounting', '회계 관리', '/admin/accounting', 'ACCOUNTING_READ', 130 UNION ALL
    SELECT 'coupons', '쿠폰 관리', '/admin/coupons', 'COUPON_MANAGE', 140 UNION ALL
    SELECT 'staff', '직원 관리', '/admin/settings/staff', 'STAFF_MANAGE', 150 UNION ALL
    SELECT 'permission_groups', '권한 그룹 관리', '/admin/settings/permission-groups', 'ROLE_MANAGE', 160 UNION ALL
    SELECT 'roles', '역할/권한 설정', '/admin/settings/roles', 'ROLE_MANAGE', 170 UNION ALL
    SELECT 'menu_permissions', '메뉴/기능 권한', '/admin/settings/menu-permissions', 'ROLE_MANAGE', 180 UNION ALL
    SELECT 'audit_logs', '관리자 작업 이력', '/admin/settings/audit-logs', 'AUDIT_LOG_READ', 190 UNION ALL
    SELECT 'settings', '시스템 설정', '/admin/settings', 'SETTINGS_MANAGE', 200
) seed
WHERE NOT EXISTS (SELECT 1 FROM admin_menu_permissions amp WHERE amp.menu_key = seed.menu_key);
