/*
 * *
 *  * Created by Alexandr Krasnov on 02.04.21 18:13
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 02.04.21 18:04
 *
 */

package pw.appcode.mimic;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

/**
 * xtype ‘combo’
 * label 'Choose State',
 * store [
 *        {
 * 		name ‘Саша’
 * 		addr ‘Cheboksary
 *    }
 * ]
 * displayField 'name'
 * valueField 'abbr'
 */
class UIComboBoxField extends UIField {
    private Spinner mCombo;

    public UIComboBoxField(Context context, MimicUIParser mimicUIParser) {
        super(context, mimicUIParser);
    }

    @Override
    public void setValue(Object value) {
        mMimicUIParser.setProperty(Names.VALUE, String.valueOf(value));

        mCombo.setSelection(getPositionByValue(String.valueOf(value)));
    }

    @Override
    public View getComponentView() {
        if(mCombo != null) {
            return mCombo;
        }
        mCombo = new Spinner(getContext());
        mCombo.setAdapter(getStore());
        mCombo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mMimicUIParser.setProperty(Names.VALUE, String.valueOf(getKeyByValue(String.valueOf(parent.getSelectedItem()))));
                change(getValue(), parent.getSelectedItem(), UIComboBoxField.this);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        if(getValue() != null && getName() != null) {
            mCombo.setTag(getName());
            mCombo.setSelection(getPositionByValue(String.valueOf(getValue())));
        }
        return mCombo;
    }

    /**
     * Получение имени поля для хранения значений для пользователя
     * @return имя поля
     */
    public String getDisplayField() {
        String value = mMimicUIParser.getStringValue(Names.DISPLAY_FIELD);
        return value == null ? Names.VALUE : value;
    }

    /**
     * Установка значения
     * @param displayField значение
     */
    public void setDisplayField(String displayField) {
        mMimicUIParser.setProperty(Names.DISPLAY_FIELD, displayField);
    }

    /**
     * Получение имени поля для хранения значений
     * @return имя поля
     */
    public String getValueField() {
        String value = mMimicUIParser.getStringValue(Names.VALUE_FIELD);
        return value == null ? Names.KEY : value;
    }

    /**
     * Установка значения
     * @param valueField значение
     */
    public void setValueField(String valueField) {
        mMimicUIParser.setProperty(Names.VALUE_FIELD, valueField);
    }

    /**
     * Получение хранилища
     * @return хранилище
     */
    public ArrayAdapter<String> getStore() {
        MimicUIParser[] mimicUIParsers = mMimicUIParser.getArray(Names.STORE);
        List<String> items = new ArrayList<>();
        String displayField = getDisplayField();
        for (MimicUIParser parser: mimicUIParsers) {
            if(parser.isProperty(displayField)) {
                items.add(parser.getStringValue(displayField));
            }
        }

        return new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, items);
    }

    /**
     * Получение ключа по переданному значению
     * @param value значение
     * @return ключ
     */
    private Object getKeyByValue(String value) {
        MimicUIParser[] mimicUIParsers = mMimicUIParser.getArray(Names.STORE);
        String displayField = getDisplayField();
        for (MimicUIParser parser: mimicUIParsers) {
            if(parser.isProperty(displayField)) {
                if(value.equals(parser.getStringValue(displayField))) {
                    return parser.getStringValue(getValueField());
                }
            }
        }

        return null;
    }

    /**
     * Получение позиции по значению отображаемому для пользователя
     * @param value значение
     * @return позиция в списке
     */
    private int getPositionByValue(String value) {
        int i = 0;
        if(value.isEmpty()) {
            return i;
        }

        MimicUIParser[] mimicUIParsers = mMimicUIParser.getArray(Names.STORE);
        String displayField = getDisplayField();
        for (MimicUIParser parser: mimicUIParsers) {
            if(parser.isProperty(displayField)) {
                i++;
                if(value.equals(parser.getStringValue(displayField))) {
                    return i;
                }
            }
        }

        return i;
    }

}
