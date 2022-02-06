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
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Базовый класс для компонентов
 */
abstract class UIField extends UIContainer
        implements OnField {

    protected TextView mLabelView;

    public UIField(Context context, MimicUIParser mimicUIParser) {
        super(context, mimicUIParser);
    }

    @Override
    public View getView() {
        if(mRootView == null) {
            if(!(getComponentView() instanceof Spinner)) {
                getComponentView().setOnClickListener(v -> click(this));
            }

            if (getLabel() != null && !getLabel().isEmpty()) {
                LinearLayout linearLayout = new LinearLayout(getContext());
                boolean horizontal_orientation = getOrientation().equals(Names.H_BOX);
                linearLayout.setOrientation(horizontal_orientation ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);

                if (horizontal_orientation) {
                    linearLayout.addView(getLabelView(), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f));
                    linearLayout.addView(getComponentView(), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f));
                } else {
                    linearLayout.addView(getLabelView(), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    linearLayout.addView(getComponentView(), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                }

                return mRootView = linearLayout;
            } else {
                mRootView = getComponentView();
                return mRootView = getComponentView();
            }
        } else {
            return mRootView;
        }
    }

    @Override
    public String getName() {
        return mMimicUIParser.getStringValue(Names.NAME);
    }

    @Override
    public String getLabel() {
        String label = mMimicUIParser.getStringValue(Names.LABEL);
        return label == null ? "" : label;
    }

    @Override
    public Object getValue() {
        String value = mMimicUIParser.getStringValue(Names.VALUE);
        return value == null ? "" : value;
    }

    @Override
    public boolean getRequire() {
        Boolean require = mMimicUIParser.getBooleanValue(Names.REQUIRE);
        return require == null ? false : require;
    }

    @Override
    public boolean getDisabled() {
        Boolean disabled = mMimicUIParser.getBooleanValue(Names.DISABLED);
        return disabled == null ? false : disabled;
    }

    @Override
    public boolean getHidden() {
        Boolean hidden = mMimicUIParser.getBooleanValue(Names.HIDDEN);
        return hidden == null ? false : hidden;
    }

    @Override
    public void setHidden(boolean hidden) {
        if(getComponentView() != null) {
            getComponentView().setVisibility(hidden ? View.GONE : View.VISIBLE);
        }

        if(getLabelView() != null) {
            getLabelView().setVisibility(hidden ? View.GONE : View.VISIBLE);
        }

        mMimicUIParser.setProperty(Names.HIDDEN, String.valueOf(hidden));
    }

    @Override
    public void setLabel(String label) {
        getLabelView().setText(label);

        mMimicUIParser.setProperty(Names.LABEL,convertMimicString(label));
    }

    @Override
    public void setDisabled(boolean disabled) {
        getComponentView().setEnabled(!disabled);

        mMimicUIParser.setProperty(Names.DISABLED, String.valueOf(disabled));
    }

    @Override
    public void setRequire(boolean value) {
        mMimicUIParser.setProperty(Names.REQUIRE, String.valueOf(value));
    }

    /**
     * Получение подсказки
     * @return подсказка
     */
    @Override
    public String getHint() {
        String hint = mMimicUIParser.getStringValue(Names.HINT);
        return hint == null ? "" : hint;
    }

    /**
     * Установка подсказки
     * @param value подсказка
     */
    @Override
    public void setHint(String value) {
        mMimicUIParser.setProperty(Names.HINT, convertMimicString(value));
    }

    /**
     * Получение описания
     * @return представление
     */
    public TextView getLabelView() {
        if(mLabelView != null) {
            return mLabelView;
        }
        TextView tvLabel = new TextView(getContext());
        tvLabel.setText(getLabel());
        tvLabel.setTextColor(MimicUtil.getColor(getContext(), android.R.attr.textColorSecondary));
        return mLabelView = tvLabel;
    }

    @Override
    public void click(OnMimicObject e) {
        if(mMimicUIParser.isEvent(Names.EVENT_CLICK)) {
            onCompiler(Names.EVENT_CLICK, e);
        }
    }

    @Override
    public void change(Object old, Object current, OnMimicObject e) {
        if(mMimicUIParser.isEvent(Names.EVENT_CHANGE)) {
            onCompiler(Names.EVENT_CHANGE, e);
        }
    }

    /**
     * Проверка валидности данных
     * @return Если строка не пустая, то значит есть ошибка
     */
    public String isValid() {
        return "";
    }

    /**
     * Преобразование строки
     * @param msg строка с сообщением
     * @return строка
     */
    protected String convertMimicString(String msg) {
        return Names.TRIM_STRING_SYMBOL + msg + Names.TRIM_STRING_SYMBOL;
    }
}
