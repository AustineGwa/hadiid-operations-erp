package com.hadiid.erp.planning;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Optional;

/**
 * §E.8 Production Planning — explicit SQL, no ORM. monthlyCapacityTarget and
 * the weekly budget figures are stored (manual entry); Planned to Start,
 * Actual Released, Labor Actual, and WIP are always computed here at read
 * time from fabrication_jobs / labor_payments (Rule 17).
 */
@Repository
public class PlanningRepository {

    private final JdbcTemplate jdbc;

    public PlanningRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ---------- Monthly capacity targets ----------

    public int findMonthlyCapacityTarget(int sectionId, LocalDate monthStart) {
        Integer v = jdbc.query("SELECT monthly_capacity_target FROM production_plan_targets WHERE section_id = ? AND plan_month = ?",
                (rs, n) -> rs.getInt(1), sectionId, Date.valueOf(monthStart)).stream().findFirst().orElse(null);
        return v == null ? 0 : v;
    }

    public void upsertMonthlyCapacityTarget(int sectionId, LocalDate monthStart, int target) {
        int updated = jdbc.update("UPDATE production_plan_targets SET monthly_capacity_target = ? WHERE section_id = ? AND plan_month = ?",
                target, sectionId, Date.valueOf(monthStart));
        if (updated == 0) {
            jdbc.update("INSERT INTO production_plan_targets (section_id, plan_month, monthly_capacity_target) VALUES (?, ?, ?)",
                    sectionId, Date.valueOf(monthStart), target);
        }
    }

    public long countPlannedToStart(int sectionId, LocalDate monthStart, LocalDate monthEndExclusive) {
        Long count = jdbc.queryForObject("""
                SELECT COUNT(*) FROM fabrication_jobs
                WHERE section_id = ? AND planned_start_date >= ? AND planned_start_date < ?
                """, Long.class, sectionId, Date.valueOf(monthStart), Date.valueOf(monthEndExclusive));
        return count == null ? 0 : count;
    }

    public long countActualReleased(int sectionId, LocalDate monthStart, LocalDate monthEndExclusive) {
        Long count = jdbc.queryForObject("""
                SELECT COUNT(*) FROM fabrication_jobs
                WHERE section_id = ? AND release_date >= ? AND release_date < ?
                """, Long.class, sectionId, Date.valueOf(monthStart), Date.valueOf(monthEndExclusive));
        return count == null ? 0 : count;
    }

    // ---------- Weekly budgets ----------

    public Optional<BigDecimal[]> findWeeklyBudget(int sectionId, LocalDate weekStart) {
        return jdbc.query("SELECT materials_budget, materials_actual, labor_budget FROM weekly_budget_entries WHERE section_id = ? AND week_start = ?",
                (rs, n) -> new BigDecimal[]{rs.getBigDecimal(1), rs.getBigDecimal(2), rs.getBigDecimal(3)},
                sectionId, Date.valueOf(weekStart)).stream().findFirst();
    }

    public void upsertWeeklyBudget(int sectionId, LocalDate weekStart, LocalDate weekEnd,
                                    BigDecimal materialsBudget, BigDecimal materialsActual, BigDecimal laborBudget) {
        int updated = jdbc.update("""
                UPDATE weekly_budget_entries SET materials_budget = ?, materials_actual = ?, labor_budget = ?, updated_at = CURRENT_TIMESTAMP
                WHERE section_id = ? AND week_start = ?
                """, materialsBudget, materialsActual, laborBudget, sectionId, Date.valueOf(weekStart));
        if (updated == 0) {
            jdbc.update("""
                    INSERT INTO weekly_budget_entries (section_id, week_start, week_end, materials_budget, materials_actual, labor_budget)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """, sectionId, Date.valueOf(weekStart), Date.valueOf(weekEnd), materialsBudget, materialsActual, laborBudget);
        }
    }

    /** Computed, never stored — matches the V7 comment that Labor Actual has no column. */
    public BigDecimal sumLaborActualForWeek(int sectionId, LocalDate weekStart, LocalDate weekEnd) {
        BigDecimal sum = jdbc.queryForObject("""
                SELECT COALESCE(SUM(p.amount_paid), 0)
                FROM labor_payments p
                JOIN job_worker_assignments a ON a.id = p.assignment_id
                JOIN fabrication_jobs j ON j.id = a.job_id
                WHERE j.section_id = ? AND p.date_paid BETWEEN ? AND ?
                """, BigDecimal.class, sectionId, Date.valueOf(weekStart), Date.valueOf(weekEnd));
        return sum == null ? BigDecimal.ZERO : sum;
    }

    // ---------- WIP rollup ----------

    /** Jobs already started (planned_start_date <= asOf) and not yet released, per section. */
    public long countWipOpen(int sectionId, LocalDate asOf) {
        Long count = jdbc.queryForObject("""
                SELECT COUNT(*) FROM fabrication_jobs j
                JOIN fabrication_stages fs ON fs.id = j.current_stage_id
                WHERE j.section_id = ? AND j.planned_start_date <= ? AND fs.code <> 'RELEASED'
                """, Long.class, sectionId, Date.valueOf(asOf));
        return count == null ? 0 : count;
    }
}
