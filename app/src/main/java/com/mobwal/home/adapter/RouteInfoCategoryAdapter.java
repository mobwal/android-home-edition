package com.mobwal.home.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import com.mobwal.home.R;
import com.mobwal.home.adapter.holder.RouteInfoCategoryHolder;
import com.mobwal.home.models.RouteInfo;

public class RouteInfoCategoryAdapter extends RecyclerView.Adapter<RouteInfoCategoryHolder> {
    private final Context mContext;
    private final RouteInfo[][] mItems;
    private final RecyclerView.RecycledViewPool mViewPool;

    public RouteInfoCategoryAdapter(Context context, @NotNull RouteInfo[][] items) {
        mContext = context;
        mItems = items;

        mViewPool = new RecyclerView.RecycledViewPool();
    }

    @NonNull
    @Override
    public RouteInfoCategoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.route_info_category, parent, false);
        RecyclerView rvRoutes = view.findViewById(R.id.route_info_items);
        rvRoutes.setLayoutManager(new LinearLayoutManager(mContext));
        rvRoutes.setRecycledViewPool(mViewPool);

        return new RouteInfoCategoryHolder(view, rvRoutes);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteInfoCategoryHolder holder, int position) {
        if(mItems[position] != null) {
            String title = "";
            switch (position) {
                case 0:
                    title = mContext.getString(R.string.history);
                    break;
                case 1:
                    title = mContext.getString(R.string.params);
                    break;
            }

            holder.setText(title);
            holder.setAdapter(new RouteInfoItemAdapter(mContext, mItems[position]));
        }
    }

    @Override
    public int getItemCount() {
        return mItems.length;
    }

    public void updateItem(RouteInfo[] item, int position) {
        mItems[0] = item;
        notifyItemChanged(position);
    }
}