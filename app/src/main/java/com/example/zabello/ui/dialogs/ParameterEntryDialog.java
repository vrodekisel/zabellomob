package com.example.zabello.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;

import com.example.zabello.R;
import com.example.zabello.data.entity.ParameterEntry;
import com.example.zabello.data.entity.ParameterType;
import com.example.zabello.repository.HealthRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Диалог "Новая запись":
 * 1) выбор типа показателя (Spinner)
 * 2) ввод значения (число) ИЛИ выбор категории (Spinner) для категориальных типов
 */
public class ParameterEntryDialog extends DialogFragment {

    private static final String ARG_USER_ID = "arg.userId";
    private static final String TAG = "ParameterEntryDialog";

    public interface OnSavedListener { void onSaved(@Nullable ParameterEntry saved); }
    private OnSavedListener onSavedListener;

    public static void show(@NonNull FragmentManager fm, long userId, @NonNull OnSavedListener cb) {
        ParameterEntryDialog d = new ParameterEntryDialog();
        Bundle b = new Bundle();
        b.putLong(ARG_USER_ID, userId);
        d.setArguments(b);
        d.setOnSavedListener(cb);
        d.show(fm, TAG);
    }

    public void setOnSavedListener(@NonNull OnSavedListener cb) { this.onSavedListener = cb; }

    // UI
    private Spinner spType;
    private TextView tvUnit;
    private EditText etValue;
    private Spinner spCategory;

    // data
    private HealthRepository repo;
    private final List<ParameterType> types = new ArrayList<>();
    private ArrayAdapter<String> typesAdapter;
    private ArrayAdapter<String> categoryAdapter;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        repo = new HealthRepository(requireContext());

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * requireContext().getResources().getDisplayMetrics().density);
        root.setPadding(pad, pad, pad, 0);

        // --- Type spinner
        spType = new Spinner(requireContext());
        typesAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, new ArrayList<>());
        spType.setAdapter(typesAdapter);
        root.addView(spType);

        // --- Unit / value
        LinearLayout valueRow = new LinearLayout(requireContext());
        valueRow.setOrientation(LinearLayout.HORIZONTAL);
        valueRow.setPadding(0, pad / 2, 0, 0);

        etValue = new EditText(requireContext());
        etValue.setHint(R.string.enter_value);
        etValue.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etValue.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        tvUnit = new TextView(requireContext());
        tvUnit.setText("");
        tvUnit.setPadding(pad, 0, 0, 0);

        valueRow.addView(etValue);
        valueRow.addView(tvUnit);
        root.addView(valueRow);

        // --- Category spinner (для MOOD / FOOD / ACTIVITY)
        spCategory = new Spinner(requireContext());
        categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, new ArrayList<>());
        spCategory.setAdapter(categoryAdapter);
        spCategory.setVisibility(View.GONE);
        root.addView(spCategory);

        // observe types
        repo.getAllTypes().observe(this, new Observer<List<ParameterType>>() {
            @Override public void onChanged(List<ParameterType> list) {
                types.clear();
                if (list != null) types.addAll(list);
                List<String> titles = new ArrayList<>();
                for (ParameterType t : types) titles.add(t.title);
                typesAdapter.clear();
                typesAdapter.addAll(titles);
                typesAdapter.notifyDataSetChanged();

                // выберем первый доступный тип
                if (!types.isEmpty()) spType.setSelection(0);
                updateInputsForSelectedType();
            }
        });

        spType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                updateInputsForSelectedType();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        AlertDialog.Builder b = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.add_record_title)
                .setView(root)
                .setNegativeButton(android.R.string.cancel, (d, w) -> d.dismiss())
                .setPositiveButton(R.string.save, (d, w) -> onSave());

        return b.create();
    }

    private void updateInputsForSelectedType() {
        int pos = spType.getSelectedItemPosition();
        if (pos < 0 || pos >= types.size()) {
            etValue.setVisibility(View.VISIBLE);
            tvUnit.setText("");
            spCategory.setVisibility(View.GONE);
            return;
        }
        ParameterType t = types.get(pos);

        String code = safe(t.code);
        String unit = safe(t.unit);
        tvUnit.setText(unit);

        // Категориальные коды → показываем спиннер
        if ("MOOD".equalsIgnoreCase(code)) {
            showCategory(new String[]{"Плохое", "Ниже среднего", "Нормальное", "Хорошее", "Отличное"});
        } else if ("FOOD".equalsIgnoreCase(code)) {
            showCategory(new String[]{"Пропуск", "Лёгкий приём", "Обычный", "Плотный"});
        } else if ("ACTIVITY".equalsIgnoreCase(code)) {
            showCategory(new String[]{"Не занимался", "Лёгкая", "Средняя", "Высокая"});
        } else if ("WELLBEING".equalsIgnoreCase(code)) {
            // текстовая заметка — оставляем EditText, убираем категорию
            hideCategory();
            etValue.setHint(R.string.enter_note_short);
            etValue.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        } else {
            // числовой тип
            hideCategory();
            etValue.setHint(R.string.enter_value);
            etValue.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        }
    }

    private void showCategory(String[] items) {
        etValue.setVisibility(View.GONE);
        spCategory.setVisibility(View.VISIBLE);
        categoryAdapter.clear();
        for (String s : items) categoryAdapter.add(s);
        categoryAdapter.notifyDataSetChanged();
        if (items.length > 0) spCategory.setSelection(0);
    }

    private void hideCategory() {
        etValue.setVisibility(View.VISIBLE);
        spCategory.setVisibility(View.GONE);
        categoryAdapter.clear();
        categoryAdapter.notifyDataSetChanged();
    }

    private void onSave() {
        int pos = spType.getSelectedItemPosition();
        if (pos < 0 || pos >= types.size()) {
            if (onSavedListener != null) onSavedListener.onSaved(null);
            return;
        }
        long userId = getArguments() != null ? getArguments().getLong(ARG_USER_ID, -1L) : -1L;
        if (userId <= 0L) {
            if (onSavedListener != null) onSavedListener.onSaved(null);
            return;
        }
        ParameterType t = types.get(pos);

        ParameterEntry pe = new ParameterEntry();
        pe.userId = userId;
        pe.typeId = t.id;
        pe.timestamp = new Date();

        String code = safe(t.code);

        if (spCategory.getVisibility() == View.VISIBLE) {
            // категориальный — сохраняем индекс выбранного пункта как value
            int idx = spCategory.getSelectedItemPosition();
            if (idx < 0) idx = 0;
            pe.value = (float) idx; // 0..N
        } else {
            if ("WELLBEING".equalsIgnoreCase(code)) {
                // текстовая заметка — value можно оставить 0
                String txt = etValue.getText() != null ? etValue.getText().toString().trim() : "";
                if (TextUtils.isEmpty(txt)) {
                    // пустую заметку не сохраняем
                    if (onSavedListener != null) onSavedListener.onSaved(null);
                    return;
                }
                // Если в сущности есть поле note — раскомментируй:
                // pe.note = txt;
                pe.value = 0f;
            } else {
                // числовое значение
                try {
                    String text = etValue.getText() != null ? etValue.getText().toString().trim() : "";
                    pe.value = text.isEmpty() ? 0f : Float.parseFloat(text.replace(',', '.'));
                } catch (Exception e) {
                    pe.value = 0f;
                }
            }
        }

        if (onSavedListener != null) onSavedListener.onSaved(pe);
    }

    private static String safe(String s) { return s == null ? "" : s; }
}
