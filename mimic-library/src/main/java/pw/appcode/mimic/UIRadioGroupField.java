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
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * xtype ‘radiogroup’
 * label 'Artichoke Hearts'
 * name 'topping'
 * items [
 *        {
 * 		text ‘Муж’
 * 		value 1
 * 		checked true
 *    }
 *    {
 * 		text ‘Котенок’
 * 		value 2
 *    }
 * ]
 * oninit {
 *
 * }
 * onchange {
 *
 * }
 */
class UIRadioGroupField extends UIField
        implements View.OnClickListener {
    private RadioGroup mRadioGroup;
    private List<RadioButton> mRadioButtons;

    public UIRadioGroupField(Context context, MimicUIParser mimicUIParser) {
        super(context, mimicUIParser);
    }

    @Override
    public void setValue(Object value) {
        mMimicUIParser.setProperty(Names.VALUE, String.valueOf(value));

        for (RadioButton radioButton: mRadioButtons) {
            radioButton.setChecked(radioButton.getTag().equals(value));
        }
    }

    @Override
    public View getComponentView() {
        if(mRadioGroup != null) {
            return mRadioGroup;
        }

        mRadioGroup = new RadioGroup(getContext());

        if(mMimicUIParser.isArray(Names.ITEMS)) {
            mRadioButtons = new ArrayList<>();
            MimicUIParser[] mimicUIParsers = mMimicUIParser.getArray(Names.ITEMS);
            for(MimicUIParser parser : mimicUIParsers) {
                if(parser.isProperty(Names.TEXT) && parser.isProperty(Names.VALUE)) {
                    RadioButton newRadioButton = new RadioButton(getContext());
                    newRadioButton.setText(parser.getStringValue(Names.TEXT));
                    newRadioButton.setOnClickListener(this);
                    newRadioButton.setTag(parser.getStringValue(Names.VALUE));

                    newRadioButton.setChecked(parser.isProperty(Names.CHECKED) && parser.getBooleanValue(Names.CHECKED));

                    mRadioGroup.addView(newRadioButton);
                    mRadioButtons.add(newRadioButton);
                }
            }
        }

        if(getValue() != null && getName() != null) {
            mRadioGroup.setTag(getName());
        }
        return mRadioGroup;
    }

    @Override
    public void onClick(View v) {
        RadioButton rb = (RadioButton)v;
        for (RadioButton radioButton: mRadioButtons) {
            radioButton.setChecked(radioButton.equals(rb));

            if(radioButton.equals(rb)) {
                mMimicUIParser.setProperty(Names.VALUE, String.valueOf(rb.getTag()));
                change(getValue(), String.valueOf(rb.getTag()), UIRadioGroupField.this);
            }
        }
    }
}
