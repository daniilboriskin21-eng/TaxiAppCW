package ru.danii.taxiappcw.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import ru.danii.taxiappcw.R;

/**
 * Приемник для отслеживания изменений состояния сети.
 * Демонстрирует использование BroadcastReceiver согласно методичке.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!isOnline(context)) {
            Toast.makeText(context, R.string.error_no_internet, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Проверяет наличие активного интернет-соединения.
     */
    private boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}