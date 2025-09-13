package com.example.zabello.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.zabello.repository.HealthRepository;

public class ReferenceViewModel extends AndroidViewModel {

    private final HealthRepository repo;

    public ReferenceViewModel(@NonNull Application app) {
        super(app);
        repo = new HealthRepository(app);
    }

    // сюда добавим LiveData по статьям/категориям, когда опишем DAO для справочника
}
