package com.mobwal.home.ui.point;

import static android.content.Context.LOCATION_SERVICE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.android.material.snackbar.Snackbar;
import com.mobwal.home.DataManager;
import com.mobwal.home.R;
import com.mobwal.home.WalkerApplication;
import com.mobwal.home.adapter.PointInfoItemAdapter;
import com.mobwal.home.databinding.FragmentPointInfoBinding;
import com.mobwal.home.models.PointInfo;
import com.mobwal.home.models.db.Point;
import com.mobwal.home.models.db.Result;
import com.mobwal.home.ui.RecycleViewItemListeners;
import com.mobwal.home.utilits.ActivityUtil;

import org.osmdroid.views.overlay.Marker;

/**
 * информация по точке
 */
public class InfoFragment extends Fragment
        implements LocationListener, RecycleViewItemListeners {

    private final static String LOCATION = "location";

    private FragmentPointInfoBinding binding;
    private String f_point = null;
    private String f_result = null;
    private DataManager mDataManager;
    private PointInfoItemAdapter mPointInfoItemAdapter;
    private LocationManager mLocationManager;
    private final List<Marker> mMarkers;
    private Location mLocation;
    private MenuItem mDeleteMenuItem;

    @Nullable
    private Result[] mResults;

    private final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    @Nullable
    private final ActivityResultLauncher<String[]> mPermissionActivityResultLauncher;


    public InfoFragment() {
        // Required empty public constructor
        mMarkers = new ArrayList<>();

        mPermissionActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), this::onPermission);
    }

    public void onPermission(Map<String, Boolean> result) {
        boolean areAllGranted = true;
        for (Boolean b : result.values()) {
            areAllGranted = areAllGranted && b;
        }

        if (!areAllGranted) {
            String message = ActivityUtil.getMessageNotGranted(requireContext(), new String[] { requireContext().getString(R.string.location) });
            Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).setAction(requireContext().getString(R.string.more), view -> requireContext().startActivity(ActivityUtil.getIntentApplicationSetting(requireContext()))).show();
        } else {
            startUpdates();
        }
    }

    @SuppressLint("MissingPermission")
    private void startUpdates() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            WalkerApplication.Debug("Точки. Информация. Доступ к геолокации не предоставлен.");
            if(mPermissionActivityResultLauncher != null) {
                mPermissionActivityResultLauncher.launch(REQUIRED_PERMISSIONS);
            }

            //binding.pointInfoList.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        } else {
            enableCompass(requireContext(), binding.osmPointInfoListMap);
            mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 1000, 1, this);
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, this);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        WalkerApplication.Log("Точки. Информация.");
        mLocationManager = (LocationManager) requireContext().getSystemService(LOCATION_SERVICE);

        if(savedInstanceState != null) {
            mLocation = savedInstanceState.getParcelable(LOCATION);
        }

        mDataManager = new DataManager(requireContext());

        if (getArguments() != null) {
            f_point = getArguments().getString("f_point");
            f_result = getArguments().getString("f_result");

            mResults = mDataManager.getResults(f_point);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(LOCATION, mLocation);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentPointInfoBinding.inflate(inflater, container, false);
        binding.pointInfoList.setLayoutManager(new LinearLayoutManager(requireContext()));

        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.point_info_list_map);

        if (supportMapFragment != null) {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                supportMapFragment.getMapAsync(this);
            } else {
                WalkerApplication.Debug("Точки. Информация. Доступ к геолокации не предоставлен.");
                binding.pointInfoList.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                getChildFragmentManager().beginTransaction().remove(supportMapFragment).commit();
            }
        }

        return binding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();

        if(f_result == null && mResults != null && mResults.length > 0) {
            inflater.inflate(R.menu.clear_menu, menu);

            mDeleteMenuItem = menu.findItem(R.id.action_result_delete);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.action_result_delete) {

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle(R.string.attention);
            builder.setMessage(R.string.results_remove);

            builder.setCancelable(false);
            builder.setPositiveButton(R.string.yes, (dialog, which) -> {
                // ДА
                if(mResults != null) {
                    boolean b = true;
                    for (Result result : mResults) {
                        if (mDataManager.delResult(result.id)) {
                            List<PointInfo> items = mPointInfoItemAdapter.getData();
                            int i = 0;
                            for (PointInfo pointInfo : items) {
                                if (pointInfo.result != null && pointInfo.result.equals(result.id)) {
                                    mPointInfoItemAdapter.removeItem(i);
                                    break;
                                }
                                i++;
                            }
                        } else {
                            if (b) {
                                b = false;
                            }
                        }
                    }

                    if (!b) {
                        Toast.makeText(requireContext(), R.string.remove_result_error, Toast.LENGTH_SHORT).show();
                    }

                    updateResults();
                    updateLocations(mLocation);
                }
            });
            builder.setNegativeButton(R.string.no, null);

            AlertDialog alert = builder.create();
            alert.show();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onStart() {
        super.onStart();

        PointInfo[] items = mDataManager.getPointInfo(f_point);

        if (items == null || items.length == 0) {
            hiddenMap();
        } else {
            updateLocations(mLocation);
        }

        mPointInfoItemAdapter = new PointInfoItemAdapter(requireContext(), items, this);
        binding.pointInfoList.setAdapter(mPointInfoItemAdapter);

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            WalkerApplication.Debug("Точки. Информация. Доступ к геолокации не предоставлен.");
            return;
        }
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, this);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onStop() {
        super.onStop();

        mLocationManager.removeUpdates(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        updateLocations(mLocation);
    }

    /**
     * Скрыть карту
     */
    private void hiddenMap() {
        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.point_info_list_map);
        if(fragment != null) {
            getChildFragmentManager().beginTransaction().remove(fragment).commit();
        }
    }

    /**
     * Обновление меток на карте
     * @param location текущее местоположение
     */
    private void updateLocations(@Nullable Location location) {
        if(mMap == null) {
            return;
        }

        if(mMarkers.size() > 0) {
            for (Marker marker: mMarkers) {
                marker.remove();
            }
            mMarkers.clear();
        }

        Point pointItem = mDataManager.getPoint(f_point);
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        if(pointItem != null) {
            LatLng point = pointItem.convertToLatLng();
            if(point != null) {
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(point)
                        .title(pointItem.c_address));

                mMarkers.add(marker);
                builder.include(point);
            }
        }

        Result[] results = mDataManager.getResults(f_point);
        if(results != null && results.length > 0) {
            for (Result result: results) {
                LatLng point = result.convertToLatLng();

                if(point != null) {
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                            .position(point));

                    mMarkers.add(marker);
                    builder.include(point);
                }
            }
        }

        if(location != null) {
            LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(point)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .title(getString(R.string.my_coordinate)));

            if(marker != null) {
                marker.showInfoWindow();
            }

            mMarkers.add(marker);
            builder.include(point);
        }

        if(mMarkers.size() > 0) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
        }
    }

    /**
     * Обработчик нажатия на результат
     * @param id идентификатор результата
     */
    @Override
    public void onViewItemClick(String id) {
        Result result = mDataManager.getResult(id);
        if(result != null && mMap != null) {
            LatLng point = result.convertToLatLng();
            if(point != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 18.0f));
            }
        }
    }

    /**
     * Обработчик удаления
     * @param id позиция в списке
     */
    @Override
    public void onViewItemInfo(String id) {

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.attention);
        builder.setMessage(R.string.result_remove);

        final int position = Integer.parseInt(id);
        final PointInfo pointItem = mPointInfoItemAdapter.getData().get(position);

        builder.setCancelable(false);
        builder.setPositiveButton(R.string.yes, (dialog, which) -> {
            // ДА
            if (pointItem.result != null && mDataManager.delResult(pointItem.result)) {
                mPointInfoItemAdapter.removeItem(position);
                updateLocations(mLocation);
                updateResults();
            } else {
                Toast.makeText(requireContext(), R.string.remove_result_error, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.no, null);

        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        mLocation = location;
        updateLocations(mLocation);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }

    /**
     * Обновление массива с результатами
     */
    private void updateResults() {
        mResults = mDataManager.getResults(f_point);
        if(mDeleteMenuItem != null) {
            if (!(f_result != null && mResults != null && mResults.length > 0)) {
                mDeleteMenuItem.setVisible(false);
            }
        }
    }

    /**
     * Очистка меток на карте
     */
    private void clearMarkers() {
        if(mMarkers.size() > 0) {
            for (org.osmdroid.views.overlay.Marker marker: mMarkers) {
                marker.remove(binding.osmPointInfoListMap);
            }
            mMarkers.clear();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        mMap = null;
    }
}