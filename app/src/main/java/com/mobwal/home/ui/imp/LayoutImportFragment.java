package com.mobwal.home.ui.imp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;

import com.mobwal.home.CustomLayoutManager;
import com.mobwal.home.R;
import com.mobwal.home.WalkerApplication;
import com.mobwal.home.databinding.FragmentLayoutImportBinding;
import com.mobwal.home.ui.BaseFragment;
import com.mobwal.home.utilits.StreamUtil;
import com.mobwal.home.utilits.StringUtil;

/**
 * Импорт шаблона форм
 */
public class LayoutImportFragment extends BaseFragment
    implements View.OnClickListener {

    private FragmentLayoutImportBinding mBinding;
    private String mData;
    private String mContentType;

    @Nullable
    @Override
    public String getSubTitle() {
        return mContentType;
    }

    public LayoutImportFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WalkerApplication.Log("Импорт. Шаблона.");
        if(savedInstanceState != null) {
            mData = (String) savedInstanceState.getSerializable("data");
        }

        Intent intent = requireActivity().getIntent();
        if(intent != null) {
            mContentType = intent.getType();

            if(mData == null) {
                Uri textUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                try {
                    InputStream iStream = requireContext().getContentResolver().openInputStream(textUri);
                    byte[] bytes = StreamUtil.readBytes(iStream);
                    mData = new String(bytes, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    WalkerApplication.Log("Импорт. Ошибка чтения шаблона форм", e);
                    visibleLayout(e);
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("data", mData);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mBinding = FragmentLayoutImportBinding.inflate(inflater, container, false);
        mBinding.layoutImportSave.setOnClickListener(this);
        mBinding.layoutImportBody.init(mData, new Hashtable<>());
        return mBinding.getRoot();
    }

    /**
     * Видимость формы
     * @param e исключение
     */
    private void visibleLayout(Exception e) {
        mBinding.layoutImportEmpty.setVisibility(View.VISIBLE);

        if(WalkerApplication.Debug) {
            mBinding.layoutImportEmpty.setText(StringUtil.exceptionToString(e));
        }

        mBinding.layoutImportBody.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.attention);
        builder.setMessage(R.string.import_run);

        builder.setCancelable(false);
        builder.setPositiveButton(R.string.yes, (dialog, which) -> {
            CustomLayoutManager manager = new CustomLayoutManager(requireContext());
            if(manager.updateLayout(mData)) {
                WalkerApplication.Log("Замена шаблона форм");
                requireActivity().onBackPressed();
            } else {
                Toast.makeText(requireContext(), R.string.layout_import_error, Toast.LENGTH_SHORT).show();
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
}