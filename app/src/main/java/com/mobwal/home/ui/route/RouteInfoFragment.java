package com.mobwal.home.ui.route;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;

import com.mobwal.home.DataManager;
import com.mobwal.home.R;
import com.mobwal.home.WalkerApplication;
import com.mobwal.home.adapter.RouteInfoCategoryAdapter;
import com.mobwal.home.databinding.FragmentRouteInfoBinding;
import com.mobwal.home.models.RouteInfo;
import com.mobwal.home.models.db.Route;
import com.mobwal.home.utilits.ExportToShared;
import com.mobwal.home.utilits.NewThread;
import com.mobwal.home.utilits.ZipManager;

/**
 * Информация по маршруту
 */
public class RouteInfoFragment extends Fragment {

    private FragmentRouteInfoBinding binding;
    @Nullable
    private String f_route = null;
    private RouteInfoCategoryAdapter mRouteInfoCategoryAdapter;
    private DataManager mDataManager;
    @Nullable
    private Route mRoute;

    private NewThread mPackThread;

    private MenuItem mShareMenuItem;

    public RouteInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        WalkerApplication.Log("Маршруты. Информация.");
        if(getArguments() != null) {
            f_route = getArguments().getString("f_route");
        }

        mDataManager = new DataManager(requireContext());
        mRoute = mDataManager.getRoute(f_route);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRouteInfoBinding.inflate(inflater, container, false);
        binding.routeInfo.setLayoutManager(new LinearLayoutManager(requireContext()));
        return binding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();

        // тут нужна проверка, что в маршруте выполнена хотя бы одна точка
        Long count = mDataManager.getResultCount(f_route);
        if(count != null && count > 0) {
            inflater.inflate(R.menu.share_menu, menu);
            mShareMenuItem = menu.findItem(R.id.action_share);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.action_share && f_route != null) {
            item.setEnabled(false);
            binding.routeInfoProgress.setVisible(true);
            binding.routeInfoProgress.setIndeterminate(getString(R.string.pack));
            mPackThread.run();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();

        RouteInfo[][] info = mDataManager.getRouteInfo(f_route);

        mRouteInfoCategoryAdapter = new RouteInfoCategoryAdapter(requireContext(), info);
        binding.routeInfo.setAdapter(mRouteInfoCategoryAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        // обновляется история после изменения
        RouteInfo[][] info = mDataManager.getRouteInfo(f_route);
        mRouteInfoCategoryAdapter.updateItem(info[0], 0);
    }

    @Override
    public void onStop() {
        super.onStop();

        mPackThread.destroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        mPackThread = new NewThread(requireActivity()) {

            private String mResult;
            private File mOutputFile;

            @Override
            public void onBackgroundExecute() {
                if(f_route != null) {

                    ExportToShared exportToShared = new ExportToShared(f_route, mOutputFile);
                    mResult = exportToShared.generate(requireContext(), new ZipManager.ZipListeners() {
                        @Override
                        public void onZipPack(int total, int current) {
                            if(!RouteInfoFragment.this.isDetached()) {
                                requireActivity().runOnUiThread(() -> binding.routeInfoProgress.setProgress((current * 100) / total));
                            }
                        }
                    });

                    mOutputFile = exportToShared.OutputZip;
                }
            }

            @Override
            public void onPostExecute() {
                binding.routeInfoProgress.setVisible(false);
                mShareMenuItem.setEnabled(true);

                if(RouteInfoFragment.this.isAdded()) {
                    if (TextUtils.isEmpty(mResult)) {
                        ShareCompat.IntentBuilder intentBuilder = new ShareCompat.IntentBuilder(requireActivity());
                        if (mRoute != null) {
                            intentBuilder.setSubject(mRoute.toExportTitle(requireContext()));
                        }

                        Uri fileUri = FileProvider.getUriForFile(
                                requireContext(),
                                "com.mobwal.home.provider", //(use your app signature + ".provider" )
                                mOutputFile);

                        intentBuilder.setStream(fileUri);
                        intentBuilder.setType("application/zip");
                        intentBuilder.startChooser();
                    } else {
                        WalkerApplication.Debug(mResult);
                        Toast.makeText(requireContext(), mResult, Toast.LENGTH_LONG).show();
                    }
                }
            }
        };
    }
}