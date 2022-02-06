package com.mobwal.home.adapter.holder;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.MessageFormat;

import com.mobwal.home.R;
import com.mobwal.home.models.db.complex.ResultTemplate;
import com.mobwal.home.ui.RecycleViewItemListeners;

public class ResultChoiceItemHolder extends RecyclerView.ViewHolder
    implements View.OnClickListener {

    private final TextView mTitle;
    private final ImageButton mCheck;

    private ResultTemplate mItem;
    private final RecycleViewItemListeners mListeners;

    public ResultChoiceItemHolder(@NonNull View itemView, RecycleViewItemListeners listeners) {
        super(itemView);

        mListeners = listeners;
        mTitle = itemView.findViewById(R.id.result_choice_item_title);
        mCheck = itemView.findViewById(R.id.result_choice_item_check);

        itemView.setOnClickListener(this);
    }

    public void bind(ResultTemplate item) {
        mItem = item;

        mTitle.setText(item.c_template);
        mCheck.setVisibility(item.isExistsResult() ? View.VISIBLE: View.GONE);
    }

    @Override
    public void onClick(View v) {
        if(mListeners != null) {
            mListeners.onViewItemClick(MessageFormat.format("{0}|{1}", mItem.f_result, mItem.c_const));
        }
    }
}
