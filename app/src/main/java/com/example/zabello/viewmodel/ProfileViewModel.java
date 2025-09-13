package com.example.zabello.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.zabello.data.entity.User;
import com.example.zabello.domain.session.SessionManager;
import com.example.zabello.repository.HealthRepository;

public class ProfileViewModel extends AndroidViewModel {

    private final HealthRepository repo;
    private final SessionManager session;

    public ProfileViewModel(@NonNull Application app) {
        super(app);
        repo = new HealthRepository(app);
        session = SessionManager.getInstance(app);
    }

    public LiveData<User> getCurrentUser() {
        long uid = session.getUserId();
        return uid > 0L ? repo.getUserLive(uid) : null;
    }

    public void updateUser(User user, HealthRepository.Callback<Integer> cb) {
        repo.updateUser(user, cb);
    }
}
