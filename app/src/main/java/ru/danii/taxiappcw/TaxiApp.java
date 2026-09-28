package ru.danii.taxiappcw;

import android.app.Application;
import com.yandex.mapkit.MapKitFactory;

public class TaxiApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Ключ берётся из локальной конфигурации сборки.
        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY);
        MapKitFactory.initialize(this);
    }
}