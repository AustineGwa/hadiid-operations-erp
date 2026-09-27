-- V8: Audit log (Rule 33) — conceptually distinct from fabrication_stage_history.
-- old_value/new_value stored as JSON text for portability across MySQL/H2.

CREATE TABLE audit_logs (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    action      VARCHAR(60) NOT NULL,
    module      VARCHAR(40) NOT NULL,
    record_id   BIGINT,
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    old_value   TEXT,
    new_value   TEXT,
    reason      VARCHAR(255),
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_audit_record ON audit_logs (module, record_id);
CREATE INDEX idx_audit_user ON audit_logs (user_id);
CREATE INDEX idx_audit_time ON audit_logs (occurred_at);
