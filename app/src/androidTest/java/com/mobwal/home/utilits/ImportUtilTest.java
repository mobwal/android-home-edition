package com.mobwal.home.utilits;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.mobwal.home.WalkerApplication;
import com.mobwal.home.WalkerSQLContext;
import com.mobwal.home.models.db.Point;
import com.mobwal.home.models.db.Route;
import com.mobwal.home.models.db.Setting;
import com.mobwal.home.models.db.Template;

public class ImportUtilTest {
    private Context mContext;

    private WalkerSQLContext db;
    private String routeName;
    private String routeID;

    @Before
    public void setUp() {
        mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        routeName = UUID.randomUUID().toString();
        routeID = UUID.randomUUID().toString();
        db = WalkerApplication.getWalkerSQLContext(mContext);
    }

    @Test
    public void convertRowsToPoints() {
        String data = "point1;0.0;0.0;description 1;;true;1\r\npoint2;0.0;0.0;description 2;;false;2";
        CsvReader csvReader = new CsvReader(data);
        String[][] rows = csvReader.getRows();

        Point[] points = ImportUtil.convertRowsToPoints(rows, "");

        assert points != null;
        Assert.assertEquals(points[0].c_address, "point1");
        Assert.assertEquals(points[0].jb_data, "{\"{5}\":true,\"{6}\":\"1\"}");
    }

    @Test
    public void getPointData() {
        List<String> rows = new ArrayList<>();
        rows.add("point1");
        rows.add("0.0");
        rows.add("0.0");
        rows.add("description");
        rows.add("");
        rows.add("test1");
        rows.add("test2");

        String data = ImportUtil.getPointData(rows.toArray(new String[0]), 5);
        Assert.assertEquals(data, "{\"{5}\":\"test1\",\"{6}\":\"test2\"}");
    }

    @Test
    public void generateRouteFromZip() throws IOException {
        String folder = "zip-reader";
        FileManager fileManager = new FileManager(new File(mContext.getCacheDir(), folder));


        fileManager.writeBytes(folder, "METER.txt", ("layout 'vbox'\n" +
                "    textview 'c_notice' 'Notice'").getBytes(StandardCharsets.UTF_8));

        fileManager.writeBytes(folder, "DEFAULT.txt", ("layout 'vbox'\n" +
                "    switchfield 'b_done' 'Done'\n" +
                "    textview 'c_notice' 'Notice'").getBytes(StandardCharsets.UTF_8));

        fileManager.writeBytes(folder, "tags.csv", ("Обход;DEFAULT;template\n" +
                "Снятие показаний;METER;template").getBytes(StandardCharsets.UTF_8));

        fileManager.writeBytes(folder, "settings.csv", ("GEO;true\n" +
                "GEO_QUALITY;HIGH\n" +
                "IMAGE;true\n" +
                "IMAGE_QUALITY;0.6\n" +
                "IMAGE_HEIGHT;1080").getBytes(StandardCharsets.UTF_8));

        fileManager.writeBytes(folder, "id.txt", routeID.getBytes(StandardCharsets.UTF_8));
        fileManager.writeBytes(folder, "readme.txt", "Описание маршрута".getBytes(StandardCharsets.UTF_8));
        fileManager.writeBytes(folder, "points.csv", ("L-Università ta' Malta Msida, MSD 2080;35.902910;14.484800;Lying at the cross-roads of the Mediterranean, UM has been, over its 400-year history...\n" +
                "55 Triq Il-Flotta, Il-Gżira;35.908612;14.498061;Located right in the middle of the Mediterraneans...\n" +
                "George Borg Olivier Street Sliema, SLM 1807;35.913693;14.503444\n" +
                "Learnkey House, 83, Mannarino Road Birkirkara, BKR 9084;35.897530;14.467413\n" +
                "MCAST Institute of Community Services;35.878324;14.505170\n" +
                "Triq Kordin, Raħal Ġdid;35.876534;14.506371").getBytes(StandardCharsets.UTF_8));

        File outputFile = new File(mContext.getCacheDir(), folder + ".zip");

        ZipManager.zip(mContext, fileManager.getRootCatalog(null), outputFile.getPath());
        FileManager.deleteRecursive(fileManager.getRootCatalog(null));

        Assert.assertTrue(outputFile.exists());

        ZipReader zipReader = new ZipReader(mContext, outputFile.getPath(), null, null);
        Assert.assertTrue(zipReader.isExtracted());

        String result = ImportUtil.generateRouteFromZip(mContext, zipReader, routeName, "");
        Assert.assertNull(result);

        Collection<Route> routes = db.select("select * from route where id = ?", new String[] { routeID }, Route.class);
        assert routes != null;
        Assert.assertEquals(1, routes.size());

        Collection<Point> points = db.select("select * from point where f_route = ?", new String[] { routeID }, Point.class);
        assert points != null;
        Assert.assertEquals(6, points.size());

        Collection<Template> templates = db.select("select * from template where f_route = ?", new String[] { routeID }, Template.class);
        assert templates != null;
        Assert.assertEquals(2, templates.size());

        Collection<Setting> settings = db.select("select * from setting where f_route = ?", new String[] { routeID }, Setting.class);
        assert settings != null;
        Assert.assertEquals(5, settings.size());

        if(outputFile.exists()) {
            outputFile.delete();
        }

        fileManager.deleteFolder(folder);
    }

    @After
    public void tearDown() {

    }
}