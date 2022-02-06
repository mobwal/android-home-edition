/*
 * *
 *  * Created by Alexandr Krasnov on 02.04.21 14:20
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 01.04.21 16:41
 *
 */

package pw.appcode.mimic;

interface OnMimicListeners {
    /**
     * Обработчик вывода сообщения
     * @param value значение
     */
    void alert(Object value);

    /**
     * Обработчик вывода логив
     * @param value значение
     */
    void log(Object value);
}
