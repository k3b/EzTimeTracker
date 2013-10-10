package com.zettsett.timetracker.model;

import android.util.SparseArray;
import de.k3b.common.IItemWithRowId;
import de.k3b.common.ISelection;

public class TimeSliceSelectedItems implements ISelection<TimeSlice> {
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

	public TimeSliceSelectedItems remove(final IItemWithRowId item) {
		if (item != null) {
			this.items.delete(item.getRowId());
		}
		return this;
	}

	public TimeSliceSelectedItems remove(final int item) {
		this.items.delete(item);
		return this;
	}

	public int size() {
		// TODO Auto-generated method stub
		return this.items.size();
	}

	public IItemWithRowId get(final int rowId) {
		return this.items.get(rowId);
	}

	@Override
	public boolean isSelected(final TimeSlice item) {
		if ((item != null) && (item != TimeSliceSelectedItems.EMPTY)) {
			final IItemWithRowId found = this.get(item.getRowId());
			if (found != null) {
				this.add(item);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isSelected(final int item) {
		final IItemWithRowId found = this.get(item);
		if (found != null) {
			return true;
		}
		return false;
	}

	@Override
	public ISelection<TimeSlice> setAsSelected(final TimeSlice item,
			final boolean value) {
		if ((item != null) && (item != TimeSliceSelectedItems.EMPTY)) {
			if (value) {
				this.add(item);
			} else {
				this.remove(item);
			}
		}
		return this;
	}

	@Override
	public ISelection<TimeSlice> setAsSelected(final int item,
			final boolean value) {
		if (item != TimeSliceSelectedItems.EMPTY.getRowId()) {
			if (value) {
				this.add(item);
			} else {
				this.remove(item);
			}
		}
		return this;
	}

}
