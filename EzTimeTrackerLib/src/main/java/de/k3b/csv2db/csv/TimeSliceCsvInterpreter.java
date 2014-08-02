package de.k3b.csv2db.csv;

import java.text.ParseException;

import de.k3b.timetracker.database.ICategoryRepsitory;
import de.k3b.timetracker.model.TimeSlice;
import de.k3b.timetracker.model.TimeSliceCategory;
import de.k3b.util.DateTimeUtil;

/**
 * analyses the content of a csv-data-row into a {@link de.k3b.timetracker.model.TimeSlice} object.
 */
public class TimeSliceCsvInterpreter {
    /**
     * This column is assoziated with {@link de.k3b.timetracker.model.TimeSlice#getStartTime()}
     */
    private static final String KNOWN_COLUMNNAME_START_DATE = "Star(tDate)";
    /**
     * This column is assoziated with {@link de.k3b.timetracker.model.TimeSlice#getEndTime()}.
     */
    private static final String KNOWN_COLUMNNAME_END_DATE = "End(Date)";
    /**
     * This column is assoziated with {@link de.k3b.timetracker.model.TimeSlice#getNotes()}.
     */
    private static final String KNOWN_COLUMNNAME_NOTES = "Not(es)";
    /**
     * This column is assoziated with {@link de.k3b.timetracker.model.TimeSlice#getCategoryName()}.
     */
    private static final String KNOWN_COLUMNNAME_CATEGORY_NAME = "Cat(egoryName)";

    /**
     * virtual colum used to calculate {@link de.k3b.timetracker.model.TimeSlice#getEndTime()} from {@link de.k3b.timetracker.model.TimeSlice#getStartTime()} + duration.
     */
    private static final String KNOWN_COLUMNNAME_DURATON_IN_MINUTES = "Dur(atonInMinutes)";

    /**
     * local helper to parse datetime
     */
    private static DateTimeUtil dt = new DateTimeUtil(0);
    /**
     * Csv column number that contains data for {@link de.k3b.timetracker.model.TimeSlice#getStartTime()}
     */
    final int colIndexStart;
    /**
     * Csv column number that contains data for {@link de.k3b.timetracker.model.TimeSlice#getEndTime()}
     */
    final int colIndexEnd;
    /**
     * Csv column number that contains data for duration, the difference between {@link de.k3b.timetracker.model.TimeSlice#getEndTime()} and {@link de.k3b.timetracker.model.TimeSlice#getStartTime()}
     */
    final int colIndexDuration;
    /**
     * Csv column number that contains data for {@link de.k3b.timetracker.model.TimeSlice#getNotes()}
     */
    final int colIndexNotes;
    /**
     * Csv column number that contains data for {@link de.k3b.timetracker.model.TimeSlice#getCategoryName()}
     */
    final int colIndexCategory;
    /**
     * Helper to find/create {@link de.k3b.timetracker.model.TimeSliceCategory}
     */
    private ICategoryRepsitory categoryRepository;

    /**
     * @param categoryRepository   used to find/create {@link de.k3b.timetracker.model.TimeSliceCategory}
     * @param csvHeaderColumnNames that define a csv column. Interpets column names that start with
     *                             start|end|dur(ation)|note|cat(egoryName) as defined in KNOWN_COLUMNNAME_xxx.
     */
    public TimeSliceCsvInterpreter(ICategoryRepsitory categoryRepository,
                                   String... csvHeaderColumnNames) {
        this.categoryRepository = categoryRepository;

        this.colIndexStart = getColumnIndexFromName(KNOWN_COLUMNNAME_START_DATE, csvHeaderColumnNames);
        this.colIndexEnd = getColumnIndexFromName(KNOWN_COLUMNNAME_END_DATE, csvHeaderColumnNames);
        this.colIndexDuration = getColumnIndexFromName(KNOWN_COLUMNNAME_DURATON_IN_MINUTES, csvHeaderColumnNames);
        this.colIndexNotes = getColumnIndexFromName(KNOWN_COLUMNNAME_NOTES, csvHeaderColumnNames);
        this.colIndexCategory = getColumnIndexFromName(KNOWN_COLUMNNAME_CATEGORY_NAME, csvHeaderColumnNames);

        if ((colIndexDuration >= 0) && (colIndexStart < 0))
            throw new CsvException("Colum " + csvHeaderColumnNames[colIndexDuration] + "[#" + colIndexDuration +
                    "]:" + KNOWN_COLUMNNAME_DURATON_IN_MINUTES + " requires mandatory column " + KNOWN_COLUMNNAME_START_DATE, null);
    }

    protected int getColumnIndexFromName(String searchColumnName, String... csvHeaderColumnNames) {
        searchColumnName = searchColumnName.split("\\(")[0].toLowerCase();
        for (int i = 0; i < csvHeaderColumnNames.length; i++) {
            if (csvHeaderColumnNames[i].toLowerCase().startsWith(searchColumnName)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * converts {columnValues} of one csv-row into a {@link de.k3b.timetracker.model.TimeSliceCategory} object.
     */
    public TimeSlice parse(String... columnValues) throws CsvException {
        if ((columnValues != null) && (columnValues.length > 0)) {
            boolean hasStartDate = false;
            TimeSlice result = new TimeSlice();
            String value;

            value = getColumnValue(columnValues, colIndexStart);
            if (value != null) {
                try {
                    result.setStartTime(dt.parseIsoDate(value));
                    hasStartDate = true;
                } catch (ParseException e) {
                    throw new CsvException(KNOWN_COLUMNNAME_START_DATE, colIndexStart, value, e);
                }
            }

            value = getColumnValue(columnValues, colIndexEnd);
            if (value != null) {
                try {
                    result.setEndTime(dt.parseIsoDate(value));
                } catch (ParseException e) {
                    throw new CsvException(KNOWN_COLUMNNAME_END_DATE, colIndexEnd, value, e);
                }
            }

            value = getColumnValue(columnValues, colIndexDuration);
            if (value != null) {
                if (!hasStartDate) {
                    throw new CsvException("Colum " + KNOWN_COLUMNNAME_DURATON_IN_MINUTES + "[#" + colIndexDuration +
                            "]=" + value + " requires mandatory columnvalue " + KNOWN_COLUMNNAME_START_DATE + "[#" + colIndexStart +
                            "]=???", null);
                }
                try {
                    int minutes = Integer.parseInt(value);
                    long value2 = dt.addMinutes(result.getStartTime(), minutes);
                    result.setEndTime(value2);
                } catch (NumberFormatException e) {
                    throw new CsvException(KNOWN_COLUMNNAME_DURATON_IN_MINUTES, colIndexDuration, value, e);
                }
            }

            value = getColumnValue(columnValues, colIndexNotes);
            if (value != null) {
                result.setNotes(value);
            }

            value = getColumnValue(columnValues, colIndexCategory);
            if (value != null) {
                TimeSliceCategory category = this.categoryRepository
                        .getOrCreateCategory(value);
                result.setCategory(category);
            }
            return result;
        }
        return null;
    }

    /**
     * returns null if {columnIndex} is not in {columnValues} or empty.
     */
    private String getColumnValue(String[] columnValues, int columnIndex) {
        String result = null;
        if ((columnIndex >= 0) & (columnIndex < columnValues.length)) {
            result = columnValues[columnIndex];
            if ((result != null) && (result.length() == 0))
                result = null;
        }
        return result;
    }

}
