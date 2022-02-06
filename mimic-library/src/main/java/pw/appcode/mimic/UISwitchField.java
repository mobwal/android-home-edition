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

import androidx.appcompat.widget.SwitchCompat;

/**
 * xtype 'switchfield'
 * label 'Artichoke Hearts'
 * name 'topping'
 * checked true
 * oninit {
 *
 * }
 * onchange {
 *
 * }
 */
class UISwitchField extends UICheckBoxField {
    private SwitchCompat mSwitchCompat;

    public UISwitchField(Context context, MimicUIParser mimicUIParser) {
        super(context, mimicUIParser);
    }

    @Override
    public void setValue(Boolean value) {
        mMimicUIParser.setProperty(Names.CHECKED, String.valueOf(value));
        mSwitchCompat.setChecked(value);
    }

    @Override
    public void setHint(String value) {
        super.setHint(value);

        mSwitchCompat.setHint(value);
    }

    @Override
    public View getComponentView() {
        if(mSwitchCompat != null) {
            return mSwitchCompat;
        }
        mSwitchCompat = new SwitchCompat(getContext());

        mSwitchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mMimicUIParser.setProperty(Names.CHECKED, String.valueOf(isChecked));
            change(getValue(), isChecked, UISwitchField.this);
        });

        if(getValue() != null && getName() != null) {
            mSwitchCompat.setTag(getName());
            setValue(getValue());
        }

        return mSwitchCompat;
    }
}
