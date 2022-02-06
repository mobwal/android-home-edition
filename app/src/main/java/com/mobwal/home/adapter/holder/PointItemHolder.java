package com.mobwal.home.adapter.holder;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.mobwal.home.R;
import com.mobwal.home.models.db.complex.PointItem;
import com.mobwal.home.ui.RecycleViewItemListeners;
import com.mobwal.home.ui.RecycleViewItemRemovable;
import com.mobwal.home.utilits.ActivityUtil;

public class PointItemHolder extends RecyclerView.ViewHolder
    implements View.OnClickListener, RecycleViewItemRemovable {

    private final TextView mTitle;
    private final TextView mDescription;
    private final ImageButton mInfo;

    private final Context mContext;
    private PointItem mItem;
    private final RecycleViewItemListeners mListeners;

    public PointItemHolder(@NonNull View itemView, RecycleViewItemListeners listeners, boolean isReadOnly) {
        super(itemView);
        mListeners = listeners;
        mContext = itemView.getContext();

        mTitle = itemView.findViewById(R.id.point_item_title);
        mDescription = itemView.findViewById(R.id.point_item_description);
        mInfo = itemView.findViewById(R.id.point_item_info);
        mInfo.setVisibility(isReadOnly ? View.GONE : View.VISIBLE);
        mInfo.setOnClickListener(this);
        if(mListeners != null) {
            itemView.setOnClickListener(this);
        }
    }

    public void bind(PointItem item) {
        mItem = item;

        mTitle.setText(item.c_address);
        String desc = item.toUserString(mContext);
        mDescription.setVisibility(TextUtils.isEmpty(desc) ? View.GONE : View.VISIBLE);
        mDescription.setText(desc);

        mInfo.setImageDrawable(item.b_done
                ? AppCompatResources.getDrawable(mContext, R.drawable.ic_baseline_check_circle_24)
                : AppCompatResources.getDrawable(mContext, R.drawable.ic_baseline_info_24));

        if(item.b_check) {
            mTitle.setTextColor(ActivityUtil.getColor(mContext, android.R.attr.textColor));
        } else {
            mTitle.setTextColor(ActivityUtil.getColor(mContext, R.attr.colorError));
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.point_item_info) {
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
        return mItem.b_anomaly;
    }
}
