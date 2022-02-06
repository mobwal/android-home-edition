package com.mobwal.home.ui.imp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.mobwal.home.Names;
import com.mobwal.home.R;
import com.mobwal.home.WalkerApplication;
import com.mobwal.home.adapter.PointItemAdapter;
import com.mobwal.home.databinding.FragmentTextImportBinding;
import com.mobwal.home.models.db.complex.PointItem;
import com.mobwal.home.ui.BaseFragment;
import com.mobwal.home.utilits.ActivityUtil;
import com.mobwal.home.utilits.CsvReader;
import com.mobwal.home.utilits.ImportUtil;
import com.mobwal.home.utilits.NewThread;
import com.mobwal.home.utilits.StreamUtil;
import com.mobwal.home.utilits.StringUtil;

/**
 * Импорт текстовых данных в приложение
 */
public class TextImportFragment extends BaseFragment
        implements View.OnClickListener {

    private FragmentTextImportBinding mBinding;
    private String mData;
    private String mContentType;
    private String mFileName;
    private NewThread mReadThread;
    private CsvReader mCsvReader;
    private PointItem[] mPointItems;

    @Nullable
    @Override
    public String getSubTitle() {
        return mContentType;
    }

    public TextImportFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WalkerApplication.Log("Импорт. Текстовый файл.");
        Intent intent = requireActivity().getIntent();
        if(intent != null) {
            mContentType = intent.getType();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentTextImportBinding.inflate(inflater, container, false);
        mBinding.importTextList.setLayoutManager(new LinearLayoutManager(requireContext()));
        mBinding.importTextListButton.setOnClickListener(this);

        return mBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        Intent intent = requireActivity().getIntent();
        if(intent != null) {
            mBinding.importTextProgress.setVisible(true);
            mBinding.importTextProgress.setTitle(getString(R.string.read_data));

            mReadThread.run();
        } else {
            visibleItems(false, null);
            WalkerApplication.Log("Импорт TXT. Intent не найден.");
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        resetData();
    }

    @Override
    public void onClick(View v) {
        if(mCsvReader == null) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.attention);
        builder.setMessage(R.string.import_run);

        builder.setCancelable(false);
        builder.setPositiveButton(R.string.yes, (dialog, which) -> {
            String result = ImportUtil.generateRouteFormCsv(requireContext(), mCsvReader, mFileName);

            if(!TextUtils.isEmpty(result)) {
                WalkerApplication.Log("Импорт. Ошибка создания маршрута из текстового файла. " + result);
                Toast.makeText(requireContext(), result, Toast.LENGTH_LONG).show();
            } else {
                WalkerApplication.Log("Загрузка заданий произведена");
                requireActivity().onBackPressed();
            }
        });
        builder.setNegativeButton(R.string.no, null);

        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    /**
     * Видимость списка
     * @param visible видимость списка
     * @param exception исключение
     */
    private void visibleItems(boolean visible, @Nullable Exception exception) {
        mBinding.importTextListEmpty.setVisibility(visible ? View.GONE : View.VISIBLE);

        if (WalkerApplication.Debug) {
            if(exception != null) {
                mBinding.importTextListEmpty.setText(StringUtil.exceptionToString(exception));
            }
        } else {
            mBinding.importTextListEmpty.setText(R.string.point_list_empty);
        }

        mBinding.importTextList.setVisibility(visible ? View.VISIBLE : View.GONE);
        mBinding.importTextListButton.setEnabled(visible);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        mReadThread = new NewThread(requireActivity()) {

            private IOException mException;

            @Override
            public void onBackgroundExecute() {
                Intent intent = requireActivity().getIntent();
                if(intent != null) {
                    Uri textUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                    try {
                        mFileName = getRealPathFromURI(requireContext(), textUri);
                        if(mFileName == null) {
                            mFileName = ActivityUtil.getFileNameFromUri(textUri);
                        }
                        InputStream iStream = requireContext().getContentResolver().openInputStream(textUri);
                        byte[] bytes = StreamUtil.readBytes(iStream);
                        mData = new String(bytes, StandardCharsets.UTF_8);

                        mCsvReader = new CsvReader(mData);
                        String[][] rows = mCsvReader.getRows();
                        mPointItems = ImportUtil.convertPointsToPointItems(ImportUtil.convertRowsToPoints(rows, ""));
                    }catch (IOException e) {
                        mException = e;
                        WalkerApplication.Log("Импорт TXT. Ошибка получения данных из Intent.", mException);
                    }
                }
            }

            @Override
            public void onPostExecute() {
                if(TextImportFragment.this.isAdded()) {
                    if(mException == null) {
                        mBinding.importTextProgress.setVisible(false);
                        updateData();
                    } else {
                        visibleItems(false, mException);
                    }
                }
            }
        };
    }

    /**
     * Получение имени файла из Uri
     * Информация получена из https://developer.android.com/training/data-storage/shared/media#java
     * @param context контекст
     * @param contentUri путь
     * @return имя файла из mediaStore
     */
    public String getRealPathFromURI(Context context, Uri contentUri) {
        String name;
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Files.FileColumns.DISPLAY_NAME };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME);
            cursor.moveToFirst();
            name = cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return name;
    }

    /**
     * Обновление данных
     */
    private void updateData() {
        PointItemAdapter pointItemAdapter = new PointItemAdapter(requireContext(), mPointItems, null, true);
        boolean isEmpty = mPointItems == null || mPointItems.length == 0;
        visibleItems(!isEmpty, null);
        mBinding.importTextList.setAdapter(pointItemAdapter);
    }

    /**
     * Сброс данных
     */
    private void resetData() {
        mReadThread.destroy();

        mBinding.importTextListButton.setEnabled(false);
        mBinding.importTextProgress.setVisible(false);
    }
}