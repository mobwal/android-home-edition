package com.mobwal.home.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mobwal.home.R;
import com.mobwal.home.adapter.holder.PointInfoItemHolder;
import com.mobwal.home.models.PointInfo;
import com.mobwal.home.ui.RecycleViewItemListeners;

public class PointInfoItemAdapter extends RecyclerView.Adapter<PointInfoItemHolder> {
    private final Context mContext;
    private final List<PointInfo> mList;
    private final RecycleViewItemListeners mListeners;

    public PointInfoItemAdapter(Context context, PointInfo[] items, RecycleViewItemListeners listeners) {
        mContext = context;
        mListeners = listeners;
        mList = new ArrayList<>();
        mList.addAll(Arrays.asList(items));
    }

    @NonNull
    @Override
    public PointInfoItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.point_info_item, parent, false);
        return new PointInfoItemHolder(view, mListeners);
    }

    @Override
    public void onBindViewHolder(@NonNull PointInfoItemHolder holder, int position) {
        holder.bind(mList.get(position));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void removeItem(int position) {
        mList.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(PointInfo item, int position) {
        mList.add(position, item);
        notifyItemInserted(position);
    }

    public List<PointInfo> getData() {
        return mList;
    }
}
