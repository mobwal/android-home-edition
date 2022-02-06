/*
 * *
 *  * Created by Alexandr Krasnov on 02.04.21 18:13
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 02.04.21 18:04
 *
 */

package pw.appcode.mimic;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;

/**
 * xtype 'textview'
 * name 'c_address'
 * value ''
 * label 'Адрес'
 * hint 'Ввести адрес'
 * minLength 0
 * maxLength 100
 * oninit {
 *
 * }
 * onchange {
 *
 * }
 */
class UITextView extends UIEditField {
    protected EditText mEditView;

    public UITextView(Context context, MimicUIParser mimicUIParser) {
        super(context, mimicUIParser);
    }

    @Override
    public void setValue(Object value) {
        if(value != null) {
            setValue(String.valueOf(value));
        }
    }

    public void setValue(String value) {
        mEditView.setText(MimicUIParser.trimString(value, Names.TRIM_STRING_SYMBOL));
    }

    @Override
    public void setHint(String value) {
        super.setHint(value);
        mEditView.setHint(value);
    }

    @Override
    public View getComponentView() {
        if(mEditView != null) {
            return mEditView;
        }
        mEditView = new EditText(getContext());
        mEditView.setTextColor(MimicUtil.getColor(getContext(), android.R.attr.textColor));
        mEditView.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        mEditView.setHint(getHint());
        mEditView.setLines(3);
        mEditView.setGravity(Gravity.TOP);

        UIField uiField = this;
        mEditView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mMimicUIParser.setProperty(Names.VALUE, s.toString());
                change(getValue(), s.toString(), uiField);
            }
        });

        if(getValue() != null && getName() != null) {
            mEditView.setTag(getName());
            mEditView.setText((String) getValue());
        }
        return mEditView;
    }

    @Override
    public String isValid() {
        if(getRequire() && getValue() != null && String.valueOf(getValue()).equals("")) {
            String msg = getContext().getString(R.string.require_field);
            mEditView.setError(msg);
            return convertMimicString(msg);
        }

        if(getValue() != null) {
            String value = String.valueOf(getValue());
            if(getMinLength() != -1 || getMaxLength() != -1) {
                if (getMinLength() != -1 && value.length() < getMinLength()) {
                    String msg = getContext().getString(R.string.error_min_value) + " " + getMinLength();
                    mEditView.setError(msg);
                    return convertMimicString(msg);
                }

                if (getMaxLength() != -1 && value.length() > getMaxLength()) {
                    String msg = getContext().getString(R.string.error_max_value) + " " + getMaxLength();
                    mEditView.setError(msg);
                    return convertMimicString(msg);
                }
            }
        }

        mEditView.setError(null);
        return "";
    }

    /**
     * Получение минимальной длины строки
     * @return значение
     */
    public int getMinLength() {
        String minLength = mMimicUIParser.getStringValue(Names.MIN_LENGTH);
        return minLength == null ? -1 : Integer.parseInt(minLength);
    }

    /**
     * Установка минимальной длины строки
     * @param value значение
     */
    public void setMinLength(int value) {
        mMimicUIParser.setProperty(Names.MIN_LENGTH, String.valueOf(value));
    }

    /**
     * Получение максимальной длины строки
     * @return значение
     */
    public int getMaxLength() {
        String maxLength = mMimicUIParser.getStringValue(Names.MAX_LENGTH);
        return maxLength == null ? -1 : Integer.parseInt(maxLength);
    }

    /**
     * Установка максимальной длины строки
     * @param value значение
     */
    public void setMaxLength(int value) {
        mMimicUIParser.setProperty(Names.MAX_LENGTH, String.valueOf(value));
    }
}
