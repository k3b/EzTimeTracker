package com.zettsett.timetracker;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;

public class LinearScroller {
	public LinearLayout mainLayout;
	public ViewGroup scrollView;
	private final Context mContext;
	private LayoutParams mLayoutParams;

	public LinearScroller(final Context context) {
		this.mContext = context;
		this.initScrollview();
	}

	public LinearScroller(final Context context, final LayoutParams layoutParams) {
		this.mContext = context;
		this.mLayoutParams = layoutParams;
		this.initScrollview();
	}

	public LinearLayout getMainLayout() {
		return this.mainLayout;
	}

	public ScrollView getScrollView() {
		return (ScrollView) this.scrollView;
	}

	public void initScrollview() {
		this.mainLayout = new LinearLayout(this.mContext);
		this.mainLayout.setOrientation(LinearLayout.VERTICAL);
		this.scrollView = new ScrollView(this.mContext);
		if (this.mLayoutParams != null) {
			this.scrollView.addView(this.mainLayout, this.mLayoutParams);
		} else {
			this.scrollView.addView(this.mainLayout);
		}
	}

	public void addView(final View child) {
		this.mainLayout.addView(child);
	}

}