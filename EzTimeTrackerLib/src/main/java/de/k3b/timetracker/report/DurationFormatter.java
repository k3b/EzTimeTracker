package de.k3b.timetracker.report;

/**
 * Created by EVE on 31.07.2014.
 */
public interface DurationFormatter {
    String timeInMillisToText(long totalTimeInMillis,
                              boolean longVersion);
}
