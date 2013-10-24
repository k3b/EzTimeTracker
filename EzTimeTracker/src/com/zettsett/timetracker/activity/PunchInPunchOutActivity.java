package com.zettsett.timetracker.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.zetter.androidTime.R;
import com.zettsett.timetracker.DateTimeFormatter;
import com.zettsett.timetracker.Global;
import com.zettsett.timetracker.Settings;
import com.zettsett.timetracker.TimeTrackerManager;
import com.zettsett.timetracker.TimeTrackerSessionData;
import com.zettsett.timetracker.database.TimeSliceCategoryRepsitory;
import com.zettsett.timetracker.model.TimeSlice;
import com.zettsett.timetracker.model.TimeSliceCategory;

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
 * GUI to start and/or stop time tracking: Workflow:
 * stopped+start->showSelectCategoryForPunchInDialog
 * ->(createNewCategory->)started(now)
 * stopped+long-start->editStartSettings()->started(selected-time,
 * selectedCategory)
 * 
 * started+long-start->editStartSettings()->started(selected-time,
 * selectedCategory) started+stop->punchOutClock()->stopped(now)
 * started+start->showSelectCategoryForPunchInDialog
 * ->(createNewCategory->)stop(now)+started(now)
 */
public class PunchInPunchOutActivity extends Activity implements
		OnChronometerTickListener, ICategorySetter {
	public static final String PREFS_NAME = "TimerPrefs";

	private static final int SELECT_CATAGORY = 0;
	private static final int CREATE_NEW_CATEGORY = 1;
	private static final int EDIT_START = 2;
	private static final int EDIT_STOP = 3;
	private static final int SELECT_CATAGORY_ALL = 4;

	private TextView elapsedTimeDisplay;
	private EditText notesEditor;

	private final TimeSliceCategoryRepsitory timeSliceRepository = new TimeSliceCategoryRepsitory(
			this);

	private TimeTrackerSessionData sessionData = new TimeTrackerSessionData();
	private TimeTrackerManager tracker = null;

	private BroadcastReceiver myReceiver = null;

	class _RemoteTimeTrackerReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (Global.isInfoEnabled()) {
				Log.i(Global.LOG_CONTEXT,
						"PunchInPunchOutActivity.onReceive(intent='" + intent
								+ "')");
			}
			PunchInPunchOutActivity.this.reloadGui();
		}
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		if (Global.isDebugEnabled()) {
			Log.d(Global.LOG_CONTEXT, "PunchInPunchOutActivity()");
		}

		super.onCreate(savedInstanceState);
		// DateTimeFormatter.getInstance().SetFormat(DateFormat.get
		// DateInstance(DateFormat.S));
		this.setContentView(R.layout.time_slice_main);
		this.elapsedTimeDisplay = (TextView) this
				.findViewById(R.id.mainViewChronOutput);
		this.elapsedTimeDisplay
				.setOnLongClickListener(new View.OnLongClickListener() {

					@Override
					public boolean onLongClick(final View v) {
						return PunchInPunchOutActivity.this
								.startActivity(R.id.details);
					}
				});

		this.notesEditor = (EditText) this
				.findViewById(R.id.main_edit_text_notes);

		this.setupButtons();

		this.tracker = new TimeTrackerManager(this);
		Settings.init(this.getBaseContext());

		this.reloadGui();
	}

	@Override
	public void onResume() {
		super.onResume();
		Settings.init(this.getBaseContext());

		if (this.myReceiver == null) {
			this.myReceiver = new _RemoteTimeTrackerReceiver();
			final IntentFilter filter = new IntentFilter(Global.REFRESH_GUI);
			this.registerReceiver(this.myReceiver, filter);
		}

		this.reloadGui();

	}

	@Override
	public void onPause() {

		if (this.myReceiver != null) {
			this.unregisterReceiver(this.myReceiver);
			this.myReceiver = null;
		}

		this.sessionData.setNotes(this.notesEditor.getText().toString());
		super.onPause();

		this.startStopTimer(true);
		this.saveState();
	}

	/**
	 * displays current session-data, start/stop elapsedTimer if necessary
	 */
	void reloadGui() {
		if (Global.isDebugEnabled()) {
			Log.d(Global.LOG_CONTEXT, "PunchInPunchOutActivity.refreshGui()");
		}

		this.sessionData = this.reloadSessionData();

		if (!this.tracker.isPunchedIn()) {
			this.updateElapsedTimeLabel(this.tracker
					.getElapsedTimeInMillisecs());
		}

		this.updateClock(this.tracker.isPunchedIn());
		this.showData();
		this.updateCategoryAndStartLabel();
	}

	private void showData() {
		final boolean punchedIn = this.sessionData.isPunchedIn();
		this.notesEditor.setEnabled(punchedIn);
		if (!punchedIn) {
			this.elapsedTimeDisplay.setTextColor(Color.RED);
		} else {
			this.elapsedTimeDisplay.setTextColor(Color.GREEN);
		}
		this.updateChronOutputTextView();
		this.getNotesView().setText(this.sessionData.getNotes());
		this.setSelectedTimeSliceCategory(this.sessionData.getCategory());
	}

	private EditText getNotesView() {
		return (EditText) this.findViewById(R.id.main_edit_text_notes);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		super.onCreateOptionsMenu(menu);

		final MenuInflater inflater = this.getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		final int itemId = item.getItemId();
		if (this.startActivity(itemId)) {
			return true;
		} else {
			switch (itemId) {
			case R.id.about:
				this.showDialog(itemId);
				return true;
			}
			return super.onOptionsItemSelected(item);
		}
	}

	private boolean startActivity(final int itemId) {
		final Class<? extends Activity> itemHandler = this
				.getMenuIntentHandler(itemId);
		if (itemHandler != null) {
			final Intent intent = new Intent().setClass(this, itemHandler);
			intent.putExtra(
					TimeSheetSummaryReportActivity.SAVED_MENU_ID_BUNDLE_NAME,
					itemId);
			this.startActivity(intent);
			return true;
		}
		return false;
	}

	private Dialog getAboutDialog() {
		final Context mContext = this;
		final AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
		alert.setTitle(R.string.about_title);
		alert.setIcon(R.drawable.icon);
		alert.setNeutralButton(R.string.cancel,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(
							final DialogInterface paramDialogInterface,
							final int paramInt) {
						paramDialogInterface.cancel();

					}
				});
		final WebView wv = new WebView(mContext);
		String html = this.getResources().getString(R.string.about_content); // "<html><body>some <b>html</b> here</body></html>";

		final Context context = this;
		try {
			final String versionName = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0).versionName;
			html = html.replace("$versionName$", versionName);
		} catch (final NameNotFoundException e) {
		}
		html = html.replace("$about$",
				this.getText(R.string.about_content_about));

		wv.loadData(html, "text/html", "UTF-8");
		wv.setVerticalScrollBarEnabled(true);

		final WebSettings mWebSettings = wv.getSettings();
		mWebSettings.setBuiltInZoomControls(true);
		wv.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
		wv.setScrollbarFadingEnabled(false);

		alert.setView(wv);

		return alert.create();
	}

	private Class<? extends Activity> getMenuIntentHandler(final int item) {
		switch (item) {
		case R.id.details:
			return TimeSheetDetailListActivity.class; // TimeSheetDetailReportActivity.class;
		case R.id.summary_day:
		case R.id.summary_month:
		case R.id.summary_week:
		case R.id.category_day:
		case R.id.category_month:
		case R.id.category_week:
			return TimeSheetSummaryReportActivity.class;
		case R.id.categories:
			return CategoryListActivity.class;
		case R.id.export:
			return TimeSliceExportActivity.class;
		case R.id.remove:
			return TimeSliceRemoveActivity.class;
		case R.id.settings:
			return SettingsActivity.class;
		default:
			return null;
		}
	}

	private void showSelectCategoryForPunchInDialog() {
		this.showDialog(PunchInPunchOutActivity.SELECT_CATAGORY);
	}

	private CategoryEditDialog edit = null;

	public void showCategoryEditDialog(final TimeSliceCategory category) {
		if (this.edit == null) {
			this.edit = new CategoryEditDialog(this, this);
		}
		this.edit.setCategory(category);
		this.showDialog(PunchInPunchOutActivity.CREATE_NEW_CATEGORY);
	}

	@Override
	protected Dialog onCreateDialog(final int id) {
		switch (id) {
		case SELECT_CATAGORY:
			return new CategorySelectDialog(this, R.style.PunchDialog,
					TimeSliceCategory.NO_CATEGORY).setCategoryCallback(this);
		case SELECT_CATAGORY_ALL:
			return new CategorySelectDialog(this, R.style.PunchDialog,
					TimeSliceCategory.NO_CATEGORY).setCategoryCallback(this);
		case CREATE_NEW_CATEGORY:
			return this.edit;
		case R.id.about:
			return this.getAboutDialog();
		}
		return null;
	}

	private void setupButtons() {
		final Button punchInButton = (Button) this
				.findViewById(R.id.btnPunchIn);
		punchInButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View view) {
				PunchInPunchOutActivity.this
						.showSelectCategoryForPunchInDialog();
			}
		});
		punchInButton.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(final View v) {
				return PunchInPunchOutActivity.this.editStartSettings();
			}
		});

		final Button punchOutButton = (Button) this
				.findViewById(R.id.btnPunchOut);
		punchOutButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View view) {
				PunchInPunchOutActivity.this.punchOutClock(TimeTrackerManager
						.currentTimeMillis());
			}
		});
		punchOutButton.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(final View v) {
				return PunchInPunchOutActivity.this.editStopSettings();
			}
		});

	}

	/**
	 * Called by showSelectCategoryForPunchInDialog
	 */
	@Override
	public void setCategory(final TimeSliceCategory selectedCategory) {
		if (selectedCategory == TimeSliceCategory.NO_CATEGORY) {
			this.showCategoryEditDialog(null);
		} else {
			if (selectedCategory.getRowId() == TimeSliceCategory.NOT_SAVED) {
				this.timeSliceRepository
						.createTimeSliceCategory(selectedCategory);
			}
			final long elapsedRealtime = TimeTrackerManager.currentTimeMillis();
			this.punchInClock(elapsedRealtime, selectedCategory);
		}
	}

	private void punchInClock(final long elapsedRealtime,
			final TimeSliceCategory selectedCategory) {
		this.tracker.punchInClock(selectedCategory, elapsedRealtime);
		this.reloadGui();
	}

	private void punchOutClock(final long elapsedRealtime) {
		this.tracker.punchOutClock(elapsedRealtime, this.notesEditor.getText()
				.toString());
		this.reloadGui();
	}

	private void updateElapsedTimeLabel(final long elapsedRealtime) {
		if (this.elapsedTimeDisplay != null) {
			this.elapsedTimeDisplay.setText(DateTimeFormatter.getInstance()
					.hrColMin(elapsedRealtime, false, true));
		}
	}

	private void startStopTimer(final boolean punchedIn) {
		final Chronometer chronometer = (Chronometer) this
				.findViewById(R.id.chron);

		if (punchedIn) {
			chronometer.start();
			chronometer.setOnChronometerTickListener(this);
			this.elapsedTimeDisplay.setTextColor(Color.GREEN);
		} else {
			chronometer.stop();
			chronometer.setOnChronometerTickListener(null);
			this.elapsedTimeDisplay.setTextColor(Color.RED);
		}
		this.updateChronOutputTextView();
	}

	private boolean editStartSettings() {
		if (this.sessionData != null) {
			final TimeSlice editItem = new TimeSlice(32531)
					.setCategory(this.sessionData.getCategory())
					.setEndTime(TimeSliceEditActivity.HIDDEN)
					.setNotes(TimeSliceEditActivity.HIDDEN_NOTES);
			// edit already running starttime
			if (this.sessionData.isPunchedIn()) {
				editItem.setStartTime(this.sessionData.getStartTime());
			} else {
				editItem.setStartTime(TimeTrackerManager.currentTimeMillis());
			}
			TimeSliceEditActivity.showTimeSliceEditActivity(this, editItem,
					PunchInPunchOutActivity.EDIT_START);
		}

		return true; // consumed
	}

	private boolean editStopSettings() {
		if ((this.sessionData != null) && (this.sessionData.isPunchedIn())) {
			final TimeSlice editItem = new TimeSlice(32531)
					.setCategory(this.sessionData.getCategory())
					.setStartTime(this.sessionData.getStartTime())
					.setEndTime(TimeTrackerManager.currentTimeMillis())
					.setNotes(TimeSliceEditActivity.HIDDEN_NOTES);
			TimeSliceEditActivity.showTimeSliceEditActivity(this, editItem,
					PunchInPunchOutActivity.EDIT_STOP);
		}
		return true; // consumed
	}

	/**
	 * Call back from sub-activities.<br/>
	 * Process Change StartTime (longpress start), Select StopTime befor stop
	 * (longpress stop)
	 */
	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		final TimeSlice updatedTimeSlice = this.getTimeSlice(intent);
		this.sessionData = this.reloadSessionData();
		if ((this.sessionData != null) && (updatedTimeSlice != null)) { // (requestCode
																		// ==
																		// EDIT_START)
																		// ||
																		// (requestCode
																		// ==
																		// EDIT_STOP))
																		// {

			final boolean punchedIn = this.sessionData.isPunchedIn();
			if ((requestCode == PunchInPunchOutActivity.EDIT_START)
					&& !punchedIn) {
				this.punchInClock(updatedTimeSlice.getStartTime(),
						updatedTimeSlice.getCategory());

			} else if ((requestCode == PunchInPunchOutActivity.EDIT_START)
					&& punchedIn) {
				this.sessionData.setCategory(updatedTimeSlice.getCategory())
						.setStartTime(updatedTimeSlice.getStartTime());
				this.saveState();
				this.reloadGui();
			} else if ((requestCode == PunchInPunchOutActivity.EDIT_STOP)
					&& punchedIn) {
				this.sessionData.setCategory(updatedTimeSlice.getCategory())
						.setStartTime(updatedTimeSlice.getStartTime());
				this.saveState();
				this.reloadGui();
				this.punchOutClock(updatedTimeSlice.getEndTime());
			}
		}
	}

	private TimeSlice getTimeSlice(final Intent intent) {
		return (intent != null) ? ((TimeSlice) intent.getExtras().get(
				Global.EXTRA_TIMESLICE)) : null;
	}

	@Override
	public void onChronometerTick(final Chronometer chron) {
		this.updateChronOutputTextView();
	}

	private void updateClock(final boolean isPunchedIn) {
		this.startStopTimer(isPunchedIn);
		if (isPunchedIn) {
			this.updateCategoryAndStartLabel();
		}
	}

	private void updateChronOutputTextView() {
		final long elapsed = (!this.tracker.isPunchedIn()) ? this.tracker
				.getElapsedTimeInMillisecs() : (TimeTrackerManager
				.currentTimeMillis() - this.sessionData.getStartTime());
		this.updateElapsedTimeLabel(elapsed);
	}

	private void updateCategoryAndStartLabel() {
		final TextView labelTv = (TextView) this
				.findViewById(R.id.tvTimeInActivity);
		if ((this.sessionData != null)
				&& (this.sessionData.getCategory() != null)) {
			final String labelCategory = String.format(
					this.getText(R.string.format_category).toString(),
					this.sessionData.getCategoryName());
			labelTv.setText(labelCategory);
		} else {
			labelTv.setText(R.string.label_no_current_activity);
		}
		final TextView tvStartTime = (TextView) this
				.findViewById(R.id.tvStartTime);
		final String labelStartTime = String.format(
				this.getText(R.string.format_start_time_).toString(),
				this.sessionData.getStartTimeStr());
		tvStartTime.setText(labelStartTime);
	}

	private void setSelectedTimeSliceCategory(final TimeSliceCategory category) {
		final Spinner catSpinner = (Spinner) this
				.findViewById(R.id.MainTimeSliceCategory);
		for (int position = 0; position < catSpinner.getCount(); position++) {
			if (catSpinner.getItemAtPosition(position).equals(category)) {
				catSpinner.setSelection(position);
			}
		}
	}

	private void saveState() {
		this.tracker.saveState();
	}

	private TimeTrackerSessionData reloadSessionData() {
		return this.tracker.reloadSessionData(); // ((TimeTrackerSessionData)
													// getLastNonConfigurationInstance());
	}

}