package com.example.zabello.repository;

import android.content.Context;

import com.example.zabello.data.dao.UserDao;
import com.example.zabello.data.db.AppDatabase;
import com.example.zabello.data.entity.User;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class HealthRepository {

    public interface Callback<T> { void onResult(T value); }

    private final UserDao userDao;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public HealthRepository(Context context) {
        this.userDao = AppDatabase.getInstance(context).userDao();
    }

    public void isLoginTaken(String login, Callback<Boolean> cb) {
        executor.execute(() -> {
            int count = userDao.countByLoginSync(login);
            cb.onResult(count > 0);
        });
    }

    public void insertUser(User user, Callback<Long> cb) {
        executor.execute(() -> {
            long id = userDao.insert(user);
            cb.onResult(id);
        });
    }

    public void signIn(String login, String passwordHash, Callback<User> cb) {
        executor.execute(() -> {
            User u = userDao.signInSync(login, passwordHash);
            cb.onResult(u);
        });
    }
}
