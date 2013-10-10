package de.k3b.common;

public interface ISelection<E> {

	/**
	 * @return true if item is in selection.
	 */
	boolean isSelected(E item);

	/**
	 * Adds item to selection if value ist true. Else removes it
	 */
	ISelection<E> setAsSelected(E item, boolean value);

}
