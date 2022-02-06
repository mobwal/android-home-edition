/*
 * *
 *  * Created by Alexandr Krasnov on 02.04.21 18:13
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 02.04.21 18:04
 *
 */

package pw.appcode.mimic;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import java.util.Hashtable;

/**
 * Шаблон формы
 */
public class SimpleFormLayout extends BaseFormLayout {

    public static boolean isSimpleLayout(String layout) {
        return BaseFormLayout.isSimpleLayout(layout);
    }

    public static String convertToMimicUIParser(String layout) {
        return BaseFormLayout.convertToMimicUIParser(layout);
    }

    public SimpleFormLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public static String[] getItems(String layout) {
        return BaseFormLayout.getItems(layout);
    }

    /**
     * Инициализация
     * @param layout шаблон
     * @param variables значения
     */
    @Override
    public void init(String layout, Hashtable<String, Object> variables) {
        super.init(layout, variables);
    }

    /**
     * Получение значений
     * @return значения
     */
    @Override
    @Nullable
    public Hashtable<String, Object> getValues() {
        return super.getValues();
    }

    /**
     * Установка значений
     * @param values значения
     */
    @Override
    public void setValues(Hashtable<String, Object> values) {
        super.setValues(values);
    }

    /**
     * Удаление объекта
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
