package com.eightydegreeswest.irisplus.custom;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

/**
 * Created by ybelenitsky on 2/22/2015.
 */
public class DetailedListPreference extends ListPreference {
    public DetailedListPreference(Context context) { super(context); }

    public DetailedListPreference(Context context, AttributeSet attrs) { super(context, attrs); }

    @Override
    public void setValue(String value) {
        super.setValue(value);
        setSummary(getEntry());
    }
}
