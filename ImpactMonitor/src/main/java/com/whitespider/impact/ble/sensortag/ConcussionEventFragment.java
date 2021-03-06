package com.whitespider.impact.ble.sensortag;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ConcussionEventFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_head_gear, container, false);
        final HeadGearActivity mDeviceActivity = (HeadGearActivity)getActivity();
        mDeviceActivity.createConcussionChart(rootView);
        return rootView;
    }
}

