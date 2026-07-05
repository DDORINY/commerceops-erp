ALTER TABLE products
    ADD COLUMN sales_status VARCHAR(30) NOT NULL DEFAULT 'ON_SALE',
    ADD COLUMN display_status VARCHAR(30) NOT NULL DEFAULT 'VISIBLE',
    ADD COLUMN deleted_at DATETIME NULL,
    ADD COLUMN safety_stock_quantity INT NOT NULL DEFAULT 5;

UPDATE products
SET sales_status = CASE
        WHEN status = 'SOLD_OUT' THEN 'SOLD_OUT'
        WHEN status = 'HIDDEN' THEN 'PAUSED'
        WHEN status = 'DELETED' THEN 'DISCONTINUED'
        ELSE 'ON_SALE'
    END,
    display_status = CASE
        WHEN status IN ('HIDDEN', 'DELETED') THEN 'HIDDEN'
        ELSE 'VISIBLE'
    END,
    deleted_at = CASE
        WHEN status = 'DELETED' THEN updated_at
        ELSE NULL
    END,
    safety_stock_quantity = 5;

CREATE INDEX idx_products_sales_display ON products (sales_status, display_status);
CREATE INDEX idx_products_deleted_at ON products (deleted_at);
