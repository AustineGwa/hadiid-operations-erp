package com.hadiid.erp.planning;

import com.hadiid.erp.security.AppUserPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Controller
@RequestMapping("/planning")
public class PlanningController {

    private final PlanningService planningService;

    public PlanningController(PlanningService planningService) {
        this.planningService = planningService;
    }

    @GetMapping
    public String plan(@RequestParam(required = false) String month, Model model) {
        YearMonth ym = month != null ? YearMonth.parse(month) : YearMonth.now();
        model.addAttribute("month", ym);
        model.addAttribute("plans", planningService.monthlyPlan(ym));
        model.addAttribute("wip", planningService.wipRollup());
        return "planning/plan";
    }

    @PostMapping("/target")
    @PreAuthorize("hasAuthority('PLAN_EDIT')")
    public String setTarget(@RequestParam int sectionId, @RequestParam String month, @RequestParam int target,
                             @AuthenticationPrincipal AppUserPrincipal principal, RedirectAttributes redirectAttributes) {
        planningService.setMonthlyCapacityTarget(sectionId, YearMonth.parse(month), target, principal.getUserId());
        redirectAttributes.addFlashAttribute("successMessage", "Capacity target updated.");
        return "redirect:/planning?month=" + month;
    }

    @GetMapping("/budget")
    public String weeklyBudget(@RequestParam(required = false) String weekStart, Model model) {
        LocalDate start = weekStart != null ? LocalDate.parse(weekStart) : LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        model.addAttribute("weekStart", start);
        model.addAttribute("budgets", planningService.weeklyBudgets(start));
        return "planning/budget";
    }

    @PostMapping("/budget")
    @PreAuthorize("hasAuthority('PLAN_EDIT')")
    public String setBudget(@RequestParam int sectionId, @RequestParam String weekStart,
                             @RequestParam BigDecimal materialsBudget, @RequestParam BigDecimal materialsActual,
                             @RequestParam BigDecimal laborBudget, @AuthenticationPrincipal AppUserPrincipal principal,
                             RedirectAttributes redirectAttributes) {
        planningService.setWeeklyBudget(sectionId, LocalDate.parse(weekStart), materialsBudget, materialsActual,
                laborBudget, principal.getUserId());
        redirectAttributes.addFlashAttribute("successMessage", "Weekly budget updated.");
        return "redirect:/planning/budget?weekStart=" + weekStart;
    }
}
