package ru.danii.taxiappcw.db;

import android.provider.BaseColumns;

/**
 * Контракт для описания структуры базы данных такси.
 * Содержит названия таблиц и столбцов.
 */
public final class TaxiContract {
    private TaxiContract() {}

    /** Таблица истории поездок */
    public static class RideEntry implements BaseColumns {
        public static final String TABLE_NAME = "rides";
        public static final String COLUMN_DEPARTURE = "departure";
        public static final String COLUMN_DESTINATION = "destination";
        public static final String COLUMN_TARIFF = "tariff";
        public static final String COLUMN_DATE = "ride_date";
    }
}