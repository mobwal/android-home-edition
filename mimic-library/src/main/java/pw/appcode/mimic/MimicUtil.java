/*
 * *
 *  * Created by Alexandr Krasnov on 02.04.21 14:20
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 02.04.21 9:33
 *
 */

package pw.appcode.mimic;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.util.TypedValue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Математические утилиты
 */
class MimicUtil {

    public static int mathInteger(int a, int b, String operator) {
        switch (operator) {
            case "+":
                return a + b;

            case "-":
                return a - b;

            case "*":
                return a * b;

            case "/":
                return a / b;

            case "%":
                return a % b;

            case ">":
                return (a > b) ? 1 : -1;

            case "!=":
                return (a != b) ? 1 : -1;

            case ">=":
                return (a >= b) ? 1 : -1;

            case "<":
                return (a < b) ? 1 : -1;

            case "<=":
                return (a <= b) ? 1 : -1;

            case "==":
                return (a == b) ? 1 : -1;
        }
        return a + b;
    }

    public static double mathDouble(double a, double b, String operator) {
        switch (operator) {
            case "+":
                return a + b;

            case "-":
                return a - b;

            case "*":
                return a * b;

            case "/":
                return a / b;

            case "%":
                return a % b;
        }
        return a + b;
    }

    public static String mathString(String a, String b, String operator) {
        switch (operator) {
            case "+":
                return a.concat(b);

            case "=":
                return a.equals("") ? Names.TRUE : Names.FALSE;

            case "!=":
                return !a.equals("") ? Names.TRUE : Names.FALSE;
        }

        return a.concat(b);
    }

    public static boolean mathBoolean(boolean a, boolean b, String operator) {
        switch (operator) {
            case "&&":
                return a && b;

            case "||":
                return a || b;
        }
        return a && b;
    }

    /**
     * Возвращение значения
     * @param typeVariable тип объекта: BOOLEAN, INTEGER, DOUBLE, STRING
     * @param value значение
     * @return результат
     */
    public static Object getValue(String typeVariable, String value) {
        switch (typeVariable) {
            case MimicVariable.BOOLEAN:
                return Boolean.parseBoolean(value);
            case MimicVariable.INTEGER:
                return Integer.parseInt(value);
            case MimicVariable.DOUBLE:
                return Double.parseDouble(value);
            case MimicVariable.STRING:
                return normalString(value);

            default:
                return null;
        }
    }


    public static String normalString(String value) {
        return MimicUIParser.trimString(value, Names.TRIM_STRING_SYMBOL);
    }

    /**
     * Тип переменной
     * @param input входные данные
     * @return результат
     */
    public static String getTypeVariable(String input, MimicLocalVariable localVariable) {
        if(MimicGlobalVariable.getInstance().exists(input) || localVariable.exists(input)) {
            return localVariable.exists(input)
                    ? (localVariable.pull(input)).getType()
                    : (MimicGlobalVariable.getInstance().pull(input)).getType();
        }

        if(input.startsWith(Names.TRIM_STRING_SYMBOL) && input.endsWith(Names.TRIM_STRING_SYMBOL)) {
            return MimicVariable.STRING;
        }

        if(input.equals(Names.TRUE) || input.equals(Names.FALSE)) {
            return MimicVariable.BOOLEAN;
        }

        Pattern pattern = Pattern.compile("\\d+\\.\\d+", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(input);
        if(matcher.find()) {
            return MimicVariable.DOUBLE;
        }

        pattern = Pattern.compile("^\\d+$", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(input);
        if(matcher.find()) {
            return MimicVariable.INTEGER;
        }

        return MimicVariable.OBJECT;
    }

    public static int getVariableInteger(String name, MimicLocalVariable localVariable) {
        if(MimicGlobalVariable.getInstance().exists(name) || localVariable.exists(name)) {
            return localVariable.exists(name)
                    ? (int) (localVariable.pull(name)).getValue()
                    : (int) (MimicGlobalVariable.getInstance().pull(name)).getValue();
        } else {
            return Integer.parseInt(name);
        }
    }

    public static boolean getVariableBoolean(String name, MimicLocalVariable localVariable) {
        if(MimicGlobalVariable.getInstance().exists(name) || localVariable.exists(name)) {
            return localVariable.exists(name)
                    ? (boolean) (localVariable.pull(name)).getValue()
                    : (boolean) (MimicGlobalVariable.getInstance().pull(name)).getValue();
        } else {
            return Boolean.parseBoolean(name);
        }
    }

    public static double getVariableDouble(String name, MimicLocalVariable localVariable) {
        if(MimicGlobalVariable.getInstance().exists(name) || localVariable.exists(name)) {
            return localVariable.exists(name)
                    ? (double) (localVariable.pull(name)).getValue()
                    : (double) (MimicGlobalVariable.getInstance().pull(name)).getValue();
        } else {
            return Double.parseDouble(name);
        }
    }

    public static String getVariableString(String name, MimicLocalVariable localVariable) {
        if(MimicGlobalVariable.getInstance().exists(name) || localVariable.exists(name)) {
            return localVariable.exists(name)
                    ? (String) (localVariable.pull(name)).getValue()
                    : (String) (MimicGlobalVariable.getInstance().pull(name)).getValue();
        } else {
            return name;
        }
    }

    public static OnMimicObject getVariableObject(String name, MimicLocalVariable localVariable) {
        if(MimicGlobalVariable.getInstance().exists(name) || localVariable.exists(name)) {
            return localVariable.exists(name)
                    ? (OnMimicObject) (localVariable.pull(name)).getValue()
                    : (OnMimicObject) (MimicGlobalVariable.getInstance().pull(name)).getValue();
        } else {
            return null;
        }
    }

    public static int getColor(
            Context context,
            int attributeId) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attributeId, typedValue, true);
        int colorRes = typedValue.resourceId;
        int color = -1;
        try {
            color = context.getResources().getColor(colorRes);
        } catch (Resources.NotFoundException e) {
            Log.w("COLOR", "Not found color resource by id: " + colorRes);
        }
        return color;
    }
}
