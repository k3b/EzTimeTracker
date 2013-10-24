package com.zettsett.timetracker.database;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.zettsett.timetracker.activity.TimeSliceFilterParameter;

import de.k3b.database.SqlFilter;

public class TimeSliceSqlTest {
	TimeSliceFilterParameter timeSliceFilter;

	@Before
	public void setUp() {
		this.timeSliceFilter = new TimeSliceFilterParameter();
	}

	@Test
	public void ShouldFormatEmpty() {
		final SqlFilter sqlFilter = this.createFilter();

		Assert.assertNotNull("sqlFilter", sqlFilter);
		Assert.assertNull("sqlFilter.args", sqlFilter.args);
		Assert.assertNull("sqlFilter.sql", sqlFilter.sql);
	}

	@Test
	public void ShouldFormatCategory() {

		this.timeSliceFilter.setCategoryId(22);
		final SqlFilter sqlFilter = this.createFilter();

		Assert.assertEquals(sqlFilter.getDebugMessage("sql"),
				"category_id = ?", sqlFilter.sql);
		Assert.assertEquals(sqlFilter.getDebugMessage("args.length"), 1,
				sqlFilter.args.length);
		Assert.assertEquals(sqlFilter.getDebugMessage("args[0]"), "22",
				sqlFilter.args[0]);
	}

	@Test
	public void ShouldFormatIgnoreDate() {
		this.timeSliceFilter.setStartTime(20010101).setEndTime(20011231)
				.setIgnoreDates(true);
		final SqlFilter sqlFilter = this.createFilter();

		Assert.assertNotNull("sqlFilter", sqlFilter);
		Assert.assertNull("sqlFilter.args", sqlFilter.args);
		Assert.assertNull("sqlFilter.sql", sqlFilter.sql);
	}

	@Test
	public void ShouldFormatDateFromTo() {
		this.timeSliceFilter.setStartTime(20010101).setEndTime(20011231);
		final SqlFilter sqlFilter = this.createFilter();

		Assert.assertEquals(sqlFilter.getDebugMessage("sql"),
				"start_time>= ? AND start_time<= ?", sqlFilter.sql);
		Assert.assertEquals(sqlFilter.getDebugMessage("args.length"), 2,
				sqlFilter.args.length);
		Assert.assertEquals(sqlFilter.getDebugMessage("args[0]"), "20010101",
				sqlFilter.args[0]);
		Assert.assertEquals(sqlFilter.getDebugMessage("args[1]"), "20011231",
				sqlFilter.args[1]);
	}

	@Test
	public void ShouldFormatNotesNotNull() {

		this.timeSliceFilter.setNotesNotNull(true);
		final SqlFilter sqlFilter = this.createFilter();

		Assert.assertEquals(sqlFilter.getDebugMessage("sql"),
				"notes IS NOT NULL AND notes <> ''", sqlFilter.sql);
		Assert.assertNull(sqlFilter.getDebugMessage("args"), sqlFilter.args);
	}

	@Test
	public void ShouldFormatNotesLike() {

		this.timeSliceFilter.setNotes("Hello World");
		final SqlFilter sqlFilter = this.createFilter();

		Assert.assertEquals(sqlFilter.getDebugMessage("sql"), "notes LIKE ?",
				sqlFilter.sql);
		Assert.assertEquals(sqlFilter.getDebugMessage("args.length"), 1,
				sqlFilter.args.length);
		Assert.assertEquals(sqlFilter.getDebugMessage("args[0]"),
				"%Hello World%", sqlFilter.args[0]);
	}

	@Test
	public void ShouldFormatAll() {

		this.timeSliceFilter.setNotes("Hello World").setCategoryId(22)
				.setStartTime(20010101).setEndTime(20011231);
		final SqlFilter sqlFilter = this.createFilter();

		Assert.assertNotNull(sqlFilter.getDebugMessage("sql"), sqlFilter.sql);
		Assert.assertEquals(sqlFilter.getDebugMessage("args.length"), 4,
				sqlFilter.args.length);

		System.out.append(sqlFilter.toString());
	}

	private SqlFilter createFilter() {
		return TimeSliceSql.createFilter(this.timeSliceFilter);
	}
}
