package com.mobwal.home.adapter.holder;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.mobwal.home.R;
import com.mobwal.home.models.db.complex.RouteItem;
import com.mobwal.home.ui.RecycleViewItemListeners;
import com.mobwal.home.ui.RecycleViewItemRemovable;

public class RouteItemHolder extends RecyclerView.ViewHolder
    implements View.OnClickListener, RecycleViewItemRemovable {

    private final TextView mTitle;
    private final TextView mDescription;
    private final ImageButton mInfo;

    private final Context mContext;
    private RouteItem mItem;
    private final RecycleViewItemListeners mListeners;

    public RouteItemHolder(@NonNull View itemView, RecycleViewItemListeners listeners) {
        super(itemView);

        mListeners = listeners;
        mContext = itemView.getContext();

        mTitle = itemView.findViewById(R.id.route_item_title);
        mDescription = itemView.findViewById(R.id.route_item_description);
        mInfo = itemView.findViewById(R.id.route_item_info);
        mInfo.setOnClickListener(this);

        itemView.setOnClickListener(this);
    }

    public void bind(RouteItem item) {
        mItem = item;

        mTitle.setText(item.c_number);
        mDescription.setText(item.toUserString(mContext));

        mInfo.setImageDrawable(item.b_export
                ? AppCompatResources.getDrawable(mContext, R.drawable.ic_baseline_check_circle_24)
                : AppCompatResources.getDrawable(mContext, R.drawable.ic_baseline_info_24));
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.route_item_info) {
            mListeners.onViewItemInfo(mItem.id);
        } else {
            mListeners.onViewItemClick(mItem.id);
        }
    }

    /**
     * разрешено удаление или нет
     * @return true - разрешено удаление
     */
    public boolean isRemovable() {
        return mItem.b_export;
    }
}
