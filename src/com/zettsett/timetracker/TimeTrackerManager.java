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
		context.deleteFile("curr_state");

		timeTrackerSessionDataPersistance.save(sessionData);
	}
	
	public TimeTrackerSessionData reloadSessionData(TimeTrackerSessionData sessionData) {
		if (sessionData == null)
			sessionData = timeTrackerSessionDataPersistance.load();
		if (sessionData == null)
			sessionData = new TimeTrackerSessionData();
		
		this.sessionData = sessionData;
		return sessionData;
	}


	public Boolean punchInClock(String selectedCategoryName, long startDateTime) {
		TimeSliceCategory cat = this.timeSlicecategoryDBAdapter.getOrCreateTimeSlice(selectedCategoryName);
		return punchInClock(cat, startDateTime);
	}
	
	public Boolean punchInClock(TimeSliceCategory selectedCategory, long startDateTime) {
		if (Log.isLoggable(Global.LOG_CONTEXT, Log.INFO))
		{
			Log.i(Global.LOG_CONTEXT, "punchInClock(category='" + selectedCategory.getCategoryName() 
					+ "', time='" + TimeSlice.getDateTimeStr(startDateTime)
					+ "', session='" + this.sessionData + "')");
		}
		
		if (hasCategoryChanged(selectedCategory)) {
			if (!sessionData.isPunchedOut()) {
				sessionData.endCurrentTimeSlice(startDateTime);
				timeSliceDBAdapter.createTimeSlice(sessionData.getCurrentTimeSlice());
			}
			sessionData.beginNewSlice(selectedCategory);
			sessionData.setPunchInTimeStartInMillisecs(startDateTime);
			saveState();

			return true;
		}
		
		Log.i(Global.LOG_CONTEXT, "punchInClock(): nothing to do");
		return false;
	}

	private boolean hasCategoryChanged(TimeSliceCategory newCategory) {
		return (sessionData.getTimeSliceCategory() != null && !sessionData.getTimeSliceCategory()
				.equals(newCategory))
				|| sessionData.isPunchedOut();
	}


	public Boolean punchOutClock() {
		if (Log.isLoggable(Global.LOG_CONTEXT, Log.INFO))
		{
			Log.i(Global.LOG_CONTEXT, "punchOutClock(" + sessionData + ")");
		}

		if (!sessionData.isPunchedOut()) {
			long startDateTime = System.currentTimeMillis();

			sessionData.setPunchedOut(true);
			sessionData.endCurrentTimeSlice(startDateTime);
			timeSliceDBAdapter.createTimeSlice(sessionData.getCurrentTimeSlice());
			saveState();
			return true;
		}
		Log.e(Global.LOG_CONTEXT, "punchOutClock(" + sessionData + ") : not punched in");
		return false;
	}


}
