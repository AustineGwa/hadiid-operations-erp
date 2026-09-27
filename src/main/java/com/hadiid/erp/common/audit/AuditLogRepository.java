package com.hadiid.erp.common.audit;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AuditLogRepository {

    private final JdbcTemplate jdbc;

    public AuditLogRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<AuditLogDto> MAPPER = (rs, rowNum) -> AuditLogDto.builder()
            .id(rs.getLong("id"))
            .userFullName(rs.getString("full_name"))
            .action(rs.getString("action"))
            .module(rs.getString("module"))
            .recordId(rs.getObject("record_id") != null ? rs.getLong("record_id") : null)
            .occurredAt(rs.getTimestamp("occurred_at").toLocalDateTime())
            .oldValue(rs.getString("old_value"))
            .newValue(rs.getString("new_value"))
            .reason(rs.getString("reason"))
            .build();

    public List<AuditLogDto> search(String module, Long userId, int limit, int offset) {
        StringBuilder sql = new StringBuilder("""
                SELECT a.id, a.action, a.module, a.record_id, a.occurred_at, a.old_value, a.new_value, a.reason,
                       u.full_name
                FROM audit_logs a JOIN users u ON u.id = a.user_id
                WHERE 1 = 1
                """);
        List<Object> params = new java.util.ArrayList<>();
        if (module != null && !module.isBlank()) {
            sql.append(" AND a.module = ?");
            params.add(module);
        }
        if (userId != null) {
            sql.append(" AND a.user_id = ?");
            params.add(userId);
        }
        sql.append(" ORDER BY a.occurred_at DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        return jdbc.query(sql.toString(), MAPPER, params.toArray());
    }
}
