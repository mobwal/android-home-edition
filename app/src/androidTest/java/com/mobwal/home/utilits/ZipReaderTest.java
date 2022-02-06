package com.mobwal.home.utilits;

import static org.junit.Assert.*;

import android.content.Context;
import android.text.TextUtils;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class ZipReaderTest {
    private Context mContext;
    private String mFolder;
    private File mOutputFile;
    private FileManager mFileManager;

    @Before
    public void setUp() throws Exception {
        mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        mFolder = "zip-reader";

        mFileManager = new FileManager(new File(mContext.getCacheDir(), mFolder));

        mFileManager.writeBytes(mFolder, "METER.txt", ("layout 'vbox'\n" +
                "    textview 'c_notice' 'Notice'").getBytes(StandardCharsets.UTF_8));

        mFileManager.writeBytes(mFolder, "DEFAULT.txt", ("layout 'vbox'\n" +
                "    switchfield 'b_done' 'Done'\n" +
                "    textview 'c_notice' 'Notice'").getBytes(StandardCharsets.UTF_8));

        mFileManager.writeBytes(mFolder, "tags.csv", ("Обход;DEFAULT;template\n" +
                "Снятие показаний;METER;template").getBytes(StandardCharsets.UTF_8));

        mFileManager.writeBytes(mFolder, "settings.csv", ("GEO;true\n" +
                "GEO_QUALITY;HIGH\n" +
                "IMAGE;true\n" +
                "IMAGE_QUALITY;0.6\n" +
                "IMAGE_HEIGHT;1080").getBytes(StandardCharsets.UTF_8));

        mFileManager.writeBytes(mFolder, "id.txt", "0000-0000".getBytes(StandardCharsets.UTF_8));
        mFileManager.writeBytes(mFolder, "readme.txt", "Описание маршрута".getBytes(StandardCharsets.UTF_8));
        mFileManager.writeBytes(mFolder, "points.csv", ("L-Università ta' Malta Msida, MSD 2080;35.902910;14.484800;Lying at the cross-roads of the Mediterranean, UM has been, over its 400-year history...\n" +
                "55 Triq Il-Flotta, Il-Gżira;35.908612;14.498061;Located right in the middle of the Mediterraneans...\n" +
                "George Borg Olivier Street Sliema, SLM 1807;35.913693;14.503444\n" +
                "Learnkey House, 83, Mannarino Road Birkirkara, BKR 9084;35.897530;14.467413\n" +
                "MCAST Institute of Community Services;35.878324;14.505170\n" +
                "Triq Kordin, Raħal Ġdid;35.876534;14.506371").getBytes(StandardCharsets.UTF_8));

        mOutputFile = new File(mContext.getCacheDir(), mFolder + ".zip");

        ZipManager.zip(mContext, mFileManager.getRootCatalog(null), mOutputFile.getPath());
        FileManager.deleteRecursive(mFileManager.getRootCatalog(null));
    }

    @Test
    public void reader() {
        ZipReader zipReader = new ZipReader(mContext, mOutputFile.getPath(), null, null);
        Assert.assertTrue(zipReader.isExtracted());

        String content = zipReader.getForm("METER", false);
        assertFalse(TextUtils.isEmpty(content));

        content = zipReader.getForm("DEFAULT", false);
        assertFalse(TextUtils.isEmpty(content));

        String[][] rows = zipReader.getTags(false);
        assertNotNull(rows);
        assertEquals(2, rows.length);

        rows = zipReader.getSettings(false);
        assertNotNull(rows);
        assertEquals(5, rows.length);

        content = zipReader.getId(false);
        assertFalse(TextUtils.isEmpty(content));
        assertEquals(content, "0000-0000");

        content = zipReader.getReadme(false);
        assertFalse(TextUtils.isEmpty(content));

        rows = zipReader.getPoints(false);
        assertNotNull(rows);
        assertEquals(6, rows.length);

        zipReader.close();
    }

    @After
    public void tearDown() {
        if(mOutputFile.exists()) {
            mOutputFile.delete();
        }

        mFileManager.deleteFolder(mFolder);
    }
}