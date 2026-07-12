CREATE TABLE IF NOT EXISTS shipping_cost_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shipment_id BIGINT NOT NULL,
    carrier_id BIGINT NULL,
    shipping_method_id BIGINT NULL,
    cost_amount BIGINT NOT NULL,
    charged_amount BIGINT NOT NULL DEFAULT 0,
    occurred_at DATETIME(6) NOT NULL,
    settlement_status VARCHAR(20) NOT NULL,
    memo VARCHAR(500) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_shipping_cost_entries_shipment UNIQUE (shipment_id),
    CONSTRAINT fk_shipping_cost_entries_shipment FOREIGN KEY (shipment_id) REFERENCES shipments (id),
    CONSTRAINT fk_shipping_cost_entries_carrier FOREIGN KEY (carrier_id) REFERENCES carriers (id),
    CONSTRAINT fk_shipping_cost_entries_method FOREIGN KEY (shipping_method_id) REFERENCES shipping_methods (id),
    INDEX idx_shipping_cost_entries_carrier (carrier_id, occurred_at),
    INDEX idx_shipping_cost_entries_status (settlement_status)
);

INSERT INTO permissions (code, name, domain, action, description, active, created_at, updated_at)
SELECT 'SHIPPING_COST_MANAGE', '택배비 매입 관리', 'ACCOUNTING', 'MANAGE',
       '배송 방법과 택배사 기준으로 택배비 비용 회계 거래를 생성하고 관리합니다.', TRUE, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM permissions p WHERE p.code = 'SHIPPING_COST_MANAGE');

INSERT INTO permission_group_permissions (permission_group_id, permission_id, created_at)
SELECT pg.id, p.id, NOW(6)
FROM permission_groups pg
JOIN permissions p ON p.code = 'SHIPPING_COST_MANAGE'
WHERE pg.code IN ('SUPER_ADMIN_GROUP', 'ADMIN_GROUP')
  AND NOT EXISTS (
      SELECT 1 FROM permission_group_permissions pgp
      WHERE pgp.permission_group_id = pg.id AND pgp.permission_id = p.id
  );
