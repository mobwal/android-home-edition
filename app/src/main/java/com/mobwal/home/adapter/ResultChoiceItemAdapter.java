package com.mobwal.home.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mobwal.home.R;
import com.mobwal.home.adapter.holder.ResultChoiceItemHolder;
import com.mobwal.home.models.db.complex.ResultTemplate;
import com.mobwal.home.ui.RecycleViewItemListeners;

public class ResultChoiceItemAdapter extends RecyclerView.Adapter<ResultChoiceItemHolder> {
    private final Context mContext;
    private final List<ResultTemplate> mResultTemplates;
    private final RecycleViewItemListeners mListeners;

    public ResultChoiceItemAdapter(Context context, RecycleViewItemListeners listeners, ResultTemplate[] items) {
        mContext = context;
        mListeners = listeners;
        mResultTemplates = new ArrayList<>();
        mResultTemplates.addAll(Arrays.asList(items));
    }

    @NonNull
    @Override
    public ResultChoiceItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.result_choice_item, parent, false);
        return new ResultChoiceItemHolder(view, mListeners);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultChoiceItemHolder holder, int position) {
        holder.bind(mResultTemplates.get(position));
    }

    @Override
    public int getItemCount() {
        return mResultTemplates.size();
    }
}
