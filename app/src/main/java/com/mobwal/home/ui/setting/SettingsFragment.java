package com.mobwal.home.ui.setting;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

import com.mobwal.home.CustomLayoutManager;
import com.mobwal.home.Names;
import com.mobwal.home.R;
import com.mobwal.home.WalkerApplication;
import com.mobwal.home.utilits.ActivityUtil;
import com.mobwal.home.utilits.PrefUtil;
import com.mobwal.home.utilits.VersionUtil;

public class SettingsFragment extends PreferenceFragmentCompat
        implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private int clickToVersion = 0;

    private SharedPreferences mSharedPreferences;

    private Preference mVersionPreference;
    private SwitchPreferenceCompat mErrorReportingPreference;
    private Preference mDebugModePreference;
    private Preference mPinPreference;
    private Preference mLayoutPreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.main_pref, rootKey);

        mSharedPreferences = requireContext().getSharedPreferences(Names.PREFERENCE_NAME, Context.MODE_PRIVATE);

        mVersionPreference = findPreference("app_version");
        Objects.requireNonNull(mVersionPreference).setOnPreferenceClickListener(this);

        Preference resetSettingsPreference = findPreference("reset_settings");
        Objects.requireNonNull(resetSettingsPreference).setOnPreferenceClickListener(this);

        mErrorReportingPreference = findPreference("error_reporting");
        Objects.requireNonNull(mErrorReportingPreference).setOnPreferenceChangeListener(this);

        mDebugModePreference = findPreference("debug");
        Objects.requireNonNull(mDebugModePreference).setOnPreferenceClickListener(this);

        mPinPreference = findPreference("pin");
        Objects.requireNonNull(mPinPreference).setOnPreferenceClickListener(this);

        mLayoutPreference = findPreference("layout");
        Objects.requireNonNull(mLayoutPreference).setOnPreferenceClickListener(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WalkerApplication.Log("Настройки");
        setHasOptionsMenu(true);

        Objects.requireNonNull(mVersionPreference).setSummary(VersionUtil.getVersionName(requireActivity()));
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void setup() {
        boolean debug = mSharedPreferences.getBoolean("debug", false);
        mDebugModePreference.setVisible(debug);

        boolean pinPrefValue = mSharedPreferences.getBoolean("pin", false);
        Objects.requireNonNull(mPinPreference).setSummary(pinPrefValue ? R.string.pin_settings_summary_on: R.string.pin_settings_summary_off);

        mLayoutPreference.setTitle(new CustomLayoutManager(requireContext()).getDefaultLayoutName());

        boolean errorReportingPrefValue = mSharedPreferences.getBoolean("error_reporting", false);
        mErrorReportingPreference.setChecked(errorReportingPrefValue);
    }

    @Override
    public void onResume() {
        super.onResume();

        setup();

        clickToVersion = 0;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if(preference.getKey().equals("error_reporting")) {
            boolean errorReportingPrefValue = Boolean.parseBoolean(String.valueOf(newValue));
            mSharedPreferences.edit().putBoolean("error_reporting", errorReportingPrefValue).apply();
        }
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if(preference.getKey().equals("reset_settings")) {

            AlertDialog.Builder adb = new AlertDialog.Builder(requireContext());
            adb.setPositiveButton(R.string.yes, (dialog, which) -> {
                if(new CustomLayoutManager(requireContext()).removeLayout()) {
                    WalkerApplication.Debug("Шаблон формы удален.");
                } else {
                    Toast.makeText(getActivity(), R.string.layout_remove_error, Toast.LENGTH_LONG).show();
                }

                PrefUtil.setPinCode(requireContext(), "");
                mSharedPreferences.edit().clear().apply();
                Toast.makeText(getActivity(), R.string.reset_setting_success, Toast.LENGTH_SHORT).show();
                setup();
            });

            adb.setNegativeButton(getResources().getString(R.string.no), null);

            AlertDialog alert = adb.create();
            alert.setTitle(R.string.reset_settings);
            alert.setMessage(getString(R.string.reset_settings_confirm));
            alert.show();
        }

        if (preference.getKey().equals("app_version")) {
            clickToVersion++;
            if (clickToVersion >= 6) {
                Toast.makeText(getActivity(), R.string.debug_mode_summary_on, Toast.LENGTH_SHORT).show();
                clickToVersion = 0;
                mDebugModePreference.setVisible(true);

                mSharedPreferences.edit().putBoolean("debug", true).apply();
            }
        } else {
            clickToVersion = 0;
        }

        if(preference.getKey().equals("debug")) {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.nav_debug_settings);
        }

        if(preference.getKey().equals("pin")) {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.nav_pin_settings);
        }

        if(preference.getKey().equals("layout")) {
            ActivityUtil.openLayout(requireActivity());
        }

        return false;
    }
}