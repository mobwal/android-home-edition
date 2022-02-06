/*
 * *
 *  * Created by Alexandr Krasnov on 02.04.21 18:13
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 02.04.21 18:07
 *
 */

package pw.appcode.mimic;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

abstract class BaseFormLayout extends LinearLayout
        implements OnFormLayout {

    public static boolean isSimpleLayout(String layout) {
        return MimicUIParser.isSimpleLayout(layout);
    }

    public static String convertToMimicUIParser(String layout) {
        return MimicUIParser.convertToMimicUIParser(layout);
    }

    public static String[] getItems(String layout) {
        MimicUIParser parser = new MimicUIParser(layout);
        MimicUIParser[] uiParsers = parser.getArray(Names.ITEMS);
        List<String> list = new ArrayList<>();

        for (MimicUIParser uiParser:
                uiParsers) {
            String name = uiParser.getStringValue(Names.NAME);
            list.add(name);
        }

        return list.toArray(new String[0]);
    }

    private UIContainer mUIContainer;

    public BaseFormLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /**
     * Инициализация
     * @param layout макет
     * @param variables переменные
     */
    @Override
    public void init(String layout, Hashtable<String, Object> variables) {
        if(MimicUIParser.isSimpleLayout(layout)) {
            layout = MimicUIParser.convertToMimicUIParser(layout);
        }

        try {
            for (String key : variables.keySet()) {
                MimicVariable mimicVariable = MimicVariable.getSimple(key, variables.get(key));
                MimicGlobalVariable.getInstance().push(mimicVariable);
            }

            mUIContainer = new UIContainer(getContext(), layout);

            LinearLayout linearLayout = new LinearLayout(getContext());
            boolean horizontal_orientation = mUIContainer.getOrientation().equals(Names.H_BOX);
            linearLayout.setOrientation(horizontal_orientation ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
            addView(linearLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            OnContainer[] containers = mUIContainer.getItems();

            if(containers.length > 0) {
                for (OnContainer container: containers) {
                    if(container != null) {
                        try {
                            View control = container.getView();

                            if (horizontal_orientation) {
                                LinearLayout.LayoutParams layoutParams;
                                if (control instanceof LinearLayout) {
                                    layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
                                } else {
                                    layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
                                }

                                linearLayout.addView(control, layoutParams);
                            } else {
                                linearLayout.addView(control, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                            }
                            container.init();
                        }catch (Exception e) {
                            TextView textView = new TextView(getContext());
                            String msg = getContext().getResources().getString(R.string.component_error) + " " + container.toComponentString();
                            textView.setText(msg);
                            textView.setTextColor(MimicUtil.getColor(getContext(), R.attr.colorError));

                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            linearLayout.addView(textView, layoutParams);
                        }
                    }
                }
            }

            mUIContainer.init();
        } catch (Exception e) {
            ScrollView linearLayout = new ScrollView(getContext());

            TextView textView = new TextView(getContext());
            String msg = getContext().getString(R.string.exception) + e.getMessage() + "\n\n" + layout;
            textView.setText(msg);
            textView.setTextColor(MimicUtil.getColor(getContext(), R.attr.colorError));

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            linearLayout.addView(textView, layoutParams);

            removeAllViews();
            addView(linearLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }

    /**
     * Получить значение с формы
     * @return данные
     */
    @Override
    @Nullable
    public Hashtable<String, Object> getValues() {
        OnContainer[] containers = mUIContainer.getItems();
        if(containers.length > 0) {
            Hashtable<String, Object> values = new Hashtable<>();
            for (OnContainer container : containers) {
                if(container instanceof OnField) {
                    values.put(((OnField)container).getName(), ((OnField)container).getValue());
                }
            }
            return values;
        }
        return null;
    }

    /**
     * Установить значение
     * @param values значения
     */
    @Override
    public void setValues(Hashtable<String, Object> values) {
        OnContainer[] containers = mUIContainer.getItems();
        if(containers.length > 0) {
            for (OnContainer container : containers) {
                if (container instanceof OnField) {
                    Object value = values.get(((OnField)container).getName());
                    ((OnField)container).setValue(value);
                }
            }
        }
    }

    /**
     * Сохранить состояние
     * @return объект
     */
    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Names.SUPER_STATE, super.onSaveInstanceState());
        Hashtable<String, Object> values = getValues();
        bundle.putSerializable(Names.FORM_VALUES, values);
        return bundle;
    }

    /**
     * Восстановление состояния
     * @param state состояние
     */
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        try {
            if (state instanceof Bundle) {
                Bundle bundle = (Bundle) state;
                @SuppressWarnings("unchecked")
                Hashtable<String, Object> values = (Hashtable<String, Object>) bundle.getSerializable(Names.FORM_VALUES);
                setValues(values);
                state = bundle.getParcelable(Names.SUPER_STATE);
            }
        } catch (ClassCastException ignored) {

        } finally {
            super.onRestoreInstanceState(state);
        }
    }

    /**
     * Удаление объекта
     */
    @Override
    public void onDestroy() {
        MimicGlobalVariable.getInstance().clearAll();
    }
}
