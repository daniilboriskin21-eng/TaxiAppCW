package ru.danii.taxiappcw;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import ru.danii.taxiappcw.db.DatabaseHelper;
import ru.danii.taxiappcw.db.TaxiContract;
import ru.danii.taxiappcw.db.TripModel;
import ru.danii.taxiappcw.utils.SettingsManager;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Применяем тему
        SettingsManager sm = new SettingsManager(this);
        sm.applyTheme(sm.getThemeMode());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        RecyclerView recyclerView = findViewById(R.id.rvHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ArrayList<TripModel> trips = new ArrayList<>();

        // Читаем из БД
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Сортировка: новые сверху (DESC)
        Cursor cursor = db.query(TaxiContract.RideEntry.TABLE_NAME, null, null, null, null, null, TaxiContract.RideEntry._ID + " DESC");

        while (cursor.moveToNext()) {
            String from = cursor.getString(cursor.getColumnIndexOrThrow(TaxiContract.RideEntry.COLUMN_DEPARTURE));
            String to = cursor.getString(cursor.getColumnIndexOrThrow(TaxiContract.RideEntry.COLUMN_DESTINATION));
            String tariff = cursor.getString(cursor.getColumnIndexOrThrow(TaxiContract.RideEntry.COLUMN_TARIFF));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(TaxiContract.RideEntry.COLUMN_DATE));

            // Форматирование даты (опционально, SQLite отдает YYYY-MM-DD HH:MM:SS)
            // Для простоты оставим как есть, или можно обрезать время: date.substring(0, 10)

            trips.add(new TripModel(from, to, tariff, date));
        }
        cursor.close();

        // Создаем адаптер с обработчиком клика
        HistoryAdapter adapter = new HistoryAdapter(trips, trip -> {
            // ЛОГИКА КЛИКА: Возвращаем данные в MainActivity
            Intent intent = new Intent();
            intent.putExtra("SELECTED_FROM", trip.from);
            intent.putExtra("SELECTED_TO", trip.to);
            setResult(RESULT_OK, intent);
            finish(); // Закрываем историю
        });

        recyclerView.setAdapter(adapter);
    }
}