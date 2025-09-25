package com.example.zabello.domain.alerts;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.zabello.R;
import com.example.zabello.data.dao.AlertRuleDao;
import com.example.zabello.data.dao.ParameterEntryDao;
import com.example.zabello.data.dao.ParameterTypeDao;
import com.example.zabello.data.db.AppDatabase;
import com.example.zabello.data.entity.AlertRule;
import com.example.zabello.data.entity.ParameterEntry;
import com.example.zabello.data.entity.ParameterType;
import com.example.zabello.domain.session.SessionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/** Периодический воркер: проверяет последние значения по типам и шлёт уведомления об аномалиях. */
public class AnomalyCheckWorker extends Worker {

    public AnomalyCheckWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context ctx = getApplicationContext();
        SessionManager sm = SessionManager.getInstance(ctx);
        if (!sm.isLoggedIn()) return Result.success();

        long userId = sm.getUserId();

        AppDatabase db = AppDatabase.getInstance(ctx);
        ParameterEntryDao entryDao = db.parameterEntryDao();
        ParameterTypeDao typeDao = db.parameterTypeDao();
        AlertRuleDao ruleDao = db.alertRuleDao();

        // Берём последние записи пользователя (до 200), сгруппируем по типу -> последнее значение на тип
        List<ParameterEntry> recent = entryDao.getByUserSync(userId, 200);
        if (recent == null || recent.isEmpty()) return Result.success();

        List<AlertRule> rules = ruleDao.getForUserSync(userId);

        HashMap<Long, ParameterEntry> lastPerType = new HashMap<>();
        for (ParameterEntry e : recent) {
            if (!lastPerType.containsKey(e.typeId)) {
                lastPerType.put(e.typeId, e);
            }
        }

        for (ParameterEntry e : lastPerType.values()) {
            ParameterType type = typeDao.getByIdSync(e.typeId);
            if (type == null) continue;

            ThresholdEvaluator.Result r = ThresholdEvaluator.evaluate(e.value, type, rules);
            if (r.anomalous) {
                String unit = type.unit != null ? type.unit : "";
                String val = String.format(Locale.getDefault(), "%.2f%s",
                        e.value, unit.isEmpty() ? "" : " " + unit);
                String base = ctx.getString(R.string.notif_anomaly_text_value, val);

                String text;
                if (r.triggeredRule != null) {
                    String min = r.triggeredRule.lowThreshold != null ? String.valueOf(r.triggeredRule.lowThreshold) : "–";
                    String max = r.triggeredRule.highThreshold != null ? String.valueOf(r.triggeredRule.highThreshold) : "–";
                    text = base + " " + ctx.getString(R.string.notif_anomaly_text_rule, min, max);
                } else {
                    String min = type.minNormal != null ? String.valueOf(type.minNormal) : "–";
                    String max = type.maxNormal != null ? String.valueOf(type.maxNormal) : "–";
                    text = base + " " + ctx.getString(R.string.notif_anomaly_text_type, min, max);
                }

                NotificationHelper.notifyAnomaly(ctx, type, e, text);
            }
        }

        return Result.success();
    }
}
