package com.hadiid.erp.scheduling;

import com.hadiid.erp.job.JobService;
import com.hadiid.erp.job.dto.JobSearchCriteria;
import com.hadiid.erp.job.dto.JobSummaryDto;
import com.hadiid.erp.reference.ReferenceDataService;
import com.hadiid.erp.reference.dto.CalendarDayDto;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * §J/§I Scheduling: a 14-day production-calendar view plus "Today's Actions"
 * (jobs due soon or overdue). All figures are computed at read time from
 * JobService's calculators — nothing here is a stored schedule table.
 */
@Service
public class SchedulingService {

    private static final int WINDOW_DAYS = 14;

    private final JobService jobService;
    private final ReferenceDataService referenceDataService;

    public SchedulingService(JobService jobService, ReferenceDataService referenceDataService) {
        this.jobService = jobService;
        this.referenceDataService = referenceDataService;
    }

    /** Day-status overrides (§B.10/§H.4 — informational only) for the next 14 days, including today. */
    public List<CalendarDayDto> fourteenDayCalendar() {
        LocalDate today = LocalDate.now();
        return referenceDataService.calendarRange(today, today.plusDays(WINDOW_DAYS - 1));
    }

    /** Jobs whose planned start date falls inside the 14-day window (§J Gantt view). */
    public List<JobSummaryDto> jobsStartingInWindow() {
        LocalDate today = LocalDate.now();
        var criteria = JobSearchCriteria.builder()
                .plannedStartFrom(today)
                .plannedStartTo(today.plusDays(WINDOW_DAYS - 1))
                .build();
        return jobService.search(criteria, 0, 200).getItems();
    }

    /** "Today's Actions" (§I) — active jobs due within 2 working days or already overdue. */
    public List<JobSummaryDto> todaysActions() {
        var dueSoon = jobService.search(JobSearchCriteria.builder().dueSoon(true).build(), 0, 200).getItems();
        var overdue = jobService.search(JobSearchCriteria.builder().overdue(true).build(), 0, 200).getItems();
        java.util.LinkedHashMap<Long, JobSummaryDto> merged = new java.util.LinkedHashMap<>();
        overdue.forEach(j -> merged.put(j.getId(), j));
        dueSoon.forEach(j -> merged.putIfAbsent(j.getId(), j));
        return List.copyOf(merged.values());
    }
}
