package com.example.zabello.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zabello.R;
import com.example.zabello.data.entity.ParameterEntry;
import com.example.zabello.domain.session.SessionManager;
import com.example.zabello.ui.dialogs.ParameterEntryDialog;
import com.example.zabello.ui.list.ParameterEntryAdapter;
import com.example.zabello.viewmodel.DashboardViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class DashboardFragment extends Fragment {

    private DashboardViewModel viewModel;
    private TextView tvDashboard;
    private TextView tvAnomalies;
    private RecyclerView rv;
    private ParameterEntryAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvDashboard = view.findViewById(R.id.tvDashboard);
        tvAnomalies = view.findViewById(R.id.tvAnomalies);
        rv = view.findViewById(R.id.rvEntries);
        FloatingActionButton fab = view.findViewById(R.id.fabAdd);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ParameterEntryAdapter();
        rv.setAdapter(adapter);

        viewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);

        viewModel.getTypes().observe(getViewLifecycleOwner(), types -> {
            if (types != null) {
                java.util.HashMap<Long, String> map = new java.util.HashMap<>();
                for (com.example.zabello.data.entity.ParameterType t : types) map.put(t.id, t.title);
                adapter.setTypeTitles(map);
            }
        });

        viewModel.getWelcomeText().observe(getViewLifecycleOwner(), text -> {
            if (tvDashboard != null) tvDashboard.setText(text);
        });

        viewModel.getLatestEntries().observe(getViewLifecycleOwner(), this::renderEntries);

        fab.setOnClickListener(v -> {
            long userId = SessionManager.getInstance(requireContext()).getUserId();
            if (userId <= 0) {
                Toast.makeText(requireContext(), "Сессия не найдена", Toast.LENGTH_SHORT).show();
                return;
            }
            ParameterEntryDialog.show(
                    getChildFragmentManager(),
                    userId,
                    (saved) -> {
                        // Гарантируем вызов тоста на главном потоке
                        if (isAdded()) requireActivity().runOnUiThread(
                                () -> Toast.makeText(requireContext(), "Добавлено", Toast.LENGTH_SHORT).show()
                        );
                    }
            );
        });
    }

    private void renderEntries(List<ParameterEntry> entries) {
        adapter.submitList(entries);
        int anomalies = adapter.getCurrentAnomalyCount();
        tvAnomalies.setText(getString(R.string.dashboard_anomalies, anomalies));
    }
}
