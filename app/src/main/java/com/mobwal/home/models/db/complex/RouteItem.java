package com.mobwal.home.models.db.complex;

import android.content.Context;

import java.text.MessageFormat;
import java.util.Date;

import com.mobwal.home.R;

public class RouteItem {
    public String id;
    public String c_number;
    public int n_task;
    public int n_done;
    /**
     * задание имеет статус "Не подтверждено"
     */
    public int n_fail;
    public Date d_date;
    public boolean b_export;
    public String c_readme;
    public int n_anomaly;
    public boolean b_check;

    public String toUserString(Context context) {
        return MessageFormat.format(context.getString(R.string.route_item_subtitle) + ": {0} из {1}.", n_fail > 0 ? n_fail : n_task - n_done, n_task);
    }
}
