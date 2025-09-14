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
import com.example.zabello.network.EuropePmcResponse;
import com.example.zabello.network.RemoteArticleMapper;
import com.example.zabello.network.RemoteArticleService;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** Репозиторий: пользователи, параметры, статьи (оффлайн+удалённый поиск), правила, истории болезней/операции. */
public class HealthRepository {

    public interface Callback<T> { void onResult(T value); }

    private final UserDao userDao;
    private final ParameterTypeDao typeDao;
    private final ParameterEntryDao entryDao;
    private final ArticleDao articleDao;
    private final AlertRuleDao ruleDao;
    private final ChronicConditionDao chronicDao;
    private final SurgeryDao surgeryDao;

    private final Executor executor = Executors.newSingleThreadExecutor();

    // --- network (Europe PMC) ---
    private final RemoteArticleService remoteService;

    public HealthRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);

        this.userDao = db.userDao();
        this.typeDao = db.parameterTypeDao();
        this.entryDao = db.parameterEntryDao();
        this.articleDao = db.articleDao();
        this.ruleDao = db.alertRuleDao();
        this.chronicDao = db.chronicConditionDao();
        this.surgeryDao = db.surgeryDao();

        // Retrofit + OkHttp
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.ebi.ac.uk/europepmc/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        remoteService = retrofit.create(RemoteArticleService.class);
    }

    // ---------------- Users ----------------
    public LiveData<User> getUserLive(long id) { return userDao.getById(id); }

    public void insertUser(User user, Callback<Long> cb) {
        executor.execute(() -> {
            long id = userDao.insert(user);
            if (cb != null) cb.onResult(id);
        });
    }

    public void updateUser(User user, Callback<Integer> cb) {
        executor.execute(() -> {
            int n = userDao.update(user);
            if (cb != null) cb.onResult(n);
        });
    }

    public void isLoginTaken(String login, Callback<Boolean> cb) {
        executor.execute(() -> {
            boolean taken = userDao.countByLoginSync(login) > 0;
            if (cb != null) cb.onResult(taken);
        });
    }

    public void signIn(String login, String passwordHash, Callback<User> cb) {
        executor.execute(() -> {
            User u = userDao.signInSync(login, passwordHash);
            if (cb != null) cb.onResult(u);
        });
    }

    // ------------- Parameter types -------------
    public LiveData<List<ParameterType>> getAllTypes() { return typeDao.getAll(); }
    public void insertType(ParameterType type, Callback<Long> cb) {
        executor.execute(() -> {
            long id = typeDao.insert(type);
            if (cb != null) cb.onResult(id);
        });
    }
    public void updateType(ParameterType type, Callback<Integer> cb) {
        executor.execute(() -> {
            int n = typeDao.update(type);
            if (cb != null) cb.onResult(n);
        });
    }
    public void deleteType(long id, Callback<Integer> cb) {
        executor.execute(() -> {
            int n = typeDao.deleteById(id);
            if (cb != null) cb.onResult(n);
        });
    }

    // ------------- Parameter entries -------------
    public LiveData<List<ParameterEntry>> getEntriesByUser(long userId) { return entryDao.getByUser(userId); }
    public LiveData<List<ParameterEntry>> getEntriesByType(long userId, long typeId) {
        return entryDao.getByUserAndType(userId, typeId);
    }
    public void insertEntry(ParameterEntry e, Callback<Long> cb) {
        executor.execute(() -> {
            long id = entryDao.insert(e);
            if (cb != null) cb.onResult(id);
        });
    }
    /** Алиас для совместимости со старыми вызовами UI. */
    public void addEntry(ParameterEntry e, Callback<Long> cb) { insertEntry(e, cb); }
    public void updateEntry(ParameterEntry e, Callback<Integer> cb) {
        executor.execute(() -> {
            int n = entryDao.update(e);
            if (cb != null) cb.onResult(n);
        });
    }
    public void deleteEntry(long id, Callback<Integer> cb) {
        executor.execute(() -> {
            int n = entryDao.deleteById(id);
            if (cb != null) cb.onResult(n);
        });
    }

    // ---------------- Articles (local) ----------------
    public LiveData<List<Article>> getAllArticles() { return articleDao.getAll(); }
    public LiveData<Article> getArticleBySlug(String slug) { return articleDao.getBySlug(slug); }
    public void insertArticle(Article a, Callback<Long> cb) {
        executor.execute(() -> {
            long id = articleDao.insert(a);
            if (cb != null) cb.onResult(id);
        });
    }
    public void updateArticle(Article a, Callback<Integer> cb) {
        executor.execute(() -> {
            int n = articleDao.update(a);
            if (cb != null) cb.onResult(n);
        });
    }
    public void deleteArticle(long id, Callback<Integer> cb) {
        executor.execute(() -> {
            int n = articleDao.deleteById(id);
            if (cb != null) cb.onResult(n);
        });
    }

    // ---------------- Articles (remote search + cache) ----------------
    /** Сетевой поиск в Europe PMC → нормализуем → upsert в Room. */
    public void searchArticlesRemote(String query, Callback<Integer> cb) {
        executor.execute(() -> {
            String q = query != null ? query.trim() : "";
            if (q.length() < 2) { if (cb != null) cb.onResult(0); return; }
            Call<EuropePmcResponse> call = remoteService.search(q, "json", 25, 1);
            try {
                Response<EuropePmcResponse> resp = call.execute();
                if (!resp.isSuccessful() || resp.body() == null) {
                    if (cb != null) cb.onResult(0);
                    return;
                }
                List<Article> mapped = RemoteArticleMapper.toEntities(resp.body());
                if (mapped == null || mapped.isEmpty()) { if (cb != null) cb.onResult(0); return; }
                int n = 0;
                for (Article a : mapped) { articleDao.upsert(a); n++; }
                if (cb != null) cb.onResult(n);
            } catch (IOException e) {
                if (cb != null) cb.onResult(0);
            }
        });
    }

    // ---------------- Alert rules ----------------
    public LiveData<List<AlertRule>> getAlertRules(Long userId) { return ruleDao.getForUser(userId); }

    public void addAlertRule(AlertRule r, Callback<Long> cb) {
        executor.execute(() -> {
            long id = ruleDao.insert(r);
            if (cb != null) cb.onResult(id);
        });
    }

    public void updateAlertRule(AlertRule r, Callback<Integer> cb) {
        executor.execute(() -> {
            int n = ruleDao.update(r);
            if (cb != null) cb.onResult(n);
        });
    }

    public void deleteAlertRule(long id, Callback<Integer> cb) {
        executor.execute(() -> {
            int n = ruleDao.deleteById(id);
            if (cb != null) cb.onResult(n);
        });
    }

    // ---------------- Chronic conditions ----------------
    public LiveData<List<ChronicCondition>> getChronicByUser(long userId) { return chronicDao.getByUser(userId); }
    public void addChronic(ChronicCondition c, Callback<Long> cb) {
        executor.execute(() -> {
            long id = chronicDao.insert(c);
            if (cb != null) cb.onResult(id);
        });
    }
    public void updateChronic(ChronicCondition c, Callback<Integer> cb) {
        executor.execute(() -> {
            int n = chronicDao.update(c);
            if (cb != null) cb.onResult(n);
        });
    }
    public void deleteChronic(long id, Callback<Integer> cb) {
        executor.execute(() -> {
            int n = chronicDao.deleteById(id);
            if (cb != null) cb.onResult(n);
        });
    }

    // ---------------- Surgeries ----------------
    public LiveData<List<Surgery>> getSurgeriesByUser(long userId) { return surgeryDao.getByUser(userId); }
    public void addSurgery(Surgery s, Callback<Long> cb) {
        executor.execute(() -> {
            long id = surgeryDao.insert(s);
            if (cb != null) cb.onResult(id);
        });
    }
    public void updateSurgery(Surgery s, Callback<Integer> cb) {
        executor.execute(() -> {
            int n = surgeryDao.update(s);
            if (cb != null) cb.onResult(n);
        });
    }
    public void deleteSurgery(long id, Callback<Integer> cb) {
        executor.execute(() -> {
            int n = surgeryDao.deleteById(id);
            if (cb != null) cb.onResult(n);
        });
    }
}
