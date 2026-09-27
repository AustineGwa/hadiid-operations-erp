-- V5: fabrication_stage_history — append-only (Rule 10, Rule 44 "no orphan stage history").
-- Never UPDATE or DELETE a row here; corrections are new rows.

CREATE TABLE fabrication_stage_history (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id             BIGINT NOT NULL,
    from_stage_id      TINYINT,
    to_stage_id        TINYINT NOT NULL,
    changed_at         TIMESTAMP NOT NULL,
    days_held          INT NOT NULL,
    changed_by_user_id BIGINT NOT NULL,
    is_correction      BOOLEAN NOT NULL DEFAULT FALSE,
    note               VARCHAR(255),
    CONSTRAINT fk_history_job FOREIGN KEY (job_id) REFERENCES fabrication_jobs (id),
    CONSTRAINT fk_history_from FOREIGN KEY (from_stage_id) REFERENCES fabrication_stages (id),
    CONSTRAINT fk_history_to FOREIGN KEY (to_stage_id) REFERENCES fabrication_stages (id),
    CONSTRAINT fk_history_user FOREIGN KEY (changed_by_user_id) REFERENCES users (id),
    CONSTRAINT chk_history_days_held CHECK (days_held >= 0)
);

CREATE INDEX idx_history_job ON fabrication_stage_history (job_id, changed_at);
CREATE INDEX idx_history_to_stage ON fabrication_stage_history (to_stage_id);
CREATE INDEX idx_history_from_stage ON fabrication_stage_history (from_stage_id);
