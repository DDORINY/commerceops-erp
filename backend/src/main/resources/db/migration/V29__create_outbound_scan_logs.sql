CREATE TABLE IF NOT EXISTS outbound_scan_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    outbound_order_id BIGINT NOT NULL,
    outbound_order_item_id BIGINT NOT NULL,
    sku_id BIGINT NULL,
    barcode VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    scanned_by BIGINT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_outbound_scan_logs_outbound_order FOREIGN KEY (outbound_order_id) REFERENCES outbound_orders (id),
    CONSTRAINT fk_outbound_scan_logs_item FOREIGN KEY (outbound_order_item_id) REFERENCES outbound_order_items (id),
    CONSTRAINT fk_outbound_scan_logs_sku FOREIGN KEY (sku_id) REFERENCES skus (id),
    CONSTRAINT fk_outbound_scan_logs_scanned_by FOREIGN KEY (scanned_by) REFERENCES users (id),
    INDEX idx_outbound_scan_logs_outbound_order_id (outbound_order_id),
    INDEX idx_outbound_scan_logs_item_id (outbound_order_item_id),
    INDEX idx_outbound_scan_logs_barcode (barcode),
    INDEX idx_outbound_scan_logs_created_at (created_at)
);
