/*
 * *
 *  * Created by Alexandr Krasnov on 02.04.21 14:38
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 02.04.21 14:21
 *  
 */

package pw.appcode.mimic;

import org.junit.Test;

import java.util.Hashtable;

import pw.appcode.mimic.MimicCompiler;
import pw.appcode.mimic.MimicLocalVariable;
import pw.appcode.mimic.MimicObject;
import pw.appcode.mimic.MimicUtil;
import pw.appcode.mimic.MimicVariable;
import pw.appcode.mimic.OnMimicObject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MimicCompilerTest {
    @Test
    public void run() {
        MimicCompiler mimicCompiler = new MimicCompiler(null);
        mimicCompiler.main("var i=3*2; var j = i*2; return j;");
        assertEquals((int) mimicCompiler.getReturnValue(), 12);
    }

    @Test
    public void getLines() {
        MimicCompiler mimicCompiler = new MimicCompiler(null);
        String[] instructions = mimicCompiler.getInstructions("var i = 10;\n" +
                "var j = 0;\n" +
                "if (i+2 > 0) {\n" +
                " j = 9 + 1;\n" +
                "}\n" +
                "return j;");
        assertEquals(instructions.length, 4);
    }

    @Test
    public void condition() {
        Hashtable<String, MimicVariable> variables = new Hashtable<>();
        variables.put("j", new MimicVariable("j", 0));
        variables.put("i", new MimicVariable("i", 10));

        MimicCompiler mimicCompiler = new MimicCompiler(variables);

        String script = "if (i > 0) {\n" +
                " j = 9 + 1;\n" +
                "}\n";
        assertTrue(mimicCompiler.condition(script));

        variables = new Hashtable<>();
        variables.put("j", new MimicVariable("j", true));
        variables.put("i", new MimicVariable("i", false));
        variables.put("n", new MimicVariable("n", 5));

        mimicCompiler = new MimicCompiler(variables);

        script = "if (i && j) {\n" +
                " var j = n + 1;\n" +
                " return j;\n" +
                "}\n" +
                "return 0;\n";
        boolean condition = mimicCompiler.condition(script);
        assertFalse(mimicCompiler.condition(script));
        mimicCompiler.runBlock(condition, script);
        assertEquals((int) mimicCompiler.getReturnValue(), 0);

        variables = new Hashtable<>();
        variables.put("j", new MimicVariable("j", true));
        variables.put("i", new MimicVariable("i", false));
        variables.put("n", new MimicVariable("n", 5));

        mimicCompiler = new MimicCompiler(variables);

        script = "if (i || j) {\n" +
                " var j = n + 1;\n" +
                " return j;\n" +
                "}\n" +
                "return 0;\n";
        condition = mimicCompiler.condition(script);
        assertTrue(mimicCompiler.condition(script));
        mimicCompiler.runBlock(condition, script);
        assertEquals((int) mimicCompiler.getReturn().getValue(), 6);
    }

    @Test
    public void mathInteger() {
        assertEquals(MimicUtil.mathInteger(1, 2, "+"), 2 + 1);
        assertEquals(MimicUtil.mathInteger(5, 1, "-"), 5 - 1);
        assertEquals(MimicUtil.mathInteger(3, 2, "*"), 3 * 2);
        assertEquals(MimicUtil.mathInteger(6, 2, "/"), 6 / 2);
        assertEquals(MimicUtil.mathInteger(7, 2, "%"), 7 % 2);

        assertEquals(MimicUtil.mathInteger(7, 2, ">") > 0, 7 > 2);
        assertEquals(MimicUtil.mathInteger(7, 2, "<") > 0, 7 < 2);
        assertEquals(MimicUtil.mathInteger(7, 2, ">=") > 0, 7 >= 2);
        assertEquals(MimicUtil.mathInteger(7, 2, "<=") > 0, 7 <= 2);
        assertEquals(MimicUtil.mathInteger(7, 2, "==") > 0, 7 == 2);
        assertEquals(MimicUtil.mathInteger(7, 7, "==") > 0, 7 == 7);
    }

    @Test
    public void mathDouble() {
        assertEquals(MimicUtil.mathDouble(1.0, 2.0, "+"), 2.0 + 1.0, 0.0);
        assertEquals(MimicUtil.mathDouble(5.0, 1.0, "-"), 5.0 - 1.0, 0.0);
        assertEquals(MimicUtil.mathDouble(3.0, 2.0, "*"), 3.0 * 2.0, 0.0);
        assertEquals(MimicUtil.mathDouble(6.0, 2.0, "/"), 6.0 / 2.0, 0.0);
        assertEquals(MimicUtil.mathDouble(7.0, 2.0, "%"), 7.0 % 2.0, 0.0);
    }

    @Test
    public void mathString() {
        String i = MimicUtil.mathString("1", "2", "+");
        assertEquals(i, "12");
    }

    @Test
    public void mathBoolean() {
        assertEquals(MimicUtil.mathBoolean(true, true, "&&"), true && true);
        assertEquals(MimicUtil.mathBoolean(true, false, "&&"), true && false);
        assertEquals(MimicUtil.mathBoolean(true, false, "||"), true || false);
    }

    @Test
    public void getType() {
        assertEquals(MimicUtil.getTypeVariable("'txt'", new MimicLocalVariable()), MimicVariable.STRING);
        assertEquals(MimicUtil.getTypeVariable("12.02", new MimicLocalVariable()), MimicVariable.DOUBLE);
        assertEquals(MimicUtil.getTypeVariable("true", new MimicLocalVariable()), MimicVariable.BOOLEAN);
        assertEquals(MimicUtil.getTypeVariable("1", new MimicLocalVariable()), MimicVariable.INTEGER);
    }

    @Test
    public void runVariableMethod() {
        Hashtable<String, MimicVariable> variables = new Hashtable<>();
        variables.put("mObject", new MimicVariable("mObject", new MyObject()));

        MimicCompiler mimicCompiler = new MimicCompiler(variables);
        String script = "var i = 10;\n" +
                "mObject.run();\n" +
                "i = mObject.getCount();\n" +
                "mObject.runParam(0);\n" +
                "i = mObject.getCount();\n" +
                "var n = mObject.getCountParam(1);\n" +
                "var m = n+i;\n"+
                "return m;";
        mimicCompiler.main(script);
        assertEquals((int) mimicCompiler.getReturnValue(), 2);
    }

    @Test
    public void lineEqualTest() {
        Hashtable<String, MimicVariable> variables = new Hashtable<>();
        variables.put("v", new MimicVariable("v", new MyObject()));

        MimicCompiler mimicCompiler = new MimicCompiler(variables);
        String script = "var i = v;";
        assertTrue(mimicCompiler.lineEqual(script));
        Object value = ((OnMimicObject) mimicCompiler.pull("i").getValue()).call("getCount");
        assertEquals((int)value, 0);
    }

    @Test
    public void lineReturnTest() {
        Hashtable<String, MimicVariable> variables = new Hashtable<>();
        variables.put("v", new MimicVariable("v", new MyObject()));

        MimicCompiler mimicCompiler = new MimicCompiler(variables);
        String script = "return v;";
        assertTrue(mimicCompiler.lineReturn(script));
        Object value = (OnMimicObject) mimicCompiler.getReturn().getValue();
        assertNotNull(value);
    }

    private class MyObject extends MimicObject {
        int idx = 0;
        public void run() {
            idx = 1;
        }

        public void runParam(Integer j) {
            idx = (int)j;
        }

        public int getCount() {
            return idx;
        }

        public int getCountParam(Integer j) {
            return j + 1;
        }
    }
}