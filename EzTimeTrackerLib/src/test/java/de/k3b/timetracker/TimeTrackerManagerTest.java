package de.k3b.timetracker;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;

import de.k3b.timetracker.database.TimeSliceCategoryRepsitoryMock;
import de.k3b.timetracker.database.TimeSliceRepositoryMock;
import de.k3b.util.DateTimeUtil;
import de.k3b.util.ISessionDataPersistance;

/**
 * Tests TimeTrackerManager with simulated Database/Persistence
 */
public class TimeTrackerManagerTest {
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
    private final String testCategoryName = "TEST";
    private final Settings settings = new Settings() {
        @Override
        public long getMinPunchOutTreshholdInMilliSecs() {
            return 120000;
        }
    };

    private TimeTrackerSessionData sessionData;
    private TimeTrackerManager sut;
    private Calendar cal;
    private TimeSliceRepositoryMock timeSliceRepository;

    public TimeTrackerManagerTest() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        Global.setDebugEnabled(false);
        Global.setInfoEnabled(false);
        final DateTimeUtil dtu = new DateTimeUtil(0);
        this.cal = dtu.getCalendar(2013, 1 - 1, 1, 17, 45, 59, 123);

        timeSliceRepository = new TimeSliceRepositoryMock();
        sessionData = new TimeTrackerSessionData();
        this.sut = new TimeTrackerManager(this.sessionDataPersistance,
                timeSliceRepository,
                new TimeSliceCategoryRepsitoryMock(),
                sessionData, null, settings);
    }

    @Test
    public void testPunchInOut() {
        this.sut.punchInClock(this.testCategoryName, this.addMinutes(1));
        this.sut.punchOutClock(this.addMinutes(10), "");
        Assert.assertEquals(1,
                timeSliceRepository.getCount());
    }

    @Test
    public void testPunchInOutNotes() {
        this.sut.punchInClock(this.testCategoryName, this.addMinutes(1));
        this.sut.punchOutClock(this.addMinutes(10), "hello");
        Assert.assertEquals(1,
                timeSliceRepository.getCount());
    }

    @Test
    public void testPunchOut() {
        this.sut.punchOutClock(this.addMinutes(10), "");
        Assert.assertEquals(0,
                timeSliceRepository.getCount());
    }

    @Test
    public void testPunchIn2Out() {
        this.sut.punchInClock(this.testCategoryName, this.addMinutes(1));
        this.sut.punchInClock(this.testCategoryName, this.addMinutes(1));
        this.sut.punchOutClock(this.addMinutes(10), "");
        Assert.assertEquals(1,
                timeSliceRepository.getCount());
    }

    @Test
    public void testPunchInChangeOut() {
        this.sut.punchInClock(this.testCategoryName, this.addMinutes(1));
        this.sut.punchInClock(this.testCategoryName + "2", this.addMinutes(1));
        this.sut.punchOutClock(this.addMinutes(10), "");
        Assert.assertEquals(2,
                timeSliceRepository.getCount());
    }

    @Test
    public void testPunchInOutInSeperateInstance() throws Exception {
        this.sut.punchInClock(this.testCategoryName, this.addMinutes(1));

        this.sut = null;

        // create new session
        setUp();
        this.sut.reloadSessionData();
        Assert.assertEquals(true,
                sut.isPunchedIn());

        this.sut.punchOutClock(this.addMinutes(10), "");
        Assert.assertEquals("timeSliceRepository.getCount()", 1,
                timeSliceRepository.getCount());
        System.out.println("session: " + this.sessionData.toString());
    }

    @Test
    public void testPunchInOut2() {
        this.sut.punchInClock(this.testCategoryName, this.addMinutes(1));
        this.sut.punchOutClock(this.addMinutes(10), "");

        // after treshhold: add new
        this.sut.punchInClock(this.testCategoryName, this.addMinutes(10));
        this.sut.punchOutClock(this.addMinutes(10), "");
        Assert.assertEquals(2,
                timeSliceRepository.getCount());
    }

    @Test
    public void testPunchInOutAppend() {
        this.sut.punchInClock(this.testCategoryName, this.addMinutes(1));
        this.sut.punchOutClock(this.addMinutes(10), "");

        this.timeSliceRepository.setAppendMode();
        // within treshhold: append (undo punchout)
        this.sut.punchInClock(this.testCategoryName, this.addMinutes(1));
        this.sut.punchOutClock(this.addMinutes(10), "");
        Assert.assertEquals(1,
                timeSliceRepository.getCount());
    }

    @Test
    public void testPunchInOutDiscard() {
        this.sut.punchInClock(this.testCategoryName, this.addMinutes(1));
        this.sut.punchOutClock(this.addMinutes(1), "");
        Assert.assertEquals(0,
                timeSliceRepository.getCount());
    }

    @Test
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
                timeSliceRepository.getCount());
    }

    @Test
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
                timeSliceRepository.getCount());
    }

    private long addMinutes(final int addMinutes) {
        this.cal.add(Calendar.MINUTE, addMinutes);
        final long dateTime = this.cal.getTimeInMillis();
        return dateTime;
    }
}
