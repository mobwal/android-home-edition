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
import android.widget.TextView;

/**
 * xtype 'displayfield'
 * label 'Home'
 * name 'home_score'
 * value '10'
 */
class UIDisplayField extends UIField {
    protected TextView mEditView;

    public UIDisplayField(Context context, MimicUIParser mimicUIParser) {
        super(context, mimicUIParser);
    }

    @Override
    public void setValue(Object value) {
        setValue(String.valueOf(value));
    }

    public void setValue(String value) {
        mEditView.setText(MimicUIParser.trimString(value, Names.TRIM_STRING_SYMBOL));
    }

    @Override
    public View getComponentView() {
        if(mEditView != null) {
            return mEditView;
        }
        mEditView = new TextView(getContext());
        mEditView.setTextColor(MimicUtil.getColor(getContext(), android.R.attr.textColor));
        if(getValue() != null) {
            mEditView.setTag(getName());
            mEditView.setText((String) getValue());
        }
        return mEditView;
    }
}
