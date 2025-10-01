package com.example.zabello.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.zabello.R;
import com.example.zabello.data.entity.ParameterEntry;
import com.example.zabello.data.entity.ParameterType;
import com.example.zabello.data.entity.User;
import com.example.zabello.domain.session.SessionManager;
import com.example.zabello.repository.HealthRepository;

import java.util.ArrayList;
import java.util.List;

public class DashboardViewModel extends AndroidViewModel {

    private final HealthRepository repo;
    private final SessionManager session;

    private final LiveData<User> currentUser;
    private final LiveData<String> welcomeText;

    private final LiveData<List<ParameterType>> types;
    private final LiveData<List<ParameterEntry>> allEntries;
    private final MediatorLiveData<List<ParameterEntry>> latestEntries = new MediatorLiveData<>();

    public DashboardViewModel(@NonNull Application app) {
        super(app);
        repo = new HealthRepository(app);
        session = SessionManager.getInstance(app);

        long uid = session.getUserId();

        // Больше НИКОГДА не отдаём null как источник LiveData — используем заглушку
        MutableLiveData<User> emptyUser = new MutableLiveData<>(null);
        currentUser = uid > 0 ? repo.getUserLive(uid) : emptyUser;

        welcomeText = Transformations.map(currentUser, u -> {
            if (u == null) {
                return getApplication().getString(R.string.login_success); // "Welcome"
            }
            String name = (u.fullName != null && !u.fullName.isEmpty()) ? u.fullName : u.login;
            return getApplication().getString(R.string.welcome_with_name, name); // "Hello, name!"
        });

        types = repo.getAllTypes();
        allEntries = uid > 0 ? repo.getEntriesByUser(uid) : new MutableLiveData<>(new ArrayList<>());

        latestEntries.addSource(allEntries, list -> latestEntries.setValue(cutTop(list, 10)));
    }

    private List<ParameterEntry> cutTop(List<ParameterEntry> list, int n) {
        if (list == null) return new ArrayList<>();
        return list.size() <= n ? list : list.subList(0, n);
    }

    public LiveData<User> getCurrentUser() { return currentUser; }
    public LiveData<String> getWelcomeText() { return welcomeText; }
    public LiveData<List<ParameterType>> getTypes() { return types; }
    public LiveData<List<ParameterEntry>> getLatestEntries() { return latestEntries; }

    public void addEntry(ParameterEntry e, HealthRepository.Callback<Long> cb) {
        repo.addEntry(e, cb);
    }
}
