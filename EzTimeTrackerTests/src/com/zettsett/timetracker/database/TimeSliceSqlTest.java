package com.zettsett.timetracker.database;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.zettsett.timetracker.activity.TimeSliceFilterParameter;

public class TimeSliceSqlTest {
	long testDateSylvester2001;
	long testDateNewYear2001;
	List<String> filterArgs;

	TimeSliceFilterParameter filter;

	@Before
	public void setUp() {
		this.testDateSylvester2001 = 20011231;
		this.testDateNewYear2001 = 20010101;

		this.filter = new TimeSliceFilterParameter().setStartTime(
				this.testDateNewYear2001)
				.setEndTime(this.testDateSylvester2001);
		this.filterArgs = new ArrayList<String>();
	}

	@Test
	public void ShouldFormatFromTo() {
		final String sqlFilter = TimeSliceSql.createFilter(this.filter);
		Assert.assertEquals("", sqlFilter);
	}

}
