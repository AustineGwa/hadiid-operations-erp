-- V3: Customers (Rule 15) — separate from jobs; jobs keep a point-in-time snapshot (see V4).

CREATE TABLE customers (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(150) NOT NULL,
    registration_no  VARCHAR(60),
    phone            VARCHAR(30),
    address          VARCHAR(255),
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_customers_reg UNIQUE (registration_no)
);

CREATE INDEX idx_customers_name ON customers (name);
