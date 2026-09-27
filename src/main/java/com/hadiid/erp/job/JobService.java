package com.hadiid.erp.job;

import com.hadiid.erp.common.audit.AuditService;
import com.hadiid.erp.common.exception.BusinessRuleException;
import com.hadiid.erp.common.exception.NotFoundException;
import com.hadiid.erp.common.web.PageResult;
import com.hadiid.erp.customer.CustomerService;
import com.hadiid.erp.job.calc.DurationCalculator;
import com.hadiid.erp.job.calc.FinishingProgressCalculator;
import com.hadiid.erp.job.calc.ProductionProgressCalculator;
import com.hadiid.erp.job.calc.WorkingDaysRemainingCalculator;
import com.hadiid.erp.job.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final CustomerService customerService;
    private final AuditService auditService;
    private final ProductionProgressCalculator productionProgressCalculator;
    private final FinishingProgressCalculator finishingProgressCalculator;
    private final WorkingDaysRemainingCalculator workingDaysRemainingCalculator;
    private final DurationCalculator durationCalculator;

    public JobService(JobRepository jobRepository, CustomerService customerService, AuditService auditService,
                       ProductionProgressCalculator productionProgressCalculator,
                       FinishingProgressCalculator finishingProgressCalculator,
                       WorkingDaysRemainingCalculator workingDaysRemainingCalculator,
                       DurationCalculator durationCalculator) {
        this.jobRepository = jobRepository;
        this.customerService = customerService;
        this.auditService = auditService;
        this.productionProgressCalculator = productionProgressCalculator;
        this.finishingProgressCalculator = finishingProgressCalculator;
        this.workingDaysRemainingCalculator = workingDaysRemainingCalculator;
        this.durationCalculator = durationCalculator;
    }

    /** Applies the §C calculators onto a freshly-loaded detail DTO. Never persisted. */
    private JobDetailDto withComputedFields(JobDetailDto j) {
        LocalDate today = LocalDate.now();
        j.setProductionPercent(productionProgressCalculator.calculate(
                j.getStageCode(), j.getStageEnteredAt(), j.getPlannedProductionDays(), today));
        j.setFinishingPercent(finishingProgressCalculator.calculate(
                j.getStageCode(), j.getStageEnteredAt(), j.getPlannedFinishingDays(), today));
        j.setWorkingDaysRemaining(workingDaysRemainingCalculator.calculate(
                j.getStageCode(), j.getStageEnteredAt(), j.getPlannedProductionDays(), j.getPlannedFinishingDays(), today));
        j.setTotalFabricationDays(durationCalculator.totalFabricationDays(j.getPlannedStartDate(), j.getInternalStoreDate()));
        j.setTotalTurnaroundDays(durationCalculator.totalTurnaroundDays(j.getPlannedStartDate(), j.getReleaseDate()));
        return j;
    }

    public JobDetailDto getByIdOrThrow(Long id) {
        JobDetailDto job = jobRepository.findById(id).orElseThrow(() -> new NotFoundException("Job not found: " + id));
        return withComputedFields(job);
    }

    public JobDetailDto getByVehicleIdOrThrow(String vehicleId) {
        JobDetailDto job = jobRepository.findByVehicleId(vehicleId)
                .orElseThrow(() -> new NotFoundException("Vehicle not found: " + vehicleId));
        return withComputedFields(job);
    }

    public PageResult<JobSummaryDto> search(JobSearchCriteria criteria, int page, int pageSize) {
        List<JobDetailDto> detailRows = jobRepository.search(criteria, pageSize, page * pageSize);
        List<JobSummaryDto> summaries = detailRows.stream()
                .map(this::withComputedFields)
                .map(this::toSummary)
                .filter(s -> matchesDueSoonOrOverdue(s, criteria))
                .toList();
        long total = jobRepository.countSearch(criteria);
        return new PageResult<>(summaries, page, pageSize, total);
    }

    private boolean matchesDueSoonOrOverdue(JobSummaryDto s, JobSearchCriteria c) {
        if (Boolean.TRUE.equals(c.getDueSoon()) && !s.isDueSoon()) return false;
        if (Boolean.TRUE.equals(c.getOverdue()) && !s.isOverdue()) return false;
        return true;
    }

    private JobSummaryDto toSummary(JobDetailDto j) {
        boolean activeStage = "IN_PRODUCTION".equals(j.getStageCode()) || "AT_FINISHING".equals(j.getStageCode());
        boolean dueSoon = activeStage && j.getWorkingDaysRemaining() != null && j.getWorkingDaysRemaining() <= 2;
        // Overdue (§K — not explicit in Excel, flagged as a business decision to confirm exact
        // definition): here, an active job whose working-days countdown has already hit zero.
        boolean overdue = activeStage && j.getWorkingDaysRemaining() != null && j.getWorkingDaysRemaining() == 0;
        return JobSummaryDto.builder()
                .id(j.getId())
                .vehicleId(j.getVehicleId())
                .jobCardNo(j.getJobCardNo())
                .sectionCode(j.getSectionCode())
                .sectionLabel(j.getSectionLabel())
                .bodyTypeLabel(j.getBodyTypeLabel())
                .vehicleModel(j.getVehicleModel())
                .stageCode(j.getStageCode())
                .stageLabel(j.getStageLabel())
                .plannedStartDate(j.getPlannedStartDate())
                .jobCardDate(j.getJobCardDate())
                .customerName(j.getCustomerNameSnapshot())
                .contractAmount(j.getContractAmount())
                .productionPercent(j.getProductionPercent())
                .finishingPercent(j.getFinishingPercent())
                .workingDaysRemaining(j.getWorkingDaysRemaining())
                .dueSoon(dueSoon)
                .overdue(overdue)
                .build();
    }

    @Transactional
    public Long create(JobCreateRequest req, Long createdByUserId, int notStartedStageId) {
        if (jobRepository.existsByVehicleId(req.getVehicleId())) {
            throw new BusinessRuleException("Vehicle ID already in use: " + req.getVehicleId());
        }
        if (jobRepository.existsByJobCardNo(req.getJobCardNo())) {
            throw new BusinessRuleException("Job Card No. already in use: " + req.getJobCardNo());
        }
        Long customerId = req.getCustomerId() != null
                ? req.getCustomerId()
                : customerService.findOrCreate(req.getCustomerName(), req.getCustomerRegistrationNo(),
                        req.getCustomerPhone(), req.getCustomerAddress());
        Long jobId = jobRepository.insert(req, customerId, notStartedStageId, createdByUserId);
        auditService.record(createdByUserId, "JOB_CREATE", "FABRICATION_JOB", jobId, null,
                req.getVehicleId() + " / " + req.getJobCardNo(), null);
        return jobId;
    }

    @Transactional
    public void edit(Long id, JobEditRequest req, Long actingUserId) {
        JobDetailDto before = getByIdOrThrow(id);
        jobRepository.update(id, req);
        auditService.record(actingUserId, "JOB_EDIT", "FABRICATION_JOB", id,
                before.getVehicleModel() + " / " + before.getPlannedStartDate(),
                req.getVehicleModel() + " / " + req.getPlannedStartDate(), null);
    }
}
