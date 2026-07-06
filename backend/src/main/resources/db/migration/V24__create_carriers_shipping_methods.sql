CREATE TABLE IF NOT EXISTS carriers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    tracking_url_template VARCHAR(500) NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_carriers_code UNIQUE (code),
    INDEX idx_carriers_active (active)
);

CREATE TABLE IF NOT EXISTS shipping_methods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    carrier_id BIGINT NULL,
    default_fee INT NOT NULL DEFAULT 0,
    description VARCHAR(500) NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_shipping_methods_code UNIQUE (code),
    CONSTRAINT fk_shipping_methods_carrier FOREIGN KEY (carrier_id) REFERENCES carriers (id),
    INDEX idx_shipping_methods_carrier_id (carrier_id),
    INDEX idx_shipping_methods_active (active)
);

INSERT INTO permissions (code, name, domain, action, description, active, created_at, updated_at)
SELECT 'CARRIER_MANAGE', '택배사/배송 방법 관리', 'DISTRIBUTION', 'MANAGE', '택배사와 배송 방법을 생성, 수정, 비활성화합니다.', TRUE, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM permissions p WHERE p.code = 'CARRIER_MANAGE');

INSERT INTO permission_group_permissions (permission_group_id, permission_id, created_at)
SELECT pg.id, p.id, NOW(6)
FROM permission_groups pg
JOIN permissions p ON p.code = 'CARRIER_MANAGE'
WHERE pg.code IN ('SUPER_ADMIN_GROUP', 'ADMIN_GROUP')
  AND NOT EXISTS (
      SELECT 1 FROM permission_group_permissions pgp
      WHERE pgp.permission_group_id = pg.id AND pgp.permission_id = p.id
  );

INSERT INTO admin_menu_permissions (menu_key, menu_label, menu_path, required_permission_code, visible, sort_order, created_at, updated_at)
SELECT 'carriers', '택배사 관리', '/admin/carriers', 'CARRIER_MANAGE', TRUE, 205, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM admin_menu_permissions amp WHERE amp.menu_key = 'carriers');

INSERT INTO admin_menu_permissions (menu_key, menu_label, menu_path, required_permission_code, visible, sort_order, created_at, updated_at)
SELECT 'shipping-methods', '배송 방법 관리', '/admin/shipping-methods', 'CARRIER_MANAGE', TRUE, 206, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM admin_menu_permissions amp WHERE amp.menu_key = 'shipping-methods');
