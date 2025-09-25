package com.example.zabello.ui.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.zabello.R;
import com.example.zabello.data.entity.ParameterEntry;
import com.example.zabello.data.entity.ParameterType;
import com.example.zabello.repository.HealthRepository;
import com.example.zabello.utils.ValidationLogic;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class ParameterEntryDialog extends androidx.fragment.app.DialogFragment {

    public interface OnSaved { void onSaved(long id); }

    private static final String ARG_USER_ID = "userId";

    public static ParameterEntryDialog newInstance(long userId, OnSaved cb) {
        ParameterEntryDialog d = new ParameterEntryDialog();
        Bundle b = new Bundle();
        b.putLong(ARG_USER_ID, userId);
        d.setArguments(b);
        d.onSaved = cb;
        return d;
    }

    private OnSaved onSaved;

    private Spinner spTypes;
    private EditText etValue;
    private TextInputLayout tilValue;
    private View groupBp;
    private EditText etBpSys, etBpDia;
    private TextInputLayout tilBpSys, tilBpDia;
    private Spinner spCategory;
    private EditText etText;
    private TextInputLayout tilText;
    private CheckBox cbSkip;

    private ArrayAdapter<String> spinnerAdapter;
    private final List<ParameterType> currentTypes = new ArrayList<>();

    @SuppressLint("MissingInflatedId")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        var ctx = requireContext();
        var root = LayoutInflater.from(ctx).inflate(R.layout.dialog_parameter_entry, null, false);

        spTypes = root.findViewById(R.id.spTypes);
        tilValue = root.findViewById(R.id.tilValue);
        etValue = root.findViewById(R.id.etValue);
        groupBp = root.findViewById(R.id.groupBp);
        tilBpSys = root.findViewById(R.id.tilBpSys);
        etBpSys = root.findViewById(R.id.etBpSys);
        tilBpDia = root.findViewById(R.id.tilBpDia);
        etBpDia = root.findViewById(R.id.etBpDia);
        spCategory = root.findViewById(R.id.spCategory);
        tilText = root.findViewById(R.id.tilText);
        etText = root.findViewById(R.id.etText);
        cbSkip = root.findViewById(R.id.cbSkip);

        spinnerAdapter = new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_dropdown_item, new ArrayList<>());
        spTypes.setAdapter(spinnerAdapter);

        // дальше вся твоя логика подстановки типов/опций (оставил как было)...

        return new MaterialAlertDialogBuilder(ctx)
                .setTitle(R.string.dialog_add_entry_title)
                .setView(root)
                .setPositiveButton(R.string.action_save, (d, w) -> save())
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    private void updateInputsVisibility() {
        int pos = spTypes.getSelectedItemPosition();
        if (pos < 0 || pos >= currentTypes.size()) {
            if (tilValue != null) tilValue.setVisibility(View.GONE);
            groupBp.setVisibility(View.GONE);
            spCategory.setVisibility(View.GONE);
            if (tilText != null) tilText.setVisibility(View.GONE);
            return;
        }
        ParameterType t = currentTypes.get(pos);
        String code = t.code != null ? t.code : "";

        // ... остальная логика видимости как была ...
    }

    private void save() {
        long userId = getArguments() != null ? getArguments().getLong(ARG_USER_ID, 0) : 0;
        if (userId <= 0) {
            Toast.makeText(requireContext(), "Нет пользователя", Toast.LENGTH_SHORT).show();
            return;
        }
        int pos = spTypes.getSelectedItemPosition();
        if (pos < 0 || pos >= currentTypes.size()) {
            Toast.makeText(requireContext(), getString(R.string.msg_select_type), Toast.LENGTH_SHORT).show();
            return;
        }

        if (cbSkip.isChecked()) {
            Toast.makeText(requireContext(), getString(R.string.msg_skipped), Toast.LENGTH_SHORT).show();
            return;
        }

        ParameterType type = currentTypes.get(pos);
        String code = type.code != null ? type.code : "";

        List<ParameterEntry> toInsert = new ArrayList<>();

        if (code.equals("MOOD") || code.equals("FOOD") || code.equals("ACTIVITY")) {
            String txt = (String) spCategory.getSelectedItem();
            if (TextUtils.isEmpty(txt)) {
                Toast.makeText(requireContext(), "Выберите вариант", Toast.LENGTH_SHORT).show();
                return;
            }
            ParameterEntry e = new ParameterEntry();
            e.userId = userId;
            e.typeId = type.id;
            e.value = 0f; // категориальные — не для графиков
            e.note = txt;
            e.timestamp = new java.util.Date();
            toInsert.add(e);
        } else if (code.equals("WELLBEING")) {
            String txt = etText.getText().toString().trim();
            if (tilText != null) tilText.setError(null);
            if (TextUtils.isEmpty(txt)) {
                if (tilText != null) tilText.setError(getString(R.string.error_required));
                return;
            }
            ParameterEntry e = new ParameterEntry();
            e.userId = userId;
            e.typeId = type.id;
            e.value = 0f; // не для статистики
            e.note = txt;
            e.timestamp = new java.util.Date();
            toInsert.add(e);
        } else {
            String valueStr = etValue.getText().toString().trim();
            if (tilValue != null) tilValue.setError(null);
            Float value = ValidationLogic.tryParseFloat(valueStr);
            String err = ValidationLogic.validateValueForType(type, value,
                    getString(R.string.error_required),
                    getString(R.string.error_number),
                    getString(R.string.error_range));
            if (err != null) {
                if (tilValue != null) tilValue.setError(err);
                return;
            }
            ParameterEntry e = new ParameterEntry();
            e.userId = userId;
            e.typeId = type.id;
            e.value = value != null ? value.floatValue() : 0f;
            e.timestamp = new java.util.Date();
            toInsert.add(e);
        }

        HealthRepository repo = new HealthRepository(requireContext());
        if (toInsert.isEmpty()) return;
        final int[] left = {toInsert.size()};
        for (ParameterEntry e : toInsert) {
            repo.addEntry(e, id -> {
                left[0]--;
                if (left[0] == 0 && onSaved != null) onSaved.onSaved(id);
            });
        }
        Toast.makeText(requireContext(), getString(R.string.msg_saved), Toast.LENGTH_SHORT).show();
    }
}
