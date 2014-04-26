package de.k3b.timetracker;

import android.content.Context;
import android.util.Log;
import de.k3b.timetracker.database.ICategoryRepsitory;
import de.k3b.timetracker.database.TimeSliceCategoryRepsitory;
import de.k3b.timetracker.database.TimeSliceRepository;
import de.k3b.timetracker.model.TimeSliceCategory;
import de.k3b.util.ISessionDataPersistance;
import de.k3b.util.SessionDataPersistance;

/**
 * Gui independant api-implementation to execute timetracking.<br/>
 * (punchIn, punchOut, save/reloadSessionData).<br/>
 * Used by GUI and BroadcastReceiver.
 * 
 */
public class TimeTrackerManager {
	private final TimeSliceRepository timeSliceRepository;
	private final ICategoryRepsitory timeSliceCategoryRepository;

	private final TimeTrackerSessionData sessionData;
	private final ISessionDataPersistance<TimeTrackerSessionData> timeTrackerSessionDataPersistance;

	public TimeTrackerManager(final Context context, final Boolean publicDir) {
		// poor man's dependency injection
		this(new SessionDataPersistance<TimeTrackerSessionData>(context),
				new TimeSliceRepository(context, publicDir),
				new TimeSliceCategoryRepsitory(context),
				new TimeTrackerSessionData());
	}

	/**
	 * Internal constructor used by tests to allow mocking of the child
	 * components.
	 */
	TimeTrackerManager(
			final ISessionDataPersistance<TimeTrackerSessionData> sessionDataPersistance,
			final TimeSliceRepository timeSliceRepository,
			final ICategoryRepsitory timeSliceCategoryRepsitory,
			final TimeTrackerSessionData sessionData) {
		this.sessionData = sessionData;
		this.timeTrackerSessionDataPersistance = sessionDataPersistance;
		this.timeSliceRepository = timeSliceRepository;
		this.timeSliceCategoryRepository = timeSliceCategoryRepsitory;
	}

	public void saveSessionData() {
		if (Global.isDebugEnabled()) {
			this.sessionData.updateCount++;

			Log.d(Global.LOG_CONTEXT, "saveState('" + this.sessionData + "')");
		}
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
			this.saveSessionData();

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
				this.saveSessionData();
				return true;
			} else {
				Log.w(Global.LOG_CONTEXT,
						"Discarding timeslice in punchOutClock("
								+ this.sessionData + ") : elapsed "
								+ this.sessionData.getElapsedTimeInMillisecs()
								+ " time smaller than trashhold "
								+ Settings.getMinPunchOutTreshholdInMilliSecs());
				this.saveSessionData();
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
