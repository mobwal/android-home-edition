package com.mobwal.home.ui.global;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import androidx.core.app.ActivityCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mobwal.home.R;
import com.mobwal.home.WalkerApplication;
import com.mobwal.home.databinding.GoogleMapBottomSheetBinding;
import com.mobwal.home.models.LocationInfo;

/**
 * Вывод Google карты
 */
public class GoogleMapBottomDialogFragment extends BottomSheetDialogFragment
    implements OnMapReadyCallback {

    private final static String LOCATION_NAME = "location";

    private GoogleMapBottomSheetBinding binding;
    private GoogleMap mMap;
    private LocationInfo mLocationInfo;

    public void addLocation(LocationInfo locationInfo) {
        mLocationInfo = locationInfo;

        if(locationInfo.isValidLocations() && mMap != null) {
            addMarkerAndMove(locationInfo);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WalkerApplication.Log("Вывод всплывающей карты.");
        if(savedInstanceState != null) {
            mLocationInfo = (LocationInfo) savedInstanceState.getSerializable(LOCATION_NAME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = GoogleMapBottomSheetBinding.inflate(inflater, container, false);

        return binding.getRoot();

    }

    @Override
    public void onStart() {
        super.onStart();

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.bottom_google_map);
            if(mapFragment != null) {
                mapFragment.getMapAsync(this);
            }
        } else {
            WalkerApplication.Log("Доступ к геолокации не предоставлен.");
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(LOCATION_NAME, mLocationInfo);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        mMap = null;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        if(mLocationInfo != null && mLocationInfo.isValidLocations()) {
            addMarkerAndMove(mLocationInfo);
        }
    }

    /**
     * Добавление точек на карту
     * @param locationInfo геолокации
     */
    private void addMarkerAndMove(LocationInfo locationInfo) {
        if(locationInfo != null && mMap != null) {
            mMap.clear();

            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            LatLng pointMy = mLocationInfo.convertToLatLng(LocationInfo.MY);

            if(pointMy != null) {
                builder.include(pointMy);

                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(pointMy)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        .title(getString(R.string.my_coordinate)));

                if(marker != null) {
                    marker.showInfoWindow();
                }
            }

            LatLng pointTask = mLocationInfo.convertToLatLng(LocationInfo.TASK);
            if(pointTask != null) {
                builder.include(pointTask);

                mMap.addMarker(new MarkerOptions()
                        .position(pointTask)
                        .title(getString(R.string.task)));
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 300));
        }
    }
}