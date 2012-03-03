package com.zettsett.timetracker.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.opengl.Visibility;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.widget.Chronometer.OnChronometerTickListener;

import com.zetter.androidTime.R;
import com.zettsett.timetracker.*;
import com.zettsett.timetracker.database.DatabaseInstance;
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
 */
public class MainActivity extends Activity implements OnChronometerTickListener {
	private TextView elapsedTimeDisplay;
	private EditText notesEditor;

	private TimeTrackerSessionData sessionData = new TimeTrackerSessionData();
	private TimeTrackerManager tracker = null; 

	public static final String PREFS_NAME = "TimerPrefs";

	private BroadcastReceiver myReceiver = null;
	
	class _RemoteTimeTrackerReceiver extends BroadcastReceiver {
		@Override
		public void onReceive (Context context, Intent intent) {
			if (Log.isLoggable(Global.LOG_CONTEXT, Log.INFO))
			{
				Log.i(Global.LOG_CONTEXT, "MainActivity.onReceive(intent='" + intent + "')");
			}
			
			reloadGui();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (Global.isDebugEnabled())
		{
			Log.d(Global.LOG_CONTEXT, "MainActivity()");
		}
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		this.elapsedTimeDisplay = (TextView) findViewById(R.id.mainViewChronOutput);
		this.notesEditor = (EditText) findViewById(R.id.main_edit_text_notes);

		setupButtons();

		this.tracker = new TimeTrackerManager(this);
		Settings.initializeCurrentTimeFormatSetting(getBaseContext());
		DatabaseInstance.initialize(this);
		DatabaseInstance.open();

		reloadGui();
	}

	@Override
	public void onResume() {
		super.onResume();
		
		DatabaseInstance.open();

		if (myReceiver == null)
		{
			myReceiver = new _RemoteTimeTrackerReceiver();
		    IntentFilter filter = new IntentFilter(Global.REFRESH_GUI);
			registerReceiver(myReceiver, filter);
		}
				
		reloadGui();

	}

	@Override
	public void onPause() {

		if (myReceiver != null)
		{
			unregisterReceiver(myReceiver);
			myReceiver = null;
		}
	
		this.sessionData.setNotes(this.notesEditor.getText().toString());
		super.onPause();
		
		startStopTimer(true);
		saveState();
	}

	/**
	 * displays current session-data, start/stop elapsedTimer if necessary
	 */
	void reloadGui()
	{
		Log.d(Global.LOG_CONTEXT, "MainActivity.refreshGui()");

		sessionData = reloadSessionData();
		
		if (this.tracker.isPunchedOut())
		{
			updateElapsedTimeLabel(tracker.getElapsedTimeInMillisecs());
		}

		updateClock(this.tracker.isPunchedOut());
		showData();
		updateCategoryAndStartLabel();
	}
	
	private void showData() {
		boolean punchedOut = sessionData.isPunchedOut();
		notesEditor.setEnabled(!punchedOut);
		if (punchedOut) {
			elapsedTimeDisplay.setTextColor(Color.RED);
		} else {
			elapsedTimeDisplay.setTextColor(Color.GREEN);
		}
		updateChronOutputTextView();
		getNotesView().setText(sessionData.getNotes());
		setSelectedTimeSliceCategory(sessionData.getCategory());
	}

	private EditText getNotesView() {
		return (EditText) findViewById(R.id.main_edit_text_notes);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
		return true;	
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		Class<? extends Activity> itemHandler = getMenuIntentHandler(item);
		if (itemHandler != null)
		{
			Intent intent = new Intent().setClass(this, itemHandler);
			startActivity(intent);
			return true;
		} else {
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	private Class<? extends Activity> getMenuIntentHandler(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.details:
	        return TimeSheetReportActivity.class;
	    case R.id.summary:
	        return SummaryReportActivity.class;
	    case R.id.categories:
	        return CategoryActivity.class;
	    case R.id.export:
	        return DataExportActivity.class;
	    case R.id.remove:
	        return RemoveTimeSliceActivity.class;
	    case R.id.settings:
	        return SettingsActivity.class;
	    default:
	    	return null;
	    }
	}

	private void showPunchInDialog() {
		PunchInDialog dialog = new PunchInDialog(this, R.style.PunchDialog);
		dialog.setCallback(this);
		dialog.show();
	}

	private void setupButtons() {
		Button punchInButton = (Button) findViewById(R.id.btnPunchIn);
		punchInButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(final View view) {
				showPunchInDialog();
			}
		});
		Button punchOutButton = (Button) findViewById(R.id.btnPunchOut);
		punchOutButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(final View view) {
				punchOutClock();
			}
		});
	}

	void punchInClock(TimeSliceCategory selectedCategory) {
		long elapsedRealtime = tracker.currentTimeMillis();
		tracker.punchInClock(selectedCategory, elapsedRealtime);
		reloadGui();
	}

	private void punchOutClock() {
		tracker.punchOutClock(tracker.currentTimeMillis(), this.notesEditor.getText().toString());
		reloadGui();
	}

	private void updateElapsedTimeLabel(long elapsed) {
		if (elapsedTimeDisplay != null) {
			elapsedTimeDisplay.setText(DateTimeFormatter
					.hrColMin(elapsed, false,true));
		}
	}

	private void startStopTimer(boolean punchedOut) {
		Chronometer chronometer = (Chronometer) findViewById(R.id.chron);
	
		if (!punchedOut)
		{
			chronometer.start();
			chronometer.setOnChronometerTickListener(this);
			elapsedTimeDisplay.setTextColor(Color.GREEN);			
		} else {
			chronometer.stop();
			chronometer.setOnChronometerTickListener(null);
			elapsedTimeDisplay.setTextColor(Color.RED);		
		}
		updateChronOutputTextView();
	}

	@Override
	public void onChronometerTick(Chronometer chron) {
		updateChronOutputTextView();
	}

	private void updateClock(boolean isPunchedOut)
	{
		startStopTimer(isPunchedOut);
		if (!isPunchedOut)
		{
			updateCategoryAndStartLabel();
		}
	}
	
	private void updateChronOutputTextView() {
		long elapsed = (tracker.isPunchedOut()) ? tracker.getElapsedTimeInMillisecs() : (tracker.currentTimeMillis() - sessionData.getStartTime());
		updateElapsedTimeLabel(elapsed);
	}

	private void updateCategoryAndStartLabel() {
		TextView labelTv = (TextView) findViewById(R.id.tvTimeInActivity);
		if (sessionData != null
				&& sessionData.getCategory() != null) {
			labelTv.setText("Time spent "
					+ sessionData.getCategory().getCategoryName() + ": ");
		} else {
			labelTv.setText("No current activity");
		}
		TextView tvStartTime = (TextView) findViewById(R.id.tvStartTime);
		tvStartTime.setText("Start time: " + sessionData.getStartTimeStr());
	}

	private void setSelectedTimeSliceCategory(TimeSliceCategory category) {
		Spinner catSpinner = (Spinner) findViewById(R.id.MainTimeSliceCategory);
		for (int position = 0; position < catSpinner.getCount(); position++) {
			if (catSpinner.getItemAtPosition(position).equals(category)) {
				catSpinner.setSelection(position);
			}
		}
	}

	private void saveState() {
		tracker.saveState();
	}
	
	private TimeTrackerSessionData reloadSessionData() {
		return tracker.reloadSessionData(); // ((TimeTrackerSessionData) getLastNonConfigurationInstance());
	}

}