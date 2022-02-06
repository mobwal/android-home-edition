package com.mobwal.home.ui.route;

import static android.content.Context.LOCATION_SERVICE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
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
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;

import com.mobwal.home.DataManager;
import com.mobwal.home.DemoManager;
import com.mobwal.home.Names;
import com.mobwal.home.R;
import com.mobwal.home.WalkerApplication;
import com.mobwal.home.adapter.RouteItemAdapter;
import com.mobwal.home.databinding.FragmentRouteBinding;
import com.mobwal.home.models.DemoPlaceItem;
import com.mobwal.home.models.db.complex.RouteItem;
import com.mobwal.home.ui.BaseFragment;
import com.mobwal.home.ui.RecycleViewItemListeners;
import com.mobwal.home.ui.RecycleViewItemRemovable;
import com.mobwal.home.utilits.ActivityUtil;
import com.mobwal.home.utilits.ImportUtil;
import com.mobwal.home.utilits.NetworkInfoUtil;
import com.mobwal.home.utilits.NewThread;

public class RouteFragment extends BaseFragment
        implements SearchView.OnQueryTextListener,
        RecycleViewItemListeners, LocationListener {

    private static final String QUERY_NAME = "query";
    private String mQuery;

    private FragmentRouteBinding binding;
    private RouteItemAdapter mRouteItemAdapter;
    private DataManager mDataManager;
    private SearchView mSearchView;

    private NewThread mReadThread;
    private URL mUrl;

    private SharedPreferences mSharedPreferences;
    private LocationManager mLocationManager;

    private final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private final ActivityResultLauncher<String[]> mPermissionActivityResultLauncher;

    public RouteFragment() {
        mPermissionActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            boolean areAllGranted = true;
            for (Boolean b : result.values()) {
                areAllGranted = areAllGranted && b;
            }

            if (!areAllGranted) {
                String message = ActivityUtil.getMessageNotGranted(requireContext(), new String[] { requireContext().getString(R.string.location) });
                Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).setAction(requireContext().getString(R.string.more), view -> requireContext().startActivity(ActivityUtil.getIntentApplicationSetting(requireContext()))).show();
            } else {
                // разрешения предоставлены
                startLocation();
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void startLocation() {
        if(NetworkInfoUtil.isNetworkAvailable(requireContext())) {
            progressVisible(true);
            showActionBar(false);

            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, this);
            mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 1000, 1, this);
        } else {
            Toast.makeText(requireContext(), getString(R.string.network_not_found), Toast.LENGTH_SHORT).show();
            WalkerApplication.Log("Для создания демонстрационного маршрута сеть не найдена.");
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        mReadThread = new NewThread(requireActivity()) {

            private DemoPlaceItem[] mPlaces;

            @Override
            public void onBackgroundExecute() {
                try {
                    mPlaces = DemoManager.getObjects(mUrl);
                } catch (IOException e) {
                    WalkerApplication.Log("Ошибка создания демо маршрута в базе данных", e);
                }
            }

            @Override
            public void onPostExecute() {

                String result = ImportUtil.generateRouteFormDemo(requireContext(), mPlaces, requireContext().getString(R.string.demo_route));

                showActionBar(true);
                progressVisible(false);

                if(result == null) {
                    mSharedPreferences.edit().putBoolean("demo", true).apply();
                    updateRoutes();
                } else {
                    Toast.makeText(requireContext(), result, Toast.LENGTH_SHORT).show();
                    WalkerApplication.Log(result);
                }
            }
        };
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mSharedPreferences = requireActivity().getSharedPreferences(Names.PREFERENCE_NAME, Context.MODE_PRIVATE);
        mLocationManager = (LocationManager)requireContext().getSystemService(LOCATION_SERVICE);
        WalkerApplication.Log("Маршруты.");
        if(savedInstanceState != null) {
            mQuery = savedInstanceState.getString(QUERY_NAME);
        }

        mDataManager = new DataManager(requireContext());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(QUERY_NAME, mQuery);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) searchItem.getActionView();
        if(!TextUtils.isEmpty(mQuery)) {
            final String searchText = mQuery;

            mSearchView.post(() -> {
                mSearchView.onActionViewExpanded();
                mSearchView.setQuery(searchText, false);
            });
        }

        mSearchView.setOnQueryTextListener(this);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @SuppressLint("MissingPermission")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentRouteBinding.inflate(inflater, container, false);
        binding.routeList.setLayoutManager(new LinearLayoutManager(requireContext()));

        binding.createDemoRouteUndo.setOnClickListener(v -> {
            if(mReadThread != null) {
                mReadThread.destroy();
            }
            showActionBar(true);
            mLocationManager.removeUpdates(RouteFragment.this);
            progressVisible(false);
        });

        // https://stackoverflow.com/questions/33985719/android-swipe-to-delete-recyclerview
        new ItemTouchHelper(new SwipeToDeleteCallback(requireContext(), 0, ItemTouchHelper.LEFT) {

            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                // запрещаем свайп если задание не завершено
                if(viewHolder instanceof RecycleViewItemRemovable) {
                    RecycleViewItemRemovable holder = (RecycleViewItemRemovable) viewHolder;
                    if(holder.isRemovable()) {
                        return super.getSwipeDirs(recyclerView, viewHolder);
                    }
                }

                return 0;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle(R.string.attention);
                builder.setMessage(R.string.route_remove);

                final int position = viewHolder.getBindingAdapterPosition();
                final RouteItem routeItem = mRouteItemAdapter.getData().get(position);

                builder.setCancelable(false);
                builder.setPositiveButton(R.string.yes, (dialog, which) -> {
                    // ДА
                    if(mDataManager.delRoute(routeItem.id)) {
                        mRouteItemAdapter.removeItem(position);

                        setEmptyText(mRouteItemAdapter.getData().toArray(new RouteItem[0]), !TextUtils.isEmpty(mQuery));
                    } else {
                        Toast.makeText(requireContext(), R.string.remove_route_error, Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton(R.string.no, (dialog, which) -> {
                    // НЕТ
                    mRouteItemAdapter.removeItem(position);
                    mRouteItemAdapter.restoreItem(routeItem, position);
                    binding.routeList.scrollToPosition(position);
                });

                AlertDialog alert = builder.create();
                alert.show();
            }

            @Override
            public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
                return Names.SWIPE_THRESHOLD;
            }
        }).attachToRecyclerView(binding.routeList);

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        boolean isDemo = mSharedPreferences.getBoolean("demo", false);

        updateRoutes();

        if(!isDemo && mRouteItemAdapter.getData().size() == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle(R.string.attention);
            builder.setMessage(getString(R.string.demo_route_create) + " " + getString(R.string.demo_route_question));

            builder.setCancelable(false);
            builder.setPositiveButton(R.string.yes, (dialog, which) -> {
                if (!isLocationGranted()) {
                    mPermissionActivityResultLauncher.launch(REQUIRED_PERMISSIONS);
                } else {
                    startLocation();
                }
            });
            builder.setNegativeButton(R.string.no, (dialog, which) -> mSharedPreferences.edit().putBoolean("demo", true).apply());

            AlertDialog alert = builder.create();
            alert.show();
        }
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onStop() {
        super.onStop();
        // отключаем геолокацию
        if (isLocationGranted()) {
            mLocationManager.removeUpdates(this);
        }
    }

    /**
     * Разрешение для геолокации предоставлены или нет
     * @return разрешения предоставлены
     */
    private boolean isLocationGranted() {
        return ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void updateRoutes() {
        RouteItem[] items = mDataManager.getRoutes(null);
        setEmptyText(items, false);

        bindAdapter(items);
    }

    private void setEmptyText(@Nullable RouteItem[] items, boolean isSearch) {
        binding.routeList.setVisibility(items == null || items.length == 0 ? View.GONE : View.VISIBLE);
        binding.routeListEmpty.setVisibility(items == null || items.length == 0 ? View.VISIBLE : View.GONE);

        if(isSearch) {
            binding.routeListEmpty.setText(R.string.search_not_result);
        } else {
            String html = getString(R.string.route_list_empty) + "<p>" + MessageFormat.format(getString(R.string.create_route_docs), "<a href=\""+Names.ROUTE_DOCS+"\">" + Names.HOME_PAGE + "</a>") + "</p>";
            binding.routeListEmpty.setText(HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY));
            binding.routeListEmpty.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(mReadThread != null) {
            mReadThread.destroy();
        }
        binding = null;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mQuery = newText;
        RouteItem[] items = mDataManager.getRoutes(mQuery);
        setEmptyText(items, true);
        bindAdapter(items);

        return false;
    }

    @Override
    public void onViewItemInfo(String id) {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
        Bundle bundle = new Bundle();
        bundle.putString("f_route", id);
        navController.navigate(R.id.nav_route_info, bundle);
    }

    @Override
    public void onViewItemClick(String id) {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
        Bundle bundle = new Bundle();
        bundle.putString("f_route", id);
        navController.navigate(R.id.nav_point, bundle);
    }

    private void bindAdapter(@Nullable RouteItem[] items) {
        mRouteItemAdapter = new RouteItemAdapter(requireContext(), this, items);
        binding.routeList.setAdapter(mRouteItemAdapter);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onLocationChanged(@NonNull Location location) {
        if(!mReadThread.isThreading()) {
            mLocationManager.removeUpdates(this);
            String lang = Locale.getDefault().getLanguage();
            int radius = 3000;

            try {
                //double lat = 56.140252;
                //double lng = 47.231596;

                double lat = location.getLatitude();
                double lng = location.getLongitude();

                mUrl = DemoManager.getUrl(lang, radius, lat, lng);
                mReadThread.run();
            } catch (MalformedURLException e) {
                WalkerApplication.Log("Ошибка создания демо маршрута в базе данных", e);
            }
        }
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    private void progressVisible(boolean visible) {
        binding.createDemoRoute.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
}