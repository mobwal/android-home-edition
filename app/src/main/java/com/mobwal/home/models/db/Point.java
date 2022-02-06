package com.mobwal.home.models.db;

import android.location.Location;
import android.location.LocationManager;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import java.util.UUID;

public class Point {
    public Point() {
        id = UUID.randomUUID().toString();
        n_latitude = null;
        n_longitude = null;
        b_anomaly = false;
        n_order = 0;
        jb_data = null;
        b_check = true;
    }

    public String id;

    public String c_address;

    @Nullable
    public Double n_latitude;

    @Nullable
    public Double n_longitude;

    public String c_description;

    public boolean b_anomaly;

    public String f_route;

    public int n_order;

    public String c_imp_id;

    @Nullable
    public String jb_data;

    public boolean b_check;

    public String c_comment;

    @Nullable
    public LatLng convertToLatLng() {
        if(n_latitude == null || n_longitude == null) {
            return null;
        } else {
            return new LatLng(n_latitude, n_longitude);
        }
    }

    @Nullable
    public Location getLocation() {
        if(n_latitude == null || n_longitude == null) {
            return null;
        } else {
            Location location = new Location(LocationManager.PASSIVE_PROVIDER);
            location.setLatitude(n_latitude);
            location.setLongitude(n_longitude);
            return location;
        }
    }
}
