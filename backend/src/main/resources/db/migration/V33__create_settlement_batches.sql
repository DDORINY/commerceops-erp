CREATE TABLE IF NOT EXISTS settlement_batches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_number VARCHAR(50) NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_sales BIGINT NOT NULL DEFAULT 0,
    total_refunds BIGINT NOT NULL DEFAULT 0,
    total_shipping_fee BIGINT NOT NULL DEFAULT 0,
    total_shipping_cost BIGINT NOT NULL DEFAULT 0,
    closed_at DATETIME(6) NULL,
    closed_by BIGINT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_settlement_batches_number UNIQUE (batch_number),
    CONSTRAINT fk_settlement_batches_closed_by FOREIGN KEY (closed_by) REFERENCES users (id),
    INDEX idx_settlement_batches_period (period_start, period_end),
    INDEX idx_settlement_batches_status (status)
);

CREATE TABLE IF NOT EXISTS settlement_batch_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    settlement_batch_id BIGINT NOT NULL,
    reference_type VARCHAR(30) NOT NULL,
    reference_id BIGINT NOT NULL,
    item_type VARCHAR(30) NOT NULL,
    amount BIGINT NOT NULL,
    memo VARCHAR(500) NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_settlement_batch_items_batch FOREIGN KEY (settlement_batch_id) REFERENCES settlement_batches (id),
    INDEX idx_settlement_batch_items_batch_type (settlement_batch_id, item_type),
    INDEX idx_settlement_batch_items_reference (reference_type, reference_id)
);

INSERT INTO permissions (code, name, domain, action, description, active, created_at, updated_at)
SELECT 'SETTLEMENT_MANAGE', '정산 배치 관리', 'ACCOUNTING', 'MANAGE',
       '기간별 정산 배치를 생성하고 정산 항목을 관리합니다.', TRUE, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM permissions p WHERE p.code = 'SETTLEMENT_MANAGE');

INSERT INTO permission_group_permissions (permission_group_id, permission_id, created_at)
SELECT pg.id, p.id, NOW(6)
FROM permission_groups pg
JOIN permissions p ON p.code = 'SETTLEMENT_MANAGE'
WHERE pg.code IN ('SUPER_ADMIN_GROUP', 'ADMIN_GROUP')
  AND NOT EXISTS (
      SELECT 1 FROM permission_group_permissions pgp
      WHERE pgp.permission_group_id = pg.id AND pgp.permission_id = p.id
  );
