/*
 * *
 *  * Created by Alexandr Krasnov on 02.04.21 18:13
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 02.04.21 18:04
 *
 */

package pw.appcode.mimic;

import java.util.Hashtable;


/**
 * Интерфейс шаблона
 */
interface OnFormLayout {
    /**
     * Инициализация
     * @param layout макет
     * @param variables переменные
     */
    void init(String layout, Hashtable<String, Object> variables);

    /**
     * Получение значений
     * @return значения
     */
    Hashtable<String, Object> getValues();

    /**
     * Установка значений
     * @param values значения
     */
    void setValues(Hashtable<String, Object> values);

    /**
     * Очистка компонента
     */
    void onDestroy();
}
