package com.hadiid.erp.labor;

import com.hadiid.erp.labor.dto.AssignmentDto;
import com.hadiid.erp.labor.dto.ContractorDto;
import com.hadiid.erp.labor.dto.PaymentDto;
import com.hadiid.erp.labor.dto.PaymentSummaryDto;
import com.hadiid.erp.labor.dto.WorkerDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

/**
 * §E.6 (Labor & Payments): explicit SQL, no ORM. The two "amount paid so far"
 * figures in this module (worker-level in AssignmentDto, job-card-level in
 * PaymentSummaryDto) are always derived here from labor_payments — never
 * stored as a mutable column — per Rule 17 and the Javadoc on those DTOs.
 */
@Repository
public class LaborRepository {

    private final JdbcTemplate jdbc;

    public LaborRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ---------- Contractors ----------

    private static final RowMapper<ContractorDto> CONTRACTOR_MAPPER = (rs, n) -> ContractorDto.builder()
            .id(rs.getLong("id"))
            .name(rs.getString("name"))
            .registrationNo(rs.getString("registration_no"))
            .phone(rs.getString("phone"))
            .build();

    public List<ContractorDto> findAllContractors() {
        return jdbc.query("SELECT id, name, registration_no, phone FROM contractors ORDER BY name", CONTRACTOR_MAPPER);
    }

    public Optional<ContractorDto> findContractorById(Long id) {
        return jdbc.query("SELECT id, name, registration_no, phone FROM contractors WHERE id = ?", CONTRACTOR_MAPPER, id)
                .stream().findFirst();
    }

    public Long insertContractor(String name, String registrationNo, String phone) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO contractors (name, registration_no, phone) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            ps.setString(2, registrationNo);
            ps.setString(3, phone);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    // ---------- Workers ----------

    private static final RowMapper<WorkerDto> WORKER_MAPPER = (rs, n) -> WorkerDto.builder()
            .id(rs.getLong("id"))
            .name(rs.getString("name"))
            .idNumber(rs.getString("id_number"))
            .phone(rs.getString("phone"))
            .contractorId(rs.getObject("contractor_id") == null ? null : rs.getLong("contractor_id"))
            .contractorName(rs.getString("contractor_name"))
            .build();

    private static final String WORKER_SELECT = """
            SELECT w.id, w.name, w.id_number, w.phone, w.contractor_id, c.name AS contractor_name
            FROM workers w
            LEFT JOIN contractors c ON c.id = w.contractor_id
            """;

    public List<WorkerDto> findAllWorkers() {
        return jdbc.query(WORKER_SELECT + " ORDER BY w.name", WORKER_MAPPER);
    }

    public Optional<WorkerDto> findWorkerById(Long id) {
        return jdbc.query(WORKER_SELECT + " WHERE w.id = ?", WORKER_MAPPER, id).stream().findFirst();
    }

    public List<WorkerDto> searchWorkers(String query) {
        String like = "%" + (query == null ? "" : query.trim()) + "%";
        return jdbc.query(WORKER_SELECT + " WHERE w.name LIKE ? OR w.id_number LIKE ? ORDER BY w.name",
                WORKER_MAPPER, like, like);
    }

    public Long insertWorker(String name, String idNumber, String phone, Long contractorId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO workers (name, id_number, phone, contractor_id) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            ps.setString(2, idNumber);
            ps.setString(3, phone);
            if (contractorId == null) {
                ps.setNull(4, java.sql.Types.BIGINT);
            } else {
                ps.setLong(4, contractorId);
            }
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    // ---------- Assignments ----------

    private static final RowMapper<AssignmentDto> ASSIGNMENT_MAPPER = (rs, n) -> {
        BigDecimal agreed = rs.getBigDecimal("agreed_amount");
        BigDecimal paid = rs.getBigDecimal("amount_paid");
        if (paid == null) paid = BigDecimal.ZERO;
        BigDecimal balance = agreed.subtract(paid);
        if (balance.signum() < 0) balance = BigDecimal.ZERO;
        return AssignmentDto.builder()
                .id(rs.getLong("id"))
                .jobId(rs.getLong("job_id"))
                .vehicleId(rs.getString("vehicle_id"))
                .stageCode(rs.getString("stage_code"))
                .stageLabel(rs.getString("stage_label"))
                .workerId(rs.getLong("worker_id"))
                .workerName(rs.getString("worker_name"))
                .workerIdNumber(rs.getString("worker_id_number"))
                .contractorName(rs.getString("contractor_name"))
                .agreedAmount(agreed)
                .amountPaid(paid)
                .balance(balance)
                .build();
    };

    private static final String ASSIGNMENT_SELECT = """
            SELECT a.id, a.job_id, j.vehicle_id, s.code AS stage_code, s.label AS stage_label,
                   w.id AS worker_id, w.name AS worker_name, w.id_number AS worker_id_number,
                   c.name AS contractor_name, a.agreed_amount,
                   (SELECT COALESCE(SUM(p.amount_paid), 0) FROM labor_payments p WHERE p.assignment_id = a.id) AS amount_paid
            FROM job_worker_assignments a
            JOIN fabrication_jobs j ON j.id = a.job_id
            JOIN fabrication_stages s ON s.id = a.stage_id
            JOIN workers w ON w.id = a.worker_id
            LEFT JOIN contractors c ON c.id = w.contractor_id
            """;

    public List<AssignmentDto> findAssignmentsByJob(Long jobId) {
        return jdbc.query(ASSIGNMENT_SELECT + " WHERE a.job_id = ? ORDER BY s.sequence_order, w.name",
                ASSIGNMENT_MAPPER, jobId);
    }

    public Optional<AssignmentDto> findAssignmentById(Long id) {
        return jdbc.query(ASSIGNMENT_SELECT + " WHERE a.id = ?", ASSIGNMENT_MAPPER, id).stream().findFirst();
    }

    public boolean assignmentExists(Long jobId, int stageId, Long workerId) {
        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM job_worker_assignments WHERE job_id = ? AND stage_id = ? AND worker_id = ?",
                Long.class, jobId, stageId, workerId);
        return count != null && count > 0;
    }

