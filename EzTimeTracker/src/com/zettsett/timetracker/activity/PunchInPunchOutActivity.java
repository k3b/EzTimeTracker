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
import android.view.*;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.*;
import android.widget.Chronometer.OnChronometerTickListener;

import com.zetter.androidTime.R;
import com.zettsett.timetracker.*;
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
 * GUI to start and/or stop time tracking:
 * Workflow:
 * stopped+start->showSelectCategoryForPunchInDialog->(createNewCategory->)started(now)
 * stopped+long-start->editStartSettings()->started(selected-time, selectedCategory)
 * 
 * started+long-start->editStartSettings()->started(selected-time, selectedCategory)
 * started+stop->punchOutClock()->stopped(now)
 * started+start->showSelectCategoryForPunchInDialog->(createNewCategory->)stop(now)+started(now)
 */
public class PunchInPunchOutActivity extends Activity implements OnChronometerTickListener, CategorySetter {
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
		public void onReceive (Context context, Intent intent) {
			if (Log.isLoggable(Global.LOG_CONTEXT, Log.INFO))
			{
				Log.i(Global.LOG_CONTEXT, "PunchInPunchOutActivity.onReceive(intent='" + intent + "')");
			}
			reloadGui();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (Global.isDebugEnabled())
		{
			Log.d(Global.LOG_CONTEXT, "PunchInPunchOutActivity()");
		}
		
		super.onCreate(savedInstanceState);
		//DateTimeFormatter.SetFormat(DateFormat.get DateInstance(DateFormat.S));
		setContentView(R.layout.main);
		this.elapsedTimeDisplay = (TextView) findViewById(R.id.mainViewChronOutput);
		this.elapsedTimeDisplay.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				return startActivity(R.id.details);
			}
		});
		
		this.notesEditor = (EditText) findViewById(R.id.main_edit_text_notes);

		setupButtons();

		this.tracker = new TimeTrackerManager(this);
		Settings.init(getBaseContext());

		reloadGui();
	}

	@Override
	public void onResume() {
		super.onResume();
		Settings.init(getBaseContext());
		
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
		Log.d(Global.LOG_CONTEXT, "PunchInPunchOutActivity.refreshGui()");

		sessionData = reloadSessionData();
		
		if (!this.tracker.isPunchedIn())
		{
			updateElapsedTimeLabel(tracker.getElapsedTimeInMillisecs());
		}

		updateClock(this.tracker.isPunchedIn());
		showData();
		updateCategoryAndStartLabel();
	}
	
	private void showData() {
		boolean punchedIn = sessionData.isPunchedIn();
		notesEditor.setEnabled(punchedIn);
		if (!punchedIn) {
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

		int itemId = item.getItemId();
		if (startActivity(itemId))
		{
			return true;
		} else {
			switch (itemId) {
				case R.id.about:
					showDialog(itemId);
					return true;
			}
	        return super.onOptionsItemSelected(item);
	    }
	}

	private boolean startActivity(int itemId) {
		Class<? extends Activity> itemHandler = getMenuIntentHandler(itemId);
		if (itemHandler != null)
		{
			Intent intent = new Intent().setClass(this, itemHandler);
			intent.putExtra(SummaryReportActivity.MENU_ID, itemId);
			startActivity(intent);
			return true;
		} 
		return false;
	}

	private Dialog getAboutDialog() {
		Context mContext = this;
		  AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
	        alert.setTitle(R.string.about_title);
	        alert.setIcon(R.drawable.icon);
	        alert.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface paramDialogInterface, int paramInt) {
					paramDialogInterface.cancel();
					
				}
			});
		  WebView wv = new WebView(mContext);
		  String html = getResources().getString(R.string.about_content); // "<html><body>some <b>html</b> here</body></html>";

		  Context context = this;
		  try {
			String versionName = context.getPackageManager().getPackageInfo (context.getPackageName(), 0).versionName;
			html = html.replace("$versionName$", versionName);
		  } catch (NameNotFoundException e) {
		  }
		  html = html.replace("$about$", getText(R.string.about_content_about));
		  
		  wv.loadData(html, "text/html", "UTF-8");
		  wv.setVerticalScrollBarEnabled(true);
		  
		  WebSettings mWebSettings = wv.getSettings();
	        mWebSettings.setBuiltInZoomControls(true);
	        wv.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
	        wv.setScrollbarFadingEnabled(false);
		  
		  
		  alert.setView(wv);
		
      return alert.create();	
    }

	private Class<? extends Activity> getMenuIntentHandler(int item) {
	    switch (item) {
	    case R.id.details:
	        return TimeSheetReportActivity.class;
	    case R.id.summary_day:
	    case R.id.summary_month:
	    case R.id.summary_week:
	    case R.id.category_day:
	    case R.id.category_month:
	    case R.id.category_week:
	        return SummaryReportActivity.class;
	    case R.id.categories:
	        return CategoryListActivity.class;
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

	private void showSelectCategoryForPunchInDialog() {
		showDialog(SELECT_CATAGORY);
	}

	private CategoryEditDialog edit = null;
	public void showCategoryEditDialog(TimeSliceCategory category)
	{
		if (this.edit == null)
		{
			this.edit = new CategoryEditDialog(this, this);
		}
		this.edit.setCategory(category);
		showDialog(CREATE_NEW_CATEGORY);
	}
	
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case SELECT_CATAGORY:
				return new SelectCategoryDialog(this, R.style.PunchDialog, TimeSliceCategory.NO_CATEGORY, TimeTrackerManager.currentTimeMillis())
							.setCategoryCallback(this);
			case SELECT_CATAGORY_ALL:
				return new SelectCategoryDialog(this, R.style.PunchDialog, TimeSliceCategory.NO_CATEGORY, TimeSliceCategory.MAX_VALID_DATE )
							.setCategoryCallback(this);
			case CREATE_NEW_CATEGORY:
				return this.edit;
			case R.id.about:
				return this.getAboutDialog();
		}
		return null;
	}
	
	private void setupButtons() {
		Button punchInButton = (Button) findViewById(R.id.btnPunchIn);
		punchInButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(final View view) {
				showSelectCategoryForPunchInDialog();
			}
		});
		punchInButton.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				return editStartSettings();
			}
		});
		
		Button punchOutButton = (Button) findViewById(R.id.btnPunchOut);
		punchOutButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(final View view) {
				punchOutClock(TimeTrackerManager.currentTimeMillis());
			}
		});
		punchOutButton.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				return editStopSettings();
			}
		});
		
	}

	/**
	 * Called by showSelectCategoryForPunchInDialog
	 */
	@Override
	public void setCategory(TimeSliceCategory selectedCategory) {
		if (selectedCategory == TimeSliceCategory.NO_CATEGORY)
		{
			showCategoryEditDialog(null);
		} else {
			if (selectedCategory.getRowId() == TimeSliceCategory.NOT_SAVED) {
				timeSliceRepository.createTimeSliceCategory(selectedCategory);
			} 
			long elapsedRealtime = TimeTrackerManager.currentTimeMillis();
			punchInClock(elapsedRealtime, selectedCategory);
		}
	}

	private void punchInClock(long elapsedRealtime,
			TimeSliceCategory selectedCategory) {
		tracker.punchInClock(selectedCategory, elapsedRealtime);
		reloadGui();
	}

	private void punchOutClock(long elapsedRealtime) {
		tracker.punchOutClock(elapsedRealtime, this.notesEditor.getText().toString());
		reloadGui();
	}

	private void updateElapsedTimeLabel(long elapsedRealtime) {
		if (elapsedTimeDisplay != null) {
			elapsedTimeDisplay.setText(DateTimeFormatter
					.hrColMin(elapsedRealtime, false,true));
		}
	}

	private void startStopTimer(boolean punchedIn) {
		Chronometer chronometer = (Chronometer) findViewById(R.id.chron);
	
		if (punchedIn)
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

	private boolean editStartSettings() {
		if (sessionData != null) { 
    		TimeSlice editItem = new TimeSlice()
			.setCategory(sessionData.getCategory())
			.setEndTime(TimeSliceEditActivity.HIDDEN)
			.setNotes(TimeSliceEditActivity.HIDDEN_NOTES)
			.setRowId(32531);
			// edit already running starttime
			if (sessionData.isPunchedIn()){
	    		editItem.setStartTime(sessionData.getStartTime());
			} else {
	    		editItem.setStartTime(TimeTrackerManager.currentTimeMillis());
			}
    		TimeSliceEditActivity.showTimeSliceEditActivity(this, editItem, EDIT_START);
		}
		
		return true;	 // consumed        	
	} 

	private boolean editStopSettings() {
		if ((sessionData != null) && (sessionData.isPunchedIn()))
    	{
    		TimeSlice editItem = new TimeSlice()
    			.setCategory(sessionData.getCategory())
    			.setStartTime(sessionData.getStartTime())
    			.setEndTime(TimeTrackerManager.currentTimeMillis())
    			.setNotes(TimeSliceEditActivity.HIDDEN_NOTES)
    			.setRowId(32531);
    		TimeSliceEditActivity.showTimeSliceEditActivity(this, editItem, EDIT_STOP);
    	}
		return true;	 // consumed        	
	} 

	/**
	 * call back from sub-activities
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		
		TimeSlice updatedTimeSlice = getTimeSlice(intent);
		sessionData = reloadSessionData();
		if ((sessionData != null) && (updatedTimeSlice != null)) { //  (requestCode == EDIT_START) || (requestCode == EDIT_STOP)) {

			boolean punchedIn = sessionData.isPunchedIn();
			if ((requestCode == EDIT_START) && !punchedIn) {
				punchInClock(updatedTimeSlice.getStartTime(), updatedTimeSlice.getCategory());
				
			} else if ((requestCode == EDIT_START) && punchedIn) {
				sessionData
					.setCategory(updatedTimeSlice.getCategory())
					.setStartTime(updatedTimeSlice.getStartTime());
				saveState();
				reloadGui();
			} else if ((requestCode == EDIT_STOP) && punchedIn) {
				sessionData
				.setCategory(updatedTimeSlice.getCategory())
				.setStartTime(updatedTimeSlice.getStartTime());
				saveState();
				reloadGui();
				punchOutClock(updatedTimeSlice.getEndTime());
			}
		}
	}

	private TimeSlice getTimeSlice(Intent intent) {
		return (intent != null) ? ((TimeSlice) intent.getExtras().get(Global.EXTRA_TIMESLICE)) : null;
	}

	@Override
	public void onChronometerTick(Chronometer chron) {
		updateChronOutputTextView();
	}

	private void updateClock(boolean isPunchedIn)
	{
		startStopTimer(isPunchedIn);
		if (isPunchedIn)
		{
			updateCategoryAndStartLabel();
		}
	}
	
	private void updateChronOutputTextView() {
		long elapsed = (!tracker.isPunchedIn()) ? tracker.getElapsedTimeInMillisecs() : (TimeTrackerManager.currentTimeMillis() - sessionData.getStartTime());
		updateElapsedTimeLabel(elapsed);
	}

	private void updateCategoryAndStartLabel() {
		TextView labelTv = (TextView) findViewById(R.id.tvTimeInActivity);
		if (sessionData != null
				&& sessionData.getCategory() != null) {
			String labelCategory = String.format(getText(R.string.format_category).toString(),
							sessionData.getCategoryName());
			labelTv.setText(labelCategory);
		} else {
			labelTv.setText(R.string.label_no_current_activity); 
		}
		TextView tvStartTime = (TextView) findViewById(R.id.tvStartTime);
		String labelStartTime = String.format(getText(R.string.format_start_time_).toString() , sessionData.getStartTimeStr());
		tvStartTime.setText(labelStartTime);
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