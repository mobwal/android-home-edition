package com.mobwal.home.models;

import android.content.Context;
import android.text.TextUtils;

import com.mobwal.home.R;

/**
 * Информация по маршруту
 */
public class RouteInfo {
    private final Context mContext;

    public RouteInfo(Context context, String label, String text) {
        mContext = context;
        this.label = label;
        this.text = translate(text);
    }

    public String label;
    public String text;

    private String translate(String text) {
        if(TextUtils.isEmpty(text)) {
            return text;
        }

        switch (text.toLowerCase()) {
            case "true":
                return mContext.getString(R.string.yes);
            case "false":
                return mContext.getString(R.string.no);

            case "low":
                return mContext.getString(R.string.low);

            case "high":
                return mContext.getString(R.string.high);

            default:
                return text;
        }
    }
}
