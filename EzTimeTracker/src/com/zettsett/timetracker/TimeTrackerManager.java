package com.zettsett.timetracker;

import com.zettsett.timetracker.database.TimeSliceCategoryDBAdapter;
import com.zettsett.timetracker.database.TimeSliceDBAdapter;
import com.zettsett.timetracker.model.TimeSlice;
import com.zettsett.timetracker.model.TimeSliceCategory;

import de.k3b.util.SessionDataPersistance;
import android.content.Context;
import android.util.Log;

public class TimeTrackerManager {

	private Context context;
	private SessionDataPersistance<TimeTrackerSessionData> timeTrackerSessionDataPersistance = null;
	private TimeSliceDBAdapter timeSliceDBAdapter;
	private TimeTrackerSessionData sessionData = new TimeTrackerSessionData();
	private TimeSliceCategoryDBAdapter timeSlicecategoryDBAdapter;

	public TimeTrackerManager(Context context)
	{
		this.context = context;
		this.timeTrackerSessionDataPersistance = new SessionDataPersistance<TimeTrackerSessionData>(context);
		this.timeSliceDBAdapter = new TimeSliceDBAdapter(context);
		this.timeSlicecategoryDBAdapter = new TimeSliceCategoryDBAdapter(context);
	}
	
	public void saveState() {		
		if (Global.isDebugEnabled())
		{
			sessionData.updateCount++;
			Log.d(Global.LOG_CONTEXT, "saveState('" + sessionData + "')");
		}
		context.deleteFile("curr_state");

		timeTrackerSessionDataPersistance.save(sessionData);
	}
	
	public TimeTrackerSessionData reloadSessionData() {
		sessionData.load(timeTrackerSessionDataPersistance.load());
		if (Global.isDebugEnabled())
		{
			Log.d(Global.LOG_CONTEXT, "reloadSessionData('" + sessionData + "')");
		}
		return sessionData;
	}


	public Boolean punchInClock(String selectedCategoryName, long startDateTime) {
		TimeSliceCategory cat = this.timeSlicecategoryDBAdapter.getOrCreateTimeSlice(selectedCategoryName);
		return punchInClock(cat, startDateTime);
	}
	
	public Boolean punchInClock(TimeSliceCategory selectedCategory, long startDateTime) {
		if (Global.isInfoEnabled())
		{
			Log.i(Global.LOG_CONTEXT, "punchInClock(category='" + selectedCategory.getCategoryName() 
					+ "', time='" + TimeSlice.getDateTimeStr(startDateTime)
					+ "', session='" + this.sessionData + "')");
		}
		
		boolean isPunchedIn = sessionData.isPunchedIn();
		boolean hasCategoryChanged = hasCategoryChanged(selectedCategory);
		if (!isPunchedIn || hasCategoryChanged) {
			if (isPunchedIn && hasCategoryChanged) {
				sessionData.setEndTime(startDateTime);
				timeSliceDBAdapter.createTimeSlice(sessionData);
			} 
			sessionData.beginNewSlice(selectedCategory, startDateTime);
			sessionData.setNotes("");
			saveState();

			return true;
		}
		
		Log.i(Global.LOG_CONTEXT, "punchInClock(): nothing to do");
		return false;
	}

	private boolean hasCategoryChanged(TimeSliceCategory newCategory) {
		return (sessionData.getCategory() == null || !sessionData.getCategory()
				.equals(newCategory));
	}

	public Boolean punchOutClock(long endDateTime, String notes) {
		if (Log.isLoggable(Global.LOG_CONTEXT, Log.INFO))
		{
			Log.i(Global.LOG_CONTEXT, "punchOutClock(" + sessionData + ")");
		}

		if ((notes != null) && (notes.length() > 0))
		{
			sessionData.setNotes(notes);
		}
		
		if ((sessionData.getCategory() != null) && (sessionData.isPunchedIn())) {
			sessionData.setEndTime(endDateTime);
			if (sessionData.getElapsedTimeInMillisecs() >  Settings.getMinminTrashholdInMilliSecs())
			{
				timeSliceDBAdapter.createTimeSlice(sessionData);
				saveState();
				return true;
			} else {
				Log.w(Global.LOG_CONTEXT, "Discarding timeslice in punchOutClock(" + sessionData + ") : elapsed " + sessionData.getElapsedTimeInMillisecs() + " time smaller than trashhold " + Settings.getMinminTrashholdInMilliSecs());
				saveState();
			}
		} else {
			Log.i(Global.LOG_CONTEXT, "punchOutClock(" + sessionData + ") : not punched in or category not set.");
		}
		return false;
	}

	public long getElapsedTimeInMillisecs() {
		return sessionData.getElapsedTimeInMillisecs();
	}

	public boolean isPunchedIn() {
		return sessionData.isPunchedIn();
	}

	public long currentTimeMillis() {
		return System.currentTimeMillis(); // SystemClock.elapsedRealtime();
	}

	public void addNotes(String note) {
		if ((note != null) && (note.length() != 0)) {
			sessionData.setNotes(sessionData.getNotes() + " " + note);
		}
		
	}


}
