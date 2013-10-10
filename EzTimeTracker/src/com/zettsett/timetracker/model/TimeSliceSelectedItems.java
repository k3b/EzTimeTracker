package com.zettsett.timetracker.model;

import android.util.SparseArray;

public class TimeSliceSelectedItems {
	public static final TimeSlice EMPTY = new TimeSlice();

	/**
	 * Do not use SparseArray<TimeSlice> because it is android only so j2se
	 * junit-test will not work.
	 */
	// @SuppressLint("UseSparseArrays")
	// private final Map<Integer, TimeSlice> items = new HashMap<Integer,
	// TimeSlice>();

	private final SparseArray<TimeSlice> items = new SparseArray<TimeSlice>();

	public TimeSliceSelectedItems add(final TimeSlice item) {
		if (item != null) {
			this.items.put(item.getRowId(), item);
		}
		return this;
	}

	public TimeSliceSelectedItems add(final int item) {
		if (item != TimeSlice.IS_NEW_TIMESLICE) {
			this.items.put(item, TimeSliceSelectedItems.EMPTY);
		}
		return this;
	}

	public int size() {
		// TODO Auto-generated method stub
		return this.items.size();
	}

	public TimeSlice get(final int rowId) {
		return this.items.get(rowId);
	}
}
