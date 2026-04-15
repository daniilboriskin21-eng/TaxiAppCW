package ru.danii.taxiappcw.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Помощник для работы с базой данных SQLite.
 * Реализует создание таблиц для хранения истории заказов.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "taxi_app.db";
    private static final int DATABASE_VERSION = 1;

    // SQL запрос для создания таблицы
    private static final String SQL_CREATE_RIDES_TABLE =
            "CREATE TABLE " + TaxiContract.RideEntry.TABLE_NAME + " (" +
                    TaxiContract.RideEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    TaxiContract.RideEntry.COLUMN_DEPARTURE + " TEXT NOT NULL," +
                    TaxiContract.RideEntry.COLUMN_DESTINATION + " TEXT NOT NULL," +
                    TaxiContract.RideEntry.COLUMN_TARIFF + " TEXT," +
                    TaxiContract.RideEntry.COLUMN_DATE + " DEFAULT CURRENT_TIMESTAMP)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_RIDES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TaxiContract.RideEntry.TABLE_NAME);
        onCreate(db);
    }

    /**
     * Добавляет новую запись о поездке в базу данных.
     * @param from Точка отправления.
     * @param to Точка назначения.
     * @param tariff Выбранный тарифный план.
     */
    public void addRide(String from, String to, String tariff) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TaxiContract.RideEntry.COLUMN_DEPARTURE, from);
        values.put(TaxiContract.RideEntry.COLUMN_DESTINATION, to);
        values.put(TaxiContract.RideEntry.COLUMN_TARIFF, tariff);

        db.insert(TaxiContract.RideEntry.TABLE_NAME, null, values);
        db.close();
    }
}