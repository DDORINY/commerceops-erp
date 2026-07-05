CREATE TABLE IF NOT EXISTS product_status_histories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    changed_by_user_id BIGINT NULL,
    changed_by_email VARCHAR(100) NULL,
    previous_sales_status VARCHAR(30) NULL,
    new_sales_status VARCHAR(30) NULL,
    previous_display_status VARCHAR(30) NULL,
    new_display_status VARCHAR(30) NULL,
    reason VARCHAR(500) NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_product_status_histories_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT fk_product_status_histories_user FOREIGN KEY (changed_by_user_id) REFERENCES users (id),
    INDEX idx_product_status_histories_product_created (product_id, created_at),
    INDEX idx_product_status_histories_user_created (changed_by_user_id, created_at)
);
