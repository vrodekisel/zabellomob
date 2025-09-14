package com.example.zabello.ui.list;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zabello.R;
import com.example.zabello.data.entity.ParameterEntry;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ParameterEntryAdapter extends ListAdapter<ParameterEntry, ParameterEntryAdapter.VH> {

    private int anomalyCount = 0;

    public ParameterEntryAdapter() {
        super(DIFF);
    }

    public int getCurrentAnomalyCount() { return anomalyCount; }

    private static final DiffUtil.ItemCallback<ParameterEntry> DIFF =
            new DiffUtil.ItemCallback<ParameterEntry>() {
                @Override
                public boolean areItemsTheSame(@NonNull ParameterEntry oldItem, @NonNull ParameterEntry newItem) {
                    return oldItem.id == newItem.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull ParameterEntry oldItem, @NonNull ParameterEntry newItem) {
                    return oldItem.userId == newItem.userId
                            && oldItem.typeId == newItem.typeId
                            && Float.compare(oldItem.value, newItem.value) == 0
                            && ((oldItem.timestamp == null && newItem.timestamp == null)
                            || (oldItem.timestamp != null && newItem.timestamp != null
                            && oldItem.timestamp.getTime() == newItem.timestamp.getTime()))
                            && ((oldItem.note == null && newItem.note == null)
                            || (oldItem.note != null && oldItem.note.equals(newItem.note)));
                }
            };

    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    @Override
    public void submitList(List<ParameterEntry> list) {
        // простой подсчёт "аномалий" — подсветим записи со значением < 0 как ошибочные
        anomalyCount = 0;
        if (list != null) {
            for (ParameterEntry e : list) if (e.value < 0f) anomalyCount++;
        }
        super.submitList(list);
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_parameter_entry, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int position) {
        ParameterEntry e = getItem(position);
        String title = h.itemView.getContext().getString(R.string.item_entry_title, e.typeId, e.value);
        h.tvTitle.setText(title);

        Date ts = e.timestamp != null ? e.timestamp : new Date();
        h.tvSubtitle.setText(df.format(ts));

        // примитивная подсветка "аномалий"
        h.indicator.setVisibility(e.value < 0f ? View.VISIBLE : View.GONE);
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvTitle;
        final TextView tvSubtitle;
        final View indicator;
        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
            indicator = itemView.findViewById(R.id.viewAnomaly);
        }
    }
}
