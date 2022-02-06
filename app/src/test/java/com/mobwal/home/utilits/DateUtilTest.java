package com.mobwal.home.utilits;

import org.junit.Test;

import java.text.ParseException;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class DateUtilTest {

    @Test
    public void convertStringToSystemDate() throws ParseException {
        Date dt = DateUtil.convertStringToSystemDate("2009-05-12T12:30:50Z");
        assertEquals(dt.getTime(), Long.parseLong("1242117050000"));
    }

    @Test
    public void convertDateToSystemString() {
        Date dt = new Date(Long.parseLong("1242117050000"));
        String str = DateUtil.convertDateToSystemString(dt);
        assertEquals("2009-05-12T12:30:50Z", str);
    }
}
