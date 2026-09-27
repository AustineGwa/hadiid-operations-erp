package com.hadiid.erp.planning;

import com.hadiid.erp.common.audit.AuditService;
import com.hadiid.erp.planning.dto.PlanTargetDto;
import com.hadiid.erp.planning.dto.WeeklyBudgetDto;
import com.hadiid.erp.planning.dto.WipRollupDto;
import com.hadiid.erp.reference.ReferenceDataRepository;
import com.hadiid.erp.reference.dto.VehicleSectionDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class PlanningService {

    private final PlanningRepository repository;
    private final ReferenceDataRepository referenceDataRepository;
    private final AuditService auditService;

    public PlanningService(PlanningRepository repository, ReferenceDataRepository referenceDataRepository,
                            AuditService auditService) {
        this.repository = repository;
        this.referenceDataRepository = referenceDataRepository;
        this.auditService = auditService;
    }

    /** §E.8: one row per section for the given month, Planned to Start/Actual Released computed at read time. */
    public List<PlanTargetDto> monthlyPlan(YearMonth month) {
        LocalDate monthStart = month.atDay(1);
        LocalDate monthEndExclusive = month.plusMonths(1).atDay(1);
        return referenceDataRepository.findAllSections().stream()
                .map(section -> {
                    int target = repository.findMonthlyCapacityTarget(section.getId(), monthStart);
                    long plannedToStart = repository.countPlannedToStart(section.getId(), monthStart, monthEndExclusive);
                    long actualReleased = repository.countActualReleased(section.getId(), monthStart, monthEndExclusive);
                    return PlanTargetDto.builder()
                            .sectionId(section.getId())
                            .sectionLabel(section.getLabel())
                            .planMonth(monthStart)
                            .monthlyCapacityTarget(target)
                            .plannedToStart(plannedToStart)
                            .actualReleased(actualReleased)
                            .variance(actualReleased - target)
                            .build();
                })
                .toList();
    }

    @Transactional
    public void setMonthlyCapacityTarget(int sectionId, YearMonth month, int target, Long actingUserId) {
        repository.upsertMonthlyCapacityTarget(sectionId, month.atDay(1), target);
        auditService.record(actingUserId, "PLAN_TARGET_SET", "PRODUCTION_PLAN_TARGET", null,
                null, sectionId + "/" + month + "=" + target, null);
    }

    /** §E.8: one row per section for the ISO week starting on weekStart (must be a Monday). */
    public List<WeeklyBudgetDto> weeklyBudgets(LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        return referenceDataRepository.findAllSections().stream()
                .map(section -> {
                    var stored = repository.findWeeklyBudget(section.getId(), weekStart)
                            .orElse(new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO});
                    BigDecimal laborActual = repository.sumLaborActualForWeek(section.getId(), weekStart, weekEnd);
                    return WeeklyBudgetDto.builder()
                            .sectionId(section.getId())
                            .sectionLabel(section.getLabel())
                            .weekStart(weekStart)
                            .weekEnd(weekEnd)
                            .materialsBudget(stored[0])
                            .materialsActual(stored[1])
                            .laborBudget(stored[2])
                            .laborActual(laborActual)
                            .build();
                })
                .toList();
    }

    @Transactional
    public void setWeeklyBudget(int sectionId, LocalDate weekStart, BigDecimal materialsBudget,
                                 BigDecimal materialsActual, BigDecimal laborBudget, Long actingUserId) {
        repository.upsertWeeklyBudget(sectionId, weekStart, weekStart.plusDays(6), materialsBudget, materialsActual, laborBudget);
        auditService.record(actingUserId, "WEEKLY_BUDGET_SET", "WEEKLY_BUDGET_ENTRY", null,
                null, sectionId + "/" + weekStart, null);
    }

    /** §E.8: WIP rollup, one row per section, as of today. */
    public List<WipRollupDto> wipRollup() {
        LocalDate today = LocalDate.now();
        return referenceDataRepository.findAllSections().stream()
                .map(section -> WipRollupDto.builder()
                        .sectionId(section.getId())
                        .sectionLabel(section.getLabel())
                        .wipCount(repository.countWipOpen(section.getId(), today))
                        .build())
                .toList();
    }

    public List<VehicleSectionDto> sections() {
        return referenceDataRepository.findAllSections();
    }
}
