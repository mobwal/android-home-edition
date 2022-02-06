package com.mobwal.home.models.db;

import android.content.Context;

import androidx.annotation.Nullable;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import com.mobwal.home.R;
import com.mobwal.home.utilits.DateUtil;

public class Route {
    public Route() {
        id = UUID.randomUUID().toString();
        d_date = new Date();
        b_export = false;
        d_export = null;
        n_date = d_date.getTime();
        b_check = false;
    }

    public String id;

    public String c_name;

    @Nullable
    public String c_catalog;

    @Nullable
    public String c_readme;

    @Nullable
    public Date d_date;

    public boolean b_export;

    @Nullable
    public Date d_export;

    public long n_date;

    public boolean b_check;

    public String toExportTitle(Context context) {
        return MessageFormat.format(context.getString(R.string.export_route_title), c_name, d_date != null ? DateUtil.toDateTimeString(d_date) : "");
    }
}
