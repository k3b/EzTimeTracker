package de.k3b.timetracker;

import java.text.ParseException;

import de.k3b.timetracker.model.TimeSlice;
import de.k3b.util.DateTimeUtil;

public class DateTimeFormatter extends DateTimeUtil {

    private static DateTimeUtil instance = null;

    public DateTimeFormatter() {
        super(TimeSlice.NO_TIME_VALUE);
    }

	public static DateTimeUtil getInstance() {
		if (DateTimeFormatter.instance == null) {
			DateTimeFormatter.instance = new DateTimeFormatter();
		}

		return DateTimeFormatter.instance;
	}

	@Override
	public long parseDate(final String mDateSelectedForAdd) {
		try {
			return super.parseDate(mDateSelectedForAdd);
		} catch (final ParseException e) {
            Global.getLogger().w("cannot reconvert " + mDateSelectedForAdd
                            + " to dateTime using " + shortDateformatter,
                    e
            );
            return TimeSlice.NO_TIME_VALUE;
		}
	}
}
