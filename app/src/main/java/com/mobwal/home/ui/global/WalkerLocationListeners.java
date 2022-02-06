package com.mobwal.home.ui.global;

import android.location.LocationListener;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

public interface WalkerLocationListeners extends LocationListener {
    default void onLocationClick(View v) {}

    @Override
    default void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    default void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    default void onProviderDisabled(@NonNull String provider) {

    }
}
