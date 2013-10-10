package de.k3b.common;

import android.util.SparseArray;

public class SelectedItemsWithRowId<E extends IItemWithRowId> implements
		ISelection<E> {
	public final E EMPTY;

	public SelectedItemsWithRowId(final E empty) {
		this.EMPTY = empty;
	}

	/**
	 * Do not use SparseArray<TimeSlice> because it is android only so j2se
	 * junit-test will not work.
	 */
	// @SuppressLint("UseSparseArrays")
	// private final Map<Integer, TimeSlice> items = new HashMap<Integer,
	// TimeSlice>();

	private final SparseArray<E> items = new SparseArray<E>();

	public SelectedItemsWithRowId<E> add(final E item) {
		if (item != null) {
			this.items.put(item.getRowId(), item);
		}
		return this;
	}

	public SelectedItemsWithRowId<E> add(final int item) {
		if (item != ItemWithRowId.IS_NEW_TIMESLICE) {
			this.items.put(item, this.EMPTY);
		}
		return this;
	}

	public SelectedItemsWithRowId<E> remove(final IItemWithRowId item) {
		if (item != null) {
			this.items.delete(item.getRowId());
		}
		return this;
	}

	public SelectedItemsWithRowId<E> remove(final int item) {
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
	public boolean isSelected(final E item) {
		if ((item != null) && (item != this.EMPTY)) {
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
	public ISelection<E> setAsSelected(final E item, final boolean value) {
		if ((item != null) && (item != this.EMPTY)) {
			if (value) {
				this.add(item);
			} else {
				this.remove(item);
			}
		}
		return this;
	}

	@Override
	public ISelection<E> setAsSelected(final int item, final boolean value) {
		if (item != this.EMPTY.getRowId()) {
			if (value) {
				this.add(item);
			} else {
				this.remove(item);
			}
		}
		return this;
	}

}
