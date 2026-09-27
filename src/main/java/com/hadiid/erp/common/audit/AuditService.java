package com.hadiid.erp.common.audit;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Central audit-trail writer (Rule 33). Deliberately separate from
 * fabrication_stage_history: this table is the security/accountability record
 * ("who changed what, and why"), while stage history is the operational record
 * of production movement. Called as a plain method inside the same @Transactional
 * boundary as the change it is recording — no event bus, per Rule 49.
 */
@Service
public class AuditService {

    private final JdbcTemplate jdbc;

    public AuditService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void record(Long userId, String action, String module, Long recordId,
                        String oldValueJson, String newValueJson, String reason) {
        jdbc.update("""
                INSERT INTO audit_logs (user_id, action, module, record_id, old_value, new_value, reason)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, userId, action, module, recordId, oldValueJson, newValueJson, reason);
    }

    public void record(Long userId, String action, String module, Long recordId) {
        record(userId, action, module, recordId, null, null, null);
    }
}
