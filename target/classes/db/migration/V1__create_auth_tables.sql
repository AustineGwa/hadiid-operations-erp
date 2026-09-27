-- V1: Identity & Access — roles, permissions, role_permissions, users.
-- No ORM: plain relational tables, real FKs, read by JdbcTemplate/RowMapper.

CREATE TABLE roles (
    id          TINYINT PRIMARY KEY,
    code        VARCHAR(20) NOT NULL,
    label       VARCHAR(50) NOT NULL,
    CONSTRAINT uq_roles_code UNIQUE (code)
);

CREATE TABLE permissions (
    id          SMALLINT PRIMARY KEY,
    code        VARCHAR(40) NOT NULL,
    description VARCHAR(150) NOT NULL,
    CONSTRAINT uq_permissions_code UNIQUE (code)
);

CREATE TABLE role_permissions (
    role_id       TINYINT NOT NULL,
    permission_id SMALLINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT fk_rp_permission FOREIGN KEY (permission_id) REFERENCES permissions (id)
);

CREATE TABLE users (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(60) NOT NULL,
    full_name     VARCHAR(120) NOT NULL,
    email         VARCHAR(150),
    phone         VARCHAR(30),
    password_hash VARCHAR(255) NOT NULL,
    role_id       TINYINT NOT NULL,
    status        VARCHAR(10) NOT NULL DEFAULT 'ACTIVE',
    team          VARCHAR(40),
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'DISABLED'))
);

CREATE INDEX idx_users_role ON users (role_id);
