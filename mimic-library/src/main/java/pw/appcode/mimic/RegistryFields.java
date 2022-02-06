/*
 * *
 *  * Created by Alexandr Krasnov on 02.04.21 18:17
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 02.04.21 18:17
 *
 */

package pw.appcode.mimic;

import java.util.Hashtable;

/**
 * Регистрация пользовательских полей
 */
public class RegistryFields {
    private static final Hashtable<String, OnField> sFields = new Hashtable<>();

    /**
     * Добавление поля
     * @param name имя поля
     * @param field поля
     */
    public static void put(String name, OnField field) {
        sFields.put(name, field);
    }

    /**
     * Получение поля
     * @param name имя поля
     * @return поле
     */
    public static OnField pull(String name) {
        return sFields.get(name);
    }

    /**
     * Доступность поля
     * @param name имя поля
     * @return доступность
     */
    public static boolean exists(String name) {
        return sFields.containsKey(name);
    }
}
