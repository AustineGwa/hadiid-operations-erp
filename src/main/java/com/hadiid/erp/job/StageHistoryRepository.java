package com.hadiid.erp.job;

import com.hadiid.erp.job.dto.StageHistoryEntryDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * fabrication_stage_history — append-only (Rule 10/44). This repository only
 * ever INSERTs and SELECTs; there is deliberately no update/delete method.
 */
@Repository
public class StageHistoryRepository {

    private final JdbcTemplate jdbc;

    public StageHistoryRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<StageHistoryEntryDto> MAPPER = (rs, n) -> StageHistoryEntryDto.builder()
            .id(rs.getLong("id"))
            .fromStageLabel(rs.getString("from_label"))
            .toStageLabel(rs.getString("to_label"))
            .changedAt(rs.getTimestamp("changed_at").toLocalDateTime())
            .daysHeld(rs.getInt("days_held"))
            .changedByFullName(rs.getString("full_name"))
            .correction(rs.getBoolean("is_correction"))
            .note(rs.getString("note"))
            .build();

    public List<StageHistoryEntryDto> findByJobId(Long jobId) {
        return jdbc.query("""
                SELECT h.id, fs_from.label AS from_label, fs_to.label AS to_label,
                       h.changed_at, h.days_held, u.full_name, h.is_correction, h.note
                FROM fabrication_stage_history h
                LEFT JOIN fabrication_stages fs_from ON fs_from.id = h.from_stage_id
                JOIN fabrication_stages fs_to ON fs_to.id = h.to_stage_id
                JOIN users u ON u.id = h.changed_by_user_id
                WHERE h.job_id = ?
                ORDER BY h.changed_at ASC, h.id ASC
                """, MAPPER, jobId);
    }

    /** Most recent entry for a job — used to compute the "held" duration for the next transition. */
    public Optional<LocalDateTime> findLastChangedAt(Long jobId) {
        return jdbc.query("SELECT changed_at FROM fabrication_stage_history WHERE job_id = ? ORDER BY changed_at DESC, id DESC LIMIT 1",
                (rs, n) -> rs.getTimestamp("changed_at").toLocalDateTime(), jobId).stream().findFirst();
    }

    public void insert(Long jobId, Integer fromStageId, int toStageId, LocalDateTime changedAt,
                        int daysHeld, Long changedByUserId, boolean isCorrection, String note) {
        jdbc.update("""
                INSERT INTO fabrication_stage_history
                  (job_id, from_stage_id, to_stage_id, changed_at, days_held, changed_by_user_id, is_correction, note)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """, jobId, fromStageId, toStageId, java.sql.Timestamp.valueOf(changedAt), daysHeld,
                changedByUserId, isCorrection, note);
    }

    /** Backing query for Stage Movement Analysis (§K) — average/max dwell per stage, overall and per section. */
    public List<Object[]> dwellStatsByFromStage(Integer sectionId) {
        String sql = sectionId == null
                ? """
                  SELECT fs.label, AVG(h.days_held), MAX(h.days_held), COUNT(*)
                  FROM fabrication_stage_history h
                  JOIN fabrication_stages fs ON fs.id = h.from_stage_id
                  GROUP BY fs.label
                  """
                : """
                  SELECT fs.label, AVG(h.days_held), MAX(h.days_held), COUNT(*)
                  FROM fabrication_stage_history h
                  JOIN fabrication_stages fs ON fs.id = h.from_stage_id
                  JOIN fabrication_jobs j ON j.id = h.job_id
                  WHERE j.section_id = ?
                  GROUP BY fs.label
                  """;
        return sectionId == null
                ? jdbc.query(sql, (rs, n) -> new Object[]{rs.getString(1), rs.getDouble(2), rs.getInt(3), rs.getLong(4)})
                : jdbc.query(sql, (rs, n) -> new Object[]{rs.getString(1), rs.getDouble(2), rs.getInt(3), rs.getLong(4)}, sectionId);
    }

    public List<Object[]> arrivalsByToStage() {
        return jdbc.query("""
                SELECT fs.label, COUNT(*) FROM fabrication_stage_history h
                JOIN fabrication_stages fs ON fs.id = h.to_stage_id
                GROUP BY fs.label
                """, (rs, n) -> new Object[]{rs.getString(1), rs.getLong(2)});
    }

    /** "What stage was job X in as of date D" — the replay logic behind §B.7 (Stage Snapshot), computed on demand. */
    public Optional<String> stageCodeAsOf(Long jobId, LocalDateTime asOf) {
        return jdbc.query("""
                SELECT fs.code FROM fabrication_stage_history h
                JOIN fabrication_stages fs ON fs.id = h.to_stage_id
                WHERE h.job_id = ? AND h.changed_at <= ?
                ORDER BY h.changed_at DESC, h.id DESC LIMIT 1
                """, (rs, n) -> rs.getString(1), jobId, java.sql.Timestamp.valueOf(asOf)).stream().findFirst();
    }
}
