package de.k3b.util;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.junit.*;

public class DateTimeUtilTests {
	DateTimeUtil dtu;
	long testDateSylvester2013;
	long testDateNewYear2013;
	
	@Before
	public void setUp() {
		Locale.setDefault(Locale.GERMANY);
		dtu = new DateTimeUtil(0);
		Calendar c = dtu.getCalendar(2013, 12-1, 31, 17, 45, 59, 123);
		testDateSylvester2013 = c.getTimeInMillis();
		c = dtu.getCalendar(2013, 1-1, 1, 1, 25, 57, 987);
		testDateNewYear2013 = c.getTimeInMillis();
	}


	@Test
	public void ShouldFormatISO() {
		String resultSylvester = dtu.getIsoDateTimeStr(testDateSylvester2013);
		String resultNewYear = dtu.getIsoDateTimeStr(testDateNewYear2013); 
		Assert.assertEquals("Sylvester", "2013-12-31T17:45:59+0100", resultSylvester);
		Assert.assertEquals("NewYear", "2013-01-01T01:25:57+0100", resultNewYear);
	}

	@Test
	public void ShouldTruncateYear() {
		long truncatedSylvester = dtu.getStartOfYear(testDateSylvester2013);
		long truncatedNewYear = dtu.getStartOfYear(testDateNewYear2013);
		Assert.assertEquals("Sylvester", "2013-01-01T00:00:00+0100", dtu.getIsoDateTimeStr(truncatedSylvester));
		Assert.assertEquals("NewYear", "2013-01-01T00:00:00+0100", dtu.getIsoDateTimeStr(truncatedNewYear));
	}

	@Test
	public void ShouldTruncateMonth() {
		long truncatedSylvester = dtu.getStartOfMonth(testDateSylvester2013);
		long truncatedNewYear = dtu.getStartOfMonth(testDateNewYear2013);
		Assert.assertEquals("Sylvester", "2013-12-01T00:00:00+0100", dtu.getIsoDateTimeStr(truncatedSylvester));
		Assert.assertEquals("NewYear", "2013-01-01T00:00:00+0100", dtu.getIsoDateTimeStr(truncatedNewYear));
	}

	@Test
	public void ShouldTruncateDay() {
		long truncatedSylvester = dtu.getStartOfDay(testDateSylvester2013);
		long truncatedNewYear = dtu.getStartOfDay(testDateNewYear2013);
		Assert.assertEquals("Sylvester", "2013-12-31T00:00:00+0100", dtu.getIsoDateTimeStr(truncatedSylvester));
		Assert.assertEquals("NewYear", "2013-01-01T00:00:00+0100", dtu.getIsoDateTimeStr(truncatedNewYear));
	}

	@Test
	public void ShouldAddDays() {
		long resultSylvester = dtu.addDays(testDateSylvester2013, 365);
		Assert.assertEquals("Sylvester", "2014-12-31T17:45:59+0100", dtu.getIsoDateTimeStr(resultSylvester));
	}

}
