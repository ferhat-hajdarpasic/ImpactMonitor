package com.whitespider.impact.history;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.whitespider.impact.ble.sensortag.R;

public class HistoryListRowHolder extends RecyclerView.ViewHolder {
    protected ImageButton historyLevelImageButton;
    protected TextView historyDetailsTextView;

    public HistoryListRowHolder(View view) {
        super(view);
        this.historyLevelImageButton = (ImageButton) view.findViewById(R.id.historyLevelImageButton);
        this.historyDetailsTextView = (TextView) view.findViewById(R.id.historyDetailsTextView);
    }

}