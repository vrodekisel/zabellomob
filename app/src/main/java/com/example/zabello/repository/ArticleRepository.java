package com.example.zabello.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.zabello.data.dao.ArticleDao;
import com.example.zabello.data.db.AppDatabase;
import com.example.zabello.data.entity.Article;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ArticleRepository {

    public interface Callback<T> { void onResult(T value); }

    private final ArticleDao articleDao;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public ArticleRepository(Context context) {
        this.articleDao = AppDatabase.getInstance(context).articleDao();
    }

    public LiveData<List<Article>> getAll() { return articleDao.getAll(); }
    public LiveData<Article> getBySlug(String slug) { return articleDao.getBySlug(slug); }

    public void insert(Article a, Callback<Long> cb) { executor.execute(() -> cb.onResult(articleDao.insert(a))); }
    public void update(Article a, Callback<Integer> cb) { executor.execute(() -> cb.onResult(articleDao.update(a))); }
    public void deleteById(long id, Callback<Integer> cb) { executor.execute(() -> cb.onResult(articleDao.deleteById(id))); }
}
