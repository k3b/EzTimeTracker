package de.k3b.common;

public interface ISelection<E> {

	/**
	 * @return true if item is in selection.
	 */
	boolean isSelected(E item);

	/**
	 * @return true if id is in selection.
	 */
	boolean isSelected(int id);

	/**
	 * Adds item to selection if value is true. Else removes it
	 */
	ISelection<E> setAsSelected(E item, boolean value);

	/**
	 * Adds id to selection if value is true. Else removes it
	 */
	ISelection<E> setAsSelected(int id, boolean value);

}
