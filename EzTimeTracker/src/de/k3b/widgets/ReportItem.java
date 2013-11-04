package de.k3b.widgets;

import android.content.Context;
import android.graphics.Color;
import android.widget.TextView;
import de.k3b.common.IItemWithRowId;

public class ReportItem<T extends IItemWithRowId> extends TextView {

	private boolean selected = false;

	public ReportItem(final Context context) {
		super(context);
	}

	@Override
	public boolean isSelected() {
		return this.selected;
	}

	public ReportItem<T> setAsSelected(final boolean value) {
		this.selected = value;
		this.setBackgroundColor((value) ? Color.DKGRAY : Color.BLACK);
		return this;
	}
}
