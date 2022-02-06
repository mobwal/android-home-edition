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
import android.widget.Button;

/**
 * xtype 'button'
 * name 'clicker'
 * text 'Согласен'
 * layout 'hbox'
 * label 'Это описание'
 * onclick {
 *
 * }
 */
class UIButtonField extends UIField {
    private Button mButton;

    public UIButtonField(Context context, MimicUIParser mimicUIParser) {
        super(context, mimicUIParser);
    }

    @Override
    public void setValue(Object value) {

    }

    @Override
    public void setHint(String value) {
        super.setHint(value);

        mButton.setHint(value);
    }

    @Override
    public View getComponentView() {
        if(mButton != null) {
            return mButton;
        }
        mButton = new Button(getContext());
        mButton.setOnClickListener(v -> click(UIButtonField.this));

        String name = getName();
        if(name != null) {
            mButton.setTag(name);
        }

        String text = getText();
        if(text != null) {
            mButton.setText(text);
        }
        return mButton;
    }

    /**
     * Получить текст кнопки
     * @return текст
     */
    public String getText() {
        String text = mMimicUIParser.getStringValue(Names.TEXT);
        return text == null ? getContext().getString(R.string.button) : text;
    }

    /**
     * Установить текст кнопки
     * @param value текст
     */
    public void setText(String value) {
        mMimicUIParser.setProperty(Names.TEXT, String.valueOf(value));
        mButton.setText(value);
    }
}
