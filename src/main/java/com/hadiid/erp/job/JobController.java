package com.hadiid.erp.job;

import com.hadiid.erp.job.dto.JobCreateRequest;
import com.hadiid.erp.job.dto.JobEditRequest;
import com.hadiid.erp.job.dto.JobSearchCriteria;
import com.hadiid.erp.labor.LaborPaymentService;
import com.hadiid.erp.reference.ReferenceDataService;
import com.hadiid.erp.security.AppUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * §I: job list/search/filter, create, edit, detail (with History and
 * Labor & Payments tabs), and the two stage-transition actions. Advance and
 * correct always delegate to StageTransitionService — this controller never
 * touches current_stage_id itself (Rule 32: backend authorization is
 * enforced here via @PreAuthorize, not by hiding buttons).
 */
@Controller
@RequestMapping("/jobs")
public class JobController {

    private final JobService jobService;
    private final StageTransitionService stageTransitionService;
    private final StageHistoryRepository stageHistoryRepository;
    private final ReferenceDataService referenceDataService;
    private final LaborPaymentService laborPaymentService;

    public JobController(JobService jobService, StageTransitionService stageTransitionService,
                          StageHistoryRepository stageHistoryRepository, ReferenceDataService referenceDataService,
                          LaborPaymentService laborPaymentService) {
        this.jobService = jobService;
        this.stageTransitionService = stageTransitionService;
        this.stageHistoryRepository = stageHistoryRepository;
        this.referenceDataService = referenceDataService;
        this.laborPaymentService = laborPaymentService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String searchText,
                        @RequestParam(required = false) Integer sectionId,
                        @RequestParam(required = false) Integer bodyTypeId,
                        @RequestParam(required = false) Integer stageId,
                        @RequestParam(required = false) Boolean dueSoon,
                        @RequestParam(required = false) Boolean overdue,
                        @RequestParam(required = false) Boolean releasedOnly,
                        @RequestParam(defaultValue = "0") int page,
                        Model model) {
        JobSearchCriteria criteria = JobSearchCriteria.builder()
                .searchText(searchText).sectionId(sectionId).bodyTypeId(bodyTypeId).stageId(stageId)
                .dueSoon(dueSoon).overdue(overdue).releasedOnly(releasedOnly)
                .build();
        model.addAttribute("result", jobService.search(criteria, page, 20));
        model.addAttribute("criteria", criteria);
        model.addAttribute("sections", referenceDataService.sections());
        model.addAttribute("stages", referenceDataService.stages());
        return "job/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAuthority('JOB_CREATE')")
    public String newForm(Model model) {
        model.addAttribute("job", new JobCreateRequest());
        model.addAttribute("sections", referenceDataService.sections());
        model.addAttribute("bodyTypes", referenceDataService.bodyTypes());
        return "job/form-create";
    }

    @PostMapping
    @PreAuthorize("hasAuthority('JOB_CREATE')")
    public String create(@Valid @ModelAttribute("job") JobCreateRequest req, BindingResult binding,
                          @AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        if (binding.hasErrors()) {
            model.addAttribute("sections", referenceDataService.sections());
            model.addAttribute("bodyTypes", referenceDataService.bodyTypes());
            return "job/form-create";
        }
        int notStartedStageId = referenceDataService.stages().stream()
                .filter(s -> "NOT_STARTED".equals(s.getCode())).findFirst().orElseThrow().getId();
        Long id = jobService.create(req, principal.getUserId(), notStartedStageId);
        return "redirect:/jobs/" + id;
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        var job = jobService.getByIdOrThrow(id);
        model.addAttribute("job", job);
        model.addAttribute("history", stageHistoryRepository.findByJobId(id));
        model.addAttribute("stages", referenceDataService.stages());
        model.addAttribute("assignments", laborPaymentService.assignmentsForJob(id));
        model.addAttribute("payments", laborPaymentService.paymentsForJob(id));
        model.addAttribute("paymentSummary", laborPaymentService.paymentSummaryForJob(id));
        model.addAttribute("workers", laborPaymentService.listWorkers());
        model.addAttribute("paymentMethods", referenceDataService.paymentMethods());
        return "job/detail";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAuthority('JOB_EDIT')")
    public String editForm(@PathVariable Long id, Model model) {
        var job = jobService.getByIdOrThrow(id);
        JobEditRequest req = new JobEditRequest();
        req.setBodyTypeId(job.getBodyTypeId());
        req.setVehicleModel(job.getVehicleModel());
        req.setPlannedStartDate(job.getPlannedStartDate());
        req.setPlannedProductionDays(job.getPlannedProductionDays());
        req.setPlannedFinishingDays(job.getPlannedFinishingDays());
        req.setContractAmount(job.getContractAmount());
        req.setCustomerName(job.getCustomerNameSnapshot());
        req.setCustomerPhone(job.getCustomerPhoneSnapshot());
        req.setCustomerAddress(job.getCustomerAddressSnapshot());
        req.setNotes(job.getNotes());
        model.addAttribute("job", req);
        model.addAttribute("jobId", id);
        model.addAttribute("bodyTypes", referenceDataService.bodyTypesForSection(job.getSectionId()));
        return "job/form-edit";
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasAuthority('JOB_EDIT')")
    public String edit(@PathVariable Long id, @Valid @ModelAttribute("job") JobEditRequest req, BindingResult binding,
                        @AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        if (binding.hasErrors()) {
            model.addAttribute("jobId", id);
            var job = jobService.getByIdOrThrow(id);
            model.addAttribute("bodyTypes", referenceDataService.bodyTypesForSection(job.getSectionId()));
            return "job/form-edit";
        }
        jobService.edit(id, req, principal.getUserId());
        return "redirect:/jobs/" + id;
    }

    @PostMapping("/{id}/advance")
    @PreAuthorize("hasAuthority('JOB_ADVANCE_STAGE')")
    public String advance(@PathVariable Long id, @AuthenticationPrincipal AppUserPrincipal principal,
                           RedirectAttributes redirectAttributes) {
        stageTransitionService.advance(id, principal.getUserId());
        redirectAttributes.addFlashAttribute("successMessage", "Stage advanced.");
        return "redirect:/jobs/" + id;
    }

    @PostMapping("/{id}/correct")
    @PreAuthorize("hasAuthority('JOB_CORRECT_STAGE')")
    public String correct(@PathVariable Long id, @RequestParam int targetStageId, @RequestParam String reason,
                           @AuthenticationPrincipal AppUserPrincipal principal, RedirectAttributes redirectAttributes) {
        stageTransitionService.correct(id, targetStageId, reason, principal.getUserId());
        redirectAttributes.addFlashAttribute("successMessage", "Stage corrected.");
        return "redirect:/jobs/" + id;
    }

    @GetMapping("/body-types")
    @ResponseBody
    public Object bodyTypesForSection(@RequestParam int sectionId) {
        return referenceDataService.bodyTypesForSection(sectionId);
    }
}
