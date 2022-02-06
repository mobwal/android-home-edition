package com.mobwal.home.ui.setting;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Hashtable;

import com.mobwal.home.CustomLayoutManager;
import com.mobwal.home.R;
import com.mobwal.home.WalkerApplication;
import com.mobwal.home.databinding.FragmentLayoutBinding;

/**
 * Просмотр шаблонов
 */
public class LayoutFragment extends Fragment {

    private FragmentLayoutBinding mBinding;

    public LayoutFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WalkerApplication.Log("Настройки. Просмотр шаблона.");
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mBinding = FragmentLayoutBinding.inflate(inflater, container, false);
        mBinding.layoutBody.init(new CustomLayoutManager(requireContext()).getDefaultLayout(), new Hashtable<>());
        return mBinding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(requireActivity() instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setSubtitle(R.string.layout_summary);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if(requireActivity() instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setSubtitle("");
            }
        }

        mBinding = null;
    }
}