package com.example.zabello.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.zabello.R;
import com.example.zabello.viewmodel.DashboardViewModel;

public class DashboardFragment extends Fragment {
    public DashboardFragment() { }

    public static DashboardFragment newInstance() { return new DashboardFragment(); }

    private TextView tvDashboard;
    private DashboardViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        tvDashboard = root.findViewById(R.id.tvDashboard);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);

        viewModel.getWelcomeText().observe(getViewLifecycleOwner(), text -> {
            if (tvDashboard != null) tvDashboard.setText(text);
        });
    }
}
