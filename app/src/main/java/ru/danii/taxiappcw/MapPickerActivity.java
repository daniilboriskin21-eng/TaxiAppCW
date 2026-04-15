package ru.danii.taxiappcw;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraListener;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.CameraUpdateReason;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.mapview.MapView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import ru.danii.taxiappcw.utils.SettingsManager;

public class MapPickerActivity extends AppCompatActivity implements CameraListener {

    private MapView mapView;
    private TextView tvAddress;
    private String currentAddress = "";
    private boolean isDepartureMode;
    private SettingsManager settingsManager; // Для темы

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1. ПРИМЕНЯЕМ ТЕМУ ПЕРЕД onCreate
        settingsManager = new SettingsManager(this);
        settingsManager.applyTheme(settingsManager.getThemeMode());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_picker);

        isDepartureMode = getIntent().getBooleanExtra("IS_DEPARTURE", true);
        mapView = findViewById(R.id.mapview_picker);
        tvAddress = findViewById(R.id.tvSelectedAddress);
        Button btnConfirm = findViewById(R.id.btnConfirmLocation);

        // Слушатель камеры
        mapView.getMap().addCameraListener(this);

        btnConfirm.setOnClickListener(v -> {
            Intent data = new Intent();
            data.putExtra("ADDRESS", currentAddress);
            data.putExtra("IS_DEPARTURE", isDepartureMode);
            setResult(RESULT_OK, data);
            finish();
        });
    }

    @Override
    public void onCameraPositionChanged(@NonNull Map map, @NonNull CameraPosition pos,
                                        @NonNull CameraUpdateReason reason, boolean finished) {
        // Обновляем только когда пользователь закончил двигать карту
        if (finished) {
            updateAddress(pos.getTarget());
        }
    }

    private void updateAddress(Point p) {
        // ВЫНОСИМ В ФОНОВЫЙ ПОТОК, ЧТОБЫ КАРТА НЕ ВИСЛА
        new Thread(() -> {
            Geocoder g = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> ads = g.getFromLocation(p.getLatitude(), p.getLongitude(), 1);
                if (ads != null && !ads.isEmpty()) {
                    String foundAddress = ads.get(0).getAddressLine(0);

                    // Возвращаемся в UI поток для обновления TextView
                    new Handler(Looper.getMainLooper()).post(() -> {
                        currentAddress = foundAddress;
                        tvAddress.setText(currentAddress);
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }

    @Override protected void onStop() {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }

}