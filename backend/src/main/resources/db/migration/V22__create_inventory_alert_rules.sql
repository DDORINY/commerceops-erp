CREATE TABLE IF NOT EXISTS inventory_alert_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku_id BIGINT NOT NULL,
    warehouse_id BIGINT NULL,
    threshold_quantity INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    memo VARCHAR(500) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_inventory_alert_rules_sku_warehouse UNIQUE (sku_id, warehouse_id),
    CONSTRAINT fk_inventory_alert_rules_sku FOREIGN KEY (sku_id) REFERENCES skus (id),
    CONSTRAINT fk_inventory_alert_rules_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses (id),
    INDEX idx_inventory_alert_rules_sku (sku_id),
    INDEX idx_inventory_alert_rules_warehouse (warehouse_id),
    INDEX idx_inventory_alert_rules_active (active)
);

INSERT INTO admin_menu_permissions (menu_key, menu_label, menu_path, required_permission_code, visible, sort_order, created_at, updated_at)
SELECT 'inventory-alerts', '안전재고 알림', '/admin/inventory-alerts', 'INVENTORY_READ', TRUE, 106, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM admin_menu_permissions amp WHERE amp.menu_key = 'inventory-alerts');
