package com.whitespider.impact.history;

import android.content.Context;
import android.graphics.Bitmap;
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
        Bitmap image = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        int color = getItemColor(feedItem);
        Paint paint = new Paint();
        paint.setColor(color);
        canvas.drawRect(rect, paint);

        feedListRowHolder.thumbnail.setImageBitmap(image);
        feedListRowHolder.title.setText(Html.fromHtml("<b>" + String.format("%.2f", feedItem.getTotalAcceleration()) + "g</b> " +
                        "Direction vector [" +
                        String.format("%.2f", feedItem.getDirection().x) + ", " +
                        String.format("%.2f", feedItem.getDirection().y) + ", " +
                        String.format("%.2f", feedItem.getDirection().z) +
                        "]" +
                        "<p>Time " + feedItem.getTime() + "</p>"
        ));
        feedListRowHolder.title.setTag(feedItem);
    }

    public int getItemColor(HistoryItem feedItem) {
        switch(feedItem.getSeverity()) {
            case 1:
                return Color.YELLOW;
            case 2:
                return Color.rgb(255, 221,100);
            case 3:
                return Color.rgb(255, 162,100);
            case 4:
                return Color.RED;
            default:
                return Color.GRAY;
        }
    }

    @Override
    public int getItemCount() {
        return (null != feedItemList ? feedItemList.size() : 0);
    }
}
