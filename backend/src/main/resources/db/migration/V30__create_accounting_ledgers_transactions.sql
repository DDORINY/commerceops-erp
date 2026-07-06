CREATE TABLE IF NOT EXISTS accounting_ledgers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ledger_number VARCHAR(50) NOT NULL,
    period VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    closed_at DATETIME(6) NULL,
    closed_by BIGINT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_accounting_ledgers_number UNIQUE (ledger_number),
    CONSTRAINT fk_accounting_ledgers_closed_by FOREIGN KEY (closed_by) REFERENCES users (id),
    INDEX idx_accounting_ledgers_period_status (period, status),
    INDEX idx_accounting_ledgers_status (status)
);

CREATE TABLE IF NOT EXISTS accounting_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ledger_id BIGINT NULL,
    transaction_number VARCHAR(50) NOT NULL,
    type VARCHAR(30) NOT NULL,
    direction VARCHAR(20) NOT NULL,
    amount BIGINT NOT NULL,
    reference_type VARCHAR(30) NOT NULL,
    reference_id BIGINT NOT NULL,
    occurred_at DATETIME(6) NOT NULL,
    memo VARCHAR(500) NULL,
    created_by BIGINT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_accounting_transactions_number UNIQUE (transaction_number),
    CONSTRAINT fk_accounting_transactions_ledger FOREIGN KEY (ledger_id) REFERENCES accounting_ledgers (id),
    CONSTRAINT fk_accounting_transactions_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    INDEX idx_accounting_transactions_ledger (ledger_id),
    INDEX idx_accounting_transactions_type_occurred (type, occurred_at),
    INDEX idx_accounting_transactions_reference (reference_type, reference_id),
    INDEX idx_accounting_transactions_direction (direction)
);

INSERT INTO permissions (code, name, domain, action, description, active, created_at, updated_at)
SELECT 'ACCOUNTING_MANAGE', '회계 거래 관리', 'ACCOUNTING', 'MANAGE', '회계 거래 생성과 보정 후보 작업을 수행합니다.', TRUE, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM permissions p WHERE p.code = 'ACCOUNTING_MANAGE');

INSERT INTO permission_group_permissions (permission_group_id, permission_id, created_at)
SELECT pg.id, p.id, NOW(6)
FROM permission_groups pg
JOIN permissions p ON p.code = 'ACCOUNTING_MANAGE'
WHERE pg.code IN ('SUPER_ADMIN_GROUP', 'ADMIN_GROUP')
  AND NOT EXISTS (
      SELECT 1 FROM permission_group_permissions pgp
      WHERE pgp.permission_group_id = pg.id AND pgp.permission_id = p.id
  );
