package com.example.zabello.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.zabello.data.entity.Article;
import com.example.zabello.repository.HealthRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReferenceViewModel extends AndroidViewModel {

    private final HealthRepository repo;
    private final LiveData<List<Article>> all;
    private final MutableLiveData<String> query = new MutableLiveData<>("");
    private final MediatorLiveData<List<Article>> filtered = new MediatorLiveData<>();

    public ReferenceViewModel(@NonNull Application app) {
        super(app);
        repo = new HealthRepository(app);

        all = repo.getAllArticles();

        filtered.addSource(all, a -> apply());
        filtered.addSource(query, s -> apply());
    }

    private void apply() {
        List<Article> base = all.getValue();
        String q = query.getValue();
        if (base == null) { filtered.setValue(new ArrayList<>()); return; }
        if (q == null || q.trim().isEmpty()) { filtered.setValue(base); return; }
        String qq = q.toLowerCase(Locale.getDefault());
        List<Article> out = new ArrayList<>();
        for (Article a : base) {
            String inTitle = a.title != null ? a.title.toLowerCase(Locale.getDefault()) : "";
            String inTags  = a.tags  != null ? a.tags.toLowerCase(Locale.getDefault())  : "";
            if (inTitle.contains(qq) || inTags.contains(qq)) out.add(a);
        }
        filtered.setValue(out);
    }

    public LiveData<List<Article>> getArticles() { return filtered; }
    public void setQuery(String q) { query.setValue(q); }
}
