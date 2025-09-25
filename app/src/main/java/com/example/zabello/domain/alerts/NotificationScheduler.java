package com.example.zabello.domain.alerts;

import android.content.Context;

import androidx.work.BackoffPolicy;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

/** Периодический фоновой монитор. */
public class NotificationScheduler {

    private static final String UNIQUE_WORK = "anomaly_periodic_check";

    public static void schedulePeriodicChecks(Context context) {
        PeriodicWorkRequest req = new PeriodicWorkRequest.Builder(
                AnomalyCheckWorker.class,
                6, TimeUnit.HOURS)
                .setInitialDelay(30, TimeUnit.MINUTES)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 30, TimeUnit.MINUTES)
                .build();
        WorkManager.getInstance(context.getApplicationContext())
                .enqueueUniquePeriodicWork(UNIQUE_WORK, ExistingPeriodicWorkPolicy.UPDATE, req);
    }

    public static void cancelPeriodicChecks(Context context) {
        WorkManager.getInstance(context.getApplicationContext()).cancelUniqueWork(UNIQUE_WORK);
    }
}
