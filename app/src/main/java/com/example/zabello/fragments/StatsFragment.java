package com.example.zabello.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zabello.R;
import com.example.zabello.data.entity.ParameterType;
import com.example.zabello.ui.list.ParameterEntryAdapter;
import com.example.zabello.viewmodel.StatsViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class StatsFragment extends Fragment {

    private StatsViewModel vm;
    private Spinner spTypes;
    private ChipGroup chipGroup;
    private final List<ParameterType> types = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        spTypes = v.findViewById(R.id.spTypes);
        chipGroup = v.findViewById(R.id.chipsPeriod);
        RecyclerView rv = v.findViewById(R.id.rvStats);

        if (spTypes == null || chipGroup == null || rv == null) {
            throw new IllegalStateException("fragment_stats.xml должен содержать spTypes, chipsPeriod и rvStats");
        }

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        ParameterEntryAdapter adapter = new ParameterEntryAdapter();
        rv.setAdapter(adapter);

        vm = new ViewModelProvider(requireActivity()).get(StatsViewModel.class);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, new ArrayList<>());
        spTypes.setAdapter(spinnerAdapter);

        vm.getTypes().observe(getViewLifecycleOwner(), list -> {
            types.clear();
            if (list != null) types.addAll(list);
            List<String> titles = new ArrayList<>();
            for (ParameterType t : types) titles.add(t.title);
            spinnerAdapter.clear();
            spinnerAdapter.addAll(titles);
            spinnerAdapter.notifyDataSetChanged();
            if (!types.isEmpty()) {
                spTypes.setSelection(0);
                vm.setSelectedType(types.get(0).id);
            }
        });

        spTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < types.size()) vm.setSelectedType(types.get(position).id);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        vm.getEntries().observe(getViewLifecycleOwner(), adapter::submitList);

        setChipListener(chipGroup.findViewById(R.id.chip7), 7);
        setChipListener(chipGroup.findViewById(R.id.chip30), 30);
        setChipListener(chipGroup.findViewById(R.id.chip90), 90);
        setChipListener(chipGroup.findViewById(R.id.chipAll), 0);
    }

    private void setChipListener(Chip chip, int days) {
        if (chip != null) chip.setOnClickListener(v -> vm.setDaysBack(days));
    }
}
