package com.example.zabello;

import android.app.Application;

import com.example.zabello.domain.alerts.NotificationHelper;
import com.example.zabello.domain.alerts.NotificationScheduler;

/** Application: инициализация каналов уведомлений и периодических задач. */
public class ZabelloApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationHelper.createChannels(this);
        NotificationScheduler.schedulePeriodicChecks(this);
    }
}
