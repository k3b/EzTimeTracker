package de.k3b.timetracker.report;

import android.content.Context;

import java.util.Locale;

import de.k3b.timetracker.R;

/**
 * Created by k3b on 31.07.2014.
 */
public class DurationFormatterAndroid implements DurationFormatter {
    private final Context context;

    /**
     * @param context to access language specific text constants.
     */
    public DurationFormatterAndroid(final Context context) {
        this.context = context;
    }

    @Override
    public String timeInMillisToText(final long totalTimeInMillis,
                                     final boolean longVersion) {
        final long minutes = (totalTimeInMillis / (1000 * 60)) % 60;
        final long hours = totalTimeInMillis / (1000 * 60 * 60);

        String hoursWord;
        if (hours == 1) {
            hoursWord = this.getString(R.string.hoursWord1);
        } else {
            hoursWord = this.getString(R.string.hoursWordN);
        }

        if (longVersion) {
            String minutesWord;

            if (minutes == 1) {
                minutesWord = this
                        .getString(R.string.minutesWord1);
            } else {
                minutesWord = this
                        .getString(R.string.minutesWordN);
            }
            final String timeString = hours + " " + hoursWord + ", " + minutes
                    + " " + minutesWord;
            return timeString;
        }
        return String.format(Locale.GERMANY, " %1$d %3$s %2$02d", hours,
                minutes, hoursWord);
    }

    protected String getString(int resId) {
        return this.context.getString(resId);
    }

}
