package com.example.zabello.fragments;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.zabello.R;
import com.example.zabello.activities.RegisterActivity;
import com.example.zabello.data.entity.Gender;
import com.example.zabello.data.entity.User;
import com.example.zabello.domain.session.SessionManager;
import com.example.zabello.viewmodel.ProfileViewModel;

import java.util.Calendar;
import java.util.Date;

public class ProfileFragment extends Fragment {

    private ProfileViewModel vm;

    private TextView tvTitle;
    private EditText etFullName, etHeight, etWeight;
    private Spinner spGender;
    private TextView tvBirthDate;
    private Button btnPickDate, btnSave, btnLogout;

    private User current;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        tvTitle = v.findViewById(R.id.tvUserTitle);
        etFullName = v.findViewById(R.id.etFullName);
        spGender = v.findViewById(R.id.spGender);
        tvBirthDate = v.findViewById(R.id.tvBirthDate);
        btnPickDate = v.findViewById(R.id.btnPickDate);
        etHeight = v.findViewById(R.id.etHeight);
        etWeight = v.findViewById(R.id.etWeight);
        btnSave = v.findViewById(R.id.btnSave);
        btnLogout = v.findViewById(R.id.btnLogout);

        vm = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);

        ArrayAdapter<Gender> genderAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, Gender.values());
        spGender.setAdapter(genderAdapter);

        vm.getCurrentUser().observe(getViewLifecycleOwner(), u -> {
            current = u;
            if (u == null) {
                tvTitle.setText(R.string.title_profile);
                return;
            }
            String name = (u.fullName != null && !u.fullName.isEmpty()) ? u.fullName : u.login;
            tvTitle.setText(getString(R.string.profile_hello, name));

            etFullName.setText(u.fullName != null ? u.fullName : "");
            spGender.setSelection(u.gender != null ? u.gender.ordinal() : Gender.OTHER.ordinal());
            if (u.birthDate != null) {
                java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("dd.MM.yyyy");
                tvBirthDate.setText(df.format(u.birthDate));
                tvBirthDate.setTag(u.birthDate);
            } else {
                tvBirthDate.setText(R.string.profile_no_birthdate);
                tvBirthDate.setTag(null);
            }
            etHeight.setText(u.heightCm != null ? String.valueOf(u.heightCm) : "");
            etWeight.setText(u.weightKg != null ? String.valueOf(u.weightKg) : "");
        });

        btnPickDate.setOnClickListener(v1 -> showDatePicker());
        btnSave.setOnClickListener(v12 -> saveUser());
        btnLogout.setOnClickListener(v13 -> {
            SessionManager.getInstance(requireContext()).clear();
            startActivity(new Intent(requireContext(), RegisterActivity.class));
            requireActivity().finish();
        });
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        Date existing = (Date) tvBirthDate.getTag();
        if (existing != null) c.setTime(existing);
        DatePickerDialog dlg = new DatePickerDialog(requireContext(),
                (DatePicker dp, int y, int m, int d) -> {
                    Calendar cc = Calendar.getInstance();
                    cc.set(y, m, d, 0, 0, 0);
                    Date chosen = cc.getTime();
                    java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("dd.MM.yyyy");
                    tvBirthDate.setText(df.format(chosen));
                    tvBirthDate.setTag(chosen);
                },
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dlg.show();
    }

    private void saveUser() {
        if (current == null) return;
        current.fullName = etFullName.getText().toString().trim();
        current.gender = (Gender) spGender.getSelectedItem();
        current.birthDate = (Date) tvBirthDate.getTag();
        try {
            String h = etHeight.getText().toString().trim();
            current.heightCm = h.isEmpty() ? null : Float.parseFloat(h.replace(',', '.'));
        } catch (NumberFormatException ignored) {}
        try {
            String w = etWeight.getText().toString().trim();
            current.weightKg = w.isEmpty() ? null : Float.parseFloat(w.replace(',', '.'));
        } catch (NumberFormatException ignored) {}

        vm.updateUser(current, rows -> {
            // можно показать тост; упрощаем без
        });
    }
}
