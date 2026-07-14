ALTER TABLE payments
    ADD COLUMN provider_order_id VARCHAR(64) NULL,
    ADD COLUMN payment_key VARCHAR(200) NULL,
    ADD COLUMN requested_amount INT NULL,
    ADD COLUMN approved_amount INT NULL,
    ADD COLUMN requested_at DATETIME(6) NULL,
    ADD COLUMN approved_at DATETIME(6) NULL,
    ADD COLUMN failure_code VARCHAR(100) NULL,
    ADD COLUMN failure_message VARCHAR(500) NULL,
    ADD COLUMN raw_response LONGTEXT NULL;

UPDATE payments p
JOIN orders o ON o.id = p.order_id
SET p.provider_order_id = CONCAT('LEGACY-', p.id),
    p.requested_amount = o.total_price,
    p.approved_amount = COALESCE(p.paid_amount, 0),
    p.requested_at = p.created_at,
    p.approved_at = CASE WHEN p.payment_status = 'PAID' THEN p.updated_at ELSE NULL END,
    p.provider = COALESCE(p.provider, 'MOCK_PROVIDER');

ALTER TABLE payments
    MODIFY provider_order_id VARCHAR(64) NOT NULL,
    MODIFY requested_amount INT NOT NULL,
    MODIFY approved_amount INT NOT NULL,
    MODIFY requested_at DATETIME(6) NOT NULL,
    ADD CONSTRAINT uk_payments_provider_order_id UNIQUE (provider_order_id),
    ADD CONSTRAINT uk_payments_payment_key UNIQUE (payment_key);
