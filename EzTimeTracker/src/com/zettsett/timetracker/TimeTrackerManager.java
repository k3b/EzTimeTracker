package com.zettsett.timetracker;

import android.content.Context;
import android.util.Log;

import com.zettsett.timetracker.database.TimeSliceCategoryRepsitory;
import com.zettsett.timetracker.database.TimeSliceRepository;
import com.zettsett.timetracker.model.TimeSliceCategory;

import de.k3b.util.SessionDataPersistance;

public class TimeTrackerManager {

	private final Context context;
	private SessionDataPersistance<TimeTrackerSessionData> timeTrackerSessionDataPersistance = null;
	private final TimeSliceRepository timeSliceRepository;
	private final TimeSliceCategoryRepsitory timeSliceCategoryRepository;
	private final TimeTrackerSessionData sessionData = new TimeTrackerSessionData();

	public TimeTrackerManager(final Context context) {
		this.context = context;
		this.timeTrackerSessionDataPersistance = new SessionDataPersistance<TimeTrackerSessionData>(
				context);
		this.timeSliceRepository = new TimeSliceRepository(context);
		this.timeSliceCategoryRepository = new TimeSliceCategoryRepsitory(
				context);
	}

	public void saveState() {
		if (Global.isDebugEnabled()) {
			this.sessionData.updateCount++;

			Log.d(Global.LOG_CONTEXT, "saveState('" + this.sessionData + "')");
		}
		this.context.deleteFile("curr_state");

		this.timeTrackerSessionDataPersistance.save(this.sessionData);
	}

	public TimeTrackerSessionData reloadSessionData() {
		this.sessionData.load(this.timeTrackerSessionDataPersistance.load());
		if (Global.isDebugEnabled()) {
			Log.d(Global.LOG_CONTEXT, "reloadSessionData('" + this.sessionData
					+ "')");
		}
		return this.sessionData;
	}

	public Boolean punchInClock(final String selectedCategoryName,
			final long startDateTime) {
		final TimeSliceCategory cat = this.timeSliceCategoryRepository
				.getOrCreateTimeSlice(selectedCategoryName);
		return this.punchInClock(cat, startDateTime);
	}

	/**
	 * Start new punchInSession.<br/>
	 * 
	 * @return true if success
	 */
	public Boolean punchInClock(final TimeSliceCategory selectedCategory,
			final long startDateTime) {
		if (Global.isInfoEnabled() && (selectedCategory != null)) {
			Log.i(Global.LOG_CONTEXT,
					"punchInClock(category='"
							+ selectedCategory.getCategoryName()
							+ "', time='"
							+ DateTimeFormatter.getInstance().getDateTimeStr(
									startDateTime) + "', session='"
							+ this.sessionData + "')");
		}

		final boolean isPunchedIn = this.sessionData.isPunchedIn();
		final boolean hasCategoryChanged = this
				.hasCategoryChanged(selectedCategory);
		if (!isPunchedIn || hasCategoryChanged) {
			if (isPunchedIn && hasCategoryChanged) {
				this.sessionData.setEndTime(startDateTime);
				this.timeSliceRepository.create(this.sessionData);
			}
			this.sessionData.beginNewSlice(selectedCategory, startDateTime);
			this.sessionData.setNotes("");
			this.saveState();

			return true;
		}

		if (Global.isInfoEnabled()) {
			Log.i(Global.LOG_CONTEXT, "punchInClock(): nothing to do");
		}
		return false;
	}

	private boolean hasCategoryChanged(final TimeSliceCategory newCategory) {
		return ((this.sessionData.getCategory() == null) || !this.sessionData
				.getCategory().equals(newCategory));
	}

	public Boolean punchOutClock(final long endDateTime, final String notes) {
		if ((notes != null) && (notes.length() > 0)) {
			this.sessionData.setNotes(notes);
		}

		if ((this.sessionData.getCategory() != null)
				&& (this.sessionData.isPunchedIn())) {
			this.sessionData.setEndTime(endDateTime);
			if (this.sessionData.getElapsedTimeInMillisecs() > Settings
					.getMinPunchOutTreshholdInMilliSecs()) {
				if (Global.isInfoEnabled()) {
					Log.i(Global.LOG_CONTEXT, "punchOutClock("
							+ this.sessionData + ") persisting ...");
				}

				this.timeSliceRepository.create(this.sessionData);
				this.saveState();
				return true;
			} else {
				Log.w(Global.LOG_CONTEXT,
						"Discarding timeslice in punchOutClock("
								+ this.sessionData + ") : elapsed "
								+ this.sessionData.getElapsedTimeInMillisecs()
								+ " time smaller than trashhold "
								+ Settings.getMinPunchOutTreshholdInMilliSecs());
				this.saveState();
			}
		} else {
			if (Global.isInfoEnabled()) {
				Log.i(Global.LOG_CONTEXT, "punchOutClock(" + this.sessionData
						+ ") : not punched in or category not set.");
			}
		}
		return false;
	}

	public long getElapsedTimeInMillisecs() {
		return this.sessionData.getElapsedTimeInMillisecs();
	}

	public boolean isPunchedIn() {
		return this.sessionData.isPunchedIn();
	}

	public static long currentTimeMillis() {
		return System.currentTimeMillis(); // SystemClock.elapsedRealtime();
	}

	public void addNotes(final String note) {
		if ((note != null) && (note.length() != 0)) {
			this.sessionData.setNotes(this.sessionData.getNotes() + " " + note);
		}

	}

}
