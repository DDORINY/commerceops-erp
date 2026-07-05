CREATE TABLE IF NOT EXISTS permission_groups (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(100) NOT NULL,
    description VARCHAR(500) NULL,
    system_group BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_permission_groups_code UNIQUE (code),
    INDEX idx_permission_groups_active (active),
    INDEX idx_permission_groups_system_active (system_group, active)
);

CREATE TABLE IF NOT EXISTS user_permission_groups (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    permission_group_id BIGINT NOT NULL,
    created_by BIGINT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_user_permission_group UNIQUE (user_id, permission_group_id),
    CONSTRAINT fk_user_permission_groups_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_permission_groups_group FOREIGN KEY (permission_group_id) REFERENCES permission_groups (id),
    CONSTRAINT fk_user_permission_groups_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    INDEX idx_user_permission_groups_user (user_id),
    INDEX idx_user_permission_groups_group (permission_group_id)
);

INSERT INTO permission_groups (name, code, description, system_group, active, created_at, updated_at)
SELECT '최고관리자 그룹', 'SUPER_ADMIN_GROUP', '기존 SUPER_ADMIN role과 병행 운영하는 기본 시스템 권한 그룹입니다.', TRUE, TRUE, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM permission_groups WHERE code = 'SUPER_ADMIN_GROUP');

INSERT INTO permission_groups (name, code, description, system_group, active, created_at, updated_at)
SELECT '관리자 그룹', 'ADMIN_GROUP', '기존 ADMIN role과 병행 운영하는 기본 시스템 권한 그룹입니다.', TRUE, TRUE, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM permission_groups WHERE code = 'ADMIN_GROUP');

INSERT INTO permission_groups (name, code, description, system_group, active, created_at, updated_at)
SELECT '매니저 그룹', 'MANAGER_GROUP', '기존 MANAGER role과 병행 운영하는 기본 시스템 권한 그룹입니다.', TRUE, TRUE, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM permission_groups WHERE code = 'MANAGER_GROUP');
