package com.hadiid.erp.reference;

import com.hadiid.erp.reference.dto.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * All reference/master data reads and writes: vehicle sections, body types,
 * fabrication stages, stage-transition rules, payment methods, and the
 * production calendar (Rule 8, Rule 18 — configurable without code changes).
 */
@Repository
public class ReferenceDataRepository {

    private final JdbcTemplate jdbc;

    public ReferenceDataRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<VehicleSectionDto> SECTION_MAPPER = (rs, n) -> VehicleSectionDto.builder()
            .id(rs.getInt("id")).code(rs.getString("code")).label(rs.getString("label")).build();

    private static final RowMapper<BodyTypeDto> BODY_TYPE_MAPPER = (rs, n) -> BodyTypeDto.builder()
            .id(rs.getInt("id")).sectionId(rs.getInt("section_id"))
            .sectionCode(rs.getString("section_code"))
            .code(rs.getString("code")).label(rs.getString("label"))
            .active(rs.getBoolean("is_active")).build();

    private static final RowMapper<FabricationStageDto> STAGE_MAPPER = (rs, n) -> FabricationStageDto.builder()
            .id(rs.getInt("id")).code(rs.getString("code")).label(rs.getString("label"))
            .sequenceOrder(rs.getInt("sequence_order")).build();

    private static final RowMapper<PaymentMethodDto> PAYMENT_METHOD_MAPPER = (rs, n) -> PaymentMethodDto.builder()
            .id(rs.getInt("id")).code(rs.getString("code")).label(rs.getString("label"))
            .active(rs.getBoolean("is_active")).build();

    private static final RowMapper<StageTransitionRuleDto> TRANSITION_MAPPER = (rs, n) -> StageTransitionRuleDto.builder()
            .fromStageId(rs.getObject("from_stage_id") != null ? rs.getInt("from_stage_id") : null)
            .toStageId(rs.getInt("to_stage_id"))
            .requiresPermissionCode(rs.getString("requires_permission_code"))
            .requiresReason(rs.getBoolean("requires_reason"))
            .build();

    public List<VehicleSectionDto> findAllSections() {
        return jdbc.query("SELECT id, code, label FROM vehicle_sections ORDER BY id", SECTION_MAPPER);
    }

    public List<BodyTypeDto> findAllBodyTypes() {
        return jdbc.query("""
                SELECT bt.id, bt.section_id, vs.code AS section_code, bt.code, bt.label, bt.is_active
                FROM body_types bt JOIN vehicle_sections vs ON vs.id = bt.section_id
                ORDER BY bt.section_id, bt.label
                """, BODY_TYPE_MAPPER);
    }

    public List<BodyTypeDto> findActiveBodyTypesForSection(int sectionId) {
        return jdbc.query("""
                SELECT bt.id, bt.section_id, vs.code AS section_code, bt.code, bt.label, bt.is_active
                FROM body_types bt JOIN vehicle_sections vs ON vs.id = bt.section_id
                WHERE bt.section_id = ? AND bt.is_active = TRUE
                ORDER BY bt.label
                """, BODY_TYPE_MAPPER, sectionId);
    }

    public List<FabricationStageDto> findAllStages() {
        return jdbc.query("SELECT id, code, label, sequence_order FROM fabrication_stages ORDER BY sequence_order", STAGE_MAPPER);
    }

    public Optional<FabricationStageDto> findStageById(int id) {
        return jdbc.query("SELECT id, code, label, sequence_order FROM fabrication_stages WHERE id = ?", STAGE_MAPPER, id)
                .stream().findFirst();
    }

    public Optional<FabricationStageDto> findStageByCode(String code) {
        return jdbc.query("SELECT id, code, label, sequence_order FROM fabrication_stages WHERE code = ?", STAGE_MAPPER, code)
                .stream().findFirst();
    }

    public List<PaymentMethodDto> findAllPaymentMethods() {
        return jdbc.query("SELECT id, code, label, is_active FROM payment_methods ORDER BY label", PAYMENT_METHOD_MAPPER);
    }

    public List<StageTransitionRuleDto> findAllTransitionRules() {
        return jdbc.query("""
                SELECT from_stage_id, to_stage_id, requires_permission_code, requires_reason
                FROM stage_transitions
                """, TRANSITION_MAPPER);
    }

    /** Looks up the rule for one specific move, if it is allowed at all (§H.1). */
    public Optional<StageTransitionRuleDto> findTransitionRule(Integer fromStageId, int toStageId) {
        String sql = fromStageId == null
                ? "SELECT from_stage_id, to_stage_id, requires_permission_code, requires_reason FROM stage_transitions WHERE from_stage_id IS NULL AND to_stage_id = ?"
                : "SELECT from_stage_id, to_stage_id, requires_permission_code, requires_reason FROM stage_transitions WHERE from_stage_id = ? AND to_stage_id = ?";
        List<StageTransitionRuleDto> rows = fromStageId == null
                ? jdbc.query(sql, TRANSITION_MAPPER, toStageId)
                : jdbc.query(sql, TRANSITION_MAPPER, fromStageId, toStageId);
        return rows.stream().findFirst();
    }

    public void addBodyType(int sectionId, String code, String label) {
        jdbc.update("INSERT INTO body_types (section_id, code, label) VALUES (?, ?, ?)", sectionId, code, label);
    }

    public void setBodyTypeActive(int id, boolean active) {
        jdbc.update("UPDATE body_types SET is_active = ? WHERE id = ?", active, id);
    }

    public Optional<CalendarDayDto> findCalendarDay(LocalDate date) {
        return jdbc.query("SELECT calendar_date, status, note FROM production_calendar WHERE calendar_date = ?",
                (rs, n) -> CalendarDayDto.builder()
                        .date(rs.getDate("calendar_date").toLocalDate())
                        .status(rs.getString("status"))
                        .note(rs.getString("note"))
                        .build(), date).stream().findFirst();
    }

    public List<CalendarDayDto> findCalendarRange(LocalDate from, LocalDate to) {
        return jdbc.query("""
                SELECT calendar_date, status, note FROM production_calendar
                WHERE calendar_date BETWEEN ? AND ? ORDER BY calendar_date
                """, (rs, n) -> CalendarDayDto.builder()
                .date(rs.getDate("calendar_date").toLocalDate())
                .status(rs.getString("status"))
                .note(rs.getString("note"))
                .build(), from, to);
    }

    public void upsertCalendarDay(LocalDate date, String status, String note, Long setByUserId) {
        int updated = jdbc.update("""
                UPDATE production_calendar SET status = ?, note = ?, set_by_user_id = ?, updated_at = CURRENT_TIMESTAMP
                WHERE calendar_date = ?
                """, status, note, setByUserId, date);
        if (updated == 0) {
            jdbc.update("""
                    INSERT INTO production_calendar (calendar_date, status, note, set_by_user_id)
                    VALUES (?, ?, ?, ?)
                    """, date, status, note, setByUserId);
        }
    }
}
