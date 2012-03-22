package de.k3b.widgets;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.*;
import android.util.AttributeSet;
import android.util.Log;

public class EditTextPreferenceWithSummary extends EditTextPreference {
	private final static String TAG = EditTextPreferenceWithSummary.class.getName();

	public EditTextPreferenceWithSummary(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    init();
	}

	public EditTextPreferenceWithSummary(Context context) {
	    super(context);
	    init();
	}

	private void init() {
	    Log.e(TAG, "init");
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());		
		String currentText = prefs.getString("test", this.getText());
 
	    this.setSummary(currentText);
	
	    setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

	        @Override
	        public boolean onPreferenceChange(Preference preference, Object newValue) {
	            Log.w(TAG, "display score changed to "+newValue);
	            preference.setSummary(newValue.toString()); // getSummary());
	            return true;
	        }
	    });
	}

	@Override
	public CharSequence getSummary() {
	    return super.getSummary();
	}

}
