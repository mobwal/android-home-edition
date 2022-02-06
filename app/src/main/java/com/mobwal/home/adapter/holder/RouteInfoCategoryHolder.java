package com.mobwal.home.adapter.holder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobwal.home.R;

public class RouteInfoCategoryHolder extends RecyclerView.ViewHolder {
    private final TextView mTitle;
    private final RecyclerView mRoutes;

    public RouteInfoCategoryHolder(@NonNull View itemView, RecyclerView innerRv) {
        super(itemView);

        mTitle = itemView.findViewById(R.id.route_info_title);
        mRoutes = innerRv;
    }

    public void setText(String title) {
        mTitle.setText(title);
    }

    public void setAdapter(RecyclerView.Adapter<RouteInfoItemHolder> adapter) {
        mRoutes.setAdapter(adapter);
    }
}