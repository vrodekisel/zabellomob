package com.example.zabello.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.zabello.R;
import com.example.zabello.data.entity.ParameterEntry;
import com.example.zabello.data.entity.ParameterType;
import com.example.zabello.repository.HealthRepository;
import com.example.zabello.viewmodel.DashboardViewModel;

import java.util.ArrayList;
import java.util.List;

public class ParameterEntryDialog extends DialogFragment {

    public interface OnSaved { void onSaved(long id); }

    private static final String ARG_USER_ID = "arg_user_id";
    private OnSaved onSaved;

    public static void show(@NonNull androidx.fragment.app.FragmentManager fm,
                            long userId,
                            @Nullable OnSaved cb) {
        ParameterEntryDialog d = new ParameterEntryDialog();
        Bundle b = new Bundle();
        b.putLong(ARG_USER_ID, userId);
        d.setArguments(b);
        d.onSaved = cb;
        d.show(fm, "ParameterEntryDialog");
    }

    private Spinner spTypes;
    private EditText etValue;
    private ArrayAdapter<String> spinnerAdapter;
    private final List<ParameterType> currentTypes = new ArrayList<>();

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        var ctx = requireContext();
        var root = LayoutInflater.from(ctx).inflate(R.layout.dialog_parameter_entry, null, false);

        spTypes = root.findViewById(R.id.spTypes);
        etValue = root.findViewById(R.id.etValue);

        spinnerAdapter = new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_dropdown_item, new ArrayList<>());
        spTypes.setAdapter(spinnerAdapter);

        DashboardViewModel vm = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);
        vm.getTypes().observe(this, types -> {
            currentTypes.clear();
            if (types != null) currentTypes.addAll(types);
            List<String> titles = new ArrayList<>();
            for (ParameterType t : currentTypes) titles.add(t.title + (t.unit != null ? " (" + t.unit + ")" : ""));
            spinnerAdapter.clear();
            spinnerAdapter.addAll(titles);
            spinnerAdapter.notifyDataSetChanged();
        });

        return new AlertDialog.Builder(ctx)
                .setTitle(R.string.dialog_add_entry_title)
                .setView(root)
                .setPositiveButton(R.string.action_save, (d, w) -> save())
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    private void save() {
        long userId = getArguments() != null ? getArguments().getLong(ARG_USER_ID, 0) : 0;
        if (userId <= 0) {
            Toast.makeText(requireContext(), "Нет пользователя", Toast.LENGTH_SHORT).show();
            return;
        }
        int pos = spTypes.getSelectedItemPosition();
        if (pos < 0 || pos >= currentTypes.size()) {
            Toast.makeText(requireContext(), "Не выбран тип", Toast.LENGTH_SHORT).show();
            return;
        }
        String valueStr = etValue.getText().toString().trim();
        if (TextUtils.isEmpty(valueStr)) {
            Toast.makeText(requireContext(), "Введите значение", Toast.LENGTH_SHORT).show();
            return;
        }
        float value;
        try {
            value = Float.parseFloat(valueStr.replace(',', '.'));
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Неверное число", Toast.LENGTH_SHORT).show();
            return;
        }

        ParameterType type = currentTypes.get(pos);
        ParameterEntry entry = new ParameterEntry();
        entry.userId = userId;
        entry.typeId = type.id;
        entry.value = value;
        entry.timestamp = new java.util.Date();

        new HealthRepository(requireContext()).addEntry(entry, id -> {
            if (onSaved != null) onSaved.onSaved(id);
        });
    }
}
