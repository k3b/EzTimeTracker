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

	private final Activity activity;
	private final ReportInterface report;
	private LinearScroller scrollView;
	private long mFromDate = TimeSlice.NO_TIME_VALUE;
	private long mToDate = TimeSlice.NO_TIME_VALUE;
	private TextView startDateTV;
	private TextView endDateTV;
	private List<TextView> reportViewList;

	public enum ReportTypes {
		TIMESHEET, SUMMARY
	};

	private ReportTypes reportType;

	public void setReportType(ReportTypes reportType) {
		this.reportType = reportType;
	}

	ReportFramework(Activity activity, ReportInterface report) {
		super();
		initializeDateRanges();
		this.activity = activity;
		this.report = report;
	}

	public long getStartDateRange() {
		return mFromDate;
	}

	private void initializeDateRanges() {
		Date currDate = new Date();
		if (mFromDate == TimeSlice.NO_TIME_VALUE) {
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(currDate);
			calendar.set(Calendar.HOUR, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.roll(Calendar.MONTH, false);
			mFromDate = calendar.getTimeInMillis();
			if (mFromDate > currDate.getTime()) {
				calendar.roll(Calendar.YEAR, false);
				mFromDate = calendar.getTimeInMillis();
			}
		}
		if (mToDate == TimeSlice.NO_TIME_VALUE) {
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(currDate);
			mToDate = calendar.getTimeInMillis();
		}
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean result = true;
		SubMenu exportMenu = menu.addSubMenu(Menu.NONE, Menu.NONE, 2, R.string.menu_export_report);
		exportMenu.add(Menu.NONE, MENU_ITEM_EXPORT_SD, 1, R.string.menu_export_report_to_sd_card);
		exportMenu.add(Menu.NONE, MENU_ITEM_EXPORT_EMAIL, 2, R.string.menu_email_report);
		SubMenu dateRangeMenu = menu.addSubMenu(Menu.NONE, Menu.NONE, 3, R.string.menu_select_date_range);
		String labelStartDate = String.format(activity.getString(R.string.formatStartDate)
				, DateTimeFormatter.getLongDateStr(getStartDateRange()));
		dateRangeMenu.add(Menu.NONE, MENU_ITEM_START_DATE, 0, labelStartDate);
		
		String labelEndDate = String.format(activity.getString(R.string.formatEndDate), 
				DateTimeFormatter.getLongDateStr(getEndDateRange()));
		dateRangeMenu.add(Menu.NONE, MENU_ITEM_END_DATE, 1, labelEndDate);

		return result;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ITEM_START_DATE:
			showDateRangeDialog(GET_START_DATETIME);
			break;
		case MENU_ITEM_END_DATE:
			showDateRangeDialog(GET_END_DATETIME);
			break;
		case MENU_ITEM_EXPORT_SD:
			SDDataExporter.exportToSD(getDefaultReportName(), activity, ReportOutput.makeFormatter(reportViewList));
			break;
		case MENU_ITEM_EXPORT_EMAIL:
			ReportOutput outPutter = ReportOutput.makeFormatter(reportViewList);
			outPutter.setTerminator("\n");
			EmailUtilities.send("", getEMailSummaryLine(), activity, outPutter.getOutput());
			break;
		}

		return true;
	}

	private void showDateRangeDialog(int doingSetStartDateRange) {
		activity.showDialog(doingSetStartDateRange);
	}

	private String getDefaultReportName() {
		String name;
		if (reportType == ReportTypes.TIMESHEET) {
			name = activity.getString(R.string.default_export_ts_name);
		} else {
			name = activity.getString(R.string.default_export_sum_name);
		}
		return name;
	}

	private String getEMailSummaryLine() {
		String appName = activity.getString(R.string.app_name);
		String summary;
		if (reportType == ReportTypes.TIMESHEET) {
			summary = String.format(activity.getString(R.string.default_mail_ts_subject), appName);
		} else {
			summary = String.format(activity.getString(R.string.default_mail_sum_subject), appName);
		}
		return summary;

	}

    // define the listener which is called once a user selected the date.
    private DateSlider.OnDateSetListener mDateTimeSetListenerStart =
        new DateSlider.OnDateSetListener() {
            public void onDateSet(DateSlider view, Calendar selectedDate) {
                // update the dateText view with the corresponding date
            	mFromDate = selectedDate.getTimeInMillis();
    			report.loadDataIntoReport(0);
            }
    };

    // define the listener which is called once a user selected the date.
    private DateSlider.OnDateSetListener mDateTimeSetListenerEnd =
        new DateSlider.OnDateSetListener() {
            public void onDateSet(DateSlider view, Calendar selectedDate) {
                // update the dateText view with the corresponding date
            	mToDate = selectedDate.getTimeInMillis();
    			report.loadDataIntoReport(0);
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
        	c.setTimeInMillis(getStartDateRange());
            return new DefaultDateSlider(this.activity,mDateTimeSetListenerStart,c);
        case GET_END_DATETIME:
        	c.setTimeInMillis(getEndDateRange());
            return new DefaultDateSlider(this.activity,mDateTimeSetListenerEnd,c);
        }
        return null;
    }
	
	public void setStartDateRange(long mFromDate) {
		this.mFromDate = mFromDate;
	}

	public long getEndDateRange() {

		return mToDate;
	}

	public void setEndDateRange(long mToDate) {
		this.mToDate = mToDate;
	}

	LinearScroller getLinearScroller() {
		return scrollView;
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
		activity.setContentView(R.layout.report_framework);
		startDateTV = new TextView(activity);
		endDateTV = new TextView(activity);
		LinearLayout contentView = (LinearLayout) activity.findViewById(R.id.report_frame);
		contentView.setOrientation(LinearLayout.VERTICAL);
		scrollView = new LinearScroller(activity);
		startDateTV.setPadding(20, 0, 0, 0);
		String labelStartDate = String.format(this.activity.getString(R.string.formatStartDate).toString(), 
				DateTimeFormatter.getLongDateStr(mFromDate));
		startDateTV.setText(labelStartDate, TextView.BufferType.SPANNABLE);
		Spannable str = (Spannable) startDateTV.getText();
		str.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), 0, 11, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		str.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, 11, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		contentView.addView(startDateTV);
		endDateTV.setPadding(49, 0, 0, 0);
		String labelEndDate = String.format(this.activity.getString(R.string.formatEndDate).toString(), 
				DateTimeFormatter.getLongDateStr(mToDate));
		endDateTV.setText(labelEndDate,
				TextView.BufferType.SPANNABLE);
		str = (Spannable) endDateTV.getText();
		str.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), 0, 9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		str.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, 9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		contentView.addView(endDateTV);
		contentView.addView(scrollView.getScrollView());
		configureDateRangeView(startDateTV, GET_START_DATETIME);
		configureDateRangeView(endDateTV, GET_END_DATETIME);
		return contentView;
	}

	public List<TextView> initializeTextViewsForExportList() {
		List<TextView> tvList = new ArrayList<TextView>();
		tvList.add(startDateTV);
		tvList.add(endDateTV);
		reportViewList = tvList;
		return tvList;
	}

}
