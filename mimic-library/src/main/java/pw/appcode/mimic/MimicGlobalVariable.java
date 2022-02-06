/*
 * *
 *  * Created by Alexandr Krasnov on 02.04.21 14:20
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 01.04.21 16:41
 *
 */

package pw.appcode.mimic;

import java.util.Hashtable;

/**
 * Глобальный объект для хранения перемнных
 */
class MimicGlobalVariable {
    public static MimicGlobalVariable sMimicGlobalVariable;

    private final Hashtable<String, MimicVariable> mGlobalVariables;

    public static MimicGlobalVariable getInstance() {
        if(sMimicGlobalVariable == null) {
            sMimicGlobalVariable = new MimicGlobalVariable();
        }

        return sMimicGlobalVariable;
    }

    private MimicGlobalVariable() {
        mGlobalVariables = new Hashtable<>();
    }

    /**
     * Добавление одной переменой
     * @param variable переменная
     */
    public void push(MimicVariable variable) {
        variable.setGlobal(true);
        getVariables().put(variable.getName(), variable);
    }

    /**
     * Добавление множества переменных
     * @param variables переменные
     */
    public void pushAll(Hashtable<String, MimicVariable> variables) {
        if(variables != null) {
            for (MimicVariable item : variables.values()) {
                item.setGlobal(true);
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

    public void clear(Hashtable<String, MimicVariable> items) {
        for (String key: items.keySet()) {
            MimicVariable mv = items.get(key);
            if(mv != null && !mv.isGlobal()) {
                getVariables().remove(key);
            }
        }
    }

    public void clearAll() {
        getVariables().clear();
    }

    public Hashtable<String, MimicVariable> getVariables() {
        return mGlobalVariables;
    }
}
