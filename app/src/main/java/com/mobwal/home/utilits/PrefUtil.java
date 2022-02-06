package com.mobwal.home.utilits;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.mobwal.home.Names;
import com.mobwal.home.WalkerApplication;

public class PrefUtil {
    /**
     * Получение информации о пин-коде
     * @param context контекст
     * @return пин-код
     */
    public static String getPinCode(Context context) {
        FileManager fileManager = new FileManager(context.getCacheDir());
        if(fileManager.exists("security", "pin.txt")) {
            try {
                return new String(fileManager.readPath("security", "pin.txt"));
            } catch (IOException e) {
                WalkerApplication.Log("Ошибка чтения пин-кода из файла.", e);
                return "";
            }
        }

        return "";
    }

    /**
     * Установка пин-кода
     * @param context контекст
     * @param pinCode пин-код
     */
    public static void setPinCode(Context context, String pinCode) {
        FileManager fileManager = new FileManager(context.getCacheDir());
        try {
            fileManager.writeBytes("security", "pin.txt", pinCode.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            WalkerApplication.Log("Ошибка записи пин-кода в файл.", e);
        }
    }
}
