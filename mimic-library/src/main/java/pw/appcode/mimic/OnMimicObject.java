/*
 * *
 *  * Created by Alexandr Krasnov on 02.04.21 14:20
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 01.04.21 16:41
 *
 */

package pw.appcode.mimic;

/**
 * Базовый интерфейс для реализации класс , который должен обрабатываться в скрипте
 */
interface OnMimicObject {
    /**
     * Вызов метода с параметрами
     * @param funcName имя метода
     * @param params параметры
     * @return результат обработки, null если функция ничего не возвращает или ошибка
     */
    Object call(String funcName, Object... params);

    /**
     * Вызов метода
     * @param funcName имя метода
     * @return результат обработки, null если функция ничего не возвращает или ошибка
     */
    Object call(String funcName);
}
