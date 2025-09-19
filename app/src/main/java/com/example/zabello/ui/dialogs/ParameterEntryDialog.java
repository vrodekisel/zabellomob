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

/** Диалог добавления записи с поддержкой числовых/категориальных/текстовых полей. */
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
    private View groupBp;
    private EditText etBpSys, etBpDia;
    private Spinner spCategory;
    private EditText etText;
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
        etValue = root.findViewById(R.id.etValue);
        groupBp = root.findViewById(R.id.groupBp);
        etBpSys = root.findViewById(R.id.etBpSys);
        etBpDia = root.findViewById(R.id.etBpDia);
        spCategory = root.findViewById(R.id.spCategory);
        etText = root.findViewById(R.id.etText);
        cbSkip = root.findViewById(R.id.cbSkip);

        spinnerAdapter = new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_dropdown_item, new ArrayList<>());
        spTypes.setAdapter(spinnerAdapter);

        DashboardViewModel vm = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);

        // Обеспечим дефолтные типы, если их ещё нет
        new HealthRepository(ctx).ensureDefaultTypes();

        vm.getTypes().observe(this, types -> {
            currentTypes.clear();
            spinnerAdapter.clear();
            if (types != null) {
                currentTypes.addAll(types);
                for (ParameterType t : types) spinnerAdapter.add(t.title);
            }
            spinnerAdapter.notifyDataSetChanged();
            updateInputsVisibility(); // на всякий случай
        });

        spTypes.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                updateInputsVisibility();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        return new AlertDialog.Builder(ctx)
                .setTitle(R.string.dialog_add_entry_title)
                .setView(root)
                .setPositiveButton(R.string.action_save, (d, w) -> save())
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    private void updateInputsVisibility() {
        int pos = spTypes.getSelectedItemPosition();
        if (pos < 0 || pos >= currentTypes.size()) {
            etValue.setVisibility(View.GONE);
            groupBp.setVisibility(View.GONE);
            spCategory.setVisibility(View.GONE);
            etText.setVisibility(View.GONE);
            return;
        }
        ParameterType t = currentTypes.get(pos);
        String code = t.code != null ? t.code : "";

        // Сброс
        etValue.setVisibility(View.GONE);
        groupBp.setVisibility(View.GONE);
        spCategory.setVisibility(View.GONE);
        etText.setVisibility(View.GONE);

        if (code.equals("TEMP") || code.equals("HR") || code.equals("SLEEP") || code.equals("WEIGHT") || code.equals("GLUCOSE")) {
            etValue.setVisibility(View.VISIBLE);
        } else if (code.equals("BP_SYS") || code.equals("BP_DIA")) {
            // Для каждой составляющей давления — одно поле ввода
            etValue.setVisibility(View.VISIBLE);
        } else if (code.equals("MOOD")) {
            spCategory.setVisibility(View.VISIBLE);
            setCategoryAdapter(new String[]{
                    getString(R.string.option_mood_great),
                    getString(R.string.option_mood_good),
                    getString(R.string.option_mood_neutral),
                    getString(R.string.option_mood_bad)
            });
        } else if (code.equals("FOOD")) {
            spCategory.setVisibility(View.VISIBLE);
            setCategoryAdapter(new String[]{
                    getString(R.string.option_food_good),
                    getString(R.string.option_food_under),
                    getString(R.string.option_food_over),
                    getString(R.string.option_food_fastfood)
            });
        } else if (code.equals("ACTIVITY")) {
            spCategory.setVisibility(View.VISIBLE);
            setCategoryAdapter(new String[]{
                    getString(R.string.option_activity_none),
                    getString(R.string.option_activity_light),
                    getString(R.string.option_activity_medium),
                    getString(R.string.option_activity_high)
            });
        } else if (code.equals("WELLBEING")) {
            etText.setVisibility(View.VISIBLE);
        } else {
            // по умолчанию число
            etValue.setVisibility(View.VISIBLE);
        }
    }

    private void setCategoryAdapter(String[] items) {
        ArrayAdapter<String> a = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, items);
        spCategory.setAdapter(a);
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
            if (TextUtils.isEmpty(txt)) {
                Toast.makeText(requireContext(), "Введите текст", Toast.LENGTH_SHORT).show();
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
            if (TextUtils.isEmpty(valueStr)) {
                Toast.makeText(requireContext(), "Введите значение", Toast.LENGTH_SHORT).show();
                return;
            }
            float value;
            try {
                value = Float.parseFloat(valueStr.replace(',', '.'));
            } catch (NumberFormatException ex) {
                Toast.makeText(requireContext(), "Неверный формат", Toast.LENGTH_SHORT).show();
                return;
            }
            ParameterEntry e = new ParameterEntry();
            e.userId = userId;
            e.typeId = type.id;
            e.value = value;
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
