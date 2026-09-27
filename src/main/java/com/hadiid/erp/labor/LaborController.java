package com.hadiid.erp.labor;

import com.hadiid.erp.security.AppUserPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * §E.6: assignment and payment actions, always in the context of a job
 * (the job detail screen's Labor & Payments tab posts here), plus a small
 * standalone worker/contractor directory under /labor.
 */
@Controller
public class LaborController {

    private final LaborPaymentService laborPaymentService;

    public LaborController(LaborPaymentService laborPaymentService) {
        this.laborPaymentService = laborPaymentService;
    }

    @PostMapping("/jobs/{jobId}/labor/assignments")
    @PreAuthorize("hasAuthority('JOB_ASSIGN')")
    public String assignWorker(@PathVariable Long jobId, @RequestParam int stageId, @RequestParam Long workerId,
                                @RequestParam BigDecimal agreedAmount, @AuthenticationPrincipal AppUserPrincipal principal,
                                RedirectAttributes redirectAttributes) {
        laborPaymentService.createAssignment(jobId, stageId, workerId, agreedAmount, principal.getUserId());
        redirectAttributes.addFlashAttribute("successMessage", "Worker assigned.");
        return "redirect:/jobs/" + jobId;
    }

    @PostMapping("/jobs/{jobId}/labor/payments")
    @PreAuthorize("hasAuthority('PAYMENT_CREATE')")
    public String recordPayment(@PathVariable Long jobId, @RequestParam Long assignmentId,
                                 @RequestParam BigDecimal amountPaid, @RequestParam LocalDate datePaid,
                                 @RequestParam int paymentMethodId, @RequestParam(required = false) String referenceNote,
                                 @AuthenticationPrincipal AppUserPrincipal principal, RedirectAttributes redirectAttributes) {
        laborPaymentService.recordPayment(assignmentId, amountPaid, datePaid, paymentMethodId, principal.getUserId(), referenceNote);
        redirectAttributes.addFlashAttribute("successMessage", "Payment recorded.");
        return "redirect:/jobs/" + jobId;
    }

    @GetMapping("/labor/workers")
    public String workers(Model model) {
        model.addAttribute("workers", laborPaymentService.listWorkers());
        model.addAttribute("contractors", laborPaymentService.listContractors());
        return "labor/workers";
    }

    @PostMapping("/labor/workers")
    @PreAuthorize("hasAuthority('SETTINGS_EDIT')")
    public String createWorker(@RequestParam String name, @RequestParam(required = false) String idNumber,
                                @RequestParam(required = false) String phone, @RequestParam(required = false) Long contractorId,
                                @AuthenticationPrincipal AppUserPrincipal principal, RedirectAttributes redirectAttributes) {
        laborPaymentService.createWorker(name, idNumber, phone, contractorId, principal.getUserId());
        redirectAttributes.addFlashAttribute("successMessage", "Worker added.");
        return "redirect:/labor/workers";
    }

    @PostMapping("/labor/contractors")
    @PreAuthorize("hasAuthority('SETTINGS_EDIT')")
    public String createContractor(@RequestParam String name, @RequestParam(required = false) String registrationNo,
                                    @RequestParam(required = false) String phone,
                                    @AuthenticationPrincipal AppUserPrincipal principal, RedirectAttributes redirectAttributes) {
        laborPaymentService.createContractor(name, registrationNo, phone, principal.getUserId());
        redirectAttributes.addFlashAttribute("successMessage", "Contractor added.");
        return "redirect:/labor/workers";
    }
}
