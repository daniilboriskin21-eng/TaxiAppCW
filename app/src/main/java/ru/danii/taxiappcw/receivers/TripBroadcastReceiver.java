package ru.danii.taxiappcw.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import ru.danii.taxiappcw.utils.SettingsManager;

/**
 * Ресивер для обработки глобальных событий приложения:
 * перезагрузка устройства, изменение статуса заказа и его отмена.
 */
public class TripBroadcastReceiver extends BroadcastReceiver {

    public static final String ACTION_TRIP_STATUS = "ru.danii.taxiappcw.ACTION_TRIP_STATUS";
    public static final String ACTION_CANCEL_ORDER = "ru.danii.taxiappcw.ACTION_CANCEL_ORDER";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;

        SettingsManager sm = new SettingsManager(context);

        switch (action) {
            case Intent.ACTION_BOOT_COMPLETED:
                // ВОССТАНОВЛЕНИЕ: Проверяем, был ли активный маршрут до выключения
                String[] lastTrip = sm.getLastTrip();
                if (!lastTrip[0].isEmpty()) {
                    // В реальном приложении здесь бы запускался сервис,
                    // мы просто напомним пользователю уведомлением.
                    Toast.makeText(context, "TaxiApp: Ваш последний маршрут сохранен!", Toast.LENGTH_LONG).show();
                }
                break;

            case ACTION_TRIP_STATUS:
                String status = intent.getStringExtra("status");
                Toast.makeText(context, "Статус обновлен: " + status, Toast.LENGTH_SHORT).show();
                break;

            case ACTION_CANCEL_ORDER:
                Toast.makeText(context, "Заказ отменен через систему", Toast.LENGTH_SHORT).show();
                // Здесь можно добавить логику очистки БД или остановки сервиса
                break;
        }
    }
}