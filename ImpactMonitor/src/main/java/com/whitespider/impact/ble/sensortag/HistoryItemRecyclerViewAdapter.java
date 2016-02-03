package com.whitespider.impact.ble.sensortag;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

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

        Picasso.with(mContext).load(feedItem.getThumbnail())
                .error(R.drawable.placeholder)
                .placeholder(R.drawable.placeholder)
                .into(feedListRowHolder.thumbnail);

        feedListRowHolder.title.setText(Html.fromHtml(feedItem.getTitle()));
    }

    @Override
    public int getItemCount() {
        return (null != feedItemList ? feedItemList.size() : 0);
    }
}
