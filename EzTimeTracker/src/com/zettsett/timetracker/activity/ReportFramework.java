package com.zettsett.timetracker.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.style.StyleSpan;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.googlecode.android.widgets.DateSlider.DateSlider;
import com.googlecode.android.widgets.DateSlider.DefaultDateSlider;

import com.zetter.androidTime.R;
import com.zettsett.timetracker.DateTimeFormatter;
import com.zettsett.timetracker.EmailUtilities;
import com.zettsett.timetracker.LinearScroller;
import com.zettsett.timetracker.model.TimeSlice;
import com.zettsett.timetracker.report.ReportInterface;
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
	
	protected static final int GET_START_DATETIME = 0;
	protected static final int GET_END_DATETIME = 1;

	private static final int MENU_ITEM_START_DATE = Menu.FIRST + 10;
	private static final int MENU_ITEM_END_DATE = Menu.FIRST + 11;
	private static final int MENU_ITEM_EXPORT_SD = Menu.FIRST + 12;
	private static final int MENU_ITEM_EXPORT_EMAIL = Menu.FIRST + 13;

	private static final int MENU_ITEM_SET_FILTER = Menu.FIRST + 21;

	private final Activity mActivity;
	private final ReportInterface mReport;
	private LinearScroller mScrollView;
	
	private FilterParameter mFilter;
	private TextView mStartDateTV;
	private TextView mEndDateTV;
	private List<TextView> mReportViewList;

	public enum ReportTypes {
		TIMESHEET, SUMMARY
	};

	private ReportTypes reportType;

	public void setReportType(ReportTypes reportType) {
		this.reportType = reportType;
	}

	ReportFramework(Activity activity, ReportInterface report, FilterParameter filter) {
		super();
		initializeDateRanges(filter);
		this.mActivity = activity;
		this.mReport = report;
	}

	private void initializeDateRanges(FilterParameter filter) {
		mFilter = (filter != null) ? filter : new FilterParameter();
		Date currDate = new Date();
		long startTime = filter.getStartTime();
		if (startTime  == TimeSlice.NO_TIME_VALUE) {
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(currDate);
			calendar.set(Calendar.HOUR, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.roll(Calendar.MONTH, false);
			startTime = calendar.getTimeInMillis();
			if (startTime > currDate.getTime()) {
				calendar.roll(Calendar.YEAR, false);
				startTime = calendar.getTimeInMillis();
			}
		}
		filter.setStartTime(startTime);
		
		long endTime = filter.getEndTime();
		if (endTime == TimeSlice.NO_TIME_VALUE) {
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(currDate);
			endTime = calendar.getTimeInMillis();
		}
		filter.setEndTime(endTime);
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean result = true;
		SubMenu exportMenu = menu.addSubMenu(Menu.NONE, Menu.NONE, 2, R.string.menu_export_report);
		exportMenu.add(Menu.NONE, MENU_ITEM_EXPORT_SD, 1, R.string.menu_export_report_to_sd_card);
		exportMenu.add(Menu.NONE, MENU_ITEM_EXPORT_EMAIL, 2, R.string.menu_email_report);
		SubMenu dateRangeMenu = menu.addSubMenu(Menu.NONE, Menu.NONE, 3, R.string.menu_select_date_range);
		
		String labelStartDate = String.format(mActivity.getString(R.string.formatStartDate)
				, DateTimeFormatter.getLongDateStr(mFilter.getStartTime()));
		dateRangeMenu.add(Menu.NONE, MENU_ITEM_START_DATE, 0, labelStartDate);
		
		String labelEndDate = String.format(mActivity.getString(R.string.formatEndDate), 
				DateTimeFormatter.getLongDateStr(mFilter.getEndTime()));
		dateRangeMenu.add(Menu.NONE, MENU_ITEM_END_DATE, 1, labelEndDate);

		dateRangeMenu.add(Menu.NONE, MENU_ITEM_SET_FILTER, 2, R.string.menu_filter);

		return result;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ITEM_SET_FILTER:
			ReportFilterActivity.showActivity(this.mActivity, this.mFilter);
			break;
		case MENU_ITEM_START_DATE:
			showDateRangeDialog(GET_START_DATETIME);
			break;
		case MENU_ITEM_END_DATE:
			showDateRangeDialog(GET_END_DATETIME);
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

	private void showDateRangeDialog(int doingSetStartDateRange) {
		mActivity.showDialog(doingSetStartDateRange);
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

    // define the listener which is called once a user selected the date.
    private DateSlider.OnDateSetListener mDateTimeSetListenerStart =
        new DateSlider.OnDateSetListener() {
            public void onDateSet(DateSlider view, Calendar selectedDate) {
                // update the dateText view with the corresponding date
            	mFilter.setStartTime(selectedDate.getTimeInMillis());
    			mReport.loadDataIntoReport(0);
            }
    };

    // define the listener which is called once a user selected the date.
    private DateSlider.OnDateSetListener mDateTimeSetListenerEnd =
        new DateSlider.OnDateSetListener() {
            public void onDateSet(DateSlider view, Calendar selectedDate) {
                // update the dateText view with the corresponding date
        		mFilter.setEndTime(selectedDate.getTimeInMillis());
    			mReport.loadDataIntoReport(0);
            }
    };

//    @Override
    public Dialog onCreateDialog(int id) {
        // this method is called after invoking 'showDialog' for the first time
        // here we initiate the corresponding DateSlideSelector and return the dialog to its caller
    	
    	// get today's date and time
        final Calendar c = Calendar.getInstance();
        
        switch (id) {
        case GET_START_DATETIME:
        	c.setTimeInMillis(mFilter.getStartTime());
            return new DefaultDateSlider(this.mActivity,mDateTimeSetListenerStart,c);
        case GET_END_DATETIME:
        	c.setTimeInMillis(mFilter.getEndTime());
            return new DefaultDateSlider(this.mActivity,mDateTimeSetListenerEnd,c);
        }
        return null;
    }
	
	LinearScroller getLinearScroller() {
		return mScrollView;
	}

	private void configureDateRangeView(final TextView view, final int isForStartDate) {
		view.setTextColor(Color.CYAN);
		view.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					((TextView) view).setTextColor(Color.BLUE);
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					((TextView) view).setTextColor(Color.CYAN);
				}
				return false;
			}
		});
		view.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
				showDateRangeDialog(isForStartDate);
				return false;
			}
		});
	}

	LinearLayout buildViews() {
		mActivity.setContentView(R.layout.report_framework);
		mStartDateTV = new TextView(mActivity);
		mEndDateTV = new TextView(mActivity);
		LinearLayout contentView = (LinearLayout) mActivity.findViewById(R.id.report_frame);
		contentView.setOrientation(LinearLayout.VERTICAL);
		mScrollView = new LinearScroller(mActivity);
		mStartDateTV.setPadding(20, 0, 0, 0);
		String labelStartDate = String.format(this.mActivity.getString(R.string.formatStartDate).toString(), 
				DateTimeFormatter.getLongDateStr(mFilter.getStartTime()));
		mStartDateTV.setText(labelStartDate, TextView.BufferType.SPANNABLE);
		Spannable str = (Spannable) mStartDateTV.getText();
		str.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), 0, 11, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		str.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, 11, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		contentView.addView(mStartDateTV);
		mEndDateTV.setPadding(49, 0, 0, 0);
		String labelEndDate = String.format(this.mActivity.getString(R.string.formatEndDate).toString(), 
				DateTimeFormatter.getLongDateStr(mFilter.getEndTime()));
		mEndDateTV.setText(labelEndDate,
				TextView.BufferType.SPANNABLE);
		str = (Spannable) mEndDateTV.getText();
		str.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), 0, 9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		str.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, 9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		contentView.addView(mEndDateTV);
		contentView.addView(mScrollView.getScrollView());
		configureDateRangeView(mStartDateTV, GET_START_DATETIME);
		configureDateRangeView(mEndDateTV, GET_END_DATETIME);
		return contentView;
	}

	public List<TextView> initializeTextViewsForExportList() {
		List<TextView> tvList = new ArrayList<TextView>();
		tvList.add(mStartDateTV);
		tvList.add(mEndDateTV);
		mReportViewList = tvList;
		return tvList;
	}

	public static FilterParameter getLastFilter(Bundle savedInstanceState, String parameterName) {
		FilterParameter rangeFilter = null;
		if (savedInstanceState != null) {
			Serializable filter = savedInstanceState.getSerializable(parameterName);
			
			if (filter instanceof FilterParameter)
				rangeFilter = (FilterParameter) filter;
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

}
