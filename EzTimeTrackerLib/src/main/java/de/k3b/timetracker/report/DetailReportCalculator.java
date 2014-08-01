package de.k3b.timetracker.report;

import java.util.ArrayList;
import java.util.List;

import de.k3b.timetracker.DateTimeFormatter;
import de.k3b.timetracker.model.TimeSlice;
import de.k3b.util.DateTimeUtil;

/**
 * converts slices with date header.
 * Created by k3b on 01.08.2014.
 */
public class DetailReportCalculator {

    public static List<Object> createStatistics(
            final List<TimeSlice> timeSlices) {
        long lastStartDate = 0;

        final DateTimeUtil formatter = DateTimeFormatter.getInstance();

        final List<Object> items = new ArrayList<Object>();

        for (final TimeSlice aSlice : timeSlices) {
            final long startDate = formatter.getStartOfDay(aSlice
                    .getStartTime());
            if (lastStartDate != startDate) {
                lastStartDate = startDate;
                items.add(Long.valueOf(lastStartDate));
            }

            items.add(aSlice);
        }

        return items;
    }
}
