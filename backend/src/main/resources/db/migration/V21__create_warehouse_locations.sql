CREATE TABLE IF NOT EXISTS warehouse_locations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    warehouse_id BIGINT NOT NULL,
    code VARCHAR(60) NOT NULL,
    name VARCHAR(120) NOT NULL,
    zone VARCHAR(60) NULL,
    aisle VARCHAR(60) NULL,
    rack VARCHAR(60) NULL,
    cell VARCHAR(60) NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_warehouse_locations_warehouse_code UNIQUE (warehouse_id, code),
    CONSTRAINT fk_warehouse_locations_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses (id),
    INDEX idx_warehouse_locations_warehouse (warehouse_id),
    INDEX idx_warehouse_locations_active (active),
    INDEX idx_warehouse_locations_code (code)
);

CREATE TABLE IF NOT EXISTS warehouse_location_stocks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    warehouse_location_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    reserved_quantity INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_warehouse_location_stocks_location_sku UNIQUE (warehouse_location_id, sku_id),
    CONSTRAINT fk_location_stocks_location FOREIGN KEY (warehouse_location_id) REFERENCES warehouse_locations (id),
    CONSTRAINT fk_location_stocks_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses (id),
    CONSTRAINT fk_location_stocks_sku FOREIGN KEY (sku_id) REFERENCES skus (id),
    CONSTRAINT fk_location_stocks_product FOREIGN KEY (product_id) REFERENCES products (id),
    INDEX idx_location_stocks_warehouse (warehouse_id),
    INDEX idx_location_stocks_location (warehouse_location_id),
    INDEX idx_location_stocks_sku (sku_id),
    INDEX idx_location_stocks_product (product_id)
);

INSERT INTO admin_menu_permissions (menu_key, menu_label, menu_path, required_permission_code, visible, sort_order, created_at, updated_at)
SELECT 'warehouse-locations', '창고 위치 관리', '/admin/warehouse-locations', 'INVENTORY_READ', TRUE, 105, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM admin_menu_permissions amp WHERE amp.menu_key = 'warehouse-locations');
