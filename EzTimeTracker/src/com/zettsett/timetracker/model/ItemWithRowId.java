package com.zettsett.timetracker.model;

public class ItemWithRowId implements IItemWithRowId {
	public static final int IS_NEW_TIMESLICE = -1;

	private int rowId = ItemWithRowId.IS_NEW_TIMESLICE;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.zettsett.timetracker.model.IItemWithRowId#getRowId()
	 */
	@Override
	public int getRowId() {
		return this.rowId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.zettsett.timetracker.model.IItemWithRowId#setRowId(int)
	 */
	@Override
	public ItemWithRowId setRowId(final int rowId) {
		this.rowId = rowId;
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.zettsett.timetracker.model.IItemWithRowId#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.getRowId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.zettsett.timetracker.model.IItemWithRowId#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}

		return (this.getRowId() == ((IItemWithRowId) obj).getRowId());
	}

}
