package com.example.zabello.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.zabello.data.dao.AlertRuleDao;
import com.example.zabello.data.dao.ArticleDao;
import com.example.zabello.data.dao.ChronicConditionDao;
import com.example.zabello.data.dao.ParameterEntryDao;
import com.example.zabello.data.dao.ParameterTypeDao;
import com.example.zabello.data.dao.SurgeryDao;
import com.example.zabello.data.dao.UserDao;
import com.example.zabello.data.db.AppDatabase;
import com.example.zabello.data.entity.AlertRule;
import com.example.zabello.data.entity.Article;
import com.example.zabello.data.entity.ChronicCondition;
import com.example.zabello.data.entity.ParameterEntry;
import com.example.zabello.data.entity.ParameterType;
import com.example.zabello.data.entity.Surgery;
import com.example.zabello.data.entity.User;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class HealthRepository {

    public interface Callback<T> { void onResult(T value); }

    private final UserDao userDao;
    private final ParameterTypeDao typeDao;
    private final ParameterEntryDao entryDao;
    private final AlertRuleDao ruleDao;
    private final ArticleDao articleDao;
    private final ChronicConditionDao chronicDao;
    private final SurgeryDao surgeryDao;

    private final Executor executor = Executors.newSingleThreadExecutor();

    public HealthRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context.getApplicationContext());
        userDao    = db.userDao();
        typeDao    = db.parameterTypeDao();
        entryDao   = db.parameterEntryDao();
        ruleDao    = db.alertRuleDao();
        articleDao = db.articleDao();
        chronicDao = db.chronicConditionDao();
        surgeryDao = db.surgeryDao();
    }

    // ---- Users ----
    public LiveData<User> getUserLive(long id) { return userDao.getById(id); }

    public void insertUser(User u, Callback<Long> cb) {
        executor.execute(() -> cb.onResult(userDao.insert(u)));
    }

    public void updateUser(User u, Callback<Integer> cb) {
        executor.execute(() -> cb.onResult(userDao.update(u)));
    }

    public void isLoginTaken(String login, Callback<Boolean> cb) {
        executor.execute(() -> cb.onResult(userDao.countByLoginSync(login) > 0));
    }

    public void signIn(String login, String passwordHash, Callback<User> cb) {
        executor.execute(() -> cb.onResult(userDao.signInSync(login, passwordHash)));
    }

    // ---- Parameter types ----
    public LiveData<List<ParameterType>> getAllTypes() { return typeDao.getAll(); }

    // ---- Parameter entries ----
    public LiveData<List<ParameterEntry>> getEntriesByUser(long userId) {
        return entryDao.getByUser(userId);
    }

    public LiveData<List<ParameterEntry>> getEntriesByType(long userId, long typeId) {
        return entryDao.getByUserAndType(userId, typeId);
    }

    public void addEntry(ParameterEntry e, Callback<Long> cb) {
        executor.execute(() -> cb.onResult(entryDao.insert(e)));
    }

    public void updateEntry(ParameterEntry e, Callback<Integer> cb) {
        executor.execute(() -> cb.onResult(entryDao.update(e)));
    }

    public void deleteEntry(long id, Callback<Integer> cb) {
        executor.execute(() -> cb.onResult(entryDao.deleteById(id)));
    }

    // ---- Alert rules ----
    public LiveData<List<AlertRule>> getRules(Long userId) { return ruleDao.getForUser(userId); }
    public void createRule(AlertRule r, Callback<Long> cb) { executor.execute(() -> cb.onResult(ruleDao.insert(r))); }
    public void updateRule(AlertRule r, Callback<Integer> cb) { executor.execute(() -> cb.onResult(ruleDao.update(r))); }
    public void deleteRule(long id, Callback<Integer> cb) { executor.execute(() -> cb.onResult(ruleDao.deleteById(id))); }

    // ---- Articles (reference) ----
    public LiveData<List<Article>> getAllArticles() { return articleDao.getAll(); }
    public LiveData<Article> getArticleBySlug(String slug) { return articleDao.getBySlug(slug); }
    public void insertArticle(Article a, Callback<Long> cb) { executor.execute(() -> cb.onResult(articleDao.insert(a))); }
    public void updateArticle(Article a, Callback<Integer> cb) { executor.execute(() -> cb.onResult(articleDao.update(a))); }
    public void deleteArticle(long id, Callback<Integer> cb) { executor.execute(() -> cb.onResult(articleDao.deleteById(id))); }

    // ---- Chronic conditions / Surgeries ----
    public LiveData<List<ChronicCondition>> getChronicByUser(long userId) { return chronicDao.getByUser(userId); }
    public void addChronic(ChronicCondition c, Callback<Long> cb) { executor.execute(() -> cb.onResult(chronicDao.insert(c))); }
    public void updateChronic(ChronicCondition c, Callback<Integer> cb) { executor.execute(() -> cb.onResult(chronicDao.update(c))); }
    public void deleteChronic(long id, Callback<Integer> cb) { executor.execute(() -> cb.onResult(chronicDao.deleteById(id))); }

    public LiveData<List<Surgery>> getSurgeriesByUser(long userId) { return surgeryDao.getByUser(userId); }
    public void addSurgery(Surgery s, Callback<Long> cb) { executor.execute(() -> cb.onResult(surgeryDao.insert(s))); }
    public void updateSurgery(Surgery s, Callback<Integer> cb) { executor.execute(() -> cb.onResult(surgeryDao.update(s))); }
    public void deleteSurgery(long id, Callback<Integer> cb) { executor.execute(() -> cb.onResult(surgeryDao.deleteById(id))); }
}
