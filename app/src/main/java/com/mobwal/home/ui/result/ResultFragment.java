package com.mobwal.home.ui.result;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.UUID;

import com.mobwal.home.DataManager;
import com.mobwal.home.R;
import com.mobwal.home.WalkerApplication;
import com.mobwal.home.databinding.FragmentResultBinding;
import com.mobwal.home.models.LocationInfo;
import com.mobwal.home.models.PointBundle;
import com.mobwal.home.models.SettingRoute;
import com.mobwal.home.models.db.Attachment;
import com.mobwal.home.models.db.Point;
import com.mobwal.home.models.db.Result;
import com.mobwal.home.models.db.Route;
import com.mobwal.home.models.db.Template;
import com.mobwal.home.ui.BaseFragment;
import com.mobwal.home.ui.global.OsmMapBottomDialogFragment;
import com.mobwal.home.ui.GeoLocationLayout;
import com.mobwal.home.ui.global.WalkerLocationListeners;
import com.mobwal.home.utilits.JsonUtil;

public class ResultFragment extends BaseFragment
        implements WalkerLocationListeners, View.OnClickListener {

    private Attachment[] mItems;

    private ActivityResultLauncher<Intent> mChoiceActivityResultLauncher;
    private final ActivityResultLauncher<String[]> mPermissionGalleryActivityResultLauncher;
    private final ActivityResultLauncher<String[]> mPermissionLocationActivityResultLauncher;

    private FragmentResultBinding binding;
    private DataManager mDataManager;
    private OsmMapBottomDialogFragment mGoogleMapBottomDialogFragment;

    private boolean mLocationRequire = false;
    private boolean mImageRequire = false;

    private String mLocationLevel = GeoLocationLayout.LEVEL;
    @Nullable
    private Location mLocation;

    private String mAttachmentFileName;

    private String f_route;
    private String f_point;
    @Nullable
    private Point mPoint;
    private String f_result;

    @Nullable
    private Result mResult;

    private String c_template;
    @Nullable
    private Template mTemplate;
    @Nullable
    private Route mRoute;

    private String id = UUID.randomUUID().toString();

    @Nullable
    @Override
    public String getSubTitle() {
        return mTemplate != null ? mTemplate.c_name : null;
    }

    public ResultFragment() {
        // Required empty public constructor

        // разрешения для фото
        mPermissionGalleryActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            binding.createResultGallery.onPermission(result);
        });

        // разрешения для геолокации
        mPermissionLocationActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            binding.createResultLocation.onPermission(result);
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WalkerApplication.Log("Результат.");
        setHasOptionsMenu(true);

        if(savedInstanceState != null) {
            mLocation = savedInstanceState.getParcelable("location");
            mItems = (Attachment[]) savedInstanceState.getSerializable("items");
            mAttachmentFileName = savedInstanceState.getString("fileName");
        }

        mDataManager = new DataManager(requireContext());
        mGoogleMapBottomDialogFragment = new OsmMapBottomDialogFragment();

        if(getArguments() != null) {
            f_route = getArguments().getString("f_route");
            f_point = getArguments().getString("f_point");
            f_result = getArguments().getString("f_result");
            c_template = getArguments().getString("c_template");

            SettingRoute settingRoute = new SettingRoute(mDataManager.getRouteSettings(f_route));
            mLocationRequire = settingRoute.geo;
            mLocationLevel = settingRoute.geo_quality;
            mImageRequire = settingRoute.image;

            mTemplate = mDataManager.getTemplate(f_route, c_template);
            mRoute = mDataManager.getRoute(f_route);
            mPoint = mDataManager.getPoint(f_point);
            mResult = mDataManager.getResult(f_result);
        }

        mChoiceActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> binding.createResultGallery.onActivityResult(result));
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("location", mLocation);

        outState.putSerializable("items", mItems);
        outState.putString("fileName", mAttachmentFileName);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentResultBinding.inflate(inflater, container, false);
        binding.createResultSave.setOnClickListener(this);
        binding.createResultGallery.setActivityResultLauncherChoice(mChoiceActivityResultLauncher);
        binding.createResultGallery.setActivityResultLauncherPermission(mPermissionGalleryActivityResultLauncher);
        binding.createResultGallery.FileName = mAttachmentFileName;

        binding.createResultLocation.setActivityResultLauncherPermission(mPermissionLocationActivityResultLauncher);
        binding.createResultLocation.setOnLocationListeners(this);
        binding.createResultLocation.setLevel(mLocationLevel);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(mTemplate != null) {
            binding.createResultForm.init(mTemplate.c_layout, new Hashtable<>());
        }

        binding.createResultSave.setEnabled(mPoint != null);
        binding.createResultCheck.bind(mPoint);

        binding.createResultGallery.setData(mItems == null ? mDataManager.getAttachments(f_result) : Arrays.asList(mItems));
        binding.createResultGallery.setPointBundle(new PointBundle(f_route, f_point, f_result));

        if(mResult != null) {
            binding.createResultLocation.setVisibility(View.GONE);
            binding.createResultForm.setValues(JsonUtil.toHashObject(mResult.jb_data));
        }

        if(mRoute != null) {
            binding.createResultCheck.setVisibility(mRoute.b_check ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        binding.createResultLocation.onStart(mLocation);
        putMapLocation(mPoint, mLocation);
    }

    @Override
    public void onStop() {
        super.onStop();

        binding.createResultLocation.onStop();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.info_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.action_info) {

            mItems = binding.createResultGallery.getData();
            mAttachmentFileName = binding.createResultGallery.FileName;

            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
            Bundle bundle = new Bundle();
            bundle.putString("f_point", f_point);
            bundle.putString("f_result", f_result);
            navController.navigate(R.id.nav_point_info, bundle);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if(mPoint == null) {
            return;
        }

        if(mImageRequire && binding.createResultGallery.getData().length == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle(R.string.attention);
            builder.setMessage(R.string.result_save_error4);

            builder.setCancelable(false);
            builder.setPositiveButton(R.string.yes, (dialog, which) -> {
                // ДА
                binding.createResultGallery.onLongClick(null);
            });
            builder.setNegativeButton(R.string.no, null);

            AlertDialog alert = builder.create();
            alert.show();

            return;
        }

        if(mLocationRequire && mLocation == null) {
            Toast.makeText(requireContext(), R.string.result_save_error5, Toast.LENGTH_LONG).show();
            return;
        }

        // получение данных с формы
        String jb_data = JsonUtil.toString(binding.createResultForm.getValues());

        Result item = mResult == null
                ? new Result(id, f_route, f_point, c_template, mLocation, mPoint)
                : mResult;

        item.jb_data = jb_data;

        String txt = "";

        binding.createResultSave.setEnabled(false);

        if(mRoute != null && mRoute.b_check) {
            if(!binding.createResultCheck.saveData(f_point)) {
                txt = getString(R.string.result_save_error3);
            }
        }

        if(mDataManager.addResult(item)) {
            if (binding.createResultGallery.saveData(item.id)) {
                requireActivity().onBackPressed();
            } else {
                txt = getString(R.string.result_save_error1);
            }
        } else {
            txt = getString(R.string.result_save_error2);
        }

        if(!TextUtils.isEmpty(txt)) {
            Toast.makeText(requireContext(), txt, Toast.LENGTH_SHORT).show();
            binding.createResultSave.setEnabled(true);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        mLocation = location;
        putMapLocation(mPoint, location);
    }

    @Override
    public void onLocationClick(View v) {
        mGoogleMapBottomDialogFragment.show(requireActivity().getSupportFragmentManager(), "map");
    }

    /**
     * Установка гелокации на карте
     * @param point точка маршрута
     * @param location геолокация
     */
    private void putMapLocation(@Nullable Point point, @Nullable Location location) {
        if(point != null) {
            LocationInfo locationInfo = new LocationInfo(location);
            locationInfo.taskLatitude = point.n_latitude;
            locationInfo.taskLongitude = point.n_longitude;

            mGoogleMapBottomDialogFragment.addLocation(locationInfo);
            binding.createResultGallery.setLocationInfo(locationInfo);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        binding.createResultForm.onDestroy();
        binding = null;
    }
}