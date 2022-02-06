/*
 * *
 *  * Created by Alexandr Krasnov on 02.04.21 18:13
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 02.04.21 18:11
 *
 */

package pw.appcode.mimic;

import android.content.Context;
import android.view.View;

interface OnContainer {
    /**
     * Тип поля
     * @return тип
     */
    String getType();

    /**
     * Расположение
     * @return горизонтальное или вертикальное
     */
    String getOrientation();

    /**
     * дочерние элементы
     * @return получение дочерних элементов
     */
    OnContainer[] getItems();

    /**
     * Контекст
     * @return контекст
     */
    Context getContext();

    /**
     * Получение представления
     * @return представление
     */
    View getView();

    /**
     * инициализация
     */
    void init();

    /**
     * Строка компонента
     * @return строка
     */
    String toComponentString();
}
