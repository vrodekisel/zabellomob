package com.example.zabello.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zabello.R;
import com.example.zabello.data.entity.ParameterEntry;
import com.example.zabello.data.entity.ParameterType;
import com.example.zabello.ui.list.ParameterEntryAdapter;
import com.example.zabello.utils.FilterPrefs;
import com.example.zabello.viewmodel.StatsViewModel;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.color.MaterialColors;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StatsFragment extends Fragment {

    private StatsViewModel vm;
    private FilterPrefs prefs;

    private Spinner spTypes;
    private ChipGroup chipGroup;
    private RecyclerView rv;
    private TextView tvEmpty;
    private LineChart chart;

    private final List<ParameterType> types = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = new FilterPrefs(requireContext());

        spTypes = view.findViewById(R.id.spTypes);
        chipGroup = view.findViewById(R.id.chipsPeriod);
        rv = view.findViewById(R.id.rvStats);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        chart = view.findViewById(R.id.chart);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        ParameterEntryAdapter adapter = new ParameterEntryAdapter();
        rv.setAdapter(adapter);

        setupChart();

        vm = new ViewModelProvider(requireActivity()).get(StatsViewModel.class);

        // Types spinner
        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, new ArrayList<>());
        spTypes.setAdapter(spAdapter);
        spTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                if (position >= 0 && position < types.size()) {
                    long typeId = types.get(position).id;
                    prefs.setStatsTypeId(typeId);
                    vm.setSelectedType(typeId);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        vm.getTypes().observe(getViewLifecycleOwner(), list -> {
            types.clear();
            if (list != null) types.addAll(list);
            List<String> titles = new ArrayList<>();
            for (ParameterType t : types) titles.add(t.title);
            spAdapter.clear();
            spAdapter.addAll(titles);
            spAdapter.notifyDataSetChanged();

            // Restore saved type selection
            long savedTypeId = prefs.getStatsTypeId();
            int idx = -1;
            for (int i = 0; i < types.size(); i++) if (types.get(i).id == savedTypeId) { idx = i; break; }
            if (idx >= 0) {
                spTypes.setSelection(idx);
            } else if (!types.isEmpty()) {
                spTypes.setSelection(0);
            }
        });

        // Period chips
        setChipListener(view.findViewById(R.id.chip7), 7);
        setChipListener(view.findViewById(R.id.chip30), 30);
        setChipListener(view.findViewById(R.id.chip90), 90);
        setChipListener(view.findViewById(R.id.chipAll), 0);

        // Restore saved days
        int savedDays = prefs.getStatsDays();
        selectDaysChip(savedDays);
        vm.setDaysBack(savedDays);

        vm.getEntries().observe(getViewLifecycleOwner(), list -> {
            adapter.submitList(list);
            tvEmpty.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
            updateChart(list);
        });
    }

    private void setChipListener(Chip chip, int days) {
        if (chip != null) chip.setOnClickListener(v -> {
            prefs.setStatsDays(days);
            vm.setDaysBack(days);
        });
    }

    private void selectDaysChip(int days) {
        int id = R.id.chip7;
        if (days == 30) id = R.id.chip30;
        else if (days == 90) id = R.id.chip90;
        else if (days == 0) id = R.id.chipAll;
        chipGroup.check(id);
    }

    // --- Chart ---
    private void setupChart() {
        chart.getDescription().setEnabled(false);
        chart.setNoDataText(getString(R.string.state_empty));

        XAxis x = chart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setGranularity(1f);
        x.setValueFormatter(new DateAxisFormatter());

        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setEnabled(true);
    }

    private void updateChart(List<ParameterEntry> list) {
        if (list == null || list.isEmpty()) {
            chart.clear();
            chart.invalidate();
            return;
        }

        // Build values sorted by time
        List<Entry> points = new ArrayList<>();
        List<ParameterEntry> copy = new ArrayList<>(list);
        Collections.sort(copy, (a,b) -> {
            long at = a.timestamp != null ? a.timestamp.getTime() : 0L;
            long bt = b.timestamp != null ? b.timestamp.getTime() : 0L;
            return Long.compare(at, bt);
        });
        for (ParameterEntry e : copy) {
            if (e.timestamp == null) continue;
            float x = (float)(e.timestamp.getTime() / 86400000.0); // дни от эпохи
            points.add(new Entry(x, e.value));
        }

        LineDataSet ds = new LineDataSet(points, buildLabel());
        int color = MaterialColors.getColor(chart, com.google.android.material.R.attr.colorPrimary, 0);
        ds.setColor(color);
        ds.setLineWidth(2f);
        ds.setDrawCircles(false);
        ds.setDrawValues(false);
        ds.setMode(LineDataSet.Mode.LINEAR);

        chart.setData(new LineData(ds));
        chart.invalidate();
    }

    private String buildLabel() {
        // Label with selected type + unit
        int pos = spTypes.getSelectedItemPosition();
        if (pos >= 0 && pos < types.size()) {
            ParameterType t = types.get(pos);
            String unit = t.unit != null ? t.unit : "";
            return getString(R.string.stats_chart_label, t.title, unit);
        }
        return getString(R.string.title_stats);
    }

    static class DateAxisFormatter extends ValueFormatter {
        private final SimpleDateFormat df = new SimpleDateFormat("d MMM", Locale.getDefault());
        @Override public String getAxisLabel(float value, AxisBase axis) {
            long millis = (long) (value * 86400000L);
            return df.format(new Date(millis));
        }
    }
}
