CREATE TABLE IF NOT EXISTS outbound_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    outbound_number VARCHAR(50) NOT NULL,
    order_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    requested_at DATETIME(6) NOT NULL,
    picked_at DATETIME(6) NULL,
    shipped_at DATETIME(6) NULL,
    memo VARCHAR(500) NULL,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_outbound_orders_number UNIQUE (outbound_number),
    CONSTRAINT fk_outbound_orders_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_outbound_orders_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses (id),
    CONSTRAINT fk_outbound_orders_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT fk_outbound_orders_updated_by FOREIGN KEY (updated_by) REFERENCES users (id),
    INDEX idx_outbound_orders_order_id (order_id),
    INDEX idx_outbound_orders_warehouse_id (warehouse_id),
    INDEX idx_outbound_orders_status (status),
    INDEX idx_outbound_orders_requested_at (requested_at)
);

CREATE TABLE IF NOT EXISTS outbound_order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    outbound_order_id BIGINT NOT NULL,
    order_item_id BIGINT NOT NULL,
    sku_id BIGINT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    picked_quantity INT NOT NULL DEFAULT 0,
    scanned_quantity INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_outbound_order_items_outbound_order FOREIGN KEY (outbound_order_id) REFERENCES outbound_orders (id),
    CONSTRAINT fk_outbound_order_items_order_item FOREIGN KEY (order_item_id) REFERENCES order_items (id),
    CONSTRAINT fk_outbound_order_items_sku FOREIGN KEY (sku_id) REFERENCES skus (id),
    CONSTRAINT fk_outbound_order_items_product FOREIGN KEY (product_id) REFERENCES products (id),
    INDEX idx_outbound_order_items_outbound_order_id (outbound_order_id),
    INDEX idx_outbound_order_items_order_item_id (order_item_id),
    INDEX idx_outbound_order_items_sku_id (sku_id),
    INDEX idx_outbound_order_items_product_id (product_id)
);

INSERT INTO permissions (code, name, domain, action, description, active, created_at, updated_at)
SELECT code, name, domain, action, description, TRUE, NOW(6), NOW(6)
FROM (
    SELECT 'OUTBOUND_READ' code, '출고 조회' name, 'DISTRIBUTION' domain, 'READ' action, '출고 지시와 출고 품목을 조회합니다.' description UNION ALL
    SELECT 'OUTBOUND_MANAGE', '출고 관리', 'DISTRIBUTION', 'MANAGE', '출고 지시 생성, 수정, 피킹, 취소를 수행합니다.'
) seed
WHERE NOT EXISTS (SELECT 1 FROM permissions p WHERE p.code = seed.code);

INSERT INTO permission_group_permissions (permission_group_id, permission_id, created_at)
SELECT pg.id, p.id, NOW(6)
FROM permission_groups pg
JOIN permissions p ON p.code IN ('OUTBOUND_READ', 'OUTBOUND_MANAGE')
WHERE pg.code IN ('SUPER_ADMIN_GROUP', 'ADMIN_GROUP')
  AND NOT EXISTS (
      SELECT 1 FROM permission_group_permissions pgp
      WHERE pgp.permission_group_id = pg.id AND pgp.permission_id = p.id
  );

INSERT INTO permission_group_permissions (permission_group_id, permission_id, created_at)
SELECT pg.id, p.id, NOW(6)
FROM permission_groups pg
JOIN permissions p ON p.code = 'OUTBOUND_READ'
WHERE pg.code = 'MANAGER_GROUP'
  AND NOT EXISTS (
      SELECT 1 FROM permission_group_permissions pgp
      WHERE pgp.permission_group_id = pg.id AND pgp.permission_id = p.id
  );

INSERT INTO admin_menu_permissions (menu_key, menu_label, menu_path, required_permission_code, visible, sort_order, created_at, updated_at)
SELECT 'outbound-orders', '출고 관리', '/admin/outbound-orders', 'OUTBOUND_READ', TRUE, 204, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM admin_menu_permissions amp WHERE amp.menu_key = 'outbound-orders');
