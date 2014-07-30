package de.k3b.timetracker.activity;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import de.k3b.timetracker.Global;
import de.k3b.timetracker.R;
import de.k3b.timetracker.TimeSliceFilterParameter;

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
		owner.startActivityForResult(intent,
				ReportFilterActivity.RESULT_FILTER_CHANGED);
	}

	@Override
	protected void onOkCLick() {
		this.filter.setEndTime(ReportFilterActivity
				.getFixedEndTime(this.filter));
		super.onOkCLick();
	}

	@Override
	protected Intent getFinishIntent() {
		final Intent intent = super.getFinishIntent();

		intent.putExtra(Global.EXTRA_FILTER, this.filter);
		return intent;
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
