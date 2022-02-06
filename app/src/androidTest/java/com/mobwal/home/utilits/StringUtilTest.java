package com.mobwal.home.utilits;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class StringUtilTest {
    private Context appContext;

    @Before
    public void setUp() {
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @Test
    public void convertTemplate() {
        String json = "{\"{0}\":\"Hello\", \"{1}\":\"World!!!\", \"{2}\":true, \"{3}\":10.0}";
        String content = "{0}, {1}. It's: {2} = {3}";
        String formatText = StringUtil.convertTemplate(appContext, content, json);
        Assert.assertEquals(formatText, "Hello, World!!!. It's: Yes = 10.0");
        formatText = StringUtil.convertTemplate(appContext, content, null);
        Assert.assertEquals(formatText, content);
    }
}
