package com.example.zabello.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.zabello.data.entity.User;
import com.example.zabello.domain.session.SessionManager;
import com.example.zabello.repository.HealthRepository;

public class MainViewModel extends AndroidViewModel {

    private final HealthRepository repo;
    private final SessionManager session;
    private LiveData<User> currentUser;

    public MainViewModel(@NonNull Application app) {
        super(app);
        repo = new HealthRepository(app);
        session = SessionManager.getInstance(app);
    }

    public LiveData<User> getCurrentUser() {
        if (currentUser == null) {
            long uid = session.getUserId();
            if (uid > 0L) {
                currentUser = repo.getUserLive(uid);
            }
        }
        return currentUser;
    }

    public void logout() {
        session.clear();
    }
}
