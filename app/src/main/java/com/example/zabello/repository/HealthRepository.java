package com.example.zabello.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.zabello.data.dao.AlertRuleDao;
import com.example.zabello.data.dao.ParameterEntryDao;
import com.example.zabello.data.dao.ParameterTypeDao;
import com.example.zabello.data.dao.UserDao;
import com.example.zabello.data.db.AppDatabase;
import com.example.zabello.data.entity.AlertRule;
import com.example.zabello.data.entity.ParameterEntry;
import com.example.zabello.data.entity.ParameterType;
import com.example.zabello.data.entity.User;
import com.example.zabello.domain.stats.StatsCalculator;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/** Репозиторий для параметров/правил/статистики + методы работы с пользователем (для RegisterActivity). */
public class HealthRepository {

    public interface Callback<T> { void onResult(T value); }

    private final ParameterTypeDao typeDao;
    private final ParameterEntryDao entryDao;
    private final AlertRuleDao ruleDao;
    private final UserDao userDao;

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final StatsCalculator statsCalculator = new StatsCalculator();

    public HealthRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.typeDao = db.parameterTypeDao();
        this.entryDao = db.parameterEntryDao();
        this.ruleDao  = db.alertRuleDao();
        this.userDao  = db.userDao();
    }

    // --- User (для RegisterActivity / Profile / Dashboard) ---
    public void isLoginTaken(String login, Callback<Boolean> cb) {
        executor.execute(() -> cb.onResult(userDao.countByLoginSync(login) > 0));
    }

    public void insertUser(User user, Callback<Long> cb) {
        executor.execute(() -> cb.onResult(userDao.insert(user)));
    }

    public void signIn(String login, String passwordHash, Callback<User> cb) {
        executor.execute(() -> cb.onResult(userDao.signInSync(login, passwordHash)));
    }

    /** NEW: живая подписка на пользователя по id — для ViewModel (Dashboard/Main/Profile). */
    public LiveData<User> getUserLive(long id) {
        return userDao.getById(id);
    }

    /** NEW: обновление профиля пользователя (если используется из ProfileViewModel). */
    public void updateUser(User user, Callback<Integer> cb) {
        executor.execute(() -> cb.onResult(userDao.update(user)));
    }

    // --- Parameter types ---
    public LiveData<List<ParameterType>> getAllTypes() { return typeDao.getAll(); }
    public void createType(ParameterType t, Callback<Long> cb) { executor.execute(() -> cb.onResult(typeDao.insert(t))); }
    public void updateType(ParameterType t, Callback<Integer> cb) { executor.execute(() -> cb.onResult(typeDao.update(t))); }
    public void deleteType(long id, Callback<Integer> cb) { executor.execute(() -> cb.onResult(typeDao.deleteById(id))); }

    // --- Parameter entries ---
    public LiveData<List<ParameterEntry>> getEntries(long userId) { return entryDao.getByUser(userId); }
    public LiveData<List<ParameterEntry>> getEntriesByType(long userId, long typeId) { return entryDao.getByUserAndType(userId, typeId); }
    public void addEntry(ParameterEntry e, Callback<Long> cb) { executor.execute(() -> cb.onResult(entryDao.insert(e))); }
    public void updateEntry(ParameterEntry e, Callback<Integer> cb) { executor.execute(() -> cb.onResult(entryDao.update(e))); }
    public void deleteEntry(long id, Callback<Integer> cb) { executor.execute(() -> cb.onResult(entryDao.deleteById(id))); }

    // --- Alert rules ---
    public LiveData<List<AlertRule>> getRules(Long userId) { return ruleDao.getForUser(userId); }
    public void createRule(AlertRule r, Callback<Long> cb) { executor.execute(() -> cb.onResult(ruleDao.insert(r))); }
    public void updateRule(AlertRule r, Callback<Integer> cb) { executor.execute(() -> cb.onResult(ruleDao.update(r))); }
    public void deleteRule(long id, Callback<Integer> cb) { executor.execute(() -> cb.onResult(ruleDao.deleteById(id))); }

    // --- Stats ---
    // StatsCalculator используем в VM/UseCase при подписке на данные из репозитория.
}
