package com.example.zabello.domain.alerts;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.zabello.R;
import com.example.zabello.activities.MainActivity;
import com.example.zabello.data.entity.ParameterEntry;
import com.example.zabello.data.entity.ParameterType;

/** Локальные уведомления об аномалиях. */
public class NotificationHelper {

    public static final String CHANNEL_ANOMALIES = "anomalies";

    public static void createChannels(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ANOMALIES,
                    context.getString(R.string.channel_anomalies_name),
                    NotificationManager.IMPORTANCE_HIGH
            );
            ch.setDescription(context.getString(R.string.channel_anomalies_desc));
            NotificationManager nm = context.getSystemService(NotificationManager.class);
            nm.createNotificationChannel(ch);
        }
    }

    /** Уведомление о конкретной записи/типе. */
    public static void notifyAnomaly(Context context,
                                     ParameterType type,
                                     ParameterEntry entry,
                                     String text) {
        createChannels(context);

        // Android 13+ требует явное разрешение POST_NOTIFICATIONS.
        if (Build.VERSION.SDK_INT >= 33) {
            int granted = ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.POST_NOTIFICATIONS);
            if (granted != PackageManager.PERMISSION_GRANTED) {
                // Разрешение не выдано — тихо выходим без краша.
                return;
            }
        }

        Intent intent = new Intent(context, MainActivity.class)
                .putExtra("open_type_id", type.id)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pi = PendingIntent.getActivity(
                context, (int) (type.id % Integer.MAX_VALUE), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        String title = context.getString(
                R.string.notif_anomaly_title,
                (type.title != null ? type.title : ("Тип #" + type.id))
        );

        NotificationCompat.Builder b = new NotificationCompat.Builder(context, CHANNEL_ANOMALIES)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat.from(context)
                .notify((int) (entry.id % Integer.MAX_VALUE), b.build());
    }
}
