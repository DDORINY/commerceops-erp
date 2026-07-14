CREATE TABLE user_addresses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    address_name VARCHAR(50) NOT NULL,
    recipient_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    postal_code VARCHAR(10) NOT NULL,
    road_address VARCHAR(255) NOT NULL,
    detail_address VARCHAR(255),
    extra_address VARCHAR(255),
    delivery_request VARCHAR(255),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_user_addresses_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_user_addresses_user_id (user_id),
    INDEX idx_user_addresses_user_default (user_id, is_default)
);

CREATE TABLE order_addresses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    recipient_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    postal_code VARCHAR(10) NOT NULL,
    road_address VARCHAR(255) NOT NULL,
    detail_address VARCHAR(255),
    extra_address VARCHAR(255),
    delivery_request VARCHAR(255),
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_order_addresses_order_id UNIQUE (order_id),
    CONSTRAINT fk_order_addresses_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE
);
