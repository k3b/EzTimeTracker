package com.zettsett.timetracker.model;

import junit.framework.Assert;

import org.junit.Test;

public class TimeSliceTest {
	final TimeSlice ts1 = new TimeSlice().setRowId(22).setStartTime(12)
			.setEndTime(77).setNotes("some Notes");
	final TimeSlice ts2 = new TimeSlice().setRowId(22).setStartTime(212)
			.setEndTime(277).setNotes("some more Notes");

	@Test
	public void ShouldBeEqualIfIdEqual() {
		Assert.assertEquals(this.ts1, this.ts2);
	}

	@Test
	public void ShouldNotStoreDuplicates() {
		final TimeSliceSelectedItems items = this
				.createTimeSliceSelectedItems().add(this.ts1).add(this.ts2);
		Assert.assertEquals(1, items.size());
	}

	@Test
	public void ShouldNotStoreDuplicatesWithID() {
		final TimeSliceSelectedItems items = this
				.createTimeSliceSelectedItems().add(this.ts1)
				.add(this.ts2.getRowId());
		Assert.assertEquals(1, items.size());
	}

	@Test
	public void ShouldGetByID() {
		final TimeSliceSelectedItems items = this
				.createTimeSliceSelectedItems().add(this.ts1);
		final TimeSlice found = items.get(this.ts1.getRowId());
		Assert.assertEquals(this.ts1, found);
	}

	private TimeSliceSelectedItems createTimeSliceSelectedItems() {
		return new TimeSliceSelectedItems();
	}
}
