ALTER TABLE audit_logs
    MODIFY target_id BIGINT NULL,
    ADD COLUMN ip_address VARCHAR(100) NULL,
    ADD COLUMN user_agent VARCHAR(500) NULL,
    ADD COLUMN request_method VARCHAR(10) NULL,
    ADD COLUMN request_path VARCHAR(500) NULL,
    ADD COLUMN before_json TEXT NULL,
    ADD COLUMN after_json TEXT NULL,
    ADD COLUMN metadata_json TEXT NULL,
    ADD INDEX idx_audit_logs_actor_email (actor_email),
    ADD INDEX idx_audit_logs_request_path (request_path);
