package de.k3b.timetracker.report;

import java.util.Locale;

/**
 * Created by k3b on 31.07.2014.
 */
public class DurationFormatterSimple implements DurationFormatter {
    @Override
    public String timeInMillisToText(final long totalTimeInMillis,
                                     final boolean longVersion) {
        final long minutes = (totalTimeInMillis / (1000 * 60)) % 60;
        final long hours = totalTimeInMillis / (1000 * 60 * 60);

        String hoursWord = "H";
        if (longVersion) {
            String minutesWord = "M";
            final String timeString = hours + " " + hoursWord + ", " + minutes
                    + " " + minutesWord;
            return timeString;
        }
        return String.format(Locale.GERMANY, " %1$d %3$s %2$02d", hours,
                minutes, hoursWord);
    }
}
