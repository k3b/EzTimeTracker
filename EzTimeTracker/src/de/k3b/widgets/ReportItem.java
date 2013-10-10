package de.k3b.widgets;

import android.content.Context;
import android.widget.TextView;
import de.k3b.common.ISelection;

public class ReportItem<T> extends TextView implements ISelection<T> {

	private final ISelection<T> selection;
	private boolean selected = false;

	public ReportItem(final Context context, final ISelection<T> selection) {
		super(context);
		if (selection == null) {
			throw new IllegalArgumentException("ISelection<T>");
		}
		this.selection = selection;
	}

	@Override
	public boolean isSelected(final T item) {
		return this.selection.isSelected(item);
	}

	@Override
	public boolean isSelected() {
		return this.selected;
	}

	@Override
	public boolean isSelected(final int item) {
		// TODO Auto-generated method stub
		return this.selection.isSelected(item);
	}

	@Override
	public ISelection<T> setAsSelected(final T item, final boolean value) {
		this.selected = value;
		// TODO Auto-generated method stub
		return this.selection.setAsSelected(item, value);
	}

	@Override
	public ISelection<T> setAsSelected(final int item, final boolean value) {
		this.selected = value;
		// TODO Auto-generated method stub
		return this.selection.setAsSelected(item, value);
	}

}
