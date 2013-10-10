package de.k3b.common;

import junit.framework.Assert;

import org.junit.Test;

public class SelectedItemsWithRowIdTest {
	private final int ID = 22;
	private final ItemWithRowId timeSliceItem = new ItemWithRowId()
			.setRowId(this.ID);

	@Test
	public void shouldGetAsSelected() {
		final ISelection<ItemWithRowId> sut = this
				.createTimeSliceSelectedItems().setAsSelected(
						this.timeSliceItem, true);
		final boolean result = sut.isSelected(this.timeSliceItem);
		Assert.assertEquals(true, result);
	}

	@Test
	public void shouldGetAsSelectedById() {
		final ISelection<ItemWithRowId> sut = this
				.createTimeSliceSelectedItems().setAsSelected(this.ID, true);
		final boolean result = sut.isSelected(this.ID);
		Assert.assertEquals(true, result);
	}

	@Test
	public void shouldGetNullAsNotSelected() {
		final ISelection<ItemWithRowId> sut = this
				.createTimeSliceSelectedItems().setAsSelected(
						this.timeSliceItem, true);
		final boolean result = sut.isSelected(null);
		Assert.assertEquals(false, result);
	}

	@Test
	public void shouldGetEmptyAsNotSelected() {
		final ISelection<ItemWithRowId> sut = this
				.createTimeSliceSelectedItems().setAsSelected(
						this.timeSliceItem, true);
		final boolean result = sut.isSelected(ItemWithRowId.EMPTY);
		Assert.assertEquals(false, result);
	}

	@Test
	public void shouldSetAsSelected() {
		final ISelection<ItemWithRowId> sut = this
				.createTimeSliceSelectedItems();
		sut.setAsSelected(this.timeSliceItem, true);
		final boolean result = sut.isSelected(this.timeSliceItem);
		Assert.assertEquals(true, result);
	}

	@Test
	public void shouldSetAsSelectedById() {
		final ISelection<ItemWithRowId> sut = this
				.createTimeSliceSelectedItems();
		sut.setAsSelected(this.ID, true);
		final boolean result = sut.isSelected(this.ID);
		Assert.assertEquals(true, result);
	}

	@Test
	public void shouldSetAsNotSelected() {
		final ISelection<ItemWithRowId> sut = this
				.createTimeSliceSelectedItems().setAsSelected(
						this.timeSliceItem, true);
		sut.setAsSelected(this.timeSliceItem, false);
		final boolean result = sut.isSelected(this.timeSliceItem);
		Assert.assertEquals(false, result);
	}

	@Test
	public void shouldSetAsNotSelectedById() {
		final ISelection<ItemWithRowId> sut = this
				.createTimeSliceSelectedItems().setAsSelected(this.ID, true);
		sut.setAsSelected(this.ID, false);
		final boolean result = sut.isSelected(this.ID);
		Assert.assertEquals(false, result);
	}

	private ISelection<ItemWithRowId> createTimeSliceSelectedItems() {
		return new SelectedItemsWithRowId<ItemWithRowId>(ItemWithRowId.EMPTY);
	}

}
