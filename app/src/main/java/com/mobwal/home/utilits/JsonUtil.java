package com.mobwal.home.utilits;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;

import com.mobwal.home.WalkerApplication;

public class JsonUtil {

    /**
     * Преобразование строки JSON в Hashtable
     * @param json строка в формате JSON
     * @return hashtable
     */
    @NotNull
    public static Hashtable<String, Object> toHashObject(@Nullable String json) {
        Hashtable<String, Object> variables = new Hashtable<>();
        if(json != null) {
            try {
                JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
                for (Map.Entry<String, JsonElement> el : jsonObject.entrySet()) {
                    String key = el.getKey();
                    if (jsonObject.get(key).isJsonNull()) {
                        variables.put(key, null);
                    }

                    if (jsonObject.getAsJsonPrimitive(key).isBoolean()) {
                        variables.put(key, jsonObject.get(key).getAsBoolean());
                    } else if (jsonObject.getAsJsonPrimitive(key).isNumber()) {
                        variables.put(key, jsonObject.get(key).getAsNumber());
                    } else {
                        variables.put(key, jsonObject.get(key).getAsString());
                    }
                }
            } catch (Exception e) {
                WalkerApplication.Log("Ошибка преобразования строки JSON в Hashtable", e);
            }
        }

        return variables;
    }

    /**
     * Преобразование hashtable в JSON
     * @param variables hashtable
     * @return строка в формате JSON
     */
    @Nullable
    public static String toString(@Nullable Hashtable<String, Object> variables) {
        String jb_data = null;
        if(variables != null) {
            try {
                jb_data = new Gson().toJson(variables);
            }catch (Exception e) {
                WalkerApplication.Log("Ошибка преобразования Hashtable в JSON", e);
            }
        }

        return jb_data;
    }
}
