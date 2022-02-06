package com.mobwal.home.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mobwal.home.R;
import com.mobwal.home.adapter.holder.PointItemHolder;
import com.mobwal.home.models.db.complex.PointItem;
import com.mobwal.home.ui.RecycleViewItemListeners;

public class PointItemAdapter extends RecyclerView.Adapter<PointItemHolder> {
    private final Context mContext;
    private final List<PointItem> mPointItemList;
    private final RecycleViewItemListeners mListeners;
    private final Boolean mIsReadOnly;

    public PointItemAdapter(Context context, @Nullable PointItem[] items, @Nullable RecycleViewItemListeners listeners) {
        this(context, items, listeners, false);
    }

    public PointItemAdapter(Context context, @Nullable PointItem[] items, @Nullable RecycleViewItemListeners listeners, boolean isReadOnly) {
        mContext = context;
        mListeners = listeners;
        mPointItemList = new ArrayList<>();
        if(items != null) {
            mPointItemList.addAll(Arrays.asList(items));
        }
        mIsReadOnly = isReadOnly;
    }

    @NonNull
    @Override
    public PointItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.point_item, parent, false);
        return new PointItemHolder(view, mListeners, mIsReadOnly);
    }

    @Override
    public void onBindViewHolder(@NonNull PointItemHolder holder, int position) {
        holder.bind(mPointItemList.get(position));
    }

    @Override
    public int getItemCount() {
        return mPointItemList.size();
    }

    public void removeItem(int position) {
        mPointItemList.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(PointItem item, int position) {
        mPointItemList.add(position, item);
        notifyItemInserted(position);
    }

    public List<PointItem> getData() {
        return mPointItemList;
    }
}
