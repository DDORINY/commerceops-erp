CREATE TABLE IF NOT EXISTS product_detail_blocks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    block_type VARCHAR(30) NOT NULL,
    title VARCHAR(200),
    content TEXT,
    image_url VARCHAR(500),
    spec_json TEXT,
    sort_order INT NOT NULL,
    visible BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_product_detail_blocks_product FOREIGN KEY (product_id) REFERENCES products (id),
    INDEX idx_product_detail_blocks_product_sort (product_id, sort_order),
    INDEX idx_product_detail_blocks_visible (visible)
);
