package com.mobwal.home.ui.global;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import com.mobwal.home.DataManager;
import com.mobwal.home.R;
import com.mobwal.home.WalkerApplication;
import com.mobwal.home.adapter.ResultChoiceItemAdapter;
import com.mobwal.home.databinding.ResultChoiceBottomSheetBinding;
import com.mobwal.home.models.db.complex.ResultTemplate;
import com.mobwal.home.ui.RecycleViewItemListeners;

/**
 * Результат работы
 */
public class ResultChoiceBottomDialogFragment extends BottomSheetDialogFragment
    implements RecycleViewItemListeners {

    private String f_route;
    private String f_point;
    private DataManager mDataManager;
    private ResultChoiceBottomSheetBinding binding;

    private static final String ROUTE_ID = "route_id";
    private static final String POINT_ID = "point_id";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null) {
            f_route = savedInstanceState.getString(ROUTE_ID);
            f_point = savedInstanceState.getString(POINT_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mDataManager = new DataManager(requireContext());
        WalkerApplication.Log("Вывод доступных шаблонов.");
        binding = ResultChoiceBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(ROUTE_ID, f_route);
        outState.putString(POINT_ID, f_point);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.resultChoiceList.setLayoutManager(new LinearLayoutManager(requireContext()));
        ResultTemplate[] resultTemplates = mDataManager.getResultTemplates(f_route, f_point);
        ResultChoiceItemAdapter resultChoiceItemAdapter = new ResultChoiceItemAdapter(requireContext(), this, resultTemplates);

        binding.resultChoiceList.setAdapter(resultChoiceItemAdapter);
    }

    public void setPoint(String f_route, String f_point) {
        this.f_route = f_route;
        this.f_point = f_point;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewItemClick(String id) {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
        Bundle bundle = new Bundle();
        bundle.putString("f_route", f_route);
        bundle.putString("f_point", f_point);
        String[] data = id.split("\\|");

        bundle.putString("f_result", data[0]);
        bundle.putString("c_template", data[1]);

        WalkerApplication.Debug("Выбран шаблон для результата " + data[1]);

        navController.navigate(R.id.nav_result, bundle);

        dismiss();
    }
}