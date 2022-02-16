package com.mobwal.home.ui;

import static android.content.Context.LOCATION_SERVICE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.Snackbar;

import java.util.Map;

import com.mobwal.home.R;
import com.mobwal.home.WalkerApplication;
import com.mobwal.home.ui.global.HorizontalProgressLayout;
import com.mobwal.home.ui.global.WalkerLocationListeners;
import com.mobwal.home.utilits.ActivityUtil;

/**
 * Отображение геолокации
 */
public class GeoLocationLayout extends LinearLayout
        implements WalkerLocationListeners {
    private final ImageView mIcon;
    private final TextView mCoordinate;
    private final ImageButton mMapIcon;
    private final HorizontalProgressLayout mProgressLayout;

    private final LocationManager mLocationManager;
    public static String LEVEL = "LOW";
    private Location mLocation;
    private WalkerLocationListeners mListeners;
    private final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    @Nullable
    private ActivityResultLauncher<String[]> mPermissionActivityResultLauncher;

    /**
     * Обработчик событий для внешних компонентов
     * @param listeners обработчик
     */
    public void setOnLocationListeners(WalkerLocationListeners listeners) {
        mListeners = listeners;
    }

    /**
     * Уровень сигнала
     * @param level сигнал LOW|HIGH
     */
    public void setLevel(String level) {
        LEVEL = level;
    }

    public GeoLocationLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.geolocation_layout, this, true);

        mProgressLayout = findViewById(R.id.location_progress);
        mIcon = findViewById(R.id.location_icon);
        mCoordinate = findViewById(R.id.location_coordinate);
        mMapIcon = findViewById(R.id.location_map);
        mMapIcon.setOnClickListener(v -> {
            if(mListeners != null) {
                mListeners.onLocationClick(v);
            }
        });

        mLocationManager = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);
    }

    public void setActivityResultLauncherPermission(ActivityResultLauncher<String[]> activityResultLauncher) {
        mPermissionActivityResultLauncher = activityResultLauncher;
    }

    public void onPermission(Map<String, Boolean> result) {
        boolean areAllGranted = true;
        for (Boolean b : result.values()) {
            areAllGranted = areAllGranted && b;
        }

        if (!areAllGranted) {
            permissionMissing();
        } else {
            startUpdates();
        }
    }

    @SuppressLint("MissingPermission")
    private void startUpdates() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            setLocationIcon(R.drawable.ic_baseline_wrong_location_24);
            if(mPermissionActivityResultLauncher != null) {
                mPermissionActivityResultLauncher.launch(REQUIRED_PERMISSIONS);
            }
            WalkerApplication.Debug("Доступ к геолокации непредоставлен.");
        } else {
            if(mLocation == null) {
                showProgress(true);
                setLocationIcon(mLocation == null ? R.drawable.ic_baseline_location_off_24 : R.drawable.ic_baseline_location_on_24);
                showMapIcon(mLocation != null);
            } else {
                locationChanged(mLocation);
            }

            if(LEVEL.equalsIgnoreCase("high")) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
            }
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, this);
            mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 1000, 1, this);
        }
    }

    private void setLocationIcon(int resID) {
        mIcon.setImageResource(resID);
    }

    /**
     * Запустить механизм получения координат
     * @param location геолокация
     */
    public void onStart(@Nullable Location location) {
        mLocation = location;

        startUpdates();
    }

    /**
     * Остановить механизм получения координат
     */
    @SuppressLint("MissingPermission")
    public void onStop() {
        mLocationManager.removeUpdates(this);
    }

    /**
     * Вывести кнопку для перехода просмотра карты
     * @param value значение
     */
    private void showMapIcon(boolean value) {
        mMapIcon.setVisibility(value ? View.VISIBLE : View.GONE);
    }

    private void permissionMissing() {
        showProgress(false);

        String message = ActivityUtil.getMessageNotGranted(getContext(), new String[] { getContext().getString(R.string.location) });
        Snackbar.make(getRootView(), message, Snackbar.LENGTH_LONG).setAction(getContext().getString(R.string.more), view -> getContext().startActivity(ActivityUtil.getIntentApplicationSetting(getContext()))).show();
        mCoordinate.setText(getContext().getString(R.string.permission_location));
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        locationChanged(location);

        if(mListeners != null) {
            mListeners.onLocationChanged(mLocation);
        }
    }

    private void locationChanged(@NonNull Location location) {
        showProgress(false);

        mLocation = location;
        setLocationIcon(R.drawable.ic_baseline_location_on_24);
        showMapIcon(true);

        mCoordinate.setText(ActivityUtil.toUserString(location));
    }

    private void showProgress(boolean visible) {
        mProgressLayout.setVisible(visible);
        mCoordinate.setVisibility(visible ? GONE : VISIBLE);
    }
}