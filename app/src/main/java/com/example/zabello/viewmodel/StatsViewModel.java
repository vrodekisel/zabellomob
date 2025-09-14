package com.example.zabello.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.zabello.data.entity.ParameterEntry;
import com.example.zabello.data.entity.ParameterType;
import com.example.zabello.domain.session.SessionManager;
import com.example.zabello.repository.HealthRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class StatsViewModel extends AndroidViewModel {

    private final HealthRepository repo;
    private final long userId;

    private final LiveData<List<ParameterType>> types;
    private final MutableLiveData<Long> selectedTypeId = new MutableLiveData<>(-1L);
    private final MutableLiveData<Integer> daysBack = new MutableLiveData<>(7);

    private final MediatorLiveData<List<ParameterEntry>> entries = new MediatorLiveData<>();

    private LiveData<List<ParameterEntry>> sourceCurrent;

    public StatsViewModel(@NonNull Application app) {
        super(app);
        repo = new HealthRepository(app);
        userId = SessionManager.getInstance(app).getUserId();
        types = repo.getAllTypes();

        selectedTypeId.observeForever(id -> reloadSource());
        daysBack.observeForever(d -> apply());
    }

    public LiveData<List<ParameterType>> getTypes() { return types; }
    public LiveData<List<ParameterEntry>> getEntries() { return entries; }

    public void setSelectedType(long typeId) {
        selectedTypeId.setValue(typeId);
    }

    public void setDaysBack(int days) {
        daysBack.setValue(days);
    }

    private void reloadSource() {
        Long typeId = selectedTypeId.getValue();
        if (typeId == null || typeId <= 0 || userId <= 0) {
            entries.setValue(new ArrayList<>());
            return;
        }
        if (sourceCurrent != null) entries.removeSource(sourceCurrent);
        sourceCurrent = repo.getEntriesByType(userId, typeId);
        entries.addSource(sourceCurrent, list -> apply());
    }

    private void apply() {
        if (sourceCurrent == null) { entries.setValue(new ArrayList<>()); return; }
        List<ParameterEntry> base = sourceCurrent.getValue();
        if (base == null) { entries.setValue(new ArrayList<>()); return; }

        Integer db = daysBack.getValue();
        if (db == null || db <= 0) {
            entries.setValue(base);
            return;
        }
        long cutoff = cutoffMillis(db);
        List<ParameterEntry> out = new ArrayList<>();
        for (ParameterEntry e : base) {
            if (e.timestamp != null && e.timestamp.getTime() >= cutoff) out.add(e);
        }
        entries.setValue(out);
    }

    private long cutoffMillis(int days) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, -days);
        return c.getTimeInMillis();
    }
}
