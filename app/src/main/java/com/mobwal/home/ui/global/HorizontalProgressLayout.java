package com.mobwal.home.ui.global;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.MessageFormat;

import com.mobwal.home.R;

/**
 * Горизонтальный progressbar
 */
public class HorizontalProgressLayout extends LinearLayout {

    private final TextView mTitle;
    private final ProgressBar mProgressBar;
    private final TextView mDescription;

    public HorizontalProgressLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        TypedArray a = context.obtainStyledAttributes(attributeSet, R.styleable.HorizontalProgressLayout, 0, 0);
        String title = a.getString(R.styleable.HorizontalProgressLayout_title);
        String description = a.getString(R.styleable.HorizontalProgressLayout_description);
        boolean indeterminate = a.getBoolean(R.styleable.HorizontalProgressLayout_indeterminate, true);
        a.recycle();

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.horizontal_progress_layout, this, true);

        mTitle = findViewById(R.id.horizontal_progress_title);
        mTitle.setText(title);

        mDescription = findViewById(R.id.horizontal_progress_description);
        mDescription.setText(description);

        mDescription.setVisibility(TextUtils.isEmpty(description) ? GONE : VISIBLE);

        mProgressBar = findViewById(R.id.horizontal_progress);
        mProgressBar.setIndeterminate(indeterminate);
    }

    /**
     * Установить процент выполнения
     * @param percent процент выполнения
     */
    public void setProgress(int percent) {
        mProgressBar.setIndeterminate(false);
        mDescription.setText(MessageFormat.format("{0}%", percent));
        mDescription.setVisibility(VISIBLE);
        mProgressBar.setProgress(percent);
    }

    /**
     * Установить заголовок
     * @param title заголовок
     */
    public void setTitle(@NonNull String title) {
        mTitle.setText(title);
    }

    /**
     * Бесконечность
     */
    public void setIndeterminate(@Nullable String title) {
        mProgressBar.setIndeterminate(true);
        clear();

        if(title != null) {
            setTitle(title);
        }
    }

    /**
     * Очистка данных
     */
    public void clear() {
        mTitle.setText("");
        mDescription.setText("");
        mDescription.setVisibility(GONE);
        mProgressBar.setProgress(0);
    }

    /**
     * Установка видимости
     * @param visible видимость
     */
    public void setVisible(boolean visible) {
        this.setVisibility(visible ? View.VISIBLE: View.GONE);
    }
}