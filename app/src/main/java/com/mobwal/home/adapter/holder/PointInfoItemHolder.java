package com.mobwal.home.adapter.holder;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobwal.home.R;
import com.mobwal.home.models.PointInfo;
import com.mobwal.home.ui.RecycleViewItemListeners;
import com.mobwal.home.ui.RecycleViewItemRemovable;

public class PointInfoItemHolder extends RecyclerView.ViewHolder
        implements RecycleViewItemRemovable {

    private final TextView mText;
    private final TextView mLabel;
    private final ImageButton mDelete;

    private PointInfo mPointInfo;
    private final RecycleViewItemListeners mListeners;

    public PointInfoItemHolder(@NonNull View itemView, RecycleViewItemListeners listeners) {
        super(itemView);

        mListeners = listeners;

        mLabel = itemView.findViewById(R.id.point_info_label);
        mText = itemView.findViewById(R.id.point_info_text);
        mDelete = itemView.findViewById(R.id.point_info_delete);

        mDelete.setOnClickListener(v -> {
            if(mListeners != null && mPointInfo.isResult()) {
                int position = getBindingAdapterPosition();
                mListeners.onViewItemInfo(String.valueOf(position));
            }
        });

        itemView.setOnClickListener(v -> {
            if(mListeners != null && mPointInfo.isResult()) {
                mListeners.onViewItemClick(mPointInfo.result);
            }
        });
    }

    public void bind(PointInfo item) {
        mPointInfo = item;

        mText.setText(item.text);
        mLabel.setText(item.label);
        mDelete.setVisibility(mPointInfo.isResult() ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean isRemovable() {
        return mPointInfo.isResult();
    }
}
