package com.hadiid.erp.scheduling;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SchedulingController {

    private final SchedulingService schedulingService;

    public SchedulingController(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    @GetMapping("/schedule")
    public String schedule(Model model) {
        model.addAttribute("calendar", schedulingService.fourteenDayCalendar());
        model.addAttribute("jobsStarting", schedulingService.jobsStartingInWindow());
        model.addAttribute("todaysActions", schedulingService.todaysActions());
        return "schedule/schedule";
    }
}
