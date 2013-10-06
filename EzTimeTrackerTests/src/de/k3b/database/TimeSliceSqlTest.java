package de.k3b.database;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.zettsett.timetracker.activity.FilterParameter;
import com.zettsett.timetracker.database.TimeSliceRepository;

public class TimeSliceSqlTest {
	long testDateSylvester2001;
	long testDateNewYear2001;
	FilterParameter filter;

	@Before
	public void setUp() {
		this.testDateSylvester2001 = 20011231;
		this.testDateNewYear2001 = 20010101;

		this.filter = new FilterParameter().setStartTime(
				this.testDateNewYear2001)
				.setEndTime(this.testDateSylvester2001);
	}

	@Test
	public void ShouldFormatFromTo() {
		final String sql = TimeSliceRepository.createFilter(this.filter);
		Assert.assertEquals("", sql);
	}

}
