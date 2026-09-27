-- V7: Production planning (Rule 19 — one reusable model, section as data, not three modules).

CREATE TABLE production_plan_targets (
    id                        BIGINT AUTO_INCREMENT PRIMARY KEY,
    section_id                TINYINT NOT NULL,
    plan_month                DATE NOT NULL, -- first day of month
    monthly_capacity_target   INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_plantargets_section FOREIGN KEY (section_id) REFERENCES vehicle_sections (id),
    CONSTRAINT uq_plantargets_section_month UNIQUE (section_id, plan_month)
);

CREATE TABLE weekly_budget_entries (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    section_id        TINYINT NOT NULL,
    week_start        DATE NOT NULL,
    week_end          DATE NOT NULL,
    materials_budget  DECIMAL(14, 2) NOT NULL DEFAULT 0,
    materials_actual  DECIMAL(14, 2) NOT NULL DEFAULT 0, -- manual today, see spec §H.6
    labor_budget      DECIMAL(14, 2) NOT NULL DEFAULT 0,
    updated_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_weeklybudget_section FOREIGN KEY (section_id) REFERENCES vehicle_sections (id),
    CONSTRAINT uq_weeklybudget_section_week UNIQUE (section_id, week_start)
);

-- Labor Actual, Planned to Start/Complete, Actual Released, Variance, WIP Opening/
-- Closing are all computed at read time (§C/§E.8) — deliberately no tables here.
