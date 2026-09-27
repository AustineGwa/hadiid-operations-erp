-- V2: Reference/master data (Rule 8, Rule 18) — configurable without code changes.

CREATE TABLE vehicle_sections (
    id    TINYINT PRIMARY KEY,
    code  VARCHAR(20) NOT NULL,
    label VARCHAR(40) NOT NULL,
    CONSTRAINT uq_sections_code UNIQUE (code)
);

CREATE TABLE body_types (
    id        SMALLINT AUTO_INCREMENT PRIMARY KEY,
    section_id TINYINT NOT NULL,
    code      VARCHAR(40) NOT NULL,
    label     VARCHAR(60) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_bodytypes_section FOREIGN KEY (section_id) REFERENCES vehicle_sections (id),
    CONSTRAINT uq_bodytypes_section_code UNIQUE (section_id, code)
);

CREATE TABLE fabrication_stages (
    id             TINYINT PRIMARY KEY,
    code           VARCHAR(30) NOT NULL,
    label          VARCHAR(40) NOT NULL,
    sequence_order TINYINT NOT NULL,
    CONSTRAINT uq_stages_code UNIQUE (code),
    CONSTRAINT uq_stages_seq UNIQUE (sequence_order)
);

-- Encodes whichever §H.1 transition policy Hadiid confirms. Seeded (V9) with
-- Option B: forward-only "advance" rows (requires_permission_code = JOB_ADVANCE_STAGE,
-- requires_reason = FALSE) plus backward/skip "correct" rows (JOB_CORRECT_STAGE,
-- requires_reason = TRUE). Change the seed data to switch policy — no code change needed.
CREATE TABLE stage_transitions (
    id                       BIGINT AUTO_INCREMENT PRIMARY KEY,
    from_stage_id            TINYINT,
    to_stage_id              TINYINT NOT NULL,
    requires_permission_code VARCHAR(40) NOT NULL,
    requires_reason          BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_st_from FOREIGN KEY (from_stage_id) REFERENCES fabrication_stages (id),
    CONSTRAINT fk_st_to FOREIGN KEY (to_stage_id) REFERENCES fabrication_stages (id)
);

CREATE TABLE payment_methods (
    id        TINYINT PRIMARY KEY,
    code      VARCHAR(20) NOT NULL,
    label     VARCHAR(30) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_paymethods_code UNIQUE (code)
);

-- Home of the Daily Schedule's "Day Status" field (§B.10 / §H.4). Informational
-- only in this release (see the spec's §H.4 business decision); a row's absence
-- for a date means "normal working day."
CREATE TABLE production_calendar (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    calendar_date  DATE NOT NULL,
    status         VARCHAR(15) NOT NULL DEFAULT 'WORKING',
    note           VARCHAR(200),
    set_by_user_id BIGINT,
    updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_calendar_date UNIQUE (calendar_date),
    CONSTRAINT fk_calendar_user FOREIGN KEY (set_by_user_id) REFERENCES users (id),
    CONSTRAINT chk_calendar_status CHECK (status IN ('WORKING', 'NO_WORK', 'ON_HOLD'))
);
