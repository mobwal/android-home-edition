package com.mobwal.home.ui.imp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import java.io.IOException;
import java.io.InputStream;

import com.mobwal.home.R;
import com.mobwal.home.WalkerApplication;
import com.mobwal.home.adapter.PointItemAdapter;
import com.mobwal.home.databinding.FragmentZipImportBinding;
import com.mobwal.home.models.db.complex.PointItem;
import com.mobwal.home.models.db.Setting;
import com.mobwal.home.ui.BaseFragment;
import com.mobwal.home.utilits.ActivityUtil;
import com.mobwal.home.utilits.FileManager;
import com.mobwal.home.utilits.ImportUtil;
import com.mobwal.home.utilits.NewThread;
import com.mobwal.home.utilits.StreamUtil;
import com.mobwal.home.utilits.ZipManager;
import com.mobwal.home.utilits.ZipReader;

/**
 * импорт zip
 */
public class ZipImportFragment extends BaseFragment
        implements View.OnClickListener {

    private FragmentZipImportBinding mBinding;
    private String mContentType;
    private File mFile;
    @Nullable
    private PointItem[] mPointItems = null;
    private FileManager mCacheFileManager;
    private ZipReader mZipReader;
    private NewThread mUnPackThread;

    @Nullable
    private MenuItem mLocationMenuItem;

    @Nullable
    private MenuItem mAttachMenuItem;

    private boolean locationVisible = false;
    private boolean attachVisible = false;

    @Nullable
    @Override
    public String getSubTitle() {
        return mContentType;
    }

    public ZipImportFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WalkerApplication.Log("Импорт. Архива.");
        setHasOptionsMenu(true);

        mCacheFileManager = new FileManager(requireContext().getCacheDir());

        Intent intent = requireActivity().getIntent();
        if(intent != null) {
            mContentType = intent.getType();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();

        inflater.inflate(R.menu.route_import_menu, menu);

        mLocationMenuItem = menu.findItem(R.id.action_location);
        mLocationMenuItem.setVisible(locationVisible);
        mAttachMenuItem = menu.findItem(R.id.action_attach);
        mAttachMenuItem.setVisible(attachVisible);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.action_location) {
            Toast.makeText(requireContext(), R.string.location_require, Toast.LENGTH_LONG).show();
            return true;
        }

        if(item.getItemId() == R.id.action_attach) {
            Toast.makeText(requireContext(), R.string.attach_require, Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentZipImportBinding.inflate(inflater, container, false);
        mBinding.importZipList.setLayoutManager(new LinearLayoutManager(requireContext()));
        mBinding.importZipListButton.setOnClickListener(this);

        return mBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        Intent intent = requireActivity().getIntent();
        if(intent != null) {
            mBinding.importZipProgress.setVisible(true);

            Uri textUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            try {
                // получение архива и сохранение его в кэш
                mBinding.importZipProgress.setTitle(getString(R.string.copy_data));
                String fileName = ActivityUtil.getFileNameFromUri(textUri);
                InputStream iStream = requireContext().getContentResolver().openInputStream(textUri);
                byte[] bytes = StreamUtil.readBytes(iStream);

                String folder = "import";
                File dir = mCacheFileManager.getRootCatalog(folder);
                if(dir.exists()) {
                    FileManager.deleteRecursive(dir);
                }

                mCacheFileManager.writeBytes(folder, fileName, bytes);

                mFile = new File(mCacheFileManager.getRootCatalog(folder), fileName);
            } catch (IOException e) {
                WalkerApplication.Log("Импорт. Ошибка сохранения архива в локальном кэш.", e);
            }

            // распаковка данных и вывод данных
            if(mFile.exists()) {
                mBinding.importZipProgress.setTitle(getString(R.string.unpack));

                mUnPackThread.run();
            } else {
                mBinding.importZipProgress.setVisible(false);
                visibleItems(false);
                WalkerApplication.Log("Импорт ZIP. Не найден скопированный файл для распаковки");
            }
        } else {
            visibleItems(false);
            WalkerApplication.Log("Импорт ZIP. Intent не найден.");
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        resetData();
    }

    @Override
    public void onClick(View v) {
        if(mZipReader == null) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.attention);
        builder.setMessage(R.string.import_run);

        builder.setCancelable(false);
        builder.setPositiveButton(R.string.yes, (dialog, which) -> {

            String result = ImportUtil.generateRouteFromZip(requireContext(), mZipReader, mZipReader.getName(mZipReader.isCheckMode()), mZipReader.getOutputDir().getName());
            if(!TextUtils.isEmpty(result)) {
                WalkerApplication.Log("Импорт. Ошибка создания маршрута из архива. " + result);
                Toast.makeText(requireContext(), result, Toast.LENGTH_LONG).show();
            } else {
                WalkerApplication.Log("Загрузка заданий из архива произведена");
                mZipReader.close();
                requireActivity().onBackPressed();
            }
        });
        builder.setNegativeButton(R.string.no, null);

        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Видимость списка
     * @param visible видимость списка
     */
    private void visibleItems(boolean visible) {
        mBinding.importZipListEmpty.setVisibility(visible ? View.GONE : View.VISIBLE);

        mBinding.importZipListEmpty.setText(R.string.point_list_empty);

        mBinding.importZipList.setVisibility(visible ? View.VISIBLE : View.GONE);
        mBinding.importZipListButton.setEnabled(visible);
    }

    /**
     * Обновление данных
     */
    private void updateData() {
        PointItemAdapter pointItemAdapter = new PointItemAdapter(requireContext(), mPointItems, null, true);
        boolean isEmpty = mPointItems == null || mPointItems.length == 0;
        visibleItems(!isEmpty);
        mBinding.importZipList.setAdapter(pointItemAdapter);

        String[][] rows = mZipReader.getSettings(mZipReader.isCheckMode());
        if(rows != null) {
            Setting[] settings = ImportUtil.convertRowsToSettings(rows, "");
            if(settings != null) {
                for (Setting setting : settings) {
                    if (setting.c_key.equalsIgnoreCase("geo")) {
                        if(setting.c_value.equalsIgnoreCase("true")) {
                            locationVisible = true;
                            if(mLocationMenuItem != null) {
                                mLocationMenuItem.setVisible(true);
                            }
                        }
                    }

                    if (setting.c_key.equalsIgnoreCase("image")) {
                        if(setting.c_value.equalsIgnoreCase("true")) {
                            attachVisible = true;
                            if(mAttachMenuItem != null) {
                                mAttachMenuItem.setVisible(true);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Сброс данных
     */
    private void resetData() {
        mUnPackThread.destroy();

        mBinding.importZipProgress.setProgress(0);
        mBinding.importZipListButton.setEnabled(false);
        mBinding.importZipProgress.setVisible(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        mUnPackThread = new NewThread(requireActivity()) {

            @Override
            public void onBackgroundExecute() {

                if(mZipReader != null) {
                    mZipReader.close();
                    mZipReader = null;
                }

                mZipReader = new ZipReader(requireActivity(), mFile.getPath(), null, new ZipManager.ZipListeners() {
                    @Override
                    public void onZipUnPack(int total, int current) {
                        if(!ZipImportFragment.this.isDetached()) {
                            requireActivity().runOnUiThread(() -> mBinding.importZipProgress.setProgress((current * 100) / total));
                        }
                    }
                });

                boolean check = mZipReader.isCheckMode();
                String[][] rows = mZipReader.getPoints(check);
                mPointItems = ImportUtil.convertPointsToPointItems(ImportUtil.convertRowsToPoints(rows, ""));
            }

            @Override
            public void onPostExecute() {
                if(ZipImportFragment.this.isAdded()) {
                    mBinding.importZipProgress.setVisible(false);
                    updateData();
                }
            }
        };
    }
}