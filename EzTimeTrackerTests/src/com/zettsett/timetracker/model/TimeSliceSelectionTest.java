package com.zettsett.timetracker.model;

import junit.framework.Assert;

import org.junit.Test;

import de.k3b.common.ISelection;

public class TimeSliceSelectionTest {
	final TimeSlice timeSliceItem = new TimeSlice().setRowId(22)
			.setStartTime(12).setEndTime(77).setNotes("some Notes");

	@Test
	public void shouldGetAsSelected() {
		final ISelection<TimeSlice> sut = this.createTimeSliceSelectedItems()
				.add(this.timeSliceItem);
		final boolean result = sut.isSelected(this.timeSliceItem);
		Assert.assertEquals(true, result);
	}

	@Test
	public void shouldGetNullAsNotSelected() {
		final ISelection<TimeSlice> sut = this.createTimeSliceSelectedItems()
				.add(this.timeSliceItem);
		final boolean result = sut.isSelected(null);
		Assert.assertEquals(false, result);
	}

	@Test
	public void shouldGetEmptyAsNotSelected() {
		final ISelection<TimeSlice> sut = this.createTimeSliceSelectedItems()
				.add(this.timeSliceItem);
		final boolean result = sut.isSelected(TimeSliceSelectedItems.EMPTY);
		Assert.assertEquals(false, result);
	}

	@Test
	public void shouldSetAsSelected() {
		final ISelection<TimeSlice> sut = this.createTimeSliceSelectedItems();
		sut.setAsSelected(this.timeSliceItem, true);
		final boolean result = sut.isSelected(this.timeSliceItem);
		Assert.assertEquals(true, result);
	}

	@Test
	public void shouldSetAsNotSelected() {
		final ISelection<TimeSlice> sut = this.createTimeSliceSelectedItems()
				.add(this.timeSliceItem);
		;
		sut.setAsSelected(this.timeSliceItem, false);
		final boolean result = sut.isSelected(this.timeSliceItem);
		Assert.assertEquals(false, result);
	}

	private TimeSliceSelectedItems createTimeSliceSelectedItems() {
		return new TimeSliceSelectedItems();
	}

}
