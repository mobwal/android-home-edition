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
import android.widget.CheckBox;

/**
 * xtype ‘checkbox’
 * label 'Artichoke Hearts'
 * name 'topping'
 * checked true
 */
class UICheckBoxField extends UIField {
    private CheckBox mCheckBox;

    public UICheckBoxField(Context context, MimicUIParser mimicUIParser) {
        super(context, mimicUIParser);
    }

    @Override
    public void setValue(Object value) {
        setValue(Boolean.parseBoolean(String.valueOf(value)));
    }

    public void setValue(Boolean value) {
        mMimicUIParser.setProperty(Names.CHECKED, String.valueOf(value));
        mCheckBox.setChecked(value);
    }

    @Override
    public Object getValue() {
        return getChecked();
    }

    @Override
    public void setHint(String value) {
        super.setHint(value);

        mCheckBox.setHint(value);
    }

    @Override
    public View getComponentView() {
        if(mCheckBox != null) {
            return mCheckBox;
        }
        mCheckBox = new CheckBox(getContext());

        mCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mMimicUIParser.setProperty(Names.CHECKED, String.valueOf(isChecked));
            change(getValue(), isChecked, UICheckBoxField.this);
        });

        if(getValue() != null && getName() != null) {
            mCheckBox.setTag(getName());
            mCheckBox.setChecked(Boolean.parseBoolean(String.valueOf(getValue())));
        }
        return mCheckBox;
    }

    /**
     * Получение минимальной длины строки
     * @return значение
     */
    public boolean getChecked() {
        Boolean checked = mMimicUIParser.getBooleanValue(Names.CHECKED);
        return checked == null ? false : checked;
    }

    /**
     * Установка выбора
     * @param value значение
     */
    public void setChecked(boolean value) {
        setValue(value);
    }
}
