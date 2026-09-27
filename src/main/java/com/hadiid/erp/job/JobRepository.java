package com.hadiid.erp.job;

import com.hadiid.erp.job.dto.JobCreateRequest;
import com.hadiid.erp.job.dto.JobDetailDto;
import com.hadiid.erp.job.dto.JobEditRequest;
import com.hadiid.erp.job.dto.JobSearchCriteria;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JobRepository {

    private final JdbcTemplate jdbc;

    public JobRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final String DETAIL_SELECT = """
            SELECT j.id, j.vehicle_id, j.job_card_no, j.job_card_date,
                   j.section_id, vs.code AS section_code, vs.label AS section_label,
                   j.body_type_id, bt.label AS body_type_label,
                   j.vehicle_model, j.planned_start_date, j.planned_production_days, j.planned_finishing_days,
                   j.current_stage_id, fs.code AS stage_code, fs.label AS stage_label, j.stage_entered_at,
                   j.internal_store_date, j.release_date, j.contract_amount,
                   j.customer_id, j.customer_name_snapshot, j.customer_reg_snapshot,
                   j.customer_phone_snapshot, j.customer_address_snapshot, j.notes
            FROM fabrication_jobs j
            JOIN vehicle_sections vs ON vs.id = j.section_id
            JOIN body_types bt ON bt.id = j.body_type_id
            JOIN fabrication_stages fs ON fs.id = j.current_stage_id
            """;

    private static final RowMapper<JobDetailDto> DETAIL_MAPPER = (rs, n) -> {
        Date storeDate = rs.getDate("internal_store_date");
        Date releaseDate = rs.getDate("release_date");
        return JobDetailDto.builder()
                .id(rs.getLong("id"))
                .vehicleId(rs.getString("vehicle_id"))
                .jobCardNo(rs.getString("job_card_no"))
                .jobCardDate(rs.getDate("job_card_date").toLocalDate())
                .sectionId(rs.getInt("section_id"))
                .sectionCode(rs.getString("section_code"))
                .sectionLabel(rs.getString("section_label"))
                .bodyTypeId(rs.getInt("body_type_id"))
                .bodyTypeLabel(rs.getString("body_type_label"))
                .vehicleModel(rs.getString("vehicle_model"))
                .plannedStartDate(rs.getDate("planned_start_date").toLocalDate())
                .plannedProductionDays(rs.getInt("planned_production_days"))
                .plannedFinishingDays(rs.getInt("planned_finishing_days"))
                .currentStageId(rs.getInt("current_stage_id"))
                .stageCode(rs.getString("stage_code"))
                .stageLabel(rs.getString("stage_label"))
                .stageEnteredAt(rs.getTimestamp("stage_entered_at").toLocalDateTime())
                .internalStoreDate(storeDate != null ? storeDate.toLocalDate() : null)
                .releaseDate(releaseDate != null ? releaseDate.toLocalDate() : null)
                .contractAmount(rs.getBigDecimal("contract_amount"))
                .customerId(rs.getObject("customer_id") != null ? rs.getLong("customer_id") : null)
                .customerNameSnapshot(rs.getString("customer_name_snapshot"))
                .customerRegSnapshot(rs.getString("customer_reg_snapshot"))
                .customerPhoneSnapshot(rs.getString("customer_phone_snapshot"))
                .customerAddressSnapshot(rs.getString("customer_address_snapshot"))
                .notes(rs.getString("notes"))
                .build();
    };

    public Optional<JobDetailDto> findById(Long id) {
        return jdbc.query(DETAIL_SELECT + " WHERE j.id = ?", DETAIL_MAPPER, id).stream().findFirst();
    }

    public Optional<JobDetailDto> findByVehicleId(String vehicleId) {
        return jdbc.query(DETAIL_SELECT + " WHERE j.vehicle_id = ?", DETAIL_MAPPER, vehicleId).stream().findFirst();
    }

    /** Server-side search/filter (§40/§41) — builds one parameterized query, never loads everything into memory. */
    public List<JobDetailDto> search(JobSearchCriteria c, int limit, int offset) {
        StringBuilder sql = new StringBuilder(DETAIL_SELECT + " WHERE 1 = 1");
        List<Object> params = new ArrayList<>();
        appendCriteria(sql, params, c);
        sql.append(" ORDER BY j.planned_start_date DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        return jdbc.query(sql.toString(), DETAIL_MAPPER, params.toArray());
    }

    public long countSearch(JobSearchCriteria c) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM fabrication_jobs j " +
                "JOIN vehicle_sections vs ON vs.id = j.section_id " +
                "JOIN body_types bt ON bt.id = j.body_type_id " +
                "JOIN fabrication_stages fs ON fs.id = j.current_stage_id WHERE 1 = 1");
        List<Object> params = new ArrayList<>();
        appendCriteria(sql, params, c);
        return jdbc.queryForObject(sql.toString(), Long.class, params.toArray());
    }

    private void appendCriteria(StringBuilder sql, List<Object> params, JobSearchCriteria c) {
        if (c.getSearchText() != null && !c.getSearchText().isBlank()) {
            String like = "%" + c.getSearchText().trim() + "%";
            sql.append(" AND (j.vehicle_id LIKE ? OR j.job_card_no LIKE ? OR j.customer_name_snapshot LIKE ?" +
                    " OR j.customer_reg_snapshot LIKE ? OR j.customer_phone_snapshot LIKE ? OR j.vehicle_model LIKE ?)");
            for (int i = 0; i < 6; i++) params.add(like);
        }
        if (c.getSectionId() != null) {
            sql.append(" AND j.section_id = ?");
            params.add(c.getSectionId());
        }
        if (c.getBodyTypeId() != null) {
            sql.append(" AND j.body_type_id = ?");
            params.add(c.getBodyTypeId());
        }
        if (c.getStageId() != null) {
            sql.append(" AND j.current_stage_id = ?");
            params.add(c.getStageId());
        }
        if (c.getPlannedStartFrom() != null) {
            sql.append(" AND j.planned_start_date >= ?");
            params.add(java.sql.Date.valueOf(c.getPlannedStartFrom()));
        }
        if (c.getPlannedStartTo() != null) {
            sql.append(" AND j.planned_start_date <= ?");
            params.add(java.sql.Date.valueOf(c.getPlannedStartTo()));
        }
        if (c.getJobCardDateFrom() != null) {
            sql.append(" AND j.job_card_date >= ?");
            params.add(java.sql.Date.valueOf(c.getJobCardDateFrom()));
        }
        if (c.getJobCardDateTo() != null) {
            sql.append(" AND j.job_card_date <= ?");
            params.add(java.sql.Date.valueOf(c.getJobCardDateTo()));
        }
        if (Boolean.TRUE.equals(c.getReleasedOnly())) {
            sql.append(" AND fs.code = 'RELEASED'");
        }
        // dueSoon/overdue are computed (Working Days Remaining), applied in the service layer after fetch.
    }

    public Long insert(JobCreateRequest req, Long customerId, int notStartedStageId, Long createdByUserId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement("""
                    INSERT INTO fabrication_jobs
                      (vehicle_id, job_card_no, job_card_date, section_id, body_type_id, vehicle_model,
                       planned_start_date, planned_production_days, planned_finishing_days,
                       current_stage_id, stage_entered_at, contract_amount,
                       customer_id, customer_name_snapshot, customer_reg_snapshot, customer_phone_snapshot, customer_address_snapshot,
                       notes, created_by_user_id)
                    VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, req.getVehicleId());
            ps.setString(2, req.getJobCardNo());
            ps.setDate(3, Date.valueOf(req.getJobCardDate()));
            ps.setInt(4, req.getSectionId());
            ps.setInt(5, req.getBodyTypeId());
            ps.setString(6, req.getVehicleModel());
            ps.setDate(7, Date.valueOf(req.getPlannedStartDate()));
            ps.setInt(8, req.getPlannedProductionDays());
            ps.setInt(9, req.getPlannedFinishingDays());
            ps.setInt(10, notStartedStageId);
            ps.setTimestamp(11, Timestamp.valueOf(LocalDateTime.now()));
            if (req.getContractAmount() != null) {
                ps.setBigDecimal(12, req.getContractAmount());
            } else {
                ps.setNull(12, java.sql.Types.DECIMAL);
            }
            ps.setLong(13, customerId);
            ps.setString(14, req.getCustomerName());
            ps.setString(15, req.getCustomerRegistrationNo());
            ps.setString(16, req.getCustomerPhone());
            ps.setString(17, req.getCustomerAddress());
            ps.setString(18, req.getNotes());
            ps.setLong(19, createdByUserId);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public void update(Long id, JobEditRequest req) {
        jdbc.update("""
                UPDATE fabrication_jobs
                SET body_type_id = ?, vehicle_model = ?, planned_start_date = ?,
                    planned_production_days = ?, planned_finishing_days = ?, contract_amount = ?,
                    customer_name_snapshot = ?, customer_phone_snapshot = ?, customer_address_snapshot = ?,
                    notes = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """,
                req.getBodyTypeId(), req.getVehicleModel(), Date.valueOf(req.getPlannedStartDate()),
                req.getPlannedProductionDays(), req.getPlannedFinishingDays(), req.getContractAmount(),
                req.getCustomerName(), req.getCustomerPhone(), req.getCustomerAddress(),
                req.getNotes(), id);
    }

    /** Called only by StageTransitionService, inside its transaction. */
    public void updateStage(Long jobId, int newStageId, LocalDateTime stageEnteredAt) {
        jdbc.update("UPDATE fabrication_jobs SET current_stage_id = ?, stage_entered_at = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                newStageId, Timestamp.valueOf(stageEnteredAt), jobId);
    }

    public void stampInternalStoreDate(Long jobId, java.time.LocalDate date) {
        jdbc.update("UPDATE fabrication_jobs SET internal_store_date = ? WHERE id = ? AND internal_store_date IS NULL",
                Date.valueOf(date), jobId);
    }

    public void stampReleaseDate(Long jobId, java.time.LocalDate date) {
        jdbc.update("UPDATE fabrication_jobs SET release_date = ? WHERE id = ?", Date.valueOf(date), jobId);
    }

    public boolean existsByVehicleId(String vehicleId) {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM fabrication_jobs WHERE vehicle_id = ?", Long.class, vehicleId);
        return count != null && count > 0;
    }

    public boolean existsByJobCardNo(String jobCardNo) {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM fabrication_jobs WHERE job_card_no = ?", Long.class, jobCardNo);
        return count != null && count > 0;
    }

    /** Live stage counts per section — powers Overview/Operations dashboards (§J). */
    public List<Object[]> stageCountsBySection() {
        return jdbc.query("""
                SELECT vs.code, fs.code, COUNT(*)
                FROM fabrication_jobs j
                JOIN vehicle_sections vs ON vs.id = j.section_id
                JOIN fabrication_stages fs ON fs.id = j.current_stage_id
                GROUP BY vs.code, fs.code
                """, (rs, n) -> new Object[]{rs.getString(1), rs.getString(2), rs.getLong(3)});
    }

    public List<JobDetailDto> findActiveJobsForDueSoonCheck() {
        return jdbc.query(DETAIL_SELECT + " WHERE fs.code IN ('IN_PRODUCTION','AT_FINISHING')", DETAIL_MAPPER);
    }

    /** For WIP/planning: jobs whose planned start predates a date and are not yet released. */
    public long countOpenJobsStartedBefore(int sectionId, java.time.LocalDate before) {
        Long count = jdbc.queryForObject("""
                SELECT COUNT(*) FROM fabrication_jobs j JOIN fabrication_stages fs ON fs.id = j.current_stage_id
                WHERE j.section_id = ? AND j.planned_start_date < ? AND fs.code <> 'RELEASED'
                """, Long.class, sectionId, Date.valueOf(before));
        return count == null ? 0 : count;
    }

    public BigDecimal sumContractAmountBySection(int sectionId) {
        BigDecimal sum = jdbc.queryForObject("SELECT COALESCE(SUM(contract_amount),0) FROM fabrication_jobs WHERE section_id = ?",
                BigDecimal.class, sectionId);
        return sum == null ? BigDecimal.ZERO : sum;
    }
}
