/*
 * *
 *  * Created by Alexandr Krasnov on 02.04.21 14:20
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 02.04.21 9:33
 *
 */

package pw.appcode.mimic;

import java.util.Hashtable;

/**
 * Объект для хранения локальных переменных
 */
class MimicLocalVariable {
    private final Hashtable<String, MimicVariable> mLocalVariables;

    public MimicLocalVariable() {
        mLocalVariables = new Hashtable<>();
    }

    /**
     * Добавление одной переменой
     * @param variable переменная
     */
    public void push(MimicVariable variable) {
        getVariables().put(variable.getName(), variable);
    }

    /**
     * Добавление множества переменных
     * @param variables переменные
     */
    public void pushAll(Hashtable<String, MimicVariable> variables) {
        if(variables != null) {
            for (MimicVariable item : variables.values()) {
                getVariables().put(item.getName(), item);
            }
        }
    }

    /**
     * получение переменной
     * @param name имя переменной
     * @return переменная
     */
    public MimicVariable pull(String name) {
        return getVariables().get(name);
    }

    /**
     * доступна ли переменная
     * @param name имя переменной
     * @return true - переменная доступна
     */
    public boolean exists(String name) {
        return getVariables().containsKey(name);
    }

    public Hashtable<String, MimicVariable> getVariables() {
        return mLocalVariables;
    }
}
