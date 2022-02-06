package com.mobwal.home.adapter.holder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobwal.home.R;
import com.mobwal.home.models.RouteInfo;

public class RouteInfoItemHolder extends RecyclerView.ViewHolder {

    private final TextView mText;
    private final TextView mLabel;

    public RouteInfoItemHolder(@NonNull View itemView) {
        super(itemView);

        mLabel = itemView.findViewById(R.id.route_info_label);
        mText = itemView.findViewById(R.id.route_info_text);
    }

    public void bind(RouteInfo item) {
        mText.setText(item.text);
        mLabel.setText(item.label);
    }
}
