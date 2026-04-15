package ru.danii.taxiappcw;

import android.content.Intent; // Добавлен импорт
import android.os.Bundle;
import android.widget.RadioGroup;
import androidx.activity.OnBackPressedCallback; // Для нового способа "Назад"
import androidx.appcompat.app.AppCompatActivity;
import ru.danii.taxiappcw.utils.SettingsManager;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

public class SettingsActivity extends AppCompatActivity {

    private SettingsManager settingsManager;
    private boolean isInternalUpdate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settingsManager = new SettingsManager(this);
        settingsManager.applyTheme(settingsManager.getThemeMode());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // --- НОВЫЙ СПОСОБ ОБРАБОТКИ КНОПКИ НАЗАД (AndroidX) ---
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Возвращаемся в Main, чтобы тема обновилась корректно
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });
        // -------------------------------------------------------

        RadioGroup rgTheme = findViewById(R.id.rgThemeSettings);
        int currentMode = settingsManager.getThemeMode();

        isInternalUpdate = true;
        if (currentMode == SettingsManager.MODE_LIGHT) {
            rgTheme.check(R.id.rbThemeLight);
        } else if (currentMode == SettingsManager.MODE_DARK) {
            rgTheme.check(R.id.rbThemeDark);
        } else {
            rgTheme.check(R.id.rbThemeSystem);
        }
        isInternalUpdate = false;

        rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            if (isInternalUpdate) return;

            int newMode = SettingsManager.MODE_SYSTEM;
            if (checkedId == R.id.rbThemeLight) newMode = SettingsManager.MODE_LIGHT;
            else if (checkedId == R.id.rbThemeDark) newMode = SettingsManager.MODE_DARK;

            if (newMode != settingsManager.getThemeMode()) {
                settingsManager.saveThemeMode(newMode);
                settingsManager.applyTheme(newMode);
                // Теперь НЕ вызываем recreate(), чтобы не ломать логику переходов
            }
        });
    }

}