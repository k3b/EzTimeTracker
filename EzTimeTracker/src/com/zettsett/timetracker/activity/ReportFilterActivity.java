package com.zettsett.timetracker.activity;

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
		this.filter.setEndTime(ReportFramework.getFixedEndTime(this.filter));

		this.finish();
	}
}
