package com.zettsett.timetracker.model;

public interface IItemWithRowId {

	public abstract int getRowId();

	public abstract IItemWithRowId setRowId(final int rowId);

	public abstract int hashCode();

	public abstract boolean equals(final Object obj);

}