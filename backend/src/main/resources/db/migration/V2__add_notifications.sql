CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(30) NOT NULL,
    title VARCHAR(150) NOT NULL,
    message VARCHAR(500) NOT NULL,
    target_type VARCHAR(50),
    target_id BIGINT,
    read_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id),
    INDEX idx_notifications_user_read_created (user_id, read_at, created_at),
    INDEX idx_notifications_type_created (type, created_at),
    INDEX idx_notifications_target (target_type, target_id)
);
