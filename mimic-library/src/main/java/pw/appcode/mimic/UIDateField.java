/*
 * *
 *  * Created by Alexandr Krasnov on 02.04.21 18:13
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 02.04.21 18:04
 *
 */

package pw.appcode.mimic;

import android.app.DatePickerDialog;
import android.content.Context;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * xtype 'datefield'
 * label 'From'
 * name 'from_date'
 * maxValue '2021-03-11'
 * minValue '2021-01-01'
 */
class UIDateField extends UIField {
    private static final String SYSTEM_FORMAT = "yyyy-MM-dd";

    private EditText mTextView;
    private Button mSelectButton;
    private DatePickerDialog mDatePickerDialog;

    private LinearLayout mLinearLayout;

    public UIDateField(Context context, MimicUIParser mimicUIParser) {
        super(context, mimicUIParser);
    }

    @Override
    public Object getValue() {
        String str = mMimicUIParser.getStringValue(Names.VALUE);
        String value = str == null ? "" : str;
        if(value.equals(Names.DATE_NOW)) {
            return DateFormat.format(SYSTEM_FORMAT, new Date().getTime());
        } else {
            return value;
        }
    }

    @Override
    public void setValue(Object value) {
        setValue(String.valueOf(value));
    }

    public void setValue(String value) {
        mMimicUIParser.setProperty(Names.VALUE, value);
        SimpleDateFormat dateFormat = new SimpleDateFormat(SYSTEM_FORMAT, Locale.getDefault());
        try {
            Date dt = dateFormat.parse((String) getValue());
            mTextView.setText(DateFormat.format(getFormat(), dt).toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setDisabled(boolean disabled) {
        mMimicUIParser.setProperty(Names.DISABLED, String.valueOf(disabled));
        mSelectButton.setEnabled(!disabled);
    }

    @Override
    public void setHidden(boolean hidden) {
        if(mSelectButton != null && mTextView != null) {
            mSelectButton.setVisibility(hidden ? View.GONE : View.VISIBLE);
            mTextView.setVisibility(hidden ? View.GONE : View.VISIBLE);
        }

        if(getLabelView() != null) {
            getLabelView().setVisibility(hidden ? View.GONE : View.VISIBLE);
        }

        mMimicUIParser.setProperty(Names.HIDDEN, String.valueOf(hidden));
    }

    public String getFormat() {
        String format = mMimicUIParser.getStringValue(Names.FORMAT);
        return format == null ? "" : format;
    }

    @Override
    public View getComponentView() {
        if(mLinearLayout != null) {
            return mLinearLayout;
        }

        mLinearLayout = new LinearLayout(getContext());
        mLinearLayout.setOrientation(LinearLayout.HORIZONTAL);

        mTextView = new EditText(getContext());
        mTextView.setTextColor(MimicUtil.getColor(getContext(), android.R.attr.textColor));
        mTextView.setEnabled(false);

        if(getValue() != null && getName() != null) {
            mTextView.setTag(getName());
            setValue(getValue());
        }

        mSelectButton = new Button(getContext());
        mSelectButton.setText(R.string.select);

        mSelectButton.setOnClickListener(v -> {
            final Calendar cldr = Calendar.getInstance();

            Date value;
            SimpleDateFormat dateFormat = new SimpleDateFormat(SYSTEM_FORMAT, Locale.getDefault());
            try {
                value = dateFormat.parse(String.valueOf(getValue()));
                if(value != null) {
                    cldr.setTime(value);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            int day = cldr.get(Calendar.DAY_OF_MONTH);
            int month = cldr.get(Calendar.MONTH);
            int year = cldr.get(Calendar.YEAR);
            // date picker dialog
            mDatePickerDialog = new DatePickerDialog(getContext(),
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        Calendar calendar = new GregorianCalendar(year1, monthOfYear, dayOfMonth);
                        String dateStr = DateFormat.format(SYSTEM_FORMAT, calendar.getTime()).toString();
                        setValue(dateStr);

                        change(getValue(), dateStr, UIDateField.this);
                    }, year, month, day);
            mDatePickerDialog.show();
        });

        mLinearLayout.addView(mTextView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f));
        mLinearLayout.addView(mSelectButton, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));

        return mRootView = mLinearLayout;
    }

    @Override
    public String isValid() {
        if(getRequire() && getValue() != null && String.valueOf(getValue()).equals("")) {
            String msg = getString(R.string.require_field);
            mTextView.setError(MimicUIParser.trimString(msg, Names.TRIM_STRING_SYMBOL));
            return convertMimicString(msg);
        }

        if(getValue() != null) {
            Date value;
            SimpleDateFormat dateFormat = new SimpleDateFormat(getFormat(), Locale.getDefault());
            try {
                value = dateFormat.parse(String.valueOf(getValue()));
                if (value != null && value.getTime() < getMinValue().getTime()) {
                    String msg = getString(R.string.min_value) + " " + DateFormat.format(getFormat(), getMinValue());
                    mTextView.setError(MimicUIParser.trimString(msg, Names.TRIM_STRING_SYMBOL));
                    return convertMimicString(msg);
                }

                if (value != null && value.getTime() > getMaxValue().getTime()) {
                    String msg = getString(R.string.max_value) + " " + DateFormat.format(getFormat(), getMaxValue());
                    mTextView.setError(MimicUIParser.trimString(msg, Names.TRIM_STRING_SYMBOL));
                    return convertMimicString(msg);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        mTextView.setError(null);
        return "";
    }

    /**
     * Получение максимальной длины строки
     * @return значение
     */
    public Date getMaxValue() {
        Date dt = new GregorianCalendar(2100, 11, 1).getTime();
        if (mMimicUIParser.getStringValue(Names.MAX_VALUE) != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(SYSTEM_FORMAT, Locale.getDefault());
            try {
                return dateFormat.parse(mMimicUIParser.getStringValue(Names.MAX_VALUE));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return dt;
    }

    /**
     * Установка максимальной длины строки
     * @param value значение
     */
    public void setMaxValue(String value) {
        mMimicUIParser.setProperty(Names.MAX_VALUE, String.valueOf(value));
    }

    /**
     * Получение минимального значения
     * @return значение
     */
    public Date getMinValue() {
        Date dt = new GregorianCalendar(1900, 0, 1).getTime();
        if (mMimicUIParser.getStringValue(Names.MIN_VALUE) != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(SYSTEM_FORMAT, Locale.getDefault());
            try {
                return dateFormat.parse(mMimicUIParser.getStringValue(Names.MIN_VALUE));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return dt;
    }

    /**
     * Установка минимального значения
     * @param value значение
     */
    public void setMinValue(String value) {
        mMimicUIParser.setProperty(Names.MIN_VALUE, String.valueOf(value));
    }
}
