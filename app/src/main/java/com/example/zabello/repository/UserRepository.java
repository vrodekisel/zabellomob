package com.example.zabello.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.zabello.data.dao.UserDao;
import com.example.zabello.data.db.AppDatabase;
import com.example.zabello.data.entity.User;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class UserRepository {

    private final UserDao userDao;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public interface Callback<T> { void onResult(T value); }

    public UserRepository(Context context) {
        this.userDao = AppDatabase.getInstance(context).userDao();
    }

    public LiveData<List<User>> getAll() { return userDao.getAll(); }

    public LiveData<User> getById(long id) { return userDao.getById(id); }

    public void insert(User user, Callback<Long> cb) {
        executor.execute(() -> cb.onResult(userDao.insert(user)));
    }

    public void update(User user, Callback<Integer> cb) {
        executor.execute(() -> cb.onResult(userDao.update(user)));
    }

    public void deleteById(long id, Callback<Integer> cb) {
        executor.execute(() -> cb.onResult(userDao.deleteById(id)));
    }

    public void signIn(String login, String passwordHash, Callback<User> cb) {
        executor.execute(() -> cb.onResult(userDao.signInSync(login, passwordHash)));
    }

    public void isLoginFree(String login, Callback<Boolean> cb) {
        executor.execute(() -> cb.onResult(userDao.countByLoginSync(login) == 0));
    }
}
