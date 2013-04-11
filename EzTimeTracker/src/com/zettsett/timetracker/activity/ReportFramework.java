package com.zettsett.timetracker.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zetter.androidTime.R;
import com.zettsett.timetracker.EmailUtilities;
import com.zettsett.timetracker.Global;
import com.zettsett.timetracker.LinearScroller;
import com.zettsett.timetracker.model.TimeSlice;
import com.zettsett.timetracker.report.ReportOutput;
import com.zettsett.timetracker.report.SDDataExporter;

/*
 * Copyright 2010 Eric Zetterbaum ezetter@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */

/**
 * Common Handling for Report-Generation and display
 */
public class ReportFramework implements Serializable {
	private static final long serialVersionUID = 394933866214361393L;
	
	private static final int MENU_ITEM_EXPORT_SD = Menu.FIRST + 12;
	private static final int MENU_ITEM_EXPORT_EMAIL = Menu.FIRST + 13;

	private static final int MENU_ITEM_SET_FILTER = Menu.FIRST + 21;

	private final Activity mActivity;
	private LinearScroller mScrollView;
	
	private FilterParameter mFilter;
	private List<TextView> mReportViewList;

	public enum ReportTypes {
		TIMESHEET, SUMMARY
	};

	private ReportTypes reportType;

	ReportFramework(Activity activity, FilterParameter filter) {
		super();
		initializeDateRanges(filter);
		this.mActivity = activity;
	}

	private void initializeDateRanges(FilterParameter filter) {
		mFilter = (filter != null) ? filter : new FilterParameter();

		Date currDate = new Date();
		long startTime = this.mFilter.getStartTime();
		if (startTime  == TimeSlice.NO_TIME_VALUE) {
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(currDate);
			calendar.set(Calendar.HOUR, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.roll(Calendar.MONTH, false);
			calendar.roll(Calendar.MONTH, false); // -= 2 months
			startTime = calendar.getTimeInMillis();
			if (startTime > currDate.getTime()) {
				calendar.roll(Calendar.YEAR, false);
				startTime = calendar.getTimeInMillis();
			}
		}
		this.mFilter.setStartTime(startTime);
		
		long endTime = this.mFilter.getEndTime();
		if (endTime == TimeSlice.NO_TIME_VALUE) {
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(currDate);
			endTime = calendar.getTimeInMillis();
			calendar.roll(Calendar.WEEK_OF_YEAR, true); // += 1 week to see also entries that where made after the programstart.
		}
		this.mFilter.setEndTime(endTime);
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean result = true;
		SubMenu exportMenu = menu.addSubMenu(Menu.NONE, Menu.NONE, 2, R.string.menu_export_report);
		exportMenu.add(Menu.NONE, MENU_ITEM_EXPORT_SD, 1, R.string.menu_export_report_to_sd_card);
		exportMenu.add(Menu.NONE, MENU_ITEM_EXPORT_EMAIL, 2, R.string.menu_email_report);
		
		menu.add(Menu.NONE, MENU_ITEM_SET_FILTER, 2, R.string.menu_filter);

		return result;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ITEM_SET_FILTER:
			ReportFilterActivity.showActivity(this.mActivity, this.mFilter);
			break;
		case MENU_ITEM_EXPORT_SD:
			SDDataExporter.exportToSD(getDefaultReportName(), mActivity, ReportOutput.makeFormatter(mReportViewList));
			break;
		case MENU_ITEM_EXPORT_EMAIL:
			ReportOutput outPutter = ReportOutput.makeFormatter(mReportViewList);
			outPutter.setTerminator("\n");
			EmailUtilities.send("", getEMailSummaryLine(), mActivity, outPutter.getOutput());
			break;
		}

		return true;
	}

	private String getDefaultReportName() {
		String name;
		if (reportType == ReportTypes.TIMESHEET) {
			name = mActivity.getString(R.string.default_export_ts_name);
		} else {
			name = mActivity.getString(R.string.default_export_sum_name);
		}
		return name;
	}

	private String getEMailSummaryLine() {
		String appName = mActivity.getString(R.string.app_name);
		String summary;
		if (reportType == ReportTypes.TIMESHEET) {
			summary = String.format(mActivity.getString(R.string.default_mail_ts_subject), appName);
		} else {
			summary = String.format(mActivity.getString(R.string.default_mail_sum_subject), appName);
		}
		return summary;

	}

	LinearScroller getLinearScroller() {
		return mScrollView;
	}

	LinearLayout buildViews() {
		mActivity.setContentView(R.layout.report_framework);
		LinearLayout contentView = (LinearLayout) mActivity.findViewById(R.id.report_frame);
		contentView.setOrientation(LinearLayout.VERTICAL);
		mScrollView = new LinearScroller(mActivity);
		contentView.addView(mScrollView.getScrollView());
		return contentView;
	}

	public List<TextView> initializeTextViewsForExportList() {
		List<TextView> tvList = new ArrayList<TextView>();
		mReportViewList = tvList;
		return tvList;
	}

	public void setReportType(ReportTypes reportType) {
		this.reportType = reportType;
	}

	public static FilterParameter getLastFilter(Bundle savedInstanceState, String parameterName, FilterParameter notFoundValue) {
		FilterParameter rangeFilter = null;
		if (savedInstanceState != null) {
			Serializable filter = savedInstanceState.getSerializable(parameterName);
			
			if (filter instanceof FilterParameter)
				rangeFilter = (FilterParameter) filter;
		}
		
		if (rangeFilter == null)
		{
			rangeFilter = notFoundValue;
		}
		
		if (rangeFilter == null)
		{
			rangeFilter = new FilterParameter();
		}
		
		return rangeFilter;
	}

	public static long getFixedEndTime(FilterParameter rangeFilter) {
		final Calendar c = Calendar.getInstance();
		c.setTime(new Date(rangeFilter.getEndTime()));
		c.set(Calendar.HOUR_OF_DAY, 23);
		c.set(Calendar.MINUTE, 59);
		long endDate = c.getTimeInMillis();
		return endDate;
	}

	public FilterParameter onActivityResult(Intent intent,
			FilterParameter previosRangeFilter) {
		FilterParameter newRangeFilter = (FilterParameter) intent.getExtras().get(Global.EXTRA_FILTER); 
		
		if (newRangeFilter == null) {
			newRangeFilter = previosRangeFilter;
		}

		initializeDateRanges(newRangeFilter);
		return newRangeFilter;
	}

}
