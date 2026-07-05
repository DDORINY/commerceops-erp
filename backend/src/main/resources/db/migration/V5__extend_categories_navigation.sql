ALTER TABLE categories
    ADD COLUMN parent_id BIGINT NULL AFTER name,
    ADD COLUMN depth INT NOT NULL DEFAULT 0 AFTER parent_id,
    ADD COLUMN sort_order INT NOT NULL DEFAULT 0 AFTER depth,
    ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE AFTER sort_order,
    ADD COLUMN visible_in_nav BOOLEAN NOT NULL DEFAULT TRUE AFTER active,
    ADD COLUMN slug VARCHAR(120) NULL AFTER visible_in_nav;

ALTER TABLE categories
    ADD CONSTRAINT fk_categories_parent FOREIGN KEY (parent_id) REFERENCES categories (id);

CREATE INDEX idx_categories_parent_sort ON categories (parent_id, sort_order);
CREATE INDEX idx_categories_nav ON categories (active, visible_in_nav, sort_order);
CREATE UNIQUE INDEX uk_categories_slug ON categories (slug);
