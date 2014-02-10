package com.zettsett.timetracker.activity;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;

import com.zetter.androidTime.R;
import com.zettsett.timetracker.Global;

public class ReportFilterActivity extends FilterActivity {
	public static final int RESULT_FILTER_CHANGED = 16288;

	public ReportFilterActivity() {
		super(R.string.label_report_filter, R.string.cmd_update,
				ReportFilterActivity.RESULT_FILTER_CHANGED);
	}

	public static void showActivity(final Activity owner,
			final TimeSliceFilterParameter filter) {
		final Intent intent = new Intent().setClass(owner,
				ReportFilterActivity.class);
		intent.putExtra(Global.EXTRA_FILTER, filter);
		owner.startActivityForResult(intent, 0);
	}

	@Override
	protected void onOkCLick() {
		super.onOkCLick();
		this.filter.setEndTime(ReportFilterActivity
				.getFixedEndTime(this.filter));

		this.finish();
	}

	/**
	 * @return endtime with time set to 23:59 from rangeFilter
	 */
	private static long getFixedEndTime(
			final TimeSliceFilterParameter rangeFilter) {
		final Calendar c = Calendar.getInstance();
		c.setTime(new Date(rangeFilter.getEndTime()));
		c.set(Calendar.HOUR_OF_DAY, 23);
		c.set(Calendar.MINUTE, 59);
		final long endDate = c.getTimeInMillis();
		return endDate;
	}

}
