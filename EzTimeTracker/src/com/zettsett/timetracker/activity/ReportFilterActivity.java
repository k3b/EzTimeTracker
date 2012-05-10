package com.zettsett.timetracker.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.zetter.androidTime.R;
import com.zettsett.timetracker.Global;

public class ReportFilterActivity extends FilterActivity {
	public ReportFilterActivity() {
		super(R.string.label_report_filter, R.string.cmd_update);
	}

	public static void showActivity(Activity owner, FilterParameter filter) {
		Intent intent = new Intent().setClass(owner, ReportFilterActivity.class);
		intent.putExtra(Global.EXTRA_FILTER, filter);
		owner.startActivityForResult(intent, 0);
	}

	@Override
	protected void onOkCLick()
	{
		super.onOkCLick();
		this.finish();
	}
}
