package com.whitespider.impact.util;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.whitespider.impact.ble.sensortag.R;

/**
 * Created by ferhat on 2/16/2016.
 */
public class HeadgearEditTestPreference extends EditTextPreference {
    public HeadgearEditTestPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public HeadgearEditTestPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        TextView summaryTextView = ((TextView)view.findViewById(android.R.id.summary));
        TextView titleTextView = ((TextView)view.findViewById(android.R.id.title));
        String title = (String)getTitle();
        switch(title) {
            case "yellow":
                titleTextView.setText(getContext().getResources().getText(R.string.minor_shock_title));
                break;
            case "orange":
                titleTextView.setText(getContext().getResources().getText(R.string.medium_shock_title));
                break;
            case "red":
                titleTextView.setText(getContext().getResources().getText(R.string.important_shock_title));
                break;
            case "purple":
                titleTextView.setText(getContext().getResources().getText(R.string.severe_shock_title));
                break;
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
    }

    @Override
    public void setTitle(int titleResId) {
        super.setTitle(titleResId);
    }

    public HeadgearEditTestPreference(Context context) {
        super(context);
    }
}
