package com.zettsett.timetracker.model;

import junit.framework.Assert;

import org.junit.Test;

import de.k3b.common.ISelection;

public class TimeSliceSelectionTest {
	private final int ID = 22;
	private final TimeSlice timeSliceItem = new TimeSlice(this.ID)
			.setStartTime(12).setEndTime(77).setNotes("some Notes");

	@Test
	public void shouldGetAsSelected() {
		final ISelection<TimeSlice> sut = this.createTimeSliceSelectedItems()
				.setAsSelected(this.timeSliceItem, true);
		final boolean result = sut.isSelected(this.timeSliceItem);
		Assert.assertEquals(true, result);
	}

	@Test
	public void shouldGetAsSelectedById() {
		final ISelection<TimeSlice> sut = this.createTimeSliceSelectedItems()
				.setAsSelected(this.ID, true);
		final boolean result = sut.isSelected(this.ID);
		Assert.assertEquals(true, result);
	}

	@Test
	public void shouldGetNullAsNotSelected() {
		final ISelection<TimeSlice> sut = this.createTimeSliceSelectedItems()
				.setAsSelected(this.timeSliceItem, true);
		final boolean result = sut.isSelected(null);
		Assert.assertEquals(false, result);
	}

	@Test
	public void shouldGetEmptyAsNotSelected() {
		final ISelection<TimeSlice> sut = this.createTimeSliceSelectedItems()
				.setAsSelected(this.timeSliceItem, true);
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
	public void shouldSetAsSelectedById() {
		final ISelection<TimeSlice> sut = this.createTimeSliceSelectedItems();
		sut.setAsSelected(this.ID, true);
		final boolean result = sut.isSelected(this.ID);
		Assert.assertEquals(true, result);
	}

	@Test
	public void shouldSetAsNotSelected() {
		final ISelection<TimeSlice> sut = this.createTimeSliceSelectedItems()
				.setAsSelected(this.timeSliceItem, true);
		sut.setAsSelected(this.timeSliceItem, false);
		final boolean result = sut.isSelected(this.timeSliceItem);
		Assert.assertEquals(false, result);
	}

	@Test
	public void shouldSetAsNotSelectedById() {
		final ISelection<TimeSlice> sut = this.createTimeSliceSelectedItems()
				.setAsSelected(this.ID, true);
		sut.setAsSelected(this.ID, false);
		final boolean result = sut.isSelected(this.ID);
		Assert.assertEquals(false, result);
	}

	private ISelection<TimeSlice> createTimeSliceSelectedItems() {
		return new TimeSliceSelectedItems();
	}

}
