package com.mobwal.home.utilits;

import android.text.TextUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Чтение файла в формате CSV
 */
public class CsvReader {
    public static String normalValue(@NotNull String value) {
        return value.replaceAll("\r", "");
    }

    private final String mRowSeparate;
    private final String mColumnSeparate;
    private final String mData;

    public CsvReader(@NotNull String data) {
        mRowSeparate = "\n";
        mColumnSeparate = ";";
        mData = data;
    }

    public String[][] getRows() {
        return getRows(false);
    }

    /**
     * получение информации данных из csv
     * @param withOutHeaders без заголовка. По умолчанию false
     * @return словарь с данными
     */
    public String[][] getRows(@Nullable Boolean withOutHeaders) {
        if(withOutHeaders == null) {
            withOutHeaders = false;
        }

        List<String[]> list = new ArrayList<>();

        String[] rows = mData.split(mRowSeparate);
        boolean firstIndex = true;

        for (String row: rows) {
            // пропускаем первую строку
            if (firstIndex && withOutHeaders) {
                firstIndex = false;
                continue;
            }

            String[] columns = row.split(mColumnSeparate);
            if(!TextUtils.isEmpty(columns[0])) {
                for (int i = 0; i < columns.length; i++) {
                    columns[i] = StringUtil.trimSymbol(columns[i], '\r');
                }
                list.add(columns);
            }
        }
        return list.toArray(new String[0][]);
    }
}
