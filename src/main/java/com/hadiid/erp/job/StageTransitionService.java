package com.hadiid.erp.job;

import com.hadiid.erp.common.audit.AuditService;
import com.hadiid.erp.common.exception.BusinessRuleException;
import com.hadiid.erp.common.exception.NotFoundException;
import com.hadiid.erp.reference.ReferenceDataRepository;
import com.hadiid.erp.reference.dto.FabricationStageDto;
import com.hadiid.erp.reference.dto.StageTransitionRuleDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Replaces the workbook's Apps Script (advanceStage / onEdit / stampStageDate,
 * §B.15). Implements this spec's §H.1 working assumption ("Option B"): a plain
 * forward-only advance() for everyday use, and an audited correct() for anything
 * else (backward, skip, or reopen), gated by the stage_transitions reference
 * table so the actual policy lives in data, not code — see §H.1 before relying
 * on this in production; Hadiid has not yet confirmed which option to use.
 *
 * Every transition — advance or correct — does all of the following in one
 * transaction: update fabrication_jobs.current_stage_id/stage_entered_at,
 * insert exactly one fabrication_stage_history row, and stamp
 * internal_store_date/release_date where applicable (Rule 10, Rule 12).
 */
@Service
public class StageTransitionService {

    private final JobRepository jobRepository;
    private final StageHistoryRepository stageHistoryRepository;
    private final ReferenceDataRepository referenceDataRepository;
    private final AuditService auditService;

    public StageTransitionService(JobRepository jobRepository, StageHistoryRepository stageHistoryRepository,
                                   ReferenceDataRepository referenceDataRepository, AuditService auditService) {
        this.jobRepository = jobRepository;
        this.stageHistoryRepository = stageHistoryRepository;
        this.referenceDataRepository = referenceDataRepository;
        this.auditService = auditService;
    }

    /** Forward-only, one step at a time (mirrors the workbook's "Advance Stage" button). */
    @Transactional
    public void advance(Long jobId, Long actingUserId) {
        var job = jobRepository.findById(jobId).orElseThrow(() -> new NotFoundException("Job not found: " + jobId));
        FabricationStageDto currentStage = referenceDataRepository.findStageByCode(job.getStageCode())
                .orElseThrow(() -> new BusinessRuleException("Unknown current stage"));
        FabricationStageDto nextStage = referenceDataRepository.findAllStages().stream()
                .filter(s -> s.getSequenceOrder() == currentStage.getSequenceOrder() + 1)
                .findFirst()
                .orElseThrow(() -> new BusinessRuleException(
                        job.getVehicleId() + " has already been released to the customer."));

        StageTransitionRuleDto rule = referenceDataRepository
                .findTransitionRule(currentStage.getId(), nextStage.getId())
                .orElseThrow(() -> new BusinessRuleException("This stage move is not configured as allowed."));

        applyTransition(job.getId(), job.getVehicleId(), currentStage, nextStage,
                actingUserId, false, null, rule);
    }

    /**
     * Backward move, skip, or any other non-adjacent transition. Always requires
     * a reason (Rule 11) and always writes to the audit_logs table in addition
     * to the stage-history row (Rule 33 — the two logs are conceptually distinct).
     * Requires JOB_CORRECT_STAGE at the controller/method-security layer.
     */
    @Transactional
    public void correct(Long jobId, int targetStageId, String reason, Long actingUserId) {
        if (reason == null || reason.isBlank()) {
            throw new BusinessRuleException("A reason is required to correct a job's stage.");
        }
        var job = jobRepository.findById(jobId).orElseThrow(() -> new NotFoundException("Job not found: " + jobId));
        FabricationStageDto currentStage = referenceDataRepository.findStageByCode(job.getStageCode())
                .orElseThrow(() -> new BusinessRuleException("Unknown current stage"));
        FabricationStageDto targetStage = referenceDataRepository.findStageById(targetStageId)
                .orElseThrow(() -> new NotFoundException("Unknown target stage: " + targetStageId));

        StageTransitionRuleDto rule = referenceDataRepository
                .findTransitionRule(currentStage.getId(), targetStage.getId())
                .orElseThrow(() -> new BusinessRuleException(
                        "Moving " + job.getVehicleId() + " from " + currentStage.getLabel() +
                        " to " + targetStage.getLabel() + " is not a permitted correction."));

        applyTransition(job.getId(), job.getVehicleId(), currentStage, targetStage,
                actingUserId, true, reason, rule);

        auditService.record(actingUserId, "JOB_CORRECT_STAGE", "FABRICATION_JOB", job.getId(),
                currentStage.getLabel(), targetStage.getLabel(), reason);
    }

    private void applyTransition(Long jobId, String vehicleId, FabricationStageDto fromStage, FabricationStageDto toStage,
                                  Long actingUserId, boolean isCorrection, String note, StageTransitionRuleDto rule) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime heldSince = stageHistoryRepository.findLastChangedAt(jobId).orElse(now);
        int daysHeld = (int) ChronoUnit.DAYS.between(heldSince.toLocalDate(), now.toLocalDate());

        stageHistoryRepository.insert(jobId, fromStage.getId(), toStage.getId(), now, Math.max(0, daysHeld),
                actingUserId, isCorrection, note);
        jobRepository.updateStage(jobId, toStage.getId(), now);

        LocalDate today = now.toLocalDate();
        if ("AT_DELIVERY_STORE".equals(toStage.getCode())) {
            jobRepository.stampInternalStoreDate(jobId, today);
        }
        if ("RELEASED".equals(toStage.getCode())) {
            // Backfill Internal Store Date if the job skipped straight to Released (matches the
            // Apps Script's stampStageDate behavior, §B.15).
            jobRepository.stampInternalStoreDate(jobId, today);
            jobRepository.stampReleaseDate(jobId, today);
        }
    }
}
