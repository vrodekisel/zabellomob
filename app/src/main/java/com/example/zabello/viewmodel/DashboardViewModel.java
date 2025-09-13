package com.example.zabello.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.zabello.data.entity.User;
import com.example.zabello.domain.session.SessionManager;
import com.example.zabello.repository.HealthRepository;

public class DashboardViewModel extends AndroidViewModel {

    private final HealthRepository repo;
    private final SessionManager session;

    private final LiveData<User> currentUser;
    private final LiveData<String> welcomeText;

    public DashboardViewModel(@NonNull Application app) {
        super(app);
        repo = new HealthRepository(app);
        session = SessionManager.getInstance(app);

        long uid = session.getUserId();
        LiveData<User> temp;
        if (uid > 0L) {
            temp = repo.getUserLive(uid);
        } else {
            // если по какой-то причине сессии нет — отдаём null
            MutableLiveData<User> empty = new MutableLiveData<>();
            empty.setValue(null);
            temp = empty;
        }
        currentUser = temp;

        welcomeText = Transformations.map(currentUser, u -> {
            if (u == null) return "Добро пожаловать!";
            String name = (u.fullName != null && !u.fullName.isEmpty()) ? u.fullName : u.login;
            return "Привет, " + name + "!";
        });
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<String> getWelcomeText() {
        return welcomeText;
    }
}
