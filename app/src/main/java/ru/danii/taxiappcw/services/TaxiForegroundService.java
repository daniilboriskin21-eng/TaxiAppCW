package ru.danii.taxiappcw.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import ru.danii.taxiappcw.R;

/**
 * Фоновый сервис для отслеживания местоположения во время поездки.
 * Работает в режиме Foreground Service, что позволяет приложению
 * сохранять активность при сворачивании.
 */
public class TaxiForegroundService extends Service {

    private static final int NOTIFICATION_ID = 101;
    private static final String CHANNEL_ID = "taxi_trip_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    /**
     * Вызывается при запуске сервиса. Создает стойкое уведомление (Sticky Notification),
     * которое удерживает сервис в памяти системы.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Создаем уведомление для запуска службы в режиме Foreground
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_driver_on_way))
                .setSmallIcon(R.drawable.ic_taxi_logo) // Убедись, что иконка создана
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .build();

        // Запуск службы в переднем плане (обязательно для Service)
        startForeground(NOTIFICATION_ID, notification);

        // START_STICKY позволяет системе перезапустить службу при нехватке памяти
        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // Bind не используется в данной реализации
    }
}