CREATE TABLE IF NOT EXISTS stock_count_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    count_number VARCHAR(60) NOT NULL,
    warehouse_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    memo VARCHAR(500) NULL,
    started_by BIGINT NULL,
    completed_by BIGINT NULL,
    started_at DATETIME(6) NULL,
    completed_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_stock_count_sessions_count_number UNIQUE (count_number),
    CONSTRAINT fk_stock_count_sessions_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses (id),
    CONSTRAINT fk_stock_count_sessions_started_by FOREIGN KEY (started_by) REFERENCES users (id),
    CONSTRAINT fk_stock_count_sessions_completed_by FOREIGN KEY (completed_by) REFERENCES users (id),
    INDEX idx_stock_count_sessions_warehouse (warehouse_id),
    INDEX idx_stock_count_sessions_status (status),
    INDEX idx_stock_count_sessions_created_at (created_at)
);

CREATE TABLE IF NOT EXISTS stock_count_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    system_quantity INT NOT NULL,
    counted_quantity INT NULL,
    difference_quantity INT NOT NULL DEFAULT 0,
    memo VARCHAR(500) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_stock_count_item_session_sku UNIQUE (session_id, sku_id),
    CONSTRAINT fk_stock_count_items_session FOREIGN KEY (session_id) REFERENCES stock_count_sessions (id),
    CONSTRAINT fk_stock_count_items_sku FOREIGN KEY (sku_id) REFERENCES skus (id),
    CONSTRAINT fk_stock_count_items_product FOREIGN KEY (product_id) REFERENCES products (id),
    INDEX idx_stock_count_items_session (session_id),
    INDEX idx_stock_count_items_sku (sku_id),
    INDEX idx_stock_count_items_product (product_id)
);

INSERT INTO permissions (code, name, domain, action, description, active, created_at, updated_at)
SELECT 'STOCK_COUNT_MANAGE', '재고 실사 관리', 'INVENTORY', 'STOCK_COUNT_MANAGE', '재고 실사 생성, 수정, 완료, 취소를 수행합니다.', TRUE, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM permissions p WHERE p.code = 'STOCK_COUNT_MANAGE');

INSERT INTO permission_group_permissions (permission_group_id, permission_id, created_at)
SELECT pg.id, p.id, NOW(6)
FROM permission_groups pg
JOIN permissions p ON p.code = 'STOCK_COUNT_MANAGE'
WHERE pg.code IN ('SUPER_ADMIN_GROUP', 'ADMIN_GROUP')
  AND NOT EXISTS (
    SELECT 1 FROM permission_group_permissions pgp
    WHERE pgp.permission_group_id = pg.id AND pgp.permission_id = p.id
  );

INSERT INTO admin_menu_permissions (menu_key, menu_label, menu_path, required_permission_code, visible, sort_order, created_at, updated_at)
SELECT 'stock-counts', '재고 실사', '/admin/stock-counts', 'INVENTORY_READ', TRUE, 104, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM admin_menu_permissions amp WHERE amp.menu_key = 'stock-counts');
