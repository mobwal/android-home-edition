package com.mobwal.home.models.db;

import android.location.Location;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class Result {
    public Result() {
        this(null);
    }

    public Result(String f_point) {
        id = UUID.randomUUID().toString();
        n_latitude = null;
        n_longitude = null;
        d_date = new Date();
        n_date = d_date.getTime();
        c_template = "DEFAULT";

        this.f_point = f_point;
    }

    public Result(String id, String f_route, String f_point, String c_template, @Nullable Location location, @Nullable Point point) {
        this.id = id;

        this.f_route = f_route;
        this.f_point = f_point;
        this.c_template = c_template;
        setLocation(location);
        setDistance(point, location);

        d_date = new Date();
        n_date = d_date.getTime();
    }

    public String id;

    public String f_route;

    public String f_point;

    @Nullable
    public Double n_latitude;

    @Nullable
    public Double n_longitude;

    @Nullable
    public String jb_data;

    public Date d_date;

    public long n_date;

    public String c_template;

    @Nullable
    public Double n_distance;

    @Nullable
    public LatLng convertToLatLng() {
        if(n_latitude == null || n_longitude == null) {
            return null;
        } else {
            return new LatLng(n_latitude, n_longitude);
        }
    }

    public void setLocation(@Nullable Location location) {
        if (location != null) {
            n_longitude = location.getLongitude();
            n_latitude = location.getLatitude();
        }
    }

    public void setDistance(@Nullable Point point, @Nullable Location position) {
        if(point != null && position != null && point.getLocation() != null) {
            n_distance = (double) point.getLocation().distanceTo(position);
        }
    }
}
