/*
 * *
 *  * Created by Alexandr Krasnov on 02.04.21 14:20
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 02.04.21 9:33
 *
 */

package pw.appcode.mimic;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class MimicCompiler {
    // глобальная переменная для хранения переменных
    private final MimicLocalVariable mLocalVariables;
    // результат выполнения кода
    private MimicVariable mReturn;

    private OnMimicListeners mListeners;

    /**
     * конструктор
     * @param localVariables глобальные переменные, можно передать null
     */
    public MimicCompiler(Hashtable<String, MimicVariable> localVariables) {
        mLocalVariables = new MimicLocalVariable();
        mLocalVariables.pushAll(localVariables);
    }

    public MimicCompiler(Hashtable<String, MimicVariable> localVariables, OnMimicListeners listeners) {
        this(localVariables);
        mListeners = listeners;
    }

    /**
     * Глобальные переменные
     * @return Глобальные переменные
     */
    public MimicLocalVariable getLocalVariables() {
        return mLocalVariables;
    }

    /**
     * Стартовая функция выполнения
     * @param script скрипт
     */
    public void main(String script) {
        for (String line: getInstructions(script)) {
            runBlock(condition(line), line);
        }
    }

    /**
     * Присвоение значения var i = j;
     * @param line скрипт
     * @return true - операция обработана
     */
    public boolean lineEqual(String line) {
        Pattern pattern = Pattern.compile("(var|global)\\s+([a-z_A-Z]+)\\s*=\\s*(\\d+|\\'.*\\'|[a-z_A-Z]+)\\s*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(line);
        if(matcher.find()) {
            MimicVariable variable;
            if(MimicGlobalVariable.getInstance().exists(matcher.group(3)) || getLocalVariables().exists(matcher.group(3))) {
                if(MimicGlobalVariable.getInstance().exists(matcher.group(3))) {
                    variable = new MimicVariable(matcher.group(2), MimicGlobalVariable.getInstance().pull(matcher.group(3)));
                } else {
                    variable = new MimicVariable(matcher.group(2), getLocalVariables().pull(matcher.group(3)));
                }
            } else {
                variable = MimicVariable.getSimple(matcher.group(2), matcher.group(3), getLocalVariables());
            }

            mLocalVariables.push(variable);

            String str = matcher.group(1);
            if(str != null && str.equals("global")) {
                MimicGlobalVariable.getInstance().push(variable);
            }
            return true;
        }

        return false;
    }

    /**
     * Оператор вычисления
     * @param line скрипт
     * @return true - операция обработана
     */
    public boolean lineOperator(String line) {
        Pattern pattern = Pattern.compile("(var)\\s+([a-zA-Z]+)\\s*=\\s*(\\d+|\\'\\S+\\'|[a-z_A-Z]+)\\s*([\\*|-|\\+|/|\\%|>|>=|<|<>|<=|==|\\&\\&|\\|\\|]+)\\s*(\\d+|\\'\\S+\\'|[a-z_A-Z]+)\\s*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(line);
        if(matcher.find()) {
            mLocalVariables.push(MimicVariable.getSimple(matcher.group(2), calc(matcher.group(3), matcher.group(5), matcher.group(4))));
            return true;
        }

        return false;
    }

    /**
     * Выполение метода
     * myObject.run()
     * myObject.run(0)
     * myObject.run(0, '2',true)
     * @param line скрипт
     * @return true - значит совпадение найдено
     */
    private boolean lineMethod(String line) {
        Pattern pattern = Pattern.compile("\\s*([a-z_A-Z\\d]+)\\.([a-z_A-Z\\d]+)\\((.*)\\)\\s*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(line);
        if(matcher.find()) {
            OnMimicObject onMimicObject = MimicUtil.getVariableObject(matcher.group(1), getLocalVariables());
            if(onMimicObject != null) {
                String str = matcher.group(3);
                if(str != null) {
                    if (str.equals("")) {
                        onMimicObject.call(matcher.group(2));
                    } else {
                        String[] vars = str.split(",");
                        Object[] objects = new Object[vars.length];
                        int idx = 0;
                        for (String s : vars) {

                            String value;

                            if (MimicGlobalVariable.getInstance().exists(s) || getLocalVariables().exists(s)) {
                                MimicVariable variable;
                                if (MimicGlobalVariable.getInstance().exists(s)) {
                                    variable = MimicGlobalVariable.getInstance().pull(s);
                                } else {
                                    variable = getLocalVariables().pull(s);
                                }
                                value = String.valueOf(variable.getValue());
                            } else {
                                value = s;
                            }

                            objects[idx] = MimicUtil.getValue(MimicUtil.getTypeVariable(value, getLocalVariables()), s);
                            idx++;
                        }
                        onMimicObject.call(matcher.group(2), objects);
                    }
                }
            }
            return true;
        }

        return false;
    }

    /**
     * Выполение метода
     * i =myObject.run()
     * var i= myObject.run(0)
     * var i = myObject.run(0, '2',true)
     * @param line скрипт
     * @return true - значит совпадение найдено
     */
    private boolean lineMethodReturn(String line) {
        Pattern pattern = Pattern.compile("(var)*\\s*([a-z_A-Z]+)\\s*=\\s*([a-z_A-Z\\d]+)\\.([a-z_A-Z\\d]+)\\((.*)\\)\\s*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(line);
        if(matcher.find()) {
            OnMimicObject onMimicObject = MimicUtil.getVariableObject(matcher.group(3), getLocalVariables());
            String str = matcher.group(5);
            if(str != null && onMimicObject != null) {
                if (str.equals("")) {
                    Object value = onMimicObject.call(matcher.group(4));
                    if(value != null) {
                        mLocalVariables.push(MimicVariable.getSimple(matcher.group(2), value));
                    }
                } else {
                    String[] vars = str.split(",");
                    Object[] objects = new Object[vars.length];
                    int idx = 0;

                    for (String s : vars) {

                        String value;

                        if (MimicGlobalVariable.getInstance().exists(s) || getLocalVariables().exists(s)) {
                            MimicVariable variable;
                            if (MimicGlobalVariable.getInstance().exists(s)) {
                                variable = MimicGlobalVariable.getInstance().pull(s);
                            } else {
                                variable = getLocalVariables().pull(s);
                            }
                            value = String.valueOf(variable.getValue());
                        } else {
                            value = s;
                        }

                        objects[idx] = MimicUtil.getValue(MimicUtil.getTypeVariable(value, getLocalVariables()), s);
                        idx++;
                    }

                    Object value = onMimicObject.call(matcher.group(4), objects);
                    mLocalVariables.push(MimicVariable.getSimple(matcher.group(2), value));

                }
            }
            return true;
        }

        return false;
    }

    /**
     * Вычисление
     * @param a значение
     * @param b значение
     * @param operator оператор
     * @return результат
     */
    private Object calc(String a, String b, String operator) {
        String variableType = MimicUtil.getTypeVariable(a, getLocalVariables());

        switch (variableType) {
            case MimicVariable.BOOLEAN:
                 return MimicUtil.mathBoolean(MimicUtil.getVariableBoolean(a, getLocalVariables()), MimicUtil.getVariableBoolean(b, getLocalVariables()), operator);
            case MimicVariable.STRING:
                String msg = MimicUtil.mathString(MimicUtil.getVariableString(a, getLocalVariables()), MimicUtil.getVariableString(b, getLocalVariables()), operator);
                if(msg.equals("true") || msg.equals("false")) {
                    return Boolean.parseBoolean(msg);
                }
                return msg;
            case MimicVariable.DOUBLE:
                return MimicUtil.mathDouble(MimicUtil.getVariableDouble(a, getLocalVariables()), MimicUtil.getVariableDouble(b, getLocalVariables()), operator);
            case MimicVariable.INTEGER:
            default:
                return MimicUtil.mathInteger(MimicUtil.getVariableInteger(a, getLocalVariables()), MimicUtil.getVariableInteger(b, getLocalVariables()), operator);
        }
    }

    /**
     * Обработка завершения выполнения скрипта
     * @param line строка скрипта
     * @return true - обработка произведена
     */
    public boolean lineReturn(String line) {
        Pattern pattern = Pattern.compile("(return)\\s+([a-z_A-Z\\d]+)\\s*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(line);
        if(matcher.find()) {
            if(mLocalVariables.exists(matcher.group(2))) {
                mReturn = mLocalVariables.pull(matcher.group(2));
            } else {
                String type = MimicUtil.getTypeVariable(matcher.group(2), getLocalVariables());
                mReturn = MimicVariable.getSimple("return", MimicUtil.getValue(type, matcher.group(2)));
            }
            return true;
        }

        return false;
    }

    /**
     * Вычисление инфструкций
     * @param script скрипт обработки
     * @return список инструкций
     */
    public String[] getInstructions(String script) {
        StringBuilder stringBuilder = new StringBuilder();
        List<String> lines = new ArrayList<>();
        String[] tmp = script.split("\n");
        for (String s : tmp) {
            String line = s.trim();
            if (stringBuilder.length() == 0) {
                if (line.endsWith(";")) {
                    lines.add(line);
                } else if(line.contains("if")) {
                    stringBuilder.append(line).append("\n");
                }
            } else {
                if (line.contains("}")) {
                    stringBuilder.append(line).append("\n");
                    lines.add(stringBuilder.toString());
                    stringBuilder.setLength(0);
                } else {
                    stringBuilder.append(line).append("\n");
                }
            }
        }
        return lines.toArray(new String[0]);
    }

    /**
     * Обработка условий
     * @param script код
     * @return результат выполнения
     */
    public Boolean condition(String script) {
        Pattern pattern = Pattern.compile("(if)\\s*(\\(.+\\))\\s*\\{*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(script);
        if(matcher.find()) {
            String condition = matcher.group(2);
            if(condition != null) {
                pattern = Pattern.compile("\\s*(\\d+|\\'\\S*\\'|[a-z_A-Z]+)\\s*([>|>=|<|<=|!=|==|\\&\\&|\\|\\|]+)\\s*(\\d+|\\'\\S*\\'|[a-z_A-Z]+)\\s*", Pattern.CASE_INSENSITIVE);
                matcher = pattern.matcher(condition);
                if (matcher.find()) {
                    Object item = calc(matcher.group(1), matcher.group(3), matcher.group(2));
                    if (item instanceof Integer) {
                        return (int) item == 1;
                    } else if (item instanceof Boolean) {
                        return (boolean) item;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Вычисление условия
     * @param script код
     */
    public void runBlock(Boolean condition, String script) {
        // построчное чтение кода
        String editScript;
        if(condition != null) {
            editScript = condition ? script.substring(script.indexOf("{") + 1, script.indexOf("}")) : script.substring(script.indexOf("}") + 1);
        } else {
            editScript = script;
        }
        String[] lines = editScript.split(";");
        for (String line: lines) {
            line = line.replaceAll("\\n", "").trim();
            if(line.length() > 0) {
                if(lineAlert(line)) {
                    continue;
                }
                if(lineMethodReturn(line)) {
                    continue;
                }

                if(lineMethod(line)) {
                    continue;
                }

                if (lineOperator(line)) {
                    continue;
                }

                if (lineEqual(line)) {
                    continue;
                }

                if (lineReturn(line)) {
                    return;
                }
            }
        }
    }

    /**
     * Обработка alert скрипта
     * @param line скрипт
     * @return результат выполнения
     */
    public boolean lineAlert(String line) {
        Pattern pattern = Pattern.compile("\\s*(alert|log)\\((.*)\\)\\s*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(line);

        if(matcher.find() && mListeners != null) {
            String msg;
            if(getLocalVariables().exists(matcher.group(2))) {
                MimicVariable mimicVariable = getLocalVariables().pull(matcher.group(2));
                msg = String.valueOf(mimicVariable.getValue());
            } else {
                String type = MimicUtil.getTypeVariable(matcher.group(2), getLocalVariables());
                msg = String.valueOf(MimicUtil.getValue(type, matcher.group(2)));
            }
            String str = matcher.group(1);
            if (str != null && str.equals("alert")) {
                mListeners.alert(MimicUtil.normalString(msg));
            } else {
                mListeners.log(MimicUtil.normalString(msg));
            }
            return true;
        }

        return false;
    }

    /**
     * Получение результата
     * @return Результат вычисления
     */
    public MimicVariable getReturn() {
        return mReturn;
    }

    /**
     * Получение результата
     * @return значение результата
     */
    public Object getReturnValue() {
        return mReturn.getValue();
    }

    public MimicVariable pull(String name) {
        return mLocalVariables.pull(name);
    }

    public void destroy() {
        MimicGlobalVariable.getInstance().clear(mLocalVariables.getVariables());
    }
}
