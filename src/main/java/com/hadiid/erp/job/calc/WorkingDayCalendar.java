package com.hadiid.erp.job.calc;

import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Working-day calendar used by WorkingDaysRemainingCalculator. Matches the
 * workbook's NETWORKDAYS default exactly: Monday-Friday, no holiday exclusions
 * (§H.2 flags this as a business decision Hadiid must confirm — Saturday
 * working day? Kenyan public holidays excluded?). Until that is confirmed,
 * this class is the single place that decision gets implemented, so no other
 * code needs to change when it is.
 */
@Component
public class WorkingDayCalendar {

    /** Mirrors Excel NETWORKDAYS(start, end): counts Mon-Fri days, inclusive of both ends. */
    public int networkDays(LocalDate start, LocalDate end) {
        if (end.isBefore(start)) {
            return -networkDays(end, start);
        }
        int days = 0;
        LocalDate d = start;
        while (!d.isAfter(end)) {
            if (isWorkingDay(d)) {
                days++;
            }
            d = d.plusDays(1);
        }
        return days;
    }

    public boolean isWorkingDay(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        return dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY;
    }
}
