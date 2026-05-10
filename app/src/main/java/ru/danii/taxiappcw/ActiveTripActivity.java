package ru.danii.taxiappcw;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import ru.danii.taxiappcw.db.DatabaseHelper;
import android.os.Vibrator;
import android.os.VibrationEffect;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import ru.danii.taxiappcw.receivers.TripBroadcastReceiver;
import ru.danii.taxiappcw.services.TaxiForegroundService;
import ru.danii.taxiappcw.utils.SettingsManager;

/**
 * Экран активной поездки.
 * Отвечает за симуляцию процесса поездки, управление фоновым сервисом
 * и информирование пользователя через уведомления.
 */
public class ActiveTripActivity extends AppCompatActivity {

    private TextView tvTripStatus;
    private TextView tvTripDetails;
    private ProgressBar tripProgressBar;
    private Button btnCancel;
    private SettingsManager settingsManager;

    // Лаунчер для разрешения на уведомления (Android 13+)
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Разрешение получено, можно запускать отслеживание и уведомления
                    startLocationTracking();
                } else {
                    Toast.makeText(this, "Без уведомлений вы не узнаете о прибытии такси", Toast.LENGTH_SHORT).show();
                    // Всё равно запускаем, но уведомлений не будет
                    startLocationTracking();
                }
            });

    /**
     * Инициализирует компоненты интерфейса и запускает проверку разрешений.
     * @param savedInstanceState Состояние экземпляра.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settingsManager = new SettingsManager(this);
        settingsManager.applyTheme(settingsManager.getThemeMode());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_trip);

        initViews();
        displayOrderInfo();
        checkPermissionsAndStart();
        createNotificationChannel();
    }

    private void sendStatusNotification(String message) {
        android.app.NotificationManager notificationManager =
                (android.app.NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);

        androidx.core.app.NotificationCompat.Builder builder =
                new androidx.core.app.NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_taxi_logo) // Твоя новая иконка!
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(message)
                        .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true);

        if (notificationManager != null) {
            notificationManager.notify(1, builder.build());
        }
    }

    private void initViews() {
        tvTripStatus = findViewById(R.id.tvTripStatus);
        tvTripDetails = findViewById(R.id.tvTripDetails);
        tripProgressBar = findViewById(R.id.tripProgressBar);
        btnCancel = findViewById(R.id.btnCancelTrip);

        btnCancel.setOnClickListener(v -> stopTaxiService());
    }

    /**
     * Выполняет комплексную проверку разрешений на местоположение и уведомления.
     * Запускает сервис только при наличии всех необходимых доступов.
     */
    private void checkPermissionsAndStart() {
        // 1. Проверяем GPS (ACCESS_FINE_LOCATION)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }

        // 2. Проверяем Уведомления (только для Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                return;
            }
        }

        // Если всё есть — поехали!
        startLocationTracking();
    }

    private void displayOrderInfo() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String departure = extras.getString("EXTRA_DEPARTURE", "");
            String destination = extras.getString("EXTRA_DESTINATION", "");
            String tariff = extras.getString("TARIFF", "");
            String comment = extras.getString("COMMENT", "");
            boolean hasChildSeat = extras.getBoolean("CHILD_SEAT", false);


            StringBuilder sb = new StringBuilder();

            // Используем ресурсы с подстановкой строк (%1$s)
            sb.append(getString(R.string.label_from, departure)).append("\n");
            sb.append(getString(R.string.label_to, destination)).append("\n");
            sb.append(getString(R.string.label_tariff, tariff)).append("\n");

            if (!comment.isEmpty()) {
                sb.append(getString(R.string.label_comment, comment)).append("\n");
            }

            if (hasChildSeat) {
                sb.append(getString(R.string.label_child_seat));
            }

            tvTripDetails.setText(sb.toString());
        }
    }
    private static final String CHANNEL_ID = "taxi_status_channel";

    /**
     * Создает канал уведомлений для Android 8.0+.
     * Необходимо для корректной работы NotificationManager.
     */
    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel(
                    CHANNEL_ID,
                    "Статус поездки",
                    android.app.NotificationManager.IMPORTANCE_DEFAULT
            );
            android.app.NotificationManager manager = getSystemService(android.app.NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    // Внутри ActiveTripActivity.java
    private Handler simulationHandler = new Handler(Looper.getMainLooper());
    /**
     * Реализует тактильный отклик (вибрацию) при изменении статуса заказа.
     * Использует Vibrator для старых версий и VibrationEffect для новых.
     */
    private void triggerVibration() {
        android.os.Vibrator v = (android.os.Vibrator) getSystemService(android.content.Context.VIBRATOR_SERVICE);
        if (v == null || !v.hasVibrator()) return;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // Короткая двойная вибрация для API 26+
            v.vibrate(android.os.VibrationEffect.createOneShot(500, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            // Для старых устройств
            v.vibrate(500);
        }
    }
    private boolean isTripSaved = false; // Поле класса в самом верху

    /**
     * Имитирует этапы выполнения заказа: ожидание, путь и завершение.
     * Обновляет UI и отправляет системные уведомления.
     */
    private void startTripSimulation() {
        // Сохраняем ТОЛЬКО ОДИН РАЗ в момент фактического начала процесса
        if (!isTripSaved) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                String departure = extras.getString("EXTRA_DEPARTURE", "");
                String destination = extras.getString("EXTRA_DESTINATION", "");
                String tariff = extras.getString("TARIFF", "");

                settingsManager.saveLastTrip(departure, destination, tariff);
                DatabaseHelper dbHelper = new DatabaseHelper(this);
                dbHelper.addRide(departure, destination, tariff);

                isTripSaved = true; // Блокируем повторную запись
            }
        }
        // Сохраняем, что поездка активна
        settingsManager.setTripActive(true);
        sendStatusNotification("Ищем машину...");


        // Твой текущий код симуляции (Handler и т.д.)
        simulationHandler.postDelayed(() -> {
            String status = "Водитель на месте";
            tvTripStatus.setText(status);
            tripProgressBar.setProgress(20);
            sendStatusNotification(status);
            triggerVibration();
            notifyStatusChange(status);
        }, 5000);

        // Этап 2: Поездка началась
        simulationHandler.postDelayed(() -> {
            String status = "Поездка началась. В пути...";
            tvTripStatus.setText(status);
            tripProgressBar.setProgress(60);
            btnCancel.setEnabled(false);
            btnCancel.setAlpha(0.5f);
            sendStatusNotification(status);
            triggerVibration();
            notifyStatusChange(status);
        }, 10000);

        // Этап 3: Завершение и переход в MAIN
        // Внутри startTripSimulation, в самом последнем блоке (через 20 сек)
        simulationHandler.postDelayed(() -> {
            String status = "Приехали! Спасибо за поездку.";
            tvTripStatus.setText(status);
            tripProgressBar.setProgress(100);
            sendStatusNotification(status);
            triggerVibration();
            notifyStatusChange(status);

            // Сбрасываем флаг
            settingsManager.setTripActive(false);
            // Останавливаем сервис
            stopTaxiService();

            // Переходим на главный экран и очищаем стек
            Intent intent = new Intent(ActiveTripActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);

            finish(); // Закрываем экран поездки
        }, 20000);
    }

    private void notifyStatusChange(String status) {
        Intent intent = new Intent(TripBroadcastReceiver.ACTION_TRIP_STATUS);
        intent.putExtra("status", status);
        // Отправляем на все приложение
        sendBroadcast(intent);
    }
    private void stopTaxiService() {
        // Сбрасываем флаг
        settingsManager.setTripActive(false);
        // 1. ОСТАНАВЛИВАЕМ ВСЕ ТАЙМЕРЫ
        if (simulationHandler != null) {
            simulationHandler.removeCallbacksAndMessages(null);
        }

        // 2. Останавливаем сервис
        stopService(new Intent(this, TaxiForegroundService.class));

        // 3. ПЕРЕХОДИМ В MAIN (правильно)
        Intent intent = new Intent(this, MainActivity.class);
        // Флаг CLEAR_TOP закроет все старые экраны и откроет чистый Main
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);

        finish();
    }
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            startLocationTracking();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void startLocationTracking() {
        tvTripStatus.setText(R.string.status_searching);

        Intent serviceIntent = new Intent(this, TaxiForegroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        startTripSimulation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        settingsManager.applyTheme(settingsManager.getThemeMode());
    }
}