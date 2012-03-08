package com.zettsett.timetracker.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.text.Spannable;
import android.text.format.DateFormat;
import android.text.style.StyleSpan;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zetter.androidTime.R;
import com.zettsett.timetracker.DateTimeFormatter;
import com.zettsett.timetracker.EmailUtilities;
import com.zettsett.timetracker.LinearScroller;
import com.zettsett.timetracker.report.ReportInterface;
import com.zettsett.timetracker.report.ReportOutput;
import com.zettsett.timetracker.report.SDDataExporter;

/**
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
public class ReportFramework implements Serializable {
	private static final long serialVersionUID = 394933866214361393L;
	private final Activity activity;
	private final ReportInterface report;
	private LinearScroller scrollView;
	private long startDateRange = 0;
	private long endDateRange = 0;
	private static final int MENU_ITEM_START_DATE = Menu.FIRST + 10;
	private static final int MENU_ITEM_END_DATE = Menu.FIRST + 11;
	private static final int MENU_ITEM_EXPORT_SD = Menu.FIRST + 12;
	private static final int MENU_ITEM_EXPORT_EMAIL = Menu.FIRST + 13;
	private boolean doingSetStartDateRange = false;
	private static final int DATE_DIALOG_ID = 0;
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
		return startDateRange;
	}

	private void initializeDateRanges() {
		Date currDate = new Date();
		if (startDateRange == 0) {
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(currDate);
			calendar.set(Calendar.HOUR, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.roll(Calendar.MONTH, false);
			startDateRange = calendar.getTimeInMillis();
			if (startDateRange > currDate.getTime()) {
				calendar.roll(Calendar.YEAR, false);
				startDateRange = calendar.getTimeInMillis();
			}
		}
		if (endDateRange == 0) {
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(currDate);
			endDateRange = calendar.getTimeInMillis();
		}
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean result = true;
		SubMenu exportMenu = menu.addSubMenu(Menu.NONE, Menu.NONE, 2, "Export Report");
		exportMenu.add(Menu.NONE, MENU_ITEM_EXPORT_SD, 1, "Export Report to SD Card");
		exportMenu.add(Menu.NONE, MENU_ITEM_EXPORT_EMAIL, 2, "Email Report");
		SubMenu dateRangeMenu = menu.addSubMenu(Menu.NONE, Menu.NONE, 3, "Select Date Range");
		dateRangeMenu.add(Menu.NONE, MENU_ITEM_START_DATE, 0, "Start: "
				+ DateFormat.format("E, MMMM dd, yyyy", getStartDateRange()).toString());
		dateRangeMenu.add(Menu.NONE, MENU_ITEM_END_DATE, 1, "End: "
				+ DateFormat.format("E, MMMM dd, yyyy", getEndDateRange()).toString());

		return result;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ITEM_START_DATE:
			showDateRangeDialog(true);
			break;
		case MENU_ITEM_END_DATE:
			showDateRangeDialog(false);
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

	private void showDateRangeDialog(boolean doingSetStartDateRange) {
		this.doingSetStartDateRange = doingSetStartDateRange;
		activity.showDialog(DATE_DIALOG_ID);

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
		String summary;
		if (reportType == ReportTypes.TIMESHEET) {
			summary = activity.getString(R.string.default_mail_ts_subject);
		} else {
			summary = activity.getString(R.string.default_mail_sum_subject);
		}
		return summary;

	}

	void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case DATE_DIALOG_ID:
			final Calendar c = Calendar.getInstance();
			if (doingSetStartDateRange) {
				c.setTimeInMillis(getStartDateRange());
			} else {
				c.setTimeInMillis(getEndDateRange());
			}
			((DatePickerDialog) dialog).updateDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c
					.get(Calendar.DAY_OF_MONTH));
		}
	}

	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_DIALOG_ID:
			final Calendar c = Calendar.getInstance();
			return new DatePickerDialog(activity, mDateSetListener, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c
					.get(Calendar.DAY_OF_MONTH));
		}
		return null;
	}

	private final DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			setDateRange(year, monthOfYear, dayOfMonth, doingSetStartDateRange);
			report.loadDataIntoReport();
		}
	};

	void setDateRange(int year, int monthOfYear, int dayOfMonth, boolean doingSetStartDateRange) {
		final Calendar c = DateTimeFormatter.getCalendar(year, monthOfYear, dayOfMonth);
		if (doingSetStartDateRange) {
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			startDateRange = c.getTimeInMillis();
		} else {
			endDateRange = c.getTimeInMillis();
		}
	}

	public void setStartDateRange(long startDateRange) {
		this.startDateRange = startDateRange;
	}

	public long getEndDateRange() {

		return endDateRange;
	}

	public void setEndDateRange(long endDateRange) {
		this.endDateRange = endDateRange;
	}

	LinearScroller getLinearScroller() {
		return scrollView;
	}

	private void configureDateRangeView(final TextView view, final boolean isForStartDate) {
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
		startDateTV.setText("From date: " + DateFormat.format("E, MMMM dd, yyyy", startDateRange),
				TextView.BufferType.SPANNABLE);
		Spannable str = (Spannable) startDateTV.getText();
		str.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), 0, 11, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		str.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, 11, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		contentView.addView(startDateTV);
		endDateTV.setPadding(49, 0, 0, 0);
		endDateTV.setText("To date: " + DateFormat.format("E, MMMM dd, yyyy", endDateRange),
				TextView.BufferType.SPANNABLE);
		str = (Spannable) endDateTV.getText();
		str.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), 0, 9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		str.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, 9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		contentView.addView(endDateTV);
		contentView.addView(scrollView.getScrollView());
		configureDateRangeView(startDateTV, true);
		configureDateRangeView(endDateTV, false);
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
