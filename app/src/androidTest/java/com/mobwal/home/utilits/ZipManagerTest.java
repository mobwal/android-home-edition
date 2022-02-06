package com.mobwal.home.utilits;

import static org.junit.Assert.*;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class ZipManagerTest {

    private Context mContext;
    private String mFolder;

    @Before
    public void setUp() {
        mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        mFolder = "temp";
    }

    @Test
    public void zip() throws IOException {
        FileManager fileManager = new FileManager(new File(mContext.getCacheDir(), mFolder));
        fileManager.writeBytes(mFolder, "readme.txt", "hello world!!!".getBytes(StandardCharsets.UTF_8));
        fileManager.writeBytes(mFolder, "points.txt", "points".getBytes(StandardCharsets.UTF_8));
        File fileOutput = new File(mContext.getCacheDir(), mFolder + ".zip");
        String output = fileOutput.getPath();
        File tempFolder = new File(mContext.getCacheDir(), mFolder);
        String result = ZipManager.zip(mContext, tempFolder, output);
        Assert.assertNull(result);
        FileManager.deleteRecursive(tempFolder);
        assertFalse(tempFolder.exists());

        Assert.assertTrue(fileOutput.exists());
        Assert.assertTrue(fileOutput.length() > 0);

        result = ZipManager.unzip(mContext, output, mContext.getCacheDir().getPath());
        Assert.assertNull(result);
        Assert.assertTrue(tempFolder.exists());

        Assert.assertEquals(Objects.requireNonNull(tempFolder.listFiles()).length, 2);
        FileManager.deleteRecursive(tempFolder);

        fileOutput.delete();
    }

    @After
    public void tearDown() {

    }
}