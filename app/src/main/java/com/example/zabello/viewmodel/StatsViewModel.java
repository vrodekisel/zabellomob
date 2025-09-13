package com.example.zabello.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.zabello.repository.HealthRepository;

public class StatsViewModel extends AndroidViewModel {

    private final HealthRepository repo;

    public StatsViewModel(@NonNull Application app) {
        super(app);
        repo = new HealthRepository(app);
    }

    // сюда добавим LiveData по показателям/агрегациям, когда опишем DAO для измерений
}
