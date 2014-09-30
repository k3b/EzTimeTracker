package de.k3b.timetracker;

import de.k3b.common.Logger;
import de.k3b.timetracker.database.ICategoryRepsitory;
import de.k3b.timetracker.database.ITimeSliceRepository;
import de.k3b.timetracker.model.TimeSliceCategory;
import de.k3b.util.DateTimeUtil;
import de.k3b.util.ISessionDataPersistance;

/**
 * Gui independant api-implementation to execute timetracking.<br/>
 * (punchIn, punchOut, save/reloadSessionData).<br/>
 * Used by GUI and BroadcastReceiver.
 */
public class TimeTrackerManager {
    private final ITimeSliceRepository timeSliceRepository;
    private final ICategoryRepsitory timeSliceCategoryRepository;
    private final Logger logger;
    private final Settings settings;

    private final TimeTrackerSessionData sessionData;
    private final ISessionDataPersistance<TimeTrackerSessionData> timeTrackerSessionDataPersistance;

    /**
     * Internal constructor used by tests to allow mocking of the child
     * components.
     */
    TimeTrackerManager(
            final ISessionDataPersistance<TimeTrackerSessionData> sessionDataPersistance,
            final ITimeSliceRepository timeSliceRepository,
            final ICategoryRepsitory timeSliceCategoryRepsitory,
            final TimeTrackerSessionData sessionData, final Logger logger, final Settings settings) {
        this.sessionData = sessionData;
        this.timeTrackerSessionDataPersistance = sessionDataPersistance;
        this.timeSliceRepository = timeSliceRepository;
        this.timeSliceCategoryRepository = timeSliceCategoryRepsitory;
        this.logger = logger;
        this.settings = settings;
    }

    public static long currentTimeMillis() {
        return DateTimeUtil.currentTimeMillis(); // SystemClock.elapsedRealtime();
    }

    public void saveSessionData() {
        if (Global.isDebugEnabled()) {
            this.sessionData.updateCount++;

            logger.d("saveState('" + this.sessionData + "')");
        }
        this.timeTrackerSessionDataPersistance.save(this.sessionData);
    }

    public TimeTrackerSessionData reloadSessionData() {
        this.sessionData.load(this.timeTrackerSessionDataPersistance.load());
        if (Global.isDebugEnabled()) {
            logger.d("reloadSessionData('" + this.sessionData
                    + "')");
        }
        return this.sessionData;
    }

    public StateChangeType punchInClock(final String selectedCategoryName,
                                        final long startDateTime) {
        final TimeSliceCategory cat = this.timeSliceCategoryRepository
                .getOrCreateCategory(selectedCategoryName);
        return this.punchInClock(cat, startDateTime);
    }

    /**
     * Start new punchInSession.<br/>
     *
     * @return true if success
     */
    public StateChangeType punchInClock(final TimeSliceCategory selectedCategory,
                                        final long startDateTime) {
        StateChangeType stateChangeType = StateChangeType.NONE;

        if (Global.isInfoEnabled() && (selectedCategory != null)) {
            logger.i(
                    "punchInClock(category='"
                            + selectedCategory.getCategoryName()
                            + "', time='"
                            + DateTimeFormatter.getInstance().getDateTimeStr(
                            startDateTime) + "', session='"
                            + this.sessionData + "')"
            );
        }

        final boolean isPunchedIn = this.sessionData.isPunchedIn();
        final boolean hasCategoryChanged = this
                .hasCategoryChanged(selectedCategory);
        if (!isPunchedIn || hasCategoryChanged) {
            if (isPunchedIn && hasCategoryChanged) {
                this.sessionData.setEndTime(startDateTime);
                if (this.timeSliceRepository.create(this.sessionData) >= 0)
                    stateChangeType = StateChangeType.Change;
                else
                    stateChangeType = StateChangeType.ChangeMerge;
            } else {
                stateChangeType = StateChangeType.Start;
            }
            this.sessionData.beginNewSlice(selectedCategory, startDateTime);
            this.sessionData.setNotes("");
            this.saveSessionData();

            return stateChangeType;
        }

        stateChangeType = StateChangeType.AlreadyStarted;
        if (Global.isInfoEnabled()) {
            logger.i("punchInClock(): nothing to do");
        }
        return stateChangeType;
    }

    private boolean hasCategoryChanged(final TimeSliceCategory newCategory) {
        return ((this.sessionData.getCategory() == null) || !this.sessionData
                .getCategory().equals(newCategory));
    }

    public StateChangeType punchOutClock(final long endDateTime, final String notes) {
        StateChangeType stateChangeType = StateChangeType.NONE;

        addNotes(notes);

        if ((this.sessionData.getCategory() != null)
                && (this.sessionData.isPunchedIn())) {
            this.sessionData.setEndTime(endDateTime);
            if (this.sessionData.getElapsedTimeInMillisecs() > settings
                    .getMinPunchOutTreshholdInMilliSecs()) {
                if (Global.isInfoEnabled()) {
                    logger.i("punchOutClock("
                            + this.sessionData + ") persisting ...");
                }

                if (this.timeSliceRepository.create(this.sessionData) >= 0)
                    stateChangeType = StateChangeType.Stop;
                else
                    stateChangeType = StateChangeType.StopMerge;

                this.saveSessionData();
                return stateChangeType;
            } else {
                stateChangeType = StateChangeType.UndoStart;

                if (logger != null) {
                    logger.w("Discarding timeslice in punchOutClock("
                            + this.sessionData + ") : elapsed "
                            + this.sessionData.getElapsedTimeInMillisecs()
                            + " time smaller than trashhold "
                            + settings.getMinPunchOutTreshholdInMilliSecs());
                }
                this.saveSessionData();
            }
        } else {
            stateChangeType = StateChangeType.NotStarted;

            if (Global.isInfoEnabled()) {
                logger.i("punchOutClock(" + this.sessionData
                        + ") : not punched in or category not set.");
            }
        }
        return stateChangeType;
    }

    public long getElapsedTimeInMillisecs() {
        return this.sessionData.getElapsedTimeInMillisecs();
    }

    public boolean isPunchedIn() {
        return this.sessionData.isPunchedIn();
    }

    public void addNotes(final String note) {
        if ((note != null) && (note.length() != 0)) {
            StringBuilder newNotes = new StringBuilder(this.sessionData.getNotes());
            if (newNotes.length() > 0) newNotes.append(" ");
            newNotes.append(note);
            this.sessionData.setNotes(newNotes.toString());
        }

    }

    public enum StateChangeType {
        NONE,
        /**
         * stop - start
         */
        Start,
        /**
         * start - start with category change
         */
        Change,
        /**
         * start - stop
         */
        Stop,
        /**
         * start - start no category change
         */
        AlreadyStarted,
        /**
         * start - stop change discarded because below treshold
         */
        UndoStart,
        /**
         * stop - stop
         */
        NotStarted,
        /**
         * start - start with category change the previous timeslice was merged with an existing
         */
        ChangeMerge,
        /**
         * start - stop The previous timeslice was merged with an existing
         */
        StopMerge,
    }

}
