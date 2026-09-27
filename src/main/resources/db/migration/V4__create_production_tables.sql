-- V4: fabrication_jobs — the core "one record per vehicle, for life" table (Rule 6, Rule 35).
-- section is a data value (section_id), never a separate table per section.
-- Calculated columns (Production %, Finishing %, Working Days Remaining, Total
-- Fabrication/Turnaround Days) are deliberately NOT columns here — see §C of the
-- spec; they are computed at read time by service-layer calculators.

CREATE TABLE fabrication_jobs (
    id                         BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id                 VARCHAR(20) NOT NULL,
    job_card_no                VARCHAR(20) NOT NULL,
    job_card_date              DATE NOT NULL,
    section_id                 TINYINT NOT NULL,
    body_type_id               SMALLINT NOT NULL,
    vehicle_model              VARCHAR(120) NOT NULL,
    planned_start_date         DATE NOT NULL,
    planned_production_days    SMALLINT NOT NULL,
    planned_finishing_days     SMALLINT NOT NULL,
    current_stage_id           TINYINT NOT NULL,
    stage_entered_at           TIMESTAMP NOT NULL,
    internal_store_date        DATE,
    release_date               DATE,
    contract_amount            DECIMAL(14, 2),
    customer_id                BIGINT,
    customer_name_snapshot     VARCHAR(150) NOT NULL,
    customer_reg_snapshot      VARCHAR(60),
    customer_phone_snapshot    VARCHAR(30),
    customer_address_snapshot  VARCHAR(255),
    notes                      TEXT,
    created_by_user_id         BIGINT NOT NULL,
    created_at                 TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                 TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_jobs_vehicle_id UNIQUE (vehicle_id),
    CONSTRAINT uq_jobs_job_card_no UNIQUE (job_card_no),
    CONSTRAINT fk_jobs_section FOREIGN KEY (section_id) REFERENCES vehicle_sections (id),
    CONSTRAINT fk_jobs_bodytype FOREIGN KEY (body_type_id) REFERENCES body_types (id),
    CONSTRAINT fk_jobs_stage FOREIGN KEY (current_stage_id) REFERENCES fabrication_stages (id),
    CONSTRAINT fk_jobs_customer FOREIGN KEY (customer_id) REFERENCES customers (id),
    CONSTRAINT fk_jobs_created_by FOREIGN KEY (created_by_user_id) REFERENCES users (id),
    CONSTRAINT chk_jobs_prod_days CHECK (planned_production_days > 0),
    CONSTRAINT chk_jobs_fin_days CHECK (planned_finishing_days > 0),
    CONSTRAINT chk_jobs_contract_amount CHECK (contract_amount IS NULL OR contract_amount >= 0)
);

CREATE INDEX idx_jobs_section ON fabrication_jobs (section_id);
CREATE INDEX idx_jobs_stage ON fabrication_jobs (current_stage_id);
CREATE INDEX idx_jobs_customer ON fabrication_jobs (customer_id);
CREATE INDEX idx_jobs_planned_start ON fabrication_jobs (planned_start_date);
