package com.zettsett.timetracker.model;

import junit.framework.Assert;

import org.junit.Test;

public class TimeSliceTest {
	final TimeSlice ts1 = new TimeSlice(22).setStartTime(12).setEndTime(77)
			.setNotes("some Notes");
	final TimeSlice ts2 = new TimeSlice(22).setStartTime(212).setEndTime(277)
			.setNotes("some more Notes");

	@Test
	public void shouldBeEqualIfIdEqual() {
		Assert.assertEquals(this.ts1, this.ts2);
	}

	@Test
	public void shouldNotStoreDuplicates() {
		final TimeSliceSelectedItems items = this
				.createTimeSliceSelectedItems().add(this.ts1).add(this.ts2);
		Assert.assertEquals(1, items.size());
	}

	@Test
	public void shouldNotStoreDuplicatesWithID() {
		final TimeSliceSelectedItems items = this
				.createTimeSliceSelectedItems().add(this.ts1)
				.add(this.ts2.getRowId());
		Assert.assertEquals(1, items.size());
	}

	@Test
	public void shouldGetByID() {
		final TimeSliceSelectedItems items = this
				.createTimeSliceSelectedItems().add(this.ts1);
		final IItemWithRowId found = items.get(this.ts1.getRowId());
		Assert.assertEquals(this.ts1, found);
	}

	private TimeSliceSelectedItems createTimeSliceSelectedItems() {
		return new TimeSliceSelectedItems();
	}
}