    public Long insertAssignment(Long jobId, int stageId, Long workerId, BigDecimal agreedAmount) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO job_worker_assignments (job_id, stage_id, worker_id, agreed_amount) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, jobId);
            ps.setInt(2, stageId);
            ps.setLong(3, workerId);
            ps.setBigDecimal(4, agreedAmount);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    // ---------- Payments ----------

    private static final RowMapper<PaymentDto> PAYMENT_MAPPER = (rs, n) -> PaymentDto.builder()
            .id(rs.getLong("id"))
            .assignmentId(rs.getLong("assignment_id"))
            .workerName(rs.getString("worker_name"))
            .amountPaid(rs.getBigDecimal("amount_paid"))
            .datePaid(rs.getObject("date_paid", java.time.LocalDate.class))
            .paymentMethodLabel(rs.getString("payment_method_label"))
            .paidByFullName(rs.getString("paid_by_full_name"))
            .referenceNote(rs.getString("reference_note"))
            .build();

    private static final String PAYMENT_SELECT = """
            SELECT p.id, p.assignment_id, w.name AS worker_name, p.amount_paid, p.date_paid,
                   pm.label AS payment_method_label, u.full_name AS paid_by_full_name, p.reference_note
            FROM labor_payments p
            JOIN job_worker_assignments a ON a.id = p.assignment_id
            JOIN workers w ON w.id = a.worker_id
            JOIN payment_methods pm ON pm.id = p.payment_method_id
            JOIN users u ON u.id = p.paid_by_user_id
            """;

    public List<PaymentDto> findPaymentsByAssignment(Long assignmentId) {
        return jdbc.query(PAYMENT_SELECT + " WHERE p.assignment_id = ? ORDER BY p.date_paid DESC, p.id DESC",
                PAYMENT_MAPPER, assignmentId);
    }

    public List<PaymentDto> findPaymentsByJob(Long jobId) {
        return jdbc.query(PAYMENT_SELECT + " WHERE a.job_id = ? ORDER BY p.date_paid DESC, p.id DESC",
                PAYMENT_MAPPER, jobId);
    }

    public Long insertPayment(Long assignmentId, BigDecimal amountPaid, java.time.LocalDate datePaid,
                               int paymentMethodId, Long paidByUserId, String referenceNote) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement("""
                    INSERT INTO labor_payments (assignment_id, amount_paid, date_paid, payment_method_id, paid_by_user_id, reference_note)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, assignmentId);
            ps.setBigDecimal(2, amountPaid);
            ps.setObject(3, datePaid);
            ps.setInt(4, paymentMethodId);
            ps.setLong(5, paidByUserId);
            ps.setString(6, referenceNote);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    // ---------- Payment summary (§B.5 / §C job-card rollup) ----------

    private static final RowMapper<PaymentSummaryDto> SUMMARY_MAPPER = (rs, n) -> {
        BigDecimal contract = rs.getBigDecimal("contract_amount");
        BigDecimal paid = rs.getBigDecimal("total_paid");
        if (paid == null) paid = BigDecimal.ZERO;
        BigDecimal wagesAgreed = rs.getBigDecimal("total_worker_wages_agreed");
        if (wagesAgreed == null) wagesAgreed = BigDecimal.ZERO;
        BigDecimal balance = null;
        Double percentPaid = null;
        if (contract != null) {
            balance = contract.subtract(paid);
            if (contract.signum() > 0) {
                percentPaid = paid.doubleValue() / contract.doubleValue();
            }
        }
        return PaymentSummaryDto.builder()
                .jobCardNo(rs.getString("job_card_no"))
                .sectionLabel(rs.getString("section_label"))
                .vehicleId(rs.getString("vehicle_id"))
                .contractAmount(contract)
                .totalWorkerWagesAgreed(wagesAgreed)
                .totalPaid(paid)
                .balance(balance)
                .percentPaid(percentPaid)
                .build();
    };

    private static final String SUMMARY_SELECT = """
            SELECT j.id AS job_id, j.job_card_no, j.vehicle_id, sec.label AS section_label, j.contract_amount,
                   (SELECT COALESCE(SUM(a.agreed_amount), 0) FROM job_worker_assignments a WHERE a.job_id = j.id) AS total_worker_wages_agreed,
                   (SELECT COALESCE(SUM(p.amount_paid), 0) FROM labor_payments p
                      JOIN job_worker_assignments a2 ON a2.id = p.assignment_id WHERE a2.job_id = j.id) AS total_paid
            FROM fabrication_jobs j
            JOIN vehicle_sections sec ON sec.id = j.section_id
            """;

    public Optional<PaymentSummaryDto> paymentSummaryForJob(Long jobId) {
        return jdbc.query(SUMMARY_SELECT + " WHERE j.id = ?", SUMMARY_MAPPER, jobId).stream().findFirst();
    }

    /** §J finance dashboard: one summary row per job, optionally filtered by section. */
    public List<PaymentSummaryDto> paymentSummaryBySection(Integer sectionId) {
        if (sectionId == null) {
            return jdbc.query(SUMMARY_SELECT + " ORDER BY sec.label, j.job_card_no", SUMMARY_MAPPER);
        }
        return jdbc.query(SUMMARY_SELECT + " WHERE j.section_id = ? ORDER BY j.job_card_no", SUMMARY_MAPPER, sectionId);
    }
}
