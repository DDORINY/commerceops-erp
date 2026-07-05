CREATE TABLE IF NOT EXISTS departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NULL,
    parent_id BIGINT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_departments_code UNIQUE (code),
    CONSTRAINT fk_departments_parent FOREIGN KEY (parent_id) REFERENCES departments (id),
    INDEX idx_departments_parent_sort (parent_id, sort_order),
    INDEX idx_departments_active_sort (active, sort_order)
);

CREATE TABLE IF NOT EXISTS positions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    level INT NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    INDEX idx_positions_level_sort (level, sort_order),
    INDEX idx_positions_active_sort (active, sort_order)
);

CREATE TABLE IF NOT EXISTS staff_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    department_id BIGINT NULL,
    position_id BIGINT NULL,
    employee_no VARCHAR(50) NULL,
    employment_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    joined_at DATE NULL,
    left_at DATE NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_staff_profiles_user UNIQUE (user_id),
    CONSTRAINT uk_staff_profiles_employee_no UNIQUE (employee_no),
    CONSTRAINT fk_staff_profiles_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_staff_profiles_department FOREIGN KEY (department_id) REFERENCES departments (id),
    CONSTRAINT fk_staff_profiles_position FOREIGN KEY (position_id) REFERENCES positions (id),
    INDEX idx_staff_profiles_department (department_id),
    INDEX idx_staff_profiles_position (position_id),
    INDEX idx_staff_profiles_employment_status (employment_status),
    INDEX idx_staff_profiles_active (active)
);
