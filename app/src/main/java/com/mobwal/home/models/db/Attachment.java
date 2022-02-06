package com.mobwal.home.models.db;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class Attachment implements Serializable {

    public Attachment() {
        id = UUID.randomUUID().toString();
        d_date = new Date();
        n_latitude = null;
        n_longitude = null;
        n_date = new Date().getTime();
        n_distance = null;
    }

    public String id;

    public String f_route;

    public String f_point;

    public String f_result;

    public String c_name;

    @Nullable
    public Double n_latitude;

    @Nullable
    public Double n_longitude;

    public Date d_date;

    public Long n_date;

    @Nullable
    public Double n_distance;
}
