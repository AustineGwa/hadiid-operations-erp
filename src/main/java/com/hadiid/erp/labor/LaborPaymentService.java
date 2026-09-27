package com.hadiid.erp.labor;

import com.hadiid.erp.common.audit.AuditService;
import com.hadiid.erp.common.exception.BusinessRuleException;
import com.hadiid.erp.common.exception.NotFoundException;
import com.hadiid.erp.labor.dto.AssignmentDto;
import com.hadiid.erp.labor.dto.ContractorDto;
import com.hadiid.erp.labor.dto.PaymentDto;
import com.hadiid.erp.labor.dto.PaymentSummaryDto;
import com.hadiid.erp.labor.dto.WorkerDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * §E.6 / §G: service layer for Labor & Payments. Backend @PreAuthorize checks
 * are mandatory here (Rule 32) — the UI may also hide buttons, but that is
 * never the enforcement mechanism.
 *
 * Known simplification (surfaced to the user, not silently decided): the
 * pure role-based permission model means PAYMENT_CREATE is a SUPERVISOR/ADMIN
 * permission. The source roster implies finance staff such as Grace Nyambura
 * (NORMAL_USER) also record payments day-to-day; a per-user permission
 * override was judged out of scope for this pass and is called out in the
 * project README as a follow-up decision for Hadiid.
 */
@Service
public class LaborPaymentService {

    private final LaborRepository repository;
    private final AuditService auditService;

    public LaborPaymentService(LaborRepository repository, AuditService auditService) {
        this.repository = repository;
        this.auditService = auditService;
    }

    // ---------- Contractors ----------

    public List<ContractorDto> listContractors() {
        return repository.findAllContractors();
    }

    public ContractorDto getContractorOrThrow(Long id) {
        return repository.findContractorById(id).orElseThrow(() -> new NotFoundException("Contractor not found: " + id));
    }

    // No dedicated CONTRACTOR/WORKER permission code exists in the §G matrix;
    // contractors and workers are treated as master data, gated the same as
    // other reference data (SETTINGS_EDIT), held by SUPERVISOR and ADMIN.
    @PreAuthorize("hasAuthority('SETTINGS_EDIT')")
    @Transactional
    public Long createContractor(String name, String registrationNo, String phone, Long actingUserId) {
        Long id = repository.insertContractor(name, registrationNo, phone);
        auditService.record(actingUserId, "CONTRACTOR_CREATE", "CONTRACTOR", id, null, name, null);
        return id;
    }

    // ---------- Workers ----------

    public List<WorkerDto> listWorkers() {
        return repository.findAllWorkers();
    }

    public List<WorkerDto> searchWorkers(String query) {
        return repository.searchWorkers(query);
    }

    public WorkerDto getWorkerOrThrow(Long id) {
        return repository.findWorkerById(id).orElseThrow(() -> new NotFoundException("Worker not found: " + id));
    }

    @PreAuthorize("hasAuthority('SETTINGS_EDIT')")
    @Transactional
    public Long createWorker(String name, String idNumber, String phone, Long contractorId, Long actingUserId) {
        Long id = repository.insertWorker(name, idNumber, phone, contractorId);
        auditService.record(actingUserId, "WORKER_CREATE", "WORKER", id, null, name, null);
        return id;
    }

    // ---------- Assignments ----------

    public List<AssignmentDto> assignmentsForJob(Long jobId) {
        return repository.findAssignmentsByJob(jobId);
    }

    public AssignmentDto getAssignmentOrThrow(Long id) {
        return repository.findAssignmentById(id).orElseThrow(() -> new NotFoundException("Assignment not found: " + id));
    }

    /**
     * §E.6: one worker may be assigned once per (job, stage) — matching the
     * uq_assign_job_stage_worker constraint, checked ahead of insert so the
     * person gets a business-rule message rather than a raw DB error.
     */
    @PreAuthorize("hasAuthority('JOB_ASSIGN')")
    @Transactional
    public Long createAssignment(Long jobId, int stageId, Long workerId, BigDecimal agreedAmount, Long actingUserId) {
        if (agreedAmount == null || agreedAmount.signum() < 0) {
            throw new BusinessRuleException("Agreed amount must be zero or greater.");
        }
        if (repository.assignmentExists(jobId, stageId, workerId)) {
            throw new BusinessRuleException("This worker is already assigned to this job at this stage.");
        }
        Long id = repository.insertAssignment(jobId, stageId, workerId, agreedAmount);
        auditService.record(actingUserId, "ASSIGNMENT_CREATE", "JOB_WORKER_ASSIGNMENT", id,
                null, "job=" + jobId + " stage=" + stageId + " worker=" + workerId + " agreed=" + agreedAmount, null);
        return id;
    }

    // ---------- Payments ----------

    public List<PaymentDto> paymentsForAssignment(Long assignmentId) {
        return repository.findPaymentsByAssignment(assignmentId);
    }

    public List<PaymentDto> paymentsForJob(Long jobId) {
        return repository.findPaymentsByJob(jobId);
    }

    /**
     * §E.6: a payment is an append-only event, not an overwrite of a single
     * "Amount Paid" cell — recording one never mutates the assignment row;
     * the worker-level balance is always re-derived by LaborRepository's
     * aggregation query at read time.
     */
    @PreAuthorize("hasAnyAuthority('PAYMENT_CREATE')")
    @Transactional
    public Long recordPayment(Long assignmentId, BigDecimal amountPaid, LocalDate datePaid,
                               int paymentMethodId, Long paidByUserId, String referenceNote) {
        if (amountPaid == null || amountPaid.signum() <= 0) {
            throw new BusinessRuleException("Amount paid must be greater than zero.");
        }
        AssignmentDto assignment = getAssignmentOrThrow(assignmentId);
        if (amountPaid.compareTo(assignment.getBalance()) > 0) {
            throw new BusinessRuleException("Amount paid (" + amountPaid + ") exceeds the outstanding balance ("
                    + assignment.getBalance() + ") for " + assignment.getWorkerName() + ".");
        }
        Long id = repository.insertPayment(assignmentId, amountPaid, datePaid, paymentMethodId, paidByUserId, referenceNote);
        auditService.record(paidByUserId, "PAYMENT_RECORD", "LABOR_PAYMENT", id,
                null, "assignment=" + assignmentId + " amount=" + amountPaid, referenceNote);
        return id;
    }

    // ---------- Payment summary (§B.5 / §C / §J finance dashboard) ----------

    public PaymentSummaryDto paymentSummaryForJob(Long jobId) {
        return repository.paymentSummaryForJob(jobId)
                .orElseThrow(() -> new NotFoundException("Job not found: " + jobId));
    }

    public List<PaymentSummaryDto> paymentSummaryBySection(Integer sectionId) {
        return repository.paymentSummaryBySection(sectionId);
    }
}
