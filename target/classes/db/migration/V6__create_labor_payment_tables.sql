-- V6: Labor & Payments (Rule 16) — contractors, workers, job/stage assignments, payments.
-- A payment is its own table (one row per payment event), not a mutable "Amount Paid"
-- cell — this is the one place the software genuinely upgrades the Excel model (§E.6).

CREATE TABLE contractors (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(150) NOT NULL,
    registration_no VARCHAR(60),
    phone           VARCHAR(30),
    CONSTRAINT uq_contractors_reg UNIQUE (registration_no)
);

CREATE TABLE workers (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(150) NOT NULL,
    id_number      VARCHAR(60),
    phone          VARCHAR(30),
    contractor_id  BIGINT,
    CONSTRAINT fk_workers_contractor FOREIGN KEY (contractor_id) REFERENCES contractors (id)
);

CREATE TABLE job_worker_assignments (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id         BIGINT NOT NULL,
    stage_id       TINYINT NOT NULL,
    worker_id      BIGINT NOT NULL,
    agreed_amount  DECIMAL(14, 2) NOT NULL,
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_assign_job FOREIGN KEY (job_id) REFERENCES fabrication_jobs (id),
    CONSTRAINT fk_assign_stage FOREIGN KEY (stage_id) REFERENCES fabrication_stages (id),
    CONSTRAINT fk_assign_worker FOREIGN KEY (worker_id) REFERENCES workers (id),
    CONSTRAINT uq_assign_job_stage_worker UNIQUE (job_id, stage_id, worker_id),
    CONSTRAINT chk_assign_amount CHECK (agreed_amount >= 0)
);

CREATE INDEX idx_assignments_job ON job_worker_assignments (job_id);

CREATE TABLE labor_payments (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    assignment_id     BIGINT NOT NULL,
    amount_paid       DECIMAL(14, 2) NOT NULL,
    date_paid         DATE NOT NULL,
    payment_method_id TINYINT NOT NULL,
    paid_by_user_id   BIGINT NOT NULL,
    reference_note    VARCHAR(150),
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payments_assignment FOREIGN KEY (assignment_id) REFERENCES job_worker_assignments (id),
    CONSTRAINT fk_payments_method FOREIGN KEY (payment_method_id) REFERENCES payment_methods (id),
    CONSTRAINT fk_payments_user FOREIGN KEY (paid_by_user_id) REFERENCES users (id),
    CONSTRAINT chk_payments_amount CHECK (amount_paid > 0)
);

CREATE INDEX idx_payments_assignment ON labor_payments (assignment_id);
CREATE INDEX idx_payments_date ON labor_payments (date_paid);
