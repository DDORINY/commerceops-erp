CREATE TABLE IF NOT EXISTS production_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    production_number VARCHAR(40) NOT NULL,
    status VARCHAR(30) NOT NULL,
    warehouse_id BIGINT NOT NULL,
    planned_quantity INT NOT NULL,
    completed_quantity INT NOT NULL DEFAULT 0,
    started_at DATETIME(6) NULL,
    completed_at DATETIME(6) NULL,
    memo TEXT NULL,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_production_orders_number UNIQUE (production_number),
    CONSTRAINT fk_production_orders_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses (id),
    CONSTRAINT fk_production_orders_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT fk_production_orders_updated_by FOREIGN KEY (updated_by) REFERENCES users (id),
    INDEX idx_production_orders_status (status),
    INDEX idx_production_orders_warehouse_id (warehouse_id),
    INDEX idx_production_orders_created_at (created_at)
);

CREATE TABLE IF NOT EXISTS production_order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    production_order_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    planned_quantity INT NOT NULL,
    completed_quantity INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_production_order_items_order FOREIGN KEY (production_order_id) REFERENCES production_orders (id),
    CONSTRAINT fk_production_order_items_sku FOREIGN KEY (sku_id) REFERENCES skus (id),
    CONSTRAINT fk_production_order_items_product FOREIGN KEY (product_id) REFERENCES products (id),
    INDEX idx_production_order_items_order_id (production_order_id),
    INDEX idx_production_order_items_sku_id (sku_id),
    INDEX idx_production_order_items_product_id (product_id)
);

CREATE TABLE IF NOT EXISTS production_receipts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    production_order_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    inventory_log_id BIGINT NULL,
    created_by BIGINT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_production_receipts_order FOREIGN KEY (production_order_id) REFERENCES production_orders (id),
    CONSTRAINT fk_production_receipts_sku FOREIGN KEY (sku_id) REFERENCES skus (id),
    CONSTRAINT fk_production_receipts_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT fk_production_receipts_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses (id),
    CONSTRAINT fk_production_receipts_inventory_log FOREIGN KEY (inventory_log_id) REFERENCES inventory_logs (id),
    CONSTRAINT fk_production_receipts_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    INDEX idx_production_receipts_order_id (production_order_id),
    INDEX idx_production_receipts_sku_id (sku_id),
    INDEX idx_production_receipts_product_id (product_id),
    INDEX idx_production_receipts_warehouse_id (warehouse_id),
    INDEX idx_production_receipts_created_at (created_at)
);

INSERT INTO permissions (code, name, domain, action, description, active, created_at, updated_at)
SELECT 'PRODUCTION_MANAGE', '생산 관리', 'INVENTORY', 'PRODUCTION_MANAGE', '생산 주문 생성, 수정, 시작, 완료, 취소를 수행합니다.', TRUE, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM permissions p WHERE p.code = 'PRODUCTION_MANAGE');

INSERT INTO permission_group_permissions (permission_group_id, permission_id, created_at)
SELECT pg.id, p.id, NOW(6)
FROM permission_groups pg
JOIN permissions p ON p.code = 'PRODUCTION_MANAGE'
WHERE pg.code IN ('SUPER_ADMIN_GROUP', 'ADMIN_GROUP')
  AND NOT EXISTS (
      SELECT 1 FROM permission_group_permissions pgp
      WHERE pgp.permission_group_id = pg.id AND pgp.permission_id = p.id
  );

INSERT INTO admin_menu_permissions (menu_key, menu_label, menu_path, required_permission_code, visible, sort_order, created_at, updated_at)
SELECT 'production', '생산 입고 관리', '/admin/production', 'INVENTORY_READ', TRUE, 113, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM admin_menu_permissions amp WHERE amp.menu_key = 'production');
