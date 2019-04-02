package com.eightydegreeswest.irisplus.custom;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

/**
 * Created by ybelenitsky on 2/22/2015.
 */
public class DetailedEditTextPreference extends EditTextPreference {
    public DetailedEditTextPreference(Context context) { super(context); }

    public DetailedEditTextPreference(Context context, AttributeSet attrs) { super(context, attrs); }

    @Override
    public void setText(String value) {
        super.setText(value);
        setSummary(getText());
    }
}
