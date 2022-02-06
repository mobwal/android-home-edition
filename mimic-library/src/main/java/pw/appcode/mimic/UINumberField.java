/*
 * *
 *  * Created by Alexandr Krasnov on 02.04.21 18:13
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 02.04.21 18:04
 *
 */

package pw.appcode.mimic;

import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * xtype 'numberfield'
 * name 'bottles'
 * label 'Bottles of Beer'
 * value 99
 * maxValue 99
 * minValue 0
 * hint 'Ввести номер'
 * oninit {
 *
 * }
 * onchange {
 *
 * }
 */
class UINumberField extends UIEditField {
    public UINumberField(Context context, MimicUIParser mimicUIParser) {
        super(context, mimicUIParser);
    }

    @Override
    public View getComponentView() {
        EditText editText = (EditText) super.getComponentView();
        if(getDecimalPrecision() > 0) {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        } else {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        }

        return editText;
    }

    @Override
    public String isValid() {
        String msg = super.isValid();
        if(!msg.isEmpty()) {
            mEditView.setError(MimicUIParser.trimString(msg, "'"));
            return msg;
        }

        if(getValue() != null) {
            double value = String.valueOf(getValue()).isEmpty() ? 0 : Double.parseDouble(String.valueOf(getValue()));
            if(getMinValue() != -1 || getMaxValue() != -1) {
                if (getMinValue() != -1 && value < getMinValue()) {
                    msg = getString(R.string.min_value) + " " + getMinValue();
                    mEditView.setError(msg);
                    return convertMimicString(msg);
                }

                if (getMaxValue() != -1 && value > getMaxValue()) {
                    msg = getString(R.string.max_value) + " " + getMaxValue();
                    mEditView.setError(msg);
                    return convertMimicString(msg);
                }
            }

            if(getDecimalPrecision() > 0) {
                Pattern pattern = Pattern.compile("(\\d+)([.,])*(\\d+)*", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(String.valueOf(getValue()));
                if(matcher.find()) {
                    String decimalPrecision = matcher.group(3);
                    if(decimalPrecision != null && decimalPrecision.length() > getDecimalPrecision()) {
                        msg = getString(R.string.max_value) + " " +  + getDecimalPrecision();
                        mEditView.setError(msg);
                        return convertMimicString(msg);
                    }
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
    public int getMinValue() {
        String minValue = mMimicUIParser.getStringValue(Names.MIN_VALUE);
        return minValue == null ? -1 : Integer.parseInt(minValue);
    }

    /**
     * Установка минимальной длины строки
     * @param value значение
     */
    public void setMinValue(int value) {
        mMimicUIParser.setProperty(Names.MIN_VALUE, String.valueOf(value));
    }

    /**
     * Получение максимальной длины строки
     * @return значение
     */
    public int getMaxValue() {
        String maxValue = mMimicUIParser.getStringValue(Names.MAX_VALUE);
        return maxValue == null ? -1 : Integer.parseInt(maxValue);
    }

    /**
     * Установка максимальной длины строки
     * @param value значение
     */
    public void setMaxValue(int value) {
        mMimicUIParser.setProperty(Names.MAX_VALUE, String.valueOf(value));
    }

    public int getDecimalPrecision() {
        Integer decimalPrecision = mMimicUIParser.getIntegerValue(Names.DECIMAL_PRECISION);
        return decimalPrecision == null ? -1 : decimalPrecision;
    }

    public void setDecimalPrecision(int decimal) {
        if(decimal > 0) {
            mEditView.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        } else {
            mEditView.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
        mMimicUIParser.setProperty(Names.DECIMAL_PRECISION, String.valueOf(decimal));
    }
}
