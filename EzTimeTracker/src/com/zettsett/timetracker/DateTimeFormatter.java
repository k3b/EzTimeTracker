package com.zettsett.timetracker;

import java.text.ParseException;

import android.util.Log;

import com.zettsett.timetracker.model.TimeSlice;

import de.k3b.util.DateTimeUtil;

public class DateTimeFormatter extends DateTimeUtil {

	public DateTimeFormatter() {
		super(TimeSlice.NO_TIME_VALUE);
	}

	private static DateTimeUtil instance = null;

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
			Log.w(Global.LOG_CONTEXT, "cannot reconvert " + mDateSelectedForAdd
					+ " to dateTime using " + DateTimeUtil.shortDateformatter,
					e);
			return TimeSlice.NO_TIME_VALUE;
		}
	}
}
