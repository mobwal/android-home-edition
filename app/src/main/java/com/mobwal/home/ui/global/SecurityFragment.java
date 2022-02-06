package com.mobwal.home.ui.global;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.mobwal.home.R;
import com.mobwal.home.WalkerApplication;
import com.mobwal.home.databinding.FragmentSecurityBinding;
import com.mobwal.home.ui.BaseFragment;
import com.mobwal.home.utilits.PrefUtil;

public class SecurityFragment extends BaseFragment
        implements View.OnClickListener, TextWatcher {

    private FragmentSecurityBinding mBinding;
    private String pinCode;

    public SecurityFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        WalkerApplication.Log("Безопасность. Пин-код.");
        pinCode = PrefUtil.getPinCode(requireContext());
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentSecurityBinding.inflate(inflater, container, false);
        mBinding.securityPass.addTextChangedListener(this);

        mBinding.securityOk.setOnClickListener(this);

        mBinding.securityName.setOnClickListener(this);

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        showActionBar(false);
    }

    @Override
    public void onResume() {
        super.onResume();

        mBinding.securityPass.setText("");
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mBinding.securityPass.setError(null);

        int passLength = pinCode.length();
        if(passLength > 0 && mBinding.securityPass.getText().toString().length() > passLength) {
            mBinding.securityPass.setText("");
            mBinding.securityPass.setError(getString(R.string.pin_code_fail));
            return;
        }

        if(mBinding.securityPass.getText().toString().equals(pinCode)) {
            // авторизация произведена
            onAuthorized();
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.security_ok) {
            if (mBinding.securityPass.getText().toString().equals(pinCode)) {
                // авторизация произведена
                onAuthorized();
            } else {
                mBinding.securityPass.setText("");
                mBinding.securityPass.setError(getString(R.string.pin_code_fail));
            }
        }

        if(v.getId() == R.id.security_name) {
            ObjectAnimator colorAnim = ObjectAnimator.ofInt(mBinding.securityNameBefore, "textColor",
                    Color.WHITE, Color.BLACK);
            colorAnim.setEvaluator(new ArgbEvaluator());
            colorAnim.setDuration(1000);
            colorAnim.start();

            colorAnim = ObjectAnimator.ofInt(mBinding.securityNameAfter, "textColor",
                    Color.WHITE, Color.BLACK);
            colorAnim.setEvaluator(new ArgbEvaluator());
            colorAnim.setDuration(1000);
            colorAnim.start();

            colorAnim = ObjectAnimator.ofInt(mBinding.securityNameTag, "textColor",
                    Color.WHITE, Color.parseColor("#ff9e9e9e"));
            colorAnim.setEvaluator(new ArgbEvaluator());
            colorAnim.setDuration(2000);
            colorAnim.start();
        }
    }

    private void onAuthorized() {
        mBinding.securityPass.setError(null);

        // авторизация не требуется
        WalkerApplication.setAuthorized(requireContext(), true);

        if (requireActivity().getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(requireActivity().getCurrentFocus().getWindowToken(), 0);
        }

        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
        navController.popBackStack();
        navController.navigate(R.id.nav_route);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        showActionBar(true);
    }
}