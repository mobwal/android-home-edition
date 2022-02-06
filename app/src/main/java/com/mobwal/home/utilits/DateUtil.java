package com.mobwal.home.utilits;

import org.jetbrains.annotations.Nullable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.mobwal.home.WalkerApplication;

public class DateUtil {
    public static final String SYSTEM_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    /**
     * Дата преобразуется в строку с системным форматом
     * @param date дата
     * @return возврщается строка
     */
    public static String convertDateToSystemString(Date date) {
        return new SimpleDateFormat(SYSTEM_FORMAT, Locale.getDefault()).format(date);
    }

    /**
     * Преобразование строки в дату
     * @param date дата
     * @return результат преобразования
     */
    @Nullable
    public static Date convertStringToSystemDate(String date) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(SYSTEM_FORMAT, Locale.getDefault());
            return dateFormat.parse(date);
        } catch (Exception e) {
            WalkerApplication.Debug("Ошибка преобразования строки " + date + " в дату", e);
            return null;
        }
    }

    public static String toDateTimeString(@Nullable Date date) {
        if(date != null) {
            DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.getDefault());
            return formatter.format(date);
        }

        return "";
    }
}
