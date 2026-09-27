package com.hadiid.erp.reference;

import com.hadiid.erp.security.AppUserPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/** §M Settings: reference/master data (body types, calendar day-status overrides). */
@Controller
@RequestMapping("/settings")
public class ReferenceDataController {

    private final ReferenceDataService referenceDataService;

    public ReferenceDataController(ReferenceDataService referenceDataService) {
        this.referenceDataService = referenceDataService;
    }

    @GetMapping("/reference")
    public String reference(Model model) {
        model.addAttribute("sections", referenceDataService.sections());
        model.addAttribute("bodyTypes", referenceDataService.bodyTypes());
        model.addAttribute("stages", referenceDataService.stages());
        model.addAttribute("paymentMethods", referenceDataService.paymentMethods());
        return "settings/reference";
    }

    @PostMapping("/reference/body-types")
    @PreAuthorize("hasAuthority('SETTINGS_EDIT')")
    public String addBodyType(@RequestParam int sectionId, @RequestParam String code, @RequestParam String label,
                               @AuthenticationPrincipal AppUserPrincipal principal, RedirectAttributes redirectAttributes) {
        referenceDataService.addBodyType(principal.getUserId(), sectionId, code, label);
        redirectAttributes.addFlashAttribute("successMessage", "Body type added.");
        return "redirect:/settings/reference";
    }

    @PostMapping("/reference/body-types/{id}/toggle")
    @PreAuthorize("hasAuthority('SETTINGS_EDIT')")
    public String toggleBodyType(@PathVariable int id, @RequestParam boolean active,
                                  @AuthenticationPrincipal AppUserPrincipal principal, RedirectAttributes redirectAttributes) {
        referenceDataService.setBodyTypeActive(principal.getUserId(), id, active);
        redirectAttributes.addFlashAttribute("successMessage", "Body type updated.");
        return "redirect:/settings/reference";
    }

    @GetMapping("/calendar")
    public String calendar(Model model) {
        LocalDate today = LocalDate.now();
        model.addAttribute("days", referenceDataService.calendarRange(today, today.plusDays(29)));
        return "settings/calendar";
    }

    @PostMapping("/calendar")
    @PreAuthorize("hasAuthority('SCHEDULE_EDIT')")
    public String setCalendarDay(@RequestParam LocalDate date, @RequestParam String status,
                                  @RequestParam(required = false) String note,
                                  @AuthenticationPrincipal AppUserPrincipal principal, RedirectAttributes redirectAttributes) {
        referenceDataService.setCalendarDay(principal.getUserId(), date, status, note);
        redirectAttributes.addFlashAttribute("successMessage", "Day status updated.");
        return "redirect:/settings/calendar";
    }
}
