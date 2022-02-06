/*
 * *
 *  * Created by Alexandr Krasnov on 02.04.21 14:20
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 02.04.21 8:30
 *
 */

package pw.appcode.mimic;

import java.lang.reflect.Method;

/**
 * Реализация интерфейса OnDynamicObject
 */
abstract class MimicObject
        implements OnMimicObject {

    /**
     * Вызов метода с параметрами
     * @param funcName имя метода
     * @param params параметры
     * @return результат обработки, null если функция ничего не возвращает или ошибка
     */
    @Override
    public Object call(String funcName, Object... params) {
        try {
            Class<?>[] classes = new Class[params.length];
            for(int i = 0; i < params.length; i++) {
                classes[i] = params[i].getClass();
            }
            Method method = this.getClass().getMethod(funcName, classes);
            return method.invoke(this, params);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Вызов метода
     * @param funcName имя метода
     * @return результат обработки, null если функция ничего не возвращает или ошибка
     */
    @Override
    public Object call(String funcName) {
        try {
            Method method = this.getClass().getMethod(funcName);
            return method.invoke(this);
        } catch (Exception e) {
            return null;
        }
    }
}
