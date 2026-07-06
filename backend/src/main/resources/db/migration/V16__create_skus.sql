CREATE TABLE IF NOT EXISTS skus (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    option_signature VARCHAR(500) NULL,
    sku_code VARCHAR(100) NOT NULL,
    barcode VARCHAR(100) NULL,
    name VARCHAR(200) NOT NULL,
    safety_stock_quantity INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_skus_sku_code UNIQUE (sku_code),
    CONSTRAINT uk_skus_barcode UNIQUE (barcode),
    CONSTRAINT fk_skus_product FOREIGN KEY (product_id) REFERENCES products (id),
    INDEX idx_skus_product_id (product_id),
    INDEX idx_skus_active (active),
    INDEX idx_skus_barcode (barcode)
);

INSERT INTO permissions (code, name, domain, action, description, active, created_at, updated_at)
SELECT code, name, domain, action, description, TRUE, NOW(6), NOW(6)
FROM (
    SELECT 'SKU_MANAGE' code, 'SKU 관리' name, 'INVENTORY' domain, 'SKU_MANAGE' action, 'SKU 생성, 수정, 활성 상태 변경을 수행합니다.' description UNION ALL
    SELECT 'BARCODE_MANAGE', '바코드 관리', 'INVENTORY', 'BARCODE_MANAGE', 'SKU 바코드 발급과 재발급을 수행합니다.'
) seed
WHERE NOT EXISTS (SELECT 1 FROM permissions p WHERE p.code = seed.code);

INSERT INTO permission_group_permissions (permission_group_id, permission_id, created_at)
SELECT pg.id, p.id, NOW(6)
FROM permission_groups pg
JOIN permissions p ON p.code IN ('SKU_MANAGE', 'BARCODE_MANAGE')
WHERE pg.code IN ('SUPER_ADMIN_GROUP', 'ADMIN_GROUP')
  AND NOT EXISTS (
      SELECT 1 FROM permission_group_permissions pgp
      WHERE pgp.permission_group_id = pg.id AND pgp.permission_id = p.id
  );

INSERT INTO admin_menu_permissions (menu_key, menu_label, menu_path, required_permission_code, visible, sort_order, created_at, updated_at)
SELECT 'skus', 'SKU/바코드 관리', '/admin/skus', 'INVENTORY_READ', TRUE, 101, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM admin_menu_permissions amp WHERE amp.menu_key = 'skus');
