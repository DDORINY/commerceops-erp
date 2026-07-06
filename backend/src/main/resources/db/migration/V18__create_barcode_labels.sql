CREATE TABLE IF NOT EXISTS barcode_labels (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku_id BIGINT NOT NULL,
    barcode VARCHAR(100) NOT NULL,
    label_format VARCHAR(50) NOT NULL,
    print_count INT NOT NULL DEFAULT 0,
    last_printed_at DATETIME(6) NULL,
    created_by BIGINT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_barcode_labels_sku FOREIGN KEY (sku_id) REFERENCES skus (id),
    CONSTRAINT fk_barcode_labels_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    INDEX idx_barcode_labels_sku_id (sku_id),
    INDEX idx_barcode_labels_barcode (barcode),
    INDEX idx_barcode_labels_created_at (created_at)
);

INSERT INTO admin_menu_permissions (menu_key, menu_label, menu_path, required_permission_code, visible, sort_order, created_at, updated_at)
SELECT 'barcodes', '바코드 라벨 관리', '/admin/barcodes', 'INVENTORY_READ', TRUE, 102, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM admin_menu_permissions amp WHERE amp.menu_key = 'barcodes');
