package com.hadiid.erp.reporting;

import com.hadiid.erp.job.JobRepository;
import com.hadiid.erp.job.JobService;
import com.hadiid.erp.job.StageHistoryRepository;
import com.hadiid.erp.job.dto.JobSearchCriteria;
import com.hadiid.erp.labor.LaborRepository;
import com.hadiid.erp.labor.dto.PaymentSummaryDto;
import com.hadiid.erp.reporting.dto.FinanceSummaryDto;
import com.hadiid.erp.reporting.dto.OverviewSummaryDto;
import com.hadiid.erp.reporting.dto.StageArrivalDto;
import com.hadiid.erp.reporting.dto.StageCountDto;
import com.hadiid.erp.reporting.dto.StageDwellDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * §J/§K Reporting & Dashboards: every figure here is computed at read time
 * from the operational tables (fabrication_jobs, fabrication_stage_history,
 * labor_payments) — no report or snapshot table exists (Rule 17).
 */
@Service
public class ReportingService {

    private final JobRepository jobRepository;
    private final JobService jobService;
    private final StageHistoryRepository stageHistoryRepository;
    private final LaborRepository laborRepository;

    public ReportingService(JobRepository jobRepository, JobService jobService,
                             StageHistoryRepository stageHistoryRepository, LaborRepository laborRepository) {
        this.jobRepository = jobRepository;
        this.jobService = jobService;
        this.stageHistoryRepository = stageHistoryRepository;
        this.laborRepository = laborRepository;
    }

    /** §J Overview dashboard KPI strip. */
    public OverviewSummaryDto overviewSummary() {
        long total = jobService.search(JobSearchCriteria.builder().build(), 0, 1).getTotalCount();
        long released = jobService.search(JobSearchCriteria.builder().releasedOnly(true).build(), 0, 1).getTotalCount();
        long dueSoon = jobService.search(JobSearchCriteria.builder().dueSoon(true).build(), 0, 500).getItems().size();
        long overdue = jobService.search(JobSearchCriteria.builder().overdue(true).build(), 0, 500).getItems().size();
        return OverviewSummaryDto.builder()
                .totalJobs(total)
                .releasedJobs(released)
                .dueSoonCount(dueSoon)
                .overdueCount(overdue)
                .build();
    }

    /** §J Overview/Operations dashboard: live WIP counts per (section, stage). */
    public List<StageCountDto> stageCounts() {
        return jobRepository.stageCountsBySection().stream()
                .map(row -> StageCountDto.builder()
                        .sectionCode((String) row[0])
                        .stageCode((String) row[1])
                        .count((Long) row[2])
                        .build())
                .toList();
    }

    /** §J Finance dashboard: one rollup row per section, against Contract Amount (§B.5/§C). */
    public List<FinanceSummaryDto> financeSummaryBySection() {
        List<PaymentSummaryDto> rows = laborRepository.paymentSummaryBySection(null);
        Map<String, List<PaymentSummaryDto>> bySection = rows.stream()
                .collect(Collectors.groupingBy(PaymentSummaryDto::getSectionLabel, LinkedHashMapCollector.supplier(), Collectors.toList()));
        return bySection.entrySet().stream()
                .map(e -> {
                    BigDecimal totalContract = e.getValue().stream()
                            .map(r -> r.getContractAmount() == null ? BigDecimal.ZERO : r.getContractAmount())
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal totalPaid = e.getValue().stream()
                            .map(PaymentSummaryDto::getTotalPaid)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return FinanceSummaryDto.builder()
                            .sectionLabel(e.getKey())
                            .totalContractAmount(totalContract)
                            .totalPaid(totalPaid)
                            .totalBalance(totalContract.subtract(totalPaid))
                            .jobCount(e.getValue().size())
                            .build();
                })
                .sorted(Comparator.comparing(FinanceSummaryDto::getSectionLabel))
                .toList();
    }

    /** §K Stage Movement Analysis: average/max dwell time per stage, optionally filtered by section. */
    public List<StageDwellDto> stageDwell(Integer sectionId) {
        return stageHistoryRepository.dwellStatsByFromStage(sectionId).stream()
                .map(row -> StageDwellDto.builder()
                        .fromStageLabel((String) row[0])
                        .avgDaysHeld((Double) row[1])
                        .maxDaysHeld((Integer) row[2])
                        .transitionCount((Long) row[3])
                        .build())
                .toList();
    }

    /** §K: how many times jobs have arrived at each stage (forward + corrections combined). */
    public List<StageArrivalDto> stageArrivals() {
        return stageHistoryRepository.arrivalsByToStage().stream()
                .map(row -> StageArrivalDto.builder()
                        .toStageLabel((String) row[0])
                        .arrivalCount((Long) row[1])
                        .build())
                .toList();
    }

    /** Small helper so groupingBy preserves a stable, section-ordered map without extra dependencies. */
    private static final class LinkedHashMapCollector {
        static java.util.function.Supplier<Map<String, List<PaymentSummaryDto>>> supplier() {
            return java.util.LinkedHashMap::new;
        }
    }
}
