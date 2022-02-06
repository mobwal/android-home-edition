/*
 * *
 *  * Created by Alexandr Krasnov on 02.04.21 18:13
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 02.04.21 18:04
 *  
 */

package pw.appcode.mimic;

import org.junit.Test;

import pw.appcode.mimic.MimicObject;

import static org.junit.Assert.*;

public class MimicObjectTest {
    @Test
    public void run() {
        MyObject myObject = new MyObject();
        myObject.call("run");
        assertEquals(myObject.getCount(), 1);
        assertEquals(myObject.call("getCount"), 1);

        myObject.call("runParam", 10);
        assertEquals(myObject.getCount(), 10);
        assertEquals(myObject.call("getCount"), 10);
    }

    public static class MyObject extends MimicObject {
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
    }
}