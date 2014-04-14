package de.k3b.timetracker;

import java.util.Calendar;

import junit.framework.Assert;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import de.k3b.timetracker.Global;
import de.k3b.timetracker.Settings;
import de.k3b.timetracker.TimeTrackerManager;
import de.k3b.timetracker.TimeTrackerSessionData;
import de.k3b.timetracker.database.DatabaseInstance;
import de.k3b.timetracker.database.TimeSliceCategoryRepsitory;
import de.k3b.timetracker.database.TimeSliceRepository;
import de.k3b.timetracker.model.ITimeSliceFilter;
import de.k3b.util.DateTimeUtil;
import de.k3b.util.ISessionDataPersistance;

public class TimeTrackerManagerTest extends AndroidTestCase {

	private RenamingDelegatingContext ctx;
	private final String testCategoryName = "TEST";
	private TimeTrackerManager sut;
	private Calendar cal;
	final ISessionDataPersistance<TimeTrackerSessionData> sessionDataPersistance = new ISessionDataPersistance<TimeTrackerSessionData>() {

		private TimeTrackerSessionData sessionData;

		@Override
		public void save(final TimeTrackerSessionData sessionData) {
			this.sessionData = sessionData;
		}

		@Override
		public TimeTrackerSessionData load() {
			return this.sessionData;
		}
	};

	public TimeTrackerManagerTest() {
		super();
	}

	@Override
	protected void setUp() throws Exception {
		Settings.setMinPunchInTreshholdInMilliSecs(120000);
		Settings.setMinPunchOutTreshholdInMilliSecs(120000);
		Global.setDebugEnabled(true);
		Global.setInfoEnabled(true);
		final DateTimeUtil dtu = new DateTimeUtil(0);
		this.cal = dtu.getCalendar(2013, 1 - 1, 1, 17, 45, 59, 123);

		this.ctx = new RenamingDelegatingContext(null, "test_");
		this.setContext(this.ctx);
		DatabaseInstance.getCurrentInstance().initialize(this.ctx, true,
				"TestDB_");

		this.sut = new TimeTrackerManager(this.sessionDataPersistance,
				new TimeSliceRepository(this.ctx, Boolean.TRUE),
				new TimeSliceCategoryRepsitory(this.ctx),
				new TimeTrackerSessionData());

		TimeSliceRepository.delete(null);
	}

	public void testPunchInOut() {
		this.sut.punchInClock(this.testCategoryName, this.addMinutes(1));
		this.sut.punchOutClock(this.addMinutes(10), "");
		Assert.assertEquals(1,
				TimeSliceRepository.getCount((ITimeSliceFilter) null));
	}

	public void testPunchInOut2() {
		this.sut.punchInClock(this.testCategoryName, this.addMinutes(1));
		this.sut.punchOutClock(this.addMinutes(10), "");

		// after treshhold: add new
		this.sut.punchInClock(this.testCategoryName, this.addMinutes(10));
		this.sut.punchOutClock(this.addMinutes(10), "");
		Assert.assertEquals(2,
				TimeSliceRepository.getCount((ITimeSliceFilter) null));
	}

	public void testPunchInOutAppend() {
		this.sut.punchInClock(this.testCategoryName, this.addMinutes(1));
		this.sut.punchOutClock(this.addMinutes(10), "");

		// within treshhold: append (undo punchout)
		this.sut.punchInClock(this.testCategoryName, this.addMinutes(1));
		this.sut.punchOutClock(this.addMinutes(10), "");
		Assert.assertEquals(1,
				TimeSliceRepository.getCount((ITimeSliceFilter) null));
	}

	public void testPunchInOutDiscard() {
		this.sut.punchInClock(this.testCategoryName, this.addMinutes(1));
		this.sut.punchOutClock(this.addMinutes(1), "");
		Assert.assertEquals(0,
				TimeSliceRepository.getCount((ITimeSliceFilter) null));
	}

	public void testPunchInOut5Append1() {
		this.sut.punchInClock(this.testCategoryName, this.addMinutes(1));
		this.sut.punchOutClock(this.addMinutes(5), "");
		this.sut.punchInClock(this.testCategoryName, this.addMinutes(1));
		this.sut.punchOutClock(this.addMinutes(1), "");
		this.sut.punchInClock(this.testCategoryName, this.addMinutes(1));
		this.sut.punchOutClock(this.addMinutes(1), "");
		this.sut.punchInClock(this.testCategoryName, this.addMinutes(1));
		this.sut.punchOutClock(this.addMinutes(1), "");
		this.sut.punchInClock(this.testCategoryName, this.addMinutes(1));
		this.sut.punchOutClock(this.addMinutes(1), "");
		Assert.assertEquals(1,
				TimeSliceRepository.getCount((ITimeSliceFilter) null));
	}

	public void testPunchInOut5Discard() {
		this.sut.punchInClock(this.testCategoryName, this.addMinutes(1));
		this.sut.punchOutClock(this.addMinutes(1), "");
		this.sut.punchInClock(this.testCategoryName, this.addMinutes(1));
		this.sut.punchOutClock(this.addMinutes(1), "");
		this.sut.punchInClock(this.testCategoryName, this.addMinutes(1));
		this.sut.punchOutClock(this.addMinutes(1), "");
		this.sut.punchInClock(this.testCategoryName, this.addMinutes(1));
		this.sut.punchOutClock(this.addMinutes(1), "");
		this.sut.punchInClock(this.testCategoryName, this.addMinutes(1));
		this.sut.punchOutClock(this.addMinutes(1), "");
		Assert.assertEquals(0,
				TimeSliceRepository.getCount((ITimeSliceFilter) null));
	}

	private long addMinutes(final int addMinutes) {
		this.cal.add(Calendar.MINUTE, addMinutes);
		final long dateTime = this.cal.getTimeInMillis();
		return dateTime;
	}

}
