package ru.danii.taxiappcw;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;

import ru.danii.taxiappcw.db.DatabaseHelper;
import ru.danii.taxiappcw.utils.SettingsManager;

/**
 * Экран выбора тарифа и дополнительных опций поездки.
 * Отвечает за сбор итоговых данных заказа и их сохранение в локальную БД SQLite.
 */
public class TariffActivity extends AppCompatActivity {

    private String departureAddr;
    private String destinationAddr;

    private RadioGroup rgTariff;
    private EditText etComment;
    private CheckBox cbChildSeat;
    private CheckBox cbQuiet;

    private SettingsManager settingsManager;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Применение темы до отрисовки макета
        settingsManager = new SettingsManager(this);
        settingsManager.applyTheme(settingsManager.getThemeMode());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tariff);

        // Инициализация помощника БД
        dbHelper = new DatabaseHelper(this);

        // Получение данных из предыдущего экрана (MainActivity)
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            departureAddr = extras.getString("EXTRA_DEPARTURE");
            destinationAddr = extras.getString("EXTRA_DESTINATION");
        }

        initViews();
    }

    /**
     * Инициализация элементов управления и привязка слушателей.
     */
    private void initViews() {
        rgTariff = findViewById(R.id.rgTariff);
        etComment = findViewById(R.id.etComment);
        cbChildSeat = findViewById(R.id.cbChildSeat);
        cbQuiet = findViewById(R.id.cbQuiet);
        Button btnConfirm = findViewById(R.id.btnConfirm);

        btnConfirm.setOnClickListener(v -> handleOrderConfirmation());
    }

    /**
     * Обработка нажатия кнопки подтверждения заказа.
     * Сохраняет данные в SQLite и переходит к экрану отслеживания поездки.
     */
    private void handleOrderConfirmation() {
        // Получаем выбранный тариф
        int selectedId = rgTariff.getCheckedRadioButtonId();
        RadioButton rbSelected = findViewById(selectedId);
        String tariffName = (rbSelected != null) ? rbSelected.getText().toString() : "Эконом";

        // Получаем комментарий
        String comment = etComment.getText().toString().trim();

        // 2. Подготовка интента для экрана активной поездки
        Intent intent = new Intent(this, ActiveTripActivity.class);
        Bundle orderInfo = new Bundle();

        orderInfo.putString("TARIFF", tariffName);
        orderInfo.putString("COMMENT", comment);
        orderInfo.putString("EXTRA_DEPARTURE", departureAddr);
        orderInfo.putString("EXTRA_DESTINATION", destinationAddr);
        orderInfo.putBoolean("CHILD_SEAT", cbChildSeat.isChecked());

        intent.putExtras(orderInfo);

        // Запуск экрана поездки
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        // Закрываем соединение с БД при уничтожении Activity
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }
}