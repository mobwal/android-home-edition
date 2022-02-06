package com.mobwal.home.ui;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

/**
 * Базовый класс для фрагментов
 */
public class BaseFragment extends Fragment {

    /**
     * Получение подзаголовка
     * @return подзаголовок
     */
    @Nullable
    public String getSubTitle() {
        return null;
    }

    @Override
    public void onStart() {
        super.onStart();

        if(requireActivity() instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
            if (actionBar != null && getSubTitle() != null) {
                actionBar.setSubtitle(getSubTitle());
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if(requireActivity() instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
            if (actionBar != null && getSubTitle() != null) {
                actionBar.setSubtitle("");
            }
        }
    }

    /**
     * Вывод ActionBar
     * @param visible видимость
     */
    public void showActionBar(boolean visible) {
        if(requireActivity() instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
            if (actionBar != null) {
                if (visible) {
                    actionBar.show();
                } else {
                    actionBar.hide();
                }
            }
        }
    }
}
