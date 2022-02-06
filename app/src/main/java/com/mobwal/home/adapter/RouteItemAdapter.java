package com.mobwal.home.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mobwal.home.R;
import com.mobwal.home.adapter.holder.RouteItemHolder;
import com.mobwal.home.models.db.complex.RouteItem;
import com.mobwal.home.ui.RecycleViewItemListeners;

public class RouteItemAdapter extends RecyclerView.Adapter<RouteItemHolder> {
    private final Context mContext;
    private final List<RouteItem> mRouteItemList;
    private final RecycleViewItemListeners mListeners;

    public RouteItemAdapter(Context context, RecycleViewItemListeners listeners, @Nullable RouteItem[] items) {
        mContext = context;
        mListeners = listeners;
        mRouteItemList = new ArrayList<>();
        if(items != null) {
            mRouteItemList.addAll(Arrays.asList(items));
        }
    }

    @NonNull
    @Override
    public RouteItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.route_item, parent, false);
        return new RouteItemHolder(view, mListeners);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteItemHolder holder, int position) {
        holder.bind(mRouteItemList.get(position));
    }

    @Override
    public int getItemCount() {
        return mRouteItemList.size();
    }

    public void removeItem(int position) {
        mRouteItemList.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(RouteItem item, int position) {
        mRouteItemList.add(position, item);
        notifyItemInserted(position);
    }

    public List<RouteItem> getData() {
        return mRouteItemList;
    }
}
