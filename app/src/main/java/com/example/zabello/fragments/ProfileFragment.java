package com.example.zabello.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.zabello.R;
import com.example.zabello.activities.RegisterActivity;
import com.example.zabello.domain.session.SessionManager;
import com.example.zabello.viewmodel.ProfileViewModel;

public class ProfileFragment extends Fragment {

    private TextView tvUserInfo;
    private Button btnLogout;
    private ProfileViewModel viewModel;

    public ProfileFragment() { }

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvUserInfo = view.findViewById(R.id.tvUserInfo);
        btnLogout  = view.findViewById(R.id.btnLogout);

        viewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);

        // Показываем информацию о текущем пользователе (логин / ФИО)
        if (viewModel.getCurrentUser() != null) {
            viewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
                if (user == null) {
                    tvUserInfo.setText("Пользователь не найден");
                } else {
                    String name = (user.fullName != null && !user.fullName.isEmpty())
                            ? user.fullName
                            : user.login;
                    tvUserInfo.setText("Вы вошли как: " + name);
                }
            });
        } else {
            tvUserInfo.setText("Сессия не найдена");
        }

        // Кнопка «Выйти»: чистим сессию и уходим на экран входа/регистрации
        btnLogout.setOnClickListener(v -> {
            SessionManager.getInstance(requireContext()).clear();
            startActivity(new Intent(requireContext(), RegisterActivity.class));
            requireActivity().finish();
        });
    }
}
