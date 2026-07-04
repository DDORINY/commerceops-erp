ALTER TABLE products
    ADD COLUMN product_code VARCHAR(80) NULL,
    ADD COLUMN brand VARCHAR(100) NULL,
    ADD COLUMN manufacturer VARCHAR(100) NULL,
    ADD COLUMN model_name VARCHAR(100) NULL,
    ADD COLUMN origin VARCHAR(100) NULL,
    ADD COLUMN original_price INT NULL,
    ADD COLUMN discount_price INT NULL,
    ADD COLUMN purchase_price INT NULL,
    ADD COLUMN search_keywords TEXT NULL,
    ADD COLUMN tags TEXT NULL,
    ADD COLUMN sale_start_at DATETIME(6) NULL,
    ADD COLUMN sale_end_at DATETIME(6) NULL,
    ADD COLUMN delivery_info TEXT NULL,
    ADD COLUMN seo_title VARCHAR(200) NULL,
    ADD COLUMN seo_description VARCHAR(500) NULL,
    ADD COLUMN seo_keywords TEXT NULL;

CREATE UNIQUE INDEX uk_products_product_code ON products (product_code);
CREATE INDEX idx_products_brand ON products (brand);
CREATE INDEX idx_products_sale_period ON products (sale_start_at, sale_end_at);
