CREATE TABLE IF NOT EXISTS business_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_name VARCHAR(120) NULL,
    representative_name VARCHAR(80) NULL,
    business_registration_number VARCHAR(30) NULL,
    mail_order_business_number VARCHAR(60) NULL,
    address VARCHAR(500) NULL,
    customer_service_phone VARCHAR(30) NULL,
    customer_service_email VARCHAR(120) NULL,
    brand_name VARCHAR(120) NULL,
    updated_by BIGINT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_business_settings_updated_by FOREIGN KEY (updated_by) REFERENCES users (id),
    INDEX idx_business_settings_updated_by (updated_by)
);

CREATE TABLE IF NOT EXISTS terms_versions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(40) NOT NULL,
    title VARCHAR(160) NOT NULL,
    content TEXT NOT NULL,
    version VARCHAR(40) NOT NULL,
    effective_from DATETIME(6) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by BIGINT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_terms_versions_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT uk_terms_versions_type_version UNIQUE (type, version),
    INDEX idx_terms_versions_type_active_effective (type, active, effective_from),
    INDEX idx_terms_versions_created_by (created_by)
);
