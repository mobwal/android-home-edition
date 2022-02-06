package com.mobwal.home.ui;

import android.content.Context;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.mobwal.home.DataManager;
import com.mobwal.home.R;
import com.mobwal.home.models.db.Point;
import com.mobwal.home.utilits.ActivityUtil;

/**
 * Модуль проверки результата
 */
public class CheckLayout extends LinearLayout {

    /**
     * заголовок
     */
    private final TextView mLabel;

    /**
     * Комментарий
     */
    private final EditText mComment;

    /**
     * Признак правильности выполнения
     */
    private final SwitchCompat mCheck;

    public CheckLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.check_layout, this, true);

        mCheck = findViewById(R.id.check_value);
        mCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            onCheckedChange(isChecked);
        });

        mComment = findViewById(R.id.check_comment);
        mLabel = findViewById(R.id.check_label);
    }

    private void onCheckedChange(boolean isChecked) {
        if(isChecked) {
            mLabel.setText(R.string.check);
            mLabel.setTextColor(ActivityUtil.getColor(getContext(), android.R.attr.textColorSecondary));
        } else {
            mLabel.setText(R.string.uncheck);
            mLabel.setTextColor(ActivityUtil.getColor(getContext(), R.attr.colorError));
        }
    }

    /**
     * Привязка данных
     * @param point точка маршрута
     */
    public void bind(@Nullable Point point) {
        if(point != null) {
            onCheckedChange(point.b_check);

            mComment.setText(point.c_comment);
            mCheck.setChecked(point.b_check);
        }
    }

    private String getComment() {
        return mComment.getText().toString();
    }

    private boolean isChecked() {
        return mCheck.isChecked();
    }

    /**
     * Сохранение данных в СУБД
     * @param f_point точка маршрута
     * @return статус сохранения
     */
    public boolean saveData(String f_point) {
        DataManager dataManager = new DataManager(getContext());
        return dataManager.updatePoint(f_point, isChecked(), getComment());
    }
}