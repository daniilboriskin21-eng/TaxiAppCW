package ru.danii.taxiappcw;

import android.app.Application;
import com.yandex.mapkit.MapKitFactory;

public class TaxiApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Ключ и инициализация теперь живут здесь вечно
        MapKitFactory.setApiKey("b2ed3938-0be6-4dde-94a4-b672bf916d6d");
        MapKitFactory.initialize(this);
    }
}