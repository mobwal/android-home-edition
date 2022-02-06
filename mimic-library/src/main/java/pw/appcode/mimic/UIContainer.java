/*
 * *
 *  * Created by Alexandr Krasnov on 02.04.21 18:13
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 02.04.21 18:04
 *
 */

package pw.appcode.mimic;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.View;

import java.util.Hashtable;

/**
 * layout 'hbox'
 * items [
 *             {
 *                 	xtype 'displayfield'
 *               	label 'Home'
 *         	        name 'home_score'
 *         	        value '10'
 *             }
 *             {
 *                 	xtype 'displayfield'
 *               	label 'Premise'
 *         	        name 'premise_score'
 *         	        value '1A'
 *             }
 * ]
 */
class UIContainer extends MimicObject
        implements OnContainer, OnMimicListeners {
    private final String TAG = "MIMIC COMPILER";

    protected final MimicUIParser mMimicUIParser;
    private final Context mContext;
    protected View mRootView;
    protected OnContainer[] mItems;

    public UIContainer(Context context, String layout) {
        mContext = context;
        mMimicUIParser = new MimicUIParser(layout);
        mRootView = null;
    }

    public UIContainer(Context context, MimicUIParser mimicUIParser) {
        mContext = context;
        mMimicUIParser = mimicUIParser;
        mRootView = null;
    }

    @Override
    public String getType() {
        return mMimicUIParser.getStringValue(Names.XTYPE);
    }

    @Override
    public String getOrientation() {
        String orientation = mMimicUIParser.getStringValue(Names.LAYOUT);
        return orientation == null ? Names.V_BOX : orientation;
    }

    @Override
    public OnContainer[] getItems() {
        if(mItems != null) {
            return mItems;
        }
        if(mMimicUIParser.isArray(Names.ITEMS)) {
            int count = mMimicUIParser.getArray(Names.ITEMS).length;
            if (count > 0) {
                OnContainer[] items = new OnContainer[count];
                for (int i = 0; i < count; i++) {
                    MimicUIParser mimicUIParser = mMimicUIParser.getArray(Names.ITEMS)[i];
                    if (mimicUIParser.getStringValue(Names.XTYPE).equals(Names.CONTAINER)) {
                        items[i] = new UIContainer(getContext(), mimicUIParser);
                    } else {
                        if (mimicUIParser.isProperty(Names.XTYPE)) {
                            items[i] = mimicUIParser.getInstance(getContext(), mimicUIParser.getStringValue(Names.XTYPE));

                            if(mimicUIParser.isProperty(Names.NAME)) {
                                MimicVariable mimicVariable = new MimicVariable(mimicUIParser.getStringValue(Names.NAME), items[i]);
                                MimicGlobalVariable.getInstance().push(mimicVariable);
                            }
                        }
                    }
                }
                return mItems = items;
            }
        }

        return new UIContainer[0];
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public View getView() {
        return mRootView;
    }

    @Override
    public void alert(Object value) {
        AlertDialog.Builder adb = new AlertDialog.Builder(getContext());
        AlertDialog alert = adb.create();
        alert.setTitle(getString(R.string.alert_title));
        alert.setMessage(String.valueOf(value));

        alert.show();
    }

    @Override
    public void log(Object value) {
        Log.d(TAG, String.valueOf(value));
    }

    /**
     * инициализация
     */
    @Override
    public void init() {
        if(mMimicUIParser.isEvent(Names.EVENT_INIT)) {
            onCompiler(Names.EVENT_INIT, this);
        }
    }

    @Override
    public String toComponentString() {
        return mMimicUIParser.toUIString();
    }

    protected void onCompiler(String event, OnMimicObject e) {
        if(mMimicUIParser.isEvent(event)) {
            String script = mMimicUIParser.getEvent(event);
            Hashtable<String, MimicVariable> localVariables = new Hashtable<>();
            localVariables.put(Names.CURRENT, new MimicVariable(Names.CURRENT, e));
            localVariables.putAll(MimicGlobalVariable.getInstance().getVariables());

            MimicCompiler mimicCompiler = new MimicCompiler(localVariables, this);
            mimicCompiler.main(script);

            mimicCompiler.destroy();
        }
    }

    /**
     * Получение строки из ресурсов
     * @param resId идентификатор ресурса
     * @return строка
     */
    protected String getString(int resId) {
        return getContext().getResources().getString(resId);
    }
}
