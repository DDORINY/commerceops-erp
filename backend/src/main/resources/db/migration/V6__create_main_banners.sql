CREATE TABLE main_banners (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(120) NOT NULL,
    subtitle VARCHAR(200) NULL,
    description VARCHAR(500) NULL,
    image_url VARCHAR(500) NULL,
    link_url VARCHAR(500) NULL,
    position VARCHAR(30) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    starts_at DATETIME NULL,
    ends_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_main_banners_visibility (active, position, starts_at, ends_at, sort_order),
    INDEX idx_main_banners_admin_sort (position, sort_order, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
