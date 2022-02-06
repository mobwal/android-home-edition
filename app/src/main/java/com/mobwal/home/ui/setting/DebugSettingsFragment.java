package com.mobwal.home.ui.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

import com.mobwal.home.Names;
import com.mobwal.home.R;
import com.mobwal.home.WalkerApplication;
import com.mobwal.home.utilits.ActivityUtil;

/**
 * Фрагмент для отображения параметров предназначенных для разработчика
 */
public class DebugSettingsFragment extends PreferenceFragmentCompat
        implements Preference.OnPreferenceChangeListener {

    private SharedPreferences mSharedPreferences;

    private SwitchPreferenceCompat mDebugModePreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.debug_pref, rootKey);

        mSharedPreferences = requireContext().getSharedPreferences(Names.PREFERENCE_NAME, Context.MODE_PRIVATE);

        mDebugModePreference = findPreference("debug");
        Objects.requireNonNull(mDebugModePreference).setOnPreferenceChangeListener(this);

        Preference demoPreference = findPreference("demo");
        Objects.requireNonNull(demoPreference).setOnPreferenceClickListener(preference -> {
            mSharedPreferences.edit().putBoolean("demo", false).apply();
            Snackbar.make(requireView(), getString(R.string.demo_route_created), Snackbar.LENGTH_LONG).setAction(requireContext().getString(R.string.go), v -> ActivityUtil.openRoutes(requireActivity())).show();
            return false;
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WalkerApplication.Log("Настройки. Режим отладки.");
        setHasOptionsMenu(true);

        boolean debug = mSharedPreferences.getBoolean("debug", false);
        mDebugModePreference.setChecked(debug);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if(preference.getKey().equals("debug")) {
            boolean debugPrefValue = Boolean.parseBoolean(String.valueOf(newValue));
            mSharedPreferences.edit().putBoolean("debug", debugPrefValue).apply();
        }

        return true;
    }
}