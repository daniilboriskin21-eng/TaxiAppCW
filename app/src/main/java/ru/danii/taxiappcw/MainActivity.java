package ru.danii.taxiappcw;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatDelegate;
import android.view.View;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.yandex.mapkit.MapKitFactory;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import ru.danii.taxiappcw.receivers.NetworkChangeReceiver;
import ru.danii.taxiappcw.utils.SettingsManager;

public class MainActivity extends AppCompatActivity {

    private NetworkChangeReceiver networkReceiver;
    private EditText etDeparture, etDestination;
    private FusedLocationProviderClient fusedLocationClient;
    private SettingsManager settingsManager;
    private static final int GPS_PERMISSION_CODE = 1001;

    private final ActivityResultLauncher<Intent> mapPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String address = result.getData().getStringExtra("ADDRESS");
                    boolean isDeparture = result.getData().getBooleanExtra("IS_DEPARTURE", true);
                    if (isDeparture) etDeparture.setText(address);
                    else etDestination.setText(address);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settingsManager = new SettingsManager(this);

        // ПРОВЕРКА: Если поездка еще идет, MainActivity закрывается, открывается ActiveTrip
        if (settingsManager.isTripActive()) {
            String[] lastTrip = settingsManager.getLastTrip();

            // 2. Создаем Intent и ПЕРЕДАЕМ данные обратно
            Intent intent = new Intent(this, ActiveTripActivity.class);
            intent.putExtra("EXTRA_DEPARTURE", lastTrip[0]);
            intent.putExtra("EXTRA_DESTINATION", lastTrip[1]);
            intent.putExtra("TARIFF", lastTrip[2]);

            // Флаги, чтобы не создавать цепочку экранов
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish(); // Закрываем Main, чтобы он не висел в фоне
            return;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Простая проверка: если тема в настройках поменялась — обновляем.
        // Если нет — ничего не делаем.
        int savedMode = settingsManager.getThemeMode();
        settingsManager.applyTheme(savedMode);
        networkReceiver = new NetworkChangeReceiver();
        IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(networkReceiver, filter);
    }

    private void initViews() {
        etDeparture = findViewById(R.id.etDeparture);
        etDestination = findViewById(R.id.etDestination);

        findViewById(R.id.btnSettings).setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });

        findViewById(R.id.btnGps).setOnClickListener(v -> checkGpsPermission());
        findViewById(R.id.btnMapFrom).setOnClickListener(v -> openMapPicker(true));
        findViewById(R.id.btnMapTo).setOnClickListener(v -> openMapPicker(false));
        findViewById(R.id.btnNext).setOnClickListener(v -> navigateToTariff());

        Button btnRepeat = findViewById(R.id.btnRepeatLast);
        String[] last = settingsManager.getLastTrip();
        if (!last[0].isEmpty()) {
            btnRepeat.setVisibility(View.VISIBLE);
            btnRepeat.setOnClickListener(v -> {
                etDeparture.setText(last[0]);
                etDestination.setText(last[1]);
            });
        }

        findViewById(R.id.btnHistory).setOnClickListener(v -> {
            // Запускаем историю через лаунчер
            Intent intent = new Intent(this, HistoryActivity.class);
            historyLauncher.launch(intent);
        });
    }

    private final ActivityResultLauncher<Intent> historyLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    // Получаем данные, которые передали из HistoryActivity
                    String from = result.getData().getStringExtra("SELECTED_FROM");
                    String to = result.getData().getStringExtra("SELECTED_TO");

                    // Заполняем поля ввода
                    if (etDeparture != null) etDeparture.setText(from);
                    if (etDestination != null) etDestination.setText(to);

                    Toast.makeText(this, "Маршрут из истории загружен", Toast.LENGTH_SHORT).show();
                }
            }
    );
    private void checkGpsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Вот это вызывает окно запроса!
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, GPS_PERMISSION_CODE);
        } else {
            requestGpsLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GPS_PERMISSION_CODE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestGpsLocation();
            } else {
                Toast.makeText(this, "Разрешите доступ к GPS в настройках", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestGpsLocation() {
        // Проверка для Android Lint
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                decodeAddress(location.getLatitude(), location.getLongitude());
            } else {
                Toast.makeText(this, "Координаты не найдены. Включите GPS.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void decodeAddress(double lat, double lng) {
        new Thread(() -> {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    String fullAddress = addresses.get(0).getAddressLine(0);
                    new Handler(Looper.getMainLooper()).post(() -> etDeparture.setText(fullAddress));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void navigateToTariff() {
        String from = etDeparture.getText().toString().trim();
        String to = etDestination.getText().toString().trim();

        if (from.isEmpty() || to.isEmpty()) {
            Toast.makeText(this, "Заполните адреса", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, TariffActivity.class);
        intent.putExtra("EXTRA_DEPARTURE", from);
        intent.putExtra("EXTRA_DESTINATION", to);
        startActivity(intent);
    }

    private void openMapPicker(boolean isDeparture) {
        Intent intent = new Intent(this, MapPickerActivity.class);
        intent.putExtra("IS_DEPARTURE", isDeparture);
        mapPickerLauncher.launch(intent);
    }

    private void fillLastTrip() {
        String[] lastTrip = settingsManager.getLastTrip();
        if (!lastTrip[0].isEmpty()) {
            etDeparture.setText(lastTrip[0]);
            etDestination.setText(lastTrip[1]);
            Toast.makeText(this, "Данные последней поездки загружены", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "История пуста", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (networkReceiver != null) {
            unregisterReceiver(networkReceiver);
        }
    }
}