CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL
);

CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price INT NOT NULL,
    stock_quantity INT NOT NULL,
    image_url VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    options TEXT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories (id),
    INDEX idx_products_category_id (category_id),
    INDEX idx_products_status (status),
    INDEX idx_products_created_at (created_at)
);

CREATE TABLE IF NOT EXISTS media_files (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL,
    storage_path VARCHAR(500) NOT NULL,
    public_url VARCHAR(500) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    size BIGINT NOT NULL,
    media_type VARCHAR(50) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_media_files_stored_filename UNIQUE (stored_filename),
    INDEX idx_media_files_media_type (media_type),
    INDEX idx_media_files_created_at (created_at)
);

CREATE TABLE IF NOT EXISTS carts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    selected_options TEXT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_carts_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_carts_product FOREIGN KEY (product_id) REFERENCES products (id),
    INDEX idx_carts_user_id (user_id),
    INDEX idx_carts_product_id (product_id)
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    order_number VARCHAR(50) NOT NULL,
    total_price INT NOT NULL,
    discount_amount INT,
    coupon_code VARCHAR(50),
    status VARCHAR(20) NOT NULL,
    receiver_name VARCHAR(50) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    address VARCHAR(255) NOT NULL,
    detail_address VARCHAR(255),
    payment_status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_orders_order_number UNIQUE (order_number),
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users (id),
    INDEX idx_orders_user_id (user_id),
    INDEX idx_orders_status (status),
    INDEX idx_orders_payment_status (payment_status),
    INDEX idx_orders_created_at (created_at)
);

CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    price INT NOT NULL,
    quantity INT NOT NULL,
    selected_options TEXT,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products (id),
    INDEX idx_order_items_order_id (order_id),
    INDEX idx_order_items_product_id (product_id)
);

CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    payment_status VARCHAR(20) NOT NULL,
    paid_amount INT,
    transaction_id VARCHAR(100),
    idempotency_key VARCHAR(120),
    provider VARCHAR(30),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_payments_order_id UNIQUE (order_id),
    CONSTRAINT uk_payments_idempotency_key UNIQUE (idempotency_key),
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders (id),
    INDEX idx_payments_payment_status (payment_status),
    INDEX idx_payments_created_at (created_at)
);

CREATE TABLE IF NOT EXISTS shipments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    tracking_number VARCHAR(100),
    carrier VARCHAR(100),
    shipped_at DATETIME(6),
    delivered_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_shipments_order_id UNIQUE (order_id),
    CONSTRAINT fk_shipments_order FOREIGN KEY (order_id) REFERENCES orders (id),
    INDEX idx_shipments_status (status),
    INDEX idx_shipments_tracking_number (tracking_number)
);

CREATE TABLE IF NOT EXISTS return_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    reason VARCHAR(30) NOT NULL,
    reason_detail VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    admin_note VARCHAR(500),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_return_requests_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_return_requests_user FOREIGN KEY (user_id) REFERENCES users (id),
    INDEX idx_return_requests_order_id (order_id),
    INDEX idx_return_requests_user_id (user_id),
    INDEX idx_return_requests_status (status)
);

CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    order_item_id BIGINT NOT NULL,
    rating INT NOT NULL,
    content VARCHAR(1000),
    status VARCHAR(20),
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_reviews_order_item_id UNIQUE (order_item_id),
    CONSTRAINT fk_reviews_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users (id),
    INDEX idx_reviews_product_status_created_at (product_id, status, created_at),
    INDEX idx_reviews_user_id (user_id)
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor_id BIGINT,
    actor_email VARCHAR(100) NOT NULL,
    actor_name VARCHAR(50) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id BIGINT NOT NULL,
    before_status VARCHAR(50),
    after_status VARCHAR(50),
    summary VARCHAR(500),
    created_at DATETIME(6) NOT NULL,
    INDEX idx_audit_logs_target (target_type, target_id),
    INDEX idx_audit_logs_action_type (action_type),
    INDEX idx_audit_logs_created_at (created_at)
);

CREATE TABLE IF NOT EXISTS inquiries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT,
    type VARCHAR(20) NOT NULL,
    subject VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    answer TEXT,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_inquiries_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_inquiries_product FOREIGN KEY (product_id) REFERENCES products (id),
    INDEX idx_inquiries_user_id (user_id),
    INDEX idx_inquiries_product_id (product_id),
    INDEX idx_inquiries_status (status),
    INDEX idx_inquiries_created_at (created_at)
);

CREATE TABLE IF NOT EXISTS wishlists (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_wishlists_user_product UNIQUE (user_id, product_id),
    CONSTRAINT fk_wishlists_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_wishlists_product FOREIGN KEY (product_id) REFERENCES products (id),
    INDEX idx_wishlists_user_id (user_id),
    INDEX idx_wishlists_product_id (product_id)
);

CREATE TABLE IF NOT EXISTS coupons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    discount_type VARCHAR(10) NOT NULL,
    discount_value INT NOT NULL,
    min_order_amount INT NOT NULL,
    max_usage INT NOT NULL,
    used_count INT NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_coupons_code UNIQUE (code),
    INDEX idx_coupons_active_expires_at (active, expires_at)
);

CREATE TABLE IF NOT EXISTS inventory_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    before_stock INT NOT NULL,
    after_stock INT NOT NULL,
    memo VARCHAR(255),
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_inventory_logs_product FOREIGN KEY (product_id) REFERENCES products (id),
    INDEX idx_inventory_logs_product_id (product_id),
    INDEX idx_inventory_logs_type (type),
    INDEX idx_inventory_logs_created_at (created_at)
);

CREATE TABLE IF NOT EXISTS accounting_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    amount INT NOT NULL,
    description VARCHAR(300) NOT NULL,
    reference_id VARCHAR(100),
    created_at DATETIME(6) NOT NULL,
    INDEX idx_accounting_entries_type (type),
    INDEX idx_accounting_entries_reference_id (reference_id),
    INDEX idx_accounting_entries_created_at (created_at)
);

CREATE TABLE IF NOT EXISTS warehouses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) NOT NULL,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(300) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_warehouses_code UNIQUE (code),
    INDEX idx_warehouses_active (active)
);

CREATE TABLE IF NOT EXISTS warehouse_stocks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    warehouse_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    reserved_quantity INT NOT NULL,
    version BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_warehouse_stock UNIQUE (warehouse_id, product_id),
    CONSTRAINT fk_warehouse_stocks_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses (id),
    CONSTRAINT fk_warehouse_stocks_product FOREIGN KEY (product_id) REFERENCES products (id),
    INDEX idx_warehouse_stocks_product_id (product_id)
);

CREATE TABLE IF NOT EXISTS stock_reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    order_item_id BIGINT NOT NULL,
    warehouse_stock_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_stock_reservations_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_stock_reservations_order_item FOREIGN KEY (order_item_id) REFERENCES order_items (id),
    CONSTRAINT fk_stock_reservations_warehouse_stock FOREIGN KEY (warehouse_stock_id) REFERENCES warehouse_stocks (id),
    INDEX idx_stock_reservations_order_id (order_id),
    INDEX idx_stock_reservations_order_item_id (order_item_id),
    INDEX idx_stock_reservations_warehouse_stock_id (warehouse_stock_id),
    INDEX idx_stock_reservations_status (status)
);

CREATE TABLE IF NOT EXISTS stock_transfers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transfer_number VARCHAR(50) NOT NULL,
    from_warehouse_id BIGINT NOT NULL,
    to_warehouse_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    requested_at DATETIME(6) NOT NULL,
    completed_at DATETIME(6),
    CONSTRAINT uk_stock_transfers_transfer_number UNIQUE (transfer_number),
    CONSTRAINT fk_stock_transfers_from_warehouse FOREIGN KEY (from_warehouse_id) REFERENCES warehouses (id),
    CONSTRAINT fk_stock_transfers_to_warehouse FOREIGN KEY (to_warehouse_id) REFERENCES warehouses (id),
    CONSTRAINT fk_stock_transfers_product FOREIGN KEY (product_id) REFERENCES products (id),
    INDEX idx_stock_transfers_from_warehouse_id (from_warehouse_id),
    INDEX idx_stock_transfers_to_warehouse_id (to_warehouse_id),
    INDEX idx_stock_transfers_product_id (product_id),
    INDEX idx_stock_transfers_status (status),
    INDEX idx_stock_transfers_requested_at (requested_at)
);
