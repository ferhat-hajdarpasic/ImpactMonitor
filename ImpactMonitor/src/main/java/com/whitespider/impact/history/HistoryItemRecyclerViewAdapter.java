package com.whitespider.impact.history;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;
import com.whitespider.impact.ble.sensortag.R;

import java.util.List;

public class HistoryItemRecyclerViewAdapter extends RecyclerView.Adapter<HistoryListRowHolder> {

    public static final String HEADGEAR_YELLOW = "#FDAD01";
    public static final String HEADGEAR_ORANGE = "#FD3201";
    public static final String HEADGEAR_PURPLE = "#CE00A8";
    public static final String HEADGEAR_RED = "#FD0001";
    public static final String HEADGEAR_GRAY = "#6C6C6C";
    private List<HistoryItem> feedItemList;

    private Context mContext;

    public HistoryItemRecyclerViewAdapter(Context context, List<HistoryItem> feedItemList) {
        this.feedItemList = feedItemList;
        this.mContext = context;
    }

    @Override
    public HistoryListRowHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_row, null);
        HistoryListRowHolder mh = new HistoryListRowHolder(v);

        return mh;
    }

    @Override
    public void onBindViewHolder(final HistoryListRowHolder feedListRowHolder, int i) {
        HistoryItem feedItem = feedItemList.get(i);
        Rect rect = new Rect(0, 0, 1, 1);
        String colorCode = getItemColorAsString(feedItem);
        int color = getItemColor(feedItem);

        //feedListRowHolder.historyLevelImageButton.setImageBitmap(image);
        final String html =
                "<b>" + String.format("%.2f", feedItem.getTotalAcceleration()) + " G</b>" +
                " <p>Recorded on " + feedItem.getTime() + "</p>";
        feedListRowHolder.historyDetailsTextView.setText(Html.fromHtml(html));
        feedListRowHolder.imageButtonView3D.setTag(feedItem);

        if(feedItem.getSeverity() >= 3) {
            feedListRowHolder.historyDetailsTextView.setTextColor(color);
            Bitmap errorImage = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.error);
            feedListRowHolder.historyLevelImageButton.setImageBitmap(errorImage);
        } else {
            feedListRowHolder.historyDetailsTextView.setTextColor(Color.parseColor(HEADGEAR_GRAY));
        }
    }

    public int getItemColor(HistoryItem feedItem) {
        return Color.parseColor(getItemColorAsString(feedItem));
    }

    public String getItemColorAsString(HistoryItem feedItem) {
        switch(feedItem.getSeverity()) {
            case 1:
                return HEADGEAR_YELLOW;
            case 2:
                return HEADGEAR_ORANGE;
            case 3:
                return HEADGEAR_RED;
            case 4:
                return HEADGEAR_PURPLE;
            default:
                return HEADGEAR_GRAY;
        }
    }

    @Override
    public int getItemCount() {
        return (null != feedItemList ? feedItemList.size() : 0);
    }
}
