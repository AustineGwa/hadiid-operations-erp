package com.hadiid.erp.reporting;

import com.hadiid.erp.reporting.dto.FinanceSummaryDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

/** §J/§K: Overview/Operations/Finance dashboards and Stage Movement Analysis. */
@Controller
public class ReportingController {

    private final ReportingService reportingService;

    public ReportingController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @GetMapping("/dashboard")
    public String overview(Model model) {
        model.addAttribute("stageCounts", reportingService.stageCounts());
        model.addAttribute("summary", reportingService.overviewSummary());
        return "dashboard/overview";
    }

    @GetMapping("/reports/finance")
    public String finance(Model model) {
        List<FinanceSummaryDto> summaries = reportingService.financeSummaryBySection();
        model.addAttribute("financeSummaries", summaries);
        model.addAttribute("grandContract", summaries.stream().map(FinanceSummaryDto::getTotalContractAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        model.addAttribute("grandPaid", summaries.stream().map(FinanceSummaryDto::getTotalPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        model.addAttribute("grandBalance", summaries.stream().map(FinanceSummaryDto::getTotalBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        return "dashboard/finance";
    }

    @GetMapping("/reports/stage-movement")
    public String stageMovement(@RequestParam(required = false) Integer sectionId, Model model) {
        model.addAttribute("dwellStats", reportingService.stageDwell(sectionId));
        model.addAttribute("arrivals", reportingService.stageArrivals());
        model.addAttribute("sectionId", sectionId);
        return "dashboard/stage-movement";
    }
}
