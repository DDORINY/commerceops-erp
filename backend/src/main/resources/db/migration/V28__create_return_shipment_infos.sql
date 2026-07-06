CREATE TABLE IF NOT EXISTS return_shipment_infos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    return_request_id BIGINT NOT NULL,
    carrier VARCHAR(100) NULL,
    tracking_number VARCHAR(100) NULL,
    status VARCHAR(30) NOT NULL,
    shipping_fee DECIMAL(15, 2) NULL,
    fee_payer VARCHAR(20) NOT NULL,
    memo VARCHAR(500) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_return_shipment_infos_return_request UNIQUE (return_request_id),
    CONSTRAINT fk_return_shipment_infos_return_request FOREIGN KEY (return_request_id) REFERENCES return_requests (id),
    INDEX idx_return_shipment_infos_return_id (return_request_id),
    INDEX idx_return_shipment_infos_tracking_number (tracking_number),
    INDEX idx_return_shipment_infos_status (status)
);

INSERT INTO permissions (code, name, domain, action, description, active, created_at, updated_at)
SELECT code, name, domain, action, description, TRUE, NOW(6), NOW(6)
FROM (
    SELECT 'RETURN_SHIPPING_MANAGE' code, '반품 배송 관리' name, 'DISTRIBUTION' domain, 'MANAGE' action,
           '반품 수거 송장, 배송 상태, 배송비 부담 정보를 관리합니다.' description
) seed
WHERE NOT EXISTS (SELECT 1 FROM permissions p WHERE p.code = seed.code);

INSERT INTO permission_group_permissions (permission_group_id, permission_id, created_at)
SELECT pg.id, p.id, NOW(6)
FROM permission_groups pg
JOIN permissions p ON p.code = 'RETURN_SHIPPING_MANAGE'
WHERE pg.code IN ('SUPER_ADMIN_GROUP', 'ADMIN_GROUP')
  AND NOT EXISTS (
      SELECT 1 FROM permission_group_permissions pgp
      WHERE pgp.permission_group_id = pg.id AND pgp.permission_id = p.id
  );
