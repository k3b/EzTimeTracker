/*
 * Copyright (C) 2011 Daniel Berndt - Codeus Ltd  -  DateSlider
 *
 * This class contains all the scrolling logic of the slidable elements
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.googlecode.android.widgets.DateSlider.timeview;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.googlecode.android.widgets.DateSlider.TimeObject;

/**
 * This is a more complex implementation of the TimeView consisting of a LinearLayout with
 * two TimeViews. This allows primary text and sub-text, such as the name of the day
 * and the day of the month. This class expects the text that it is passed via
 * {@link #setVals(TimeObject)} or {@link #setVals(TimeView)} to contian the primary
 * string followed by a space and then the secondary string.
 */
public class TimeLayoutView extends LinearLayout implements TimeView {
    protected long endTime, startTime;
    protected String text;
    protected boolean isCenter=false, isOutOfBounds=false;
    protected TextView topView, bottomView;

    /**
     * constructor
     *
     * @param context
     * @param isCenterView true if the element is the centered view in the ScrollLayout
     * @param topTextSize	text size of the top TextView in dps
     * @param bottomTextSize	text size of the bottom TextView in dps
     * @param lineHeight	LineHeight of the top TextView
     */
    public TimeLayoutView(Context context, boolean isCenterView, int topTextSize, int bottomTextSize, float lineHeight) {
        super(context);
        setupView(context, isCenterView, topTextSize, bottomTextSize, lineHeight);
    }

    /**
     * Setting up the top TextView and bottom TextVew
     * @param context
     * @param isCenterView true if the element is the centered view in the ScrollLayout
     * @param topTextSize	text size of the top TextView in dps
     * @param bottomTextSize	text size of the bottom TextView in dps
     * @param lineHeight	LineHeight of the top TextView
     */
    protected void setupView(Context context, boolean isCenterView, int topTextSize, int bottomTextSize, float lineHeight) {
        setOrientation(VERTICAL);
        topView = new TextView(context);
        topView.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM);
        topView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, topTextSize);
        bottomView = new TextView(context);
        bottomView.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.TOP);
        bottomView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, bottomTextSize);
        topView.setLineSpacing(0, lineHeight);
        if (isCenterView) {
            isCenter = true;
            topView.setTypeface(Typeface.DEFAULT_BOLD);
            topView.setTextColor(0xFF333333);
            bottomView.setTypeface(Typeface.DEFAULT_BOLD);
            bottomView.setTextColor(0xFF444444);
            topView.setPadding(0, 5-(int)(topTextSize/15.0), 0, 0);
        } else {
            topView.setPadding(0, 5, 0, 0);
            topView.setTextColor(0xFF666666);
            bottomView.setTextColor(0xFF666666);
        }
        addView(topView);addView(bottomView);

    }

    
    public void setVals(TimeObject to) {
        text = to.text.toString();
        setText();
        this.startTime = to.startTime;
        this.endTime = to.endTime;
    }

    
    public void setVals(TimeView other) {
        text = other.getTimeText().toString();
        setText();
        startTime = other.getStartTime();
        endTime = other.getEndTime();
    }

    /**
     * sets the TextView texts by splitting the text into two
     */
    protected void setText() {
        String[] splitTime = text.split(" ");
        topView.setText(splitTime[0]);
        bottomView.setText(splitTime[1]);
    }

    
    public String getTimeText() {
        return text;
    }

    
    public long getStartTime() {
        return startTime;
    }

    
    public long getEndTime() {
        return endTime;
    }

	public boolean isOutOfBounds() {
		return isOutOfBounds;
	}

	public void setOutOfBounds(boolean outOfBounds) {
		if (outOfBounds && !isOutOfBounds) {
			topView.setTextColor(0x44666666);
            bottomView.setTextColor(0x44666666);
		}
		else if (!outOfBounds && isOutOfBounds) {
            topView.setTextColor(0xFF666666);
            bottomView.setTextColor(0xFF666666);
		}
		isOutOfBounds = outOfBounds;
	}

}