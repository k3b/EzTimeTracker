package de.k3b.timetracker.report;

/**
 * Represents a report item statistics (duration, notes)
 */
public class ReportItemWithStatistics {
    /**
     * how item notes are seperated
     */
    private static final String NOTES_DELIMITER = "\n";

    private final Object groupingKey;
    private long duration;
    private String notes;

    /**
     * @param groupingKey where items are grouped by (Long=Date, Category, ...)
     * @param duration    sum within grouping
     * @param notes       sum of notes
     */
    public ReportItemWithStatistics(final Object groupingKey,
                                    final long duration, final String notes) {
        this.groupingKey = groupingKey;
        this.setDuration(duration);
        this.setNotes(notes);
    }

    public long getDuration() {
        return this.duration;
    }

    public void setDuration(final long duration) {
        this.duration = duration;
    }

    public void incrementDuration(final long diffTimeValue) {
        if (diffTimeValue > 0) {
            this.duration += diffTimeValue;
        }
    }

    public String getNotes() {
        return this.notes;
    }

    public void setNotes(final String notes) {
        this.notes = notes;
    }

    public boolean hasNotes() {
        return (this.notes != null) && (this.notes.length() > 0);
    }

    public void appendNotes(final String notes) {
        if ((notes != null) && (notes.length() > 0)) {
            this.notes = (this.notes == null) ? notes : (this.notes
                    + ReportItemWithStatistics.NOTES_DELIMITER + notes);
        }
    }

    public Object getGroupingKey() {
        return this.groupingKey;
    }
}