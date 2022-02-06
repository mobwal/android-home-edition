/*
 * *
 *  * Created by Alexandr Krasnov on 02.04.21 14:20
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 01.04.21 16:41
 *
 */

package pw.appcode.mimic;

class MimicVariable {

    public static final String STRING = "String";
    public static final String BOOLEAN = "Boolean";
    public static final String INTEGER = "Integer";
    public static final String DOUBLE = "Double";
    public static final String OBJECT = "Object";

    public static MimicVariable getSimple(String name, Object value) {
        return new MimicVariable(name, value);
    }

    public static MimicVariable getSimple(String name, Object value, MimicLocalVariable localVariable) {
        return new MimicVariable(name, value, localVariable);
    }

    private final String mName;
    private final String mType;

    private MimicVariable mRef;
    private boolean mGlobal;

    public MimicVariable(String name, MimicVariable mimicVariable) {
        mRef = mimicVariable;
        mName = name;
        mType = OBJECT;
    }

    public MimicVariable(String name, String type) {
        mName = name;
        mType = type;
    }
    public MimicVariable(String name, Object value) {
        this(name, value, new MimicLocalVariable());
    }

    public MimicVariable(String name, Object value, MimicLocalVariable localVariable) {
        this(name, value instanceof OnMimicObject ? MimicVariable.OBJECT
                : (value == "" ? MimicUtil.getTypeVariable(Names.TRIM_STRING_SYMBOL + Names.TRIM_STRING_SYMBOL, localVariable) : MimicUtil.getTypeVariable(String.valueOf(value), localVariable)));
        mValue = value;
    }

    private Object mValue;

    public Object getValue() {
        if(mRef != null) {
            return mRef.getValue();
        }
        return mValue;
    }

    public void setValue(Object value) {
        mValue = value;
    }

    public String getName() {
        return mName;
    }

    public String getType() {
        return mType;
    }

    public boolean isGlobal() {
        return mGlobal;
    }

    public void setGlobal(boolean global) {
        mGlobal = global;
    }
}
