package com.example.zabello.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.zabello.data.dao.UserDao;
import com.example.zabello.data.db.AppDatabase;
import com.example.zabello.data.entity.User;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class HealthRepository {

    public interface Callback<T> { void onResult(T value); }

    private final UserDao userDao;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public HealthRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        userDao = db.userDao();
    }

    // ---- User: LiveData и операции ----

    public LiveData<List<User>> getAllUsers() {
        return userDao.getAll();
    }

    public LiveData<User> getUserLive(long id) {
        return userDao.getById(id);
    }

    public void isLoginTaken(String login, Callback<Boolean> cb) {
        executor.execute(() -> {
            boolean taken = userDao.countByLoginSync(login) > 0;
            cb.onResult(taken);
        });
    }

    public void insertUser(User user, Callback<Long> cb) {
        executor.execute(() -> {
            long id = userDao.insert(user);
            cb.onResult(id);
        });
    }

    public void updateUser(User user, Callback<Integer> cb) {
        executor.execute(() -> {
            int rows = userDao.update(user);
            cb.onResult(rows);
        });
    }

    public void deleteUserById(long id, Callback<Integer> cb) {
        executor.execute(() -> {
            int rows = userDao.deleteById(id);
            cb.onResult(rows);
        });
    }

    public void signIn(String login, String passwordHash, Callback<User> cb) {
        executor.execute(() -> {
            User u = userDao.signInSync(login, passwordHash);
            cb.onResult(u);
        });
    }
}
