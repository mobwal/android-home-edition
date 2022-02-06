/*
 * *
 *  * Created by Alexandr Krasnov on 02.04.21 18:13
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 02.04.21 18:11
 *
 */

package pw.appcode.mimic;

import android.view.View;
import android.widget.TextView;

public interface OnField extends OnContainer {
    /**
     * Наименование поля
     * @return наименование
     */
    String getName();

    /**
     * Получение значения
     * @return значение
     */
    Object getValue();

    /**
     * Установка значения
     * @param value значение
     */
    void setValue(Object value);

    /**
     * Получение метки (описание)
     * @return метка
     */
    String getLabel();

    /**
     * Получение подсказки
     * @return подсказка
     */
    String getHint();

    /**
     * Установка подсказки
     * @param value подсказка
     */
    void setHint(String value);

    /**
     * Устанвка метки (описания)
     * @param label метка
     */
    void setLabel(String label);

    /**
     * Является обязательным для заполнения
     * @return обязательность
     */
    boolean getRequire();

    /**
     * Устанаовка обязательности
     * @param value обязательность
     */
    void setRequire(boolean value);

    /**
     * Отключен или нет
     * @return значение
     */
    boolean getDisabled();

    /**
     * Отключен или нет
     * @param disabled значение
     */
    void setDisabled(boolean disabled);

    /**
     * Видим элемент или нет
     * @return видимость
     */
    boolean getHidden();

    /**
     * Устанаовка видимости
     * @param hidden true- скрыт
     */
    void setHidden(boolean hidden);

    /**
     * Конечное изменение
     * @param old старое значение
     * @param current новое значение
     * @param e текущий объект
     */
    void change(Object old, Object current, OnMimicObject e);

    /**
     * Нажатие на поле
     * @param e текущий объект
     */
    void click(OnMimicObject e);

    /**
     * Элемент отображения
     * @return элемент
     */
    View getComponentView();

    /**
     * Получение описания
     * @return представление
     */
    TextView getLabelView();

    /**
     * Проверка валидности данных
     * @return Если строка не пустая, то значит есть ошибка
     */
    String isValid();
}
