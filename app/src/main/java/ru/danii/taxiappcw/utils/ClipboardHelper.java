package ru.danii.taxiappcw.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

/**
 * Утилита для работы с буфером обмена.
 * Используется для копирования и вставки адресов маршрута.
 */
public class ClipboardHelper {

    /**
     * Получает текст из буфера обмена.
     * @param context Контекст приложения.
     * @return Текст из буфера или пустая строка, если буфер пуст.
     */
    public static String pasteFromClipboard(Context context) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null && clipboard.hasPrimaryClip()) {
            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
            return item.getText().toString();
        }
        return "";
    }
}