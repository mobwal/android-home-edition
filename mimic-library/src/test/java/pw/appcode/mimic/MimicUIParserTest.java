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

import static org.junit.Assert.*;

public class MimicUIParserTest {

    @Test
    public void read() {
        String script1 = "xtype 'container'\n" +
                "layout {\n" +
                "type 'hbox'\n" +
                "layout {\n" +
                "type 'vbox'\n" +
                "}\n" +
                "}\n" +
                "onclick {\n" +
                "var i = 10;\n" +
                "}\n" +
                "items [\n" +
                "{\n" +
                "xtype 'displayfield'\n" +
                "}\n" +
                "{\n" +
                "xtype 'textfield'\n" +
                "}\n" +
                "]\n" +
                "name 'id'";
        MimicUIParser mimicUIParser = new MimicUIParser(script1);
        assertEquals(mimicUIParser.getStringValue("xtype"), "container");
        assertTrue(mimicUIParser.isEvent("onclick"));
        assertTrue(mimicUIParser.isArray("items"));
        assertTrue(mimicUIParser.isObject("layout"));

        assertFalse(mimicUIParser.isArray("onclick"));
        assertFalse(mimicUIParser.isObject("items"));
        assertFalse(mimicUIParser.isEvent("layout"));

        assertEquals(mimicUIParser.getObject("layout").getStringValue("type"), "hbox");
        MimicCompiler mimicCompiler = new MimicCompiler(new Hashtable<>());
        mimicCompiler.main(mimicUIParser.getEvent("onclick"));
        int i = Integer.parseInt((String) mimicCompiler.getLocalVariables().pull("i").getValue());
        assertEquals(10, i);

        MimicUIParser[] mimicUIParsers = mimicUIParser.getArray("items");
        assertEquals(2, mimicUIParsers.length);
        assertEquals(mimicUIParsers[0].getStringValue("xtype"), "displayfield");
    }

    @Test
    public void readSimpleLayout() {
        String script2 = "layout 'vbox'\n" +
                "    switchfield 'b_done' 'Done'\n" +
                "    textview 'c_notice' 'Notice'";
        boolean isSimpleLayout = MimicUIParser.isSimpleLayout(script2);
        assertTrue(isSimpleLayout);
        String fullLayout = MimicUIParser.convertToMimicUIParser(script2);
        assertEquals(fullLayout, "xtype 'container'\n" +
                "layout 'vbox'\n" +
                "items [\n" +
                "\t{\n" +
                "\t\txtype 'switchfield'\n" +
                "\t\tname 'b_done'\n" +
                "\t\tlabel 'Done'\n" +
                "\t\tlayout 'vbox'\t}\n" +
                "\t{\n" +
                "\t\txtype 'textview'\n" +
                "\t\tname 'c_notice'\n" +
                "\t\tlabel 'Notice'\n" +
                "\t}\n" +
                "]");
    }
}