package com.mobwal.home.models;

import android.location.Location;
import android.location.LocationManager;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import org.osmdroid.util.GeoPoint;

import java.io.Serializable;

public class LocationInfo
        implements Serializable {

    public LocationInfo() {

    }

    public LocationInfo(@Nullable Double n_longitude, @Nullable Double n_latitude) {
        myLatitude = n_latitude;
        myLongitude = n_longitude;
    }

    public LocationInfo(@Nullable Location location) {
        if(location != null) {
            myLongitude = location.getLongitude();
            myLatitude = location.getLatitude();
        }
    }

    public final static String TASK = "TASK";
    public final static String MY = "MY";

    @Nullable
    public Double myLongitude;

    @Nullable
    public Double myLatitude;

    @Nullable
    public Double taskLongitude;

    @Nullable
    public Double taskLatitude;

    @Nullable
    public Double getDistance() {
        if(myLongitude != null && myLatitude != null && taskLongitude != null && taskLatitude != null) {
            Location attachLocation = new Location(LocationManager.PASSIVE_PROVIDER);
            attachLocation.setLongitude(myLongitude);
            attachLocation.setLatitude(myLatitude);

            Location myLocation = new Location(LocationManager.PASSIVE_PROVIDER);
            myLocation.setLongitude(taskLongitude);
            myLocation.setLatitude(taskLatitude);

            return Double.valueOf(String.valueOf(myLocation.distanceTo(attachLocation)));
        }

        return null;
    }

    /**
     * MY | TASK
     * @param name наименование координаты
     * @return преобразование
     */
    @Nullable
    public GeoPoint convertToLatLng(String name) {
        if(TextUtils.isEmpty(name)) {
            return null;
        }

        if(name.equals(TASK)) {
            if(taskLongitude != null && taskLatitude != null) {
                return new GeoPoint(taskLatitude, taskLongitude);
            }
        }

        if(name.equals(MY)) {
            if(myLongitude != null && myLatitude != null) {
                return new GeoPoint(myLatitude, myLongitude);
            }
        }

        return null;
    }

    public boolean isValidLocations() {
        return myLongitude != null || myLatitude != null || taskLongitude != null || taskLatitude != null;
    }
}
