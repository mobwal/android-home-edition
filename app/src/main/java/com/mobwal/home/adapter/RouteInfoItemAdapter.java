package com.mobwal.home.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import com.mobwal.home.R;
import com.mobwal.home.adapter.holder.RouteInfoItemHolder;
import com.mobwal.home.models.RouteInfo;

public class RouteInfoItemAdapter extends RecyclerView.Adapter<RouteInfoItemHolder> {
    private final Context mContext;

    @NotNull
    private final RouteInfo[] mList;

    public RouteInfoItemAdapter(Context context, @Nullable RouteInfo[] items) {
        mContext = context;
        if(items != null) {
            mList = items;
        } else {
            mList = new RouteInfo[0];
        }
    }

    @NonNull
    @Override
    public RouteInfoItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.route_info_item, parent, false);
        return new RouteInfoItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteInfoItemHolder holder, int position) {
        holder.bind(mList[position]);
    }

    @Override
    public int getItemCount() {
        return mList.length;
    }
}
