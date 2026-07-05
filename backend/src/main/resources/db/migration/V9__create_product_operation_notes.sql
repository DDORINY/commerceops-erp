CREATE TABLE IF NOT EXISTS product_operation_notes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    writer_user_id BIGINT NULL,
    writer_email VARCHAR(100) NULL,
    content TEXT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_product_operation_notes_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT fk_product_operation_notes_user FOREIGN KEY (writer_user_id) REFERENCES users (id),
    INDEX idx_product_operation_notes_product_created (product_id, created_at),
    INDEX idx_product_operation_notes_writer_created (writer_user_id, created_at)
);
