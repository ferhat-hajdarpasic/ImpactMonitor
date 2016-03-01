package com.whitespider.impact.history;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.whitespider.impact.ble.sensortag.R;

public class HistoryListRowHolder extends RecyclerView.ViewHolder {
    protected ImageButton historyLevelImageButton;
    protected TextView historyItemIntensityTextView;
    protected TextView historyItemTimeTextView;
    protected ImageButton imageButtonView3D;

    public HistoryListRowHolder(View view) {
        super(view);
        this.historyLevelImageButton = (ImageButton) view.findViewById(R.id.historyLevelImageButton);
        this.historyItemIntensityTextView = (TextView) view.findViewById(R.id.historyItemIntensityTextView);
        this.historyItemTimeTextView = (TextView)view.findViewById(R.id.historyItemTimeTextView);
        this.imageButtonView3D = (ImageButton)view.findViewById(R.id.imageButtonView3D);
    }

}