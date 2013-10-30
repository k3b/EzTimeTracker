package de.k3b.util;

import java.util.Calendar;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DateTimeUtilTests {
	DateTimeUtil dtu;
	long testDateSylvester2013;
	long testDateNewYear2013;

	@Before
	public void setUp() {
		Locale.setDefault(Locale.GERMANY);
		this.dtu = new DateTimeUtil(0);
		Calendar c = this.dtu.getCalendar(2013, 12 - 1, 31, 17, 45, 59, 123);
		this.testDateSylvester2013 = c.getTimeInMillis();
		c = this.dtu.getCalendar(2013, 1 - 1, 1, 1, 25, 57, 987);
		this.testDateNewYear2013 = c.getTimeInMillis();
	}

	@Test
	public void ShouldFormatISO() {
		final String resultSylvester = this.dtu
				.getIsoDateTimeStr(this.testDateSylvester2013);
		final String resultNewYear = this.dtu
				.getIsoDateTimeStr(this.testDateNewYear2013);
		Assert.assertEquals("Sylvester", "2013-12-31T17:45:59+0100",
				resultSylvester);
		Assert.assertEquals("NewYear", "2013-01-01T01:25:57+0100",
				resultNewYear);
	}

	@Test
	public void ShouldFormatYear() {
		final String resultSylvester = this.dtu
				.getYearString(this.testDateSylvester2013);
		Assert.assertEquals("Sylvester", "2013", resultSylvester);
	}

	@Test
	public void ShouldTruncateYear() {
		final long truncatedSylvester = this.dtu
				.getStartOfYear(this.testDateSylvester2013);
		final long truncatedNewYear = this.dtu
				.getStartOfYear(this.testDateNewYear2013);
		Assert.assertEquals("Sylvester", "2013-01-01T00:00:00+0100",
				this.dtu.getIsoDateTimeStr(truncatedSylvester));
		Assert.assertEquals("NewYear", "2013-01-01T00:00:00+0100",
				this.dtu.getIsoDateTimeStr(truncatedNewYear));
	}

	@Test
	public void ShouldTruncateMonth() {
		final long truncatedSylvester = this.dtu
				.getStartOfMonth(this.testDateSylvester2013);
		final long truncatedNewYear = this.dtu
				.getStartOfMonth(this.testDateNewYear2013);
		Assert.assertEquals("Sylvester", "2013-12-01T00:00:00+0100",
				this.dtu.getIsoDateTimeStr(truncatedSylvester));
		Assert.assertEquals("NewYear", "2013-01-01T00:00:00+0100",
				this.dtu.getIsoDateTimeStr(truncatedNewYear));
	}

	@Test
	public void ShouldTruncateDay() {
		final long truncatedSylvester = this.dtu
				.getStartOfDay(this.testDateSylvester2013);
		final long truncatedNewYear = this.dtu
				.getStartOfDay(this.testDateNewYear2013);
		Assert.assertEquals("Sylvester", "2013-12-31T00:00:00+0100",
				this.dtu.getIsoDateTimeStr(truncatedSylvester));
		Assert.assertEquals("NewYear", "2013-01-01T00:00:00+0100",
				this.dtu.getIsoDateTimeStr(truncatedNewYear));
	}

	@Test
	public void ShouldTruncateWeek() {
		long truncated = this.dtu.getStartOfWeek(this.testDateNewYear2013);
		Assert.assertEquals("Sylvester", "2012-12-29T00:00:00+0100",
				this.dtu.getIsoDateTimeStr(truncated));

		truncated = this.dtu.getStartOfWeek(truncated);
		Assert.assertEquals("should not change", "2012-12-29T00:00:00+0100",
				this.dtu.getIsoDateTimeStr(truncated));

		truncated = this.dtu.getStartOfWeek(this.dtu.addDays(truncated, 6));
		Assert.assertEquals("date+6", "2012-12-29T00:00:00+0100",
				this.dtu.getIsoDateTimeStr(truncated));
	}

	@Test
	public void ShouldAddDays() {
		final long resultSylvester = this.dtu.addDays(
				this.testDateSylvester2013, 365);
		Assert.assertEquals("Sylvester", "2014-12-31T17:45:59+0100",
				this.dtu.getIsoDateTimeStr(resultSylvester));
	}

}
