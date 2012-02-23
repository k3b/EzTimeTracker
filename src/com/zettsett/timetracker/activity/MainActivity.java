package com.zettsett.timetracker.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Chronometer.OnChronometerTickListener;

import com.zetter.androidTime.R;
import com.zettsett.timetracker.DateTimeFormatter;
import com.zettsett.timetracker.Settings;
import com.zettsett.timetracker.TimeTrackerManager;
import com.zettsett.timetracker.TimeTrackerSessionData;
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
public class MainActivity extends Activity {
	private static final int MENU_MAIN_MENU = Menu.FIRST;

	private Chronometer chronometer;
	private TimeTrackerSessionData sessionData = new TimeTrackerSessionData();
	private TimeTrackerManager tracker = null; 

	public static final String PREFS_NAME = "TimerPrefs";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tracker = new TimeTrackerManager(this);
		Settings.initializeCurrentTimeFormatSetting(getBaseContext());
		setContentView(R.layout.main);
		DatabaseInstance.initialize(this);
		DatabaseInstance.open();
		chronometer = (Chronometer) findViewById(R.id.chron);
		setupButtons();
		showData();
		updateTimeSpentDoingLabel();
		chronometer.setOnChronometerTickListener(new OnChronometerTickListener() {
			@Override
			public void onChronometerTick(Chronometer chron) {
				updateChronOutputTextView();
			}
		});
		setupNotesChangeListener();
	}

	private void setupNotesChangeListener() {
		final EditText notesET = getNotesEditText();
		notesET.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				String notes = notesET.getText().toString();
				sessionData.setCurrentNotes(notes);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

		});
	}

	private EditText getNotesEditText() {
		return (EditText) findViewById(R.id.main_edit_text_notes);
	}

	private void updateChronOutputTextView() {
		long elapsed = (SystemClock.elapsedRealtime() - chronometer.getBase());
		((TextView) findViewById(R.id.mainViewChronOutput)).setText(DateTimeFormatter
				.hrColMinColSec(elapsed, false));
	}

	private void showData() {
		sessionData = reloadSessionData();
		
		if (sessionData.isPunchedOut()) {
			chronometer.setBase(SystemClock.elapsedRealtime()
					- sessionData.getCurrentTimeSlice().getDurationInMilliseconds());
			((TextView) findViewById(R.id.mainViewChronOutput)).setTextColor(Color.RED);
		} else {
			if (sessionData.getPunchInTimeStartInMillisecs() > SystemClock.elapsedRealtime()) {
				resetTimeAfterDeviceRestart();
			} else {
				chronometer.setBase(sessionData.getPunchInTimeStartInMillisecs());
			}
			((TextView) findViewById(R.id.mainViewChronOutput)).setTextColor(Color.GREEN);
		}
		updateChronOutputTextView();
		getNotesView().setText(sessionData.getCurrentTimeSlice().getNotes());
		setSelectedTimeSliceCategory(sessionData.getTimeSliceCategory());
	}

	private EditText getNotesView() {
		return (EditText) findViewById(R.id.main_edit_text_notes);
	}
	private void resetTimeAfterDeviceRestart() {
		chronometer.setBase(SystemClock.elapsedRealtime()
				- (System.currentTimeMillis() - sessionData.getCurrentTimeSlice().getStartTime()));
	}

	@Override
	protected void onStop() {
		super.onStop();
		saveState();
	}

	@Override
	public void onPause() {
		super.onPause();
		chronometer.stop();
		// DatabaseInstance.close();
	}

	@Override
	public void onResume() {
		super.onResume();
		DatabaseInstance.open();
		if (!sessionData.isPunchedOut()) {
			chronometer.start();
		}
		updateTimeSpentDoingLabel();
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		sessionData.setPunchInTimeStartInMillisecs(chronometer.getBase());
		return (sessionData);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_MAIN_MENU, 0, "Main Menu");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		loadMainMenu();
		return super.onOptionsItemSelected(item);
	}

	private void loadMainMenu() {
		Intent i = new Intent(this, MainMenuActivity.class);
		startActivity(i);
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
		long elapsedRealtime = SystemClock.elapsedRealtime();
		if (tracker.punchInClock(selectedCategory, elapsedRealtime))
		{
			chronometer.setBase(elapsedRealtime);
			chronometer.start();
			updateTimeSpentDoingLabel();
			getNotesEditText().setText("");
		}
		((TextView) findViewById(R.id.mainViewChronOutput)).setTextColor(Color.GREEN);
	}

	private void punchOutClock() {
		if (tracker.punchOutClock()) {
			chronometer.stop();
			((TextView) findViewById(R.id.mainViewChronOutput)).setTextColor(Color.RED);
		}
	}

	private void updateTimeSpentDoingLabel() {
		TextView labelTv = (TextView) findViewById(R.id.tvTimeInActivity);
		if (sessionData.getCurrentTimeSlice() != null
				&& sessionData.getCurrentTimeSlice().getCategory() != null) {
			labelTv.setText("Time spent "
					+ sessionData.getCurrentTimeSlice().getCategory().getCategoryName() + ": ");
		} else {
			labelTv.setText("No current activity");
		}
		TextView tvStartTime = (TextView) findViewById(R.id.tvStartTime);
		tvStartTime.setText("Start time: " + sessionData.getCurrentTimeSlice().getStartTimeStr());
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
		return tracker.reloadSessionData((TimeTrackerSessionData) getLastNonConfigurationInstance());
	}

}