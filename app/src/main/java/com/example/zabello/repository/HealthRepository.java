package com.example.zabello.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

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
import com.example.zabello.network.RemoteArticleService;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** Репозиторий: пользователи, параметры, статьи (оффлайн + удалённый поиск), правила, истории болезней/операции. */
public class HealthRepository {

    public interface Callback<T> { void onResult(T value); }

    private final UserDao userDao;
    private final ParameterTypeDao typeDao;
    private final ParameterEntryDao entryDao;
    private final ArticleDao articleDao;
    private final AlertRuleDao ruleDao;
    private final ChronicConditionDao chronicDao;
    private final SurgeryDao surgeryDao;

    private final Executor io = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

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

    /** Идемпотентный сидинг типов показателей. */
    public void ensureDefaultTypes() {
        io.execute(() -> {
            ensureType("TEMP", "Температура", "°C", 34.0f, 42.0f, null);
            ensureType("BP_SYS", "Давление (сист.)", "мм рт. ст.", 80f, 200f, null);
            ensureType("BP_DIA", "Давление (диаст.)", "мм рт. ст.", 40f, 130f, null);
            ensureType("HR", "Пульс", "уд/мин", 30f, 220f, null);
            ensureType("SLEEP", "Сон", "часы", 0f, 24f, null);
            ensureType("MOOD", "Настроение", null, null, null, "Категориальный параметр");
            ensureType("FOOD", "Питание", null, null, null, "Категориальный параметр");
            ensureType("WELLBEING", "Общее самочувствие", null, null, null, "Текстовая заметка");
            ensureType("WEIGHT", "Вес", "кг", 0f, 500f, null);
            ensureType("GLUCOSE", "Глюкоза", "ммоль/л", 0f, 30f, null);
            ensureType("ACTIVITY", "Активность", null, null, null, "Категориальный параметр");
        });
    }

    private void ensureType(String code, String title, String unit, Float min, Float max, String desc) {
        ParameterType exists = typeDao.findByCodeSync(code);
        if (exists == null) {
            ParameterType t = new ParameterType();
            t.code = code;
            t.title = title;
            t.unit = unit;
            t.minNormal = min;
            t.maxNormal = max;
            t.description = desc;
            typeDao.insert(t);
        }
    }

    // ---------------- Users ----------------
    public LiveData<User> getUserLive(long id) { return userDao.getById(id); }

    public void insertUser(User user, Callback<Long> cb) {
        io.execute(() -> {
            long id = userDao.insert(user);
            if (cb != null) main.post(() -> cb.onResult(id));
        });
    }
    public void updateUser(User user, Callback<Integer> cb) {
        io.execute(() -> {
            int n = userDao.update(user);
            if (cb != null) main.post(() -> cb.onResult(n));
        });
    }
    public void isLoginTaken(String login, Callback<Boolean> cb) {
        io.execute(() -> {
            boolean taken = userDao.countByLoginSync(login) > 0;
            if (cb != null) main.post(() -> cb.onResult(taken));
        });
    }
    public void signIn(String login, String passwordHash, Callback<User> cb) {
        io.execute(() -> {
            User u = userDao.signInSync(login, passwordHash);
            if (cb != null) main.post(() -> cb.onResult(u));
        });
    }

    // ------------- Parameter types -------------
    public LiveData<List<ParameterType>> getAllTypes() { return typeDao.getAll(); }
    public void insertType(ParameterType type, Callback<Long> cb) {
        io.execute(() -> {
            long id = typeDao.insert(type);
            if (cb != null) main.post(() -> cb.onResult(id));
        });
    }
    public void updateType(ParameterType type, Callback<Integer> cb) {
        io.execute(() -> {
            int n = typeDao.update(type);
            if (cb != null) main.post(() -> cb.onResult(n));
        });
    }
    public void deleteType(long id, Callback<Integer> cb) {
        io.execute(() -> {
            int n = typeDao.deleteById(id);
            if (cb != null) main.post(() -> cb.onResult(n));
        });
    }

    // ------------- Parameter entries -------------
    public LiveData<List<ParameterEntry>> getEntriesByUser(long userId) { return entryDao.getByUser(userId); }
    public LiveData<List<ParameterEntry>> getEntriesByType(long userId, long typeId) {
        return entryDao.getByUserAndType(userId, typeId);
    }
    public void insertEntry(ParameterEntry e, Callback<Long> cb) {
        io.execute(() -> {
            long id = entryDao.insert(e);
            if (cb != null) main.post(() -> cb.onResult(id));
        });
    }
    /** Алиас для совместимости. */
    public void addEntry(ParameterEntry e, Callback<Long> cb) { insertEntry(e, cb); }
    public void updateEntry(ParameterEntry e, Callback<Integer> cb) {
        io.execute(() -> {
            int n = entryDao.update(e);
            if (cb != null) main.post(() -> cb.onResult(n));
        });
    }
    public void deleteEntry(long id, Callback<Integer> cb) {
        io.execute(() -> {
            int n = entryDao.deleteById(id);
            if (cb != null) main.post(() -> cb.onResult(n));
        });
    }

    // ---------------- Articles (local) ----------------
    public LiveData<List<Article>> getAllArticles() { return articleDao.getAll(); }
    public LiveData<Article> getArticleBySlug(String slug) { return articleDao.getBySlug(slug); }
    public void insertArticle(Article a, Callback<Long> cb) {
        io.execute(() -> {
            long id = articleDao.insert(a);
            if (cb != null) main.post(() -> cb.onResult(id));
        });
    }
    public void updateArticle(Article a, Callback<Integer> cb) {
        io.execute(() -> {
            int n = articleDao.update(a);
            if (cb != null) main.post(() -> cb.onResult(n));
        });
    }
    public void deleteArticle(long id, Callback<Integer> cb) {
        io.execute(() -> {
            int n = articleDao.deleteById(id);
            if (cb != null) main.post(() -> cb.onResult(n));
        });
    }

    // ---------------- Remote articles (stage 3.5) ----------------
    public void searchArticlesRemote(String query, Callback<Integer> cb) {
        io.execute(() -> {
            // Заглушка: сеть не дёргаем; как подключим – сделаем upsert и вернём count
            if (cb != null) main.post(() -> cb.onResult(0));
        });
    }

    public RemoteArticleService getRemoteService() { return remoteService; }
}
