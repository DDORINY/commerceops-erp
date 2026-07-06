CREATE TABLE IF NOT EXISTS shipment_labels (
    id BIGINT NOT NULL AUTO_INCREMENT,
    shipment_id BIGINT NOT NULL,
    tracking_number VARCHAR(100) NOT NULL,
    carrier VARCHAR(100) NOT NULL,
    label_format VARCHAR(50) NOT NULL,
    print_count INT NOT NULL DEFAULT 0,
    last_printed_at DATETIME(6) NULL,
    created_by BIGINT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_shipment_labels_shipment FOREIGN KEY (shipment_id) REFERENCES shipments (id),
    CONSTRAINT fk_shipment_labels_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    INDEX idx_shipment_labels_shipment_id (shipment_id),
    INDEX idx_shipment_labels_tracking_number (tracking_number),
    INDEX idx_shipment_labels_created_at (created_at)
);

INSERT INTO permissions (code, name, domain, action, description, active, created_at, updated_at)
SELECT code, name, domain, action, description, TRUE, NOW(6), NOW(6)
FROM (
    SELECT 'SHIPPING_LABEL_PRINT' code, '송장 라벨 출력' name, 'DISTRIBUTION' domain, 'PRINT' action, '송장 라벨 생성과 출력 이력을 관리합니다.' description
) seed
WHERE NOT EXISTS (SELECT 1 FROM permissions p WHERE p.code = seed.code);

INSERT INTO permission_group_permissions (permission_group_id, permission_id, created_at)
SELECT pg.id, p.id, NOW(6)
FROM permission_groups pg
JOIN permissions p ON p.code = 'SHIPPING_LABEL_PRINT'
WHERE pg.code IN ('SUPER_ADMIN_GROUP', 'ADMIN_GROUP')
  AND NOT EXISTS (
      SELECT 1 FROM permission_group_permissions pgp
      WHERE pgp.permission_group_id = pg.id AND pgp.permission_id = p.id
  );
