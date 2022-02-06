package com.mobwal.home.utilits;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import com.mobwal.home.R;

public class StringUtil {
    private static final String NULL = "null";

    /**
     * Корректировка строки
     * @param txt входная строка
     * @return результат
     */
    public static String normalString(String txt){
        if(txt == null) {
            return "";
        }
        return txt.equals(NULL) ? "" : txt;
    }

    /**
     * строка является пустой или равна null
     * @param input входная строка
     * @return результат сравнения
     */
    public static boolean isEmptyOrNull(String input){
        String normal = normalString(input);
        return normal.isEmpty();
    }

    /**
     * Преобразование байтов в КБ, МБ, ГБ
     * @param size размер
     * @return строка
     */
    public static String getSize(@Nullable Context context, long size) {
        String s;
        double kb = (double) size / 1024;
        double mb = kb / 1024;
        double gb = mb / 1024;
        double tb = gb / 1024;
        if(size < 1024) {
            s = size + " " + (context == null ? "байт" : context.getString(R.string.byt));
        } else if(size < 1024 * 1024) {
            s =  String.format(Locale.getDefault(), "%.2f", kb) + " " + (context == null ? "КБ" : context.getString(R.string.kb));
        } else if(size < 1024 * 1024 * 1024) {
            s = String.format(Locale.getDefault(),"%.2f", mb) + " " + (context == null ? "МБ" : context.getString(R.string.mb));
        } else if(size < (long) 1024 * (long) 1024 * (long) 1024 * (long) 1024) {
            s = String.format(Locale.getDefault(),"%.2f", gb) + " " + (context == null ? "ГБ" : context.getString(R.string.gb));
        } else {
            s = String.format(Locale.getDefault(),"%.2f", tb) + " " + (context == null ? "ТБ" : context.getString(R.string.tb));
        }
        return s;
    }

    /**
     * Получение расширения файла
     *
     * @param name имя файла
     * @return расширение
     */
    @Nullable
    public static String getFileExtension(String name) {
        if (name != null && !name.isEmpty()) {
            int strLength = name.lastIndexOf(".");
            if (strLength >= 0) {
                String ext = name.substring(strLength + 1).toLowerCase();
                if (ext.isEmpty()) {
                    return null;
                } else {
                    return "." + ext;
                }
            }
        }
        return "";
    }

    /**
     * Очистка имени от расширения
     * @param name имя файла
     * @return результат
     */
    @NotNull
    public static String getNameWithOutExtension(@NotNull String name) {
        if(TextUtils.isEmpty(name)) {
            return "";
        }

        String ext = getFileExtension(name);
        if(ext != null) {
            return name.replace(ext, "");
        }

        return "";
    }

    /**
     * Преобразование исключения в строку
     *
     * @param e исключение
     * @return строка
     */
    public static String exceptionToString(Throwable e) {
        Writer writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    /**
     * преобразование шаблона
     * @param context контекст
     * @param content исходный текст
     * @param json данные для выборки
     * @return результат форматирования
     */
    @Nullable
    public static String convertTemplate(@NotNull Context context, @Nullable String content, @Nullable String json) {
        if(!TextUtils.isEmpty(json) && !TextUtils.isEmpty(content)) {
            String newContent = content;
            try {
                JsonElement jsonElement = JsonParser.parseString(Objects.requireNonNull(json));
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                Set<String> keySet = jsonObject.keySet();
                for (String key : keySet) {
                    String value;

                    if(jsonObject.get(key).isJsonNull()) {
                        continue;
                    }

                    if(jsonObject.getAsJsonPrimitive(key).isBoolean()) {
                        value = jsonObject.get(key).getAsBoolean() ? context.getString(R.string.yes) : context.getString(R.string.no);
                    } else if(jsonObject.getAsJsonPrimitive(key).isNumber()) {
                        value = jsonObject.get(key).toString();
                    } else {
                        value = jsonObject.get(key).getAsString();
                    }

                    newContent = newContent.replace(key, value);
                }
                return newContent;
            }catch (Exception ignored) {
                return content;
            }
        }
        return content;
    }

    public static String trimSymbol(String data, char symbol) {
        int len = data.length();
        int st = 0;

        while ((st < len) && (data.charAt(st) <= symbol)) {
            st++;
        }
        while ((st < len) && (data.charAt(len - 1) <= symbol)) {
            len--;
        }
        return ((st > 0) || (len < data.length())) ? data.substring(st, len) : data;
    }
}
