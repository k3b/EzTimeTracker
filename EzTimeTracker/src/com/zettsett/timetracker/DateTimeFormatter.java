package com.zettsett.timetracker;

import android.util.Log;

import com.zettsett.timetracker.model.TimeSlice;
import de.k3b.util.DateTimeUtil;

import java.text.ParseException;

public class DateTimeFormatter extends DateTimeUtil {

	public DateTimeFormatter() {
		super(TimeSlice.NO_TIME_VALUE);
	}
	
	private static DateTimeUtil instance = null;
	
	public static DateTimeUtil getInstance()
	{
		if (instance == null) instance = new DateTimeFormatter();
		
		return instance;
	}
	
	public long parseDate(String mDateSelectedForAdd) {
		try {
			return super.parseDate(mDateSelectedForAdd);
		} catch (ParseException e) {
			Log.w(Global.LOG_CONTEXT,"cannot reconvert " + mDateSelectedForAdd + " to dateTime using " + shortDateformatter,e);
			return TimeSlice.NO_TIME_VALUE;
		}
	}
}
