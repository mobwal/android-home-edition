package com.mobwal.home.utilits;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.mobwal.home.models.db.Route;
import com.mobwal.home.shared.Profile;
import com.mobwal.home.shared.SQLContextProfile;

@RunWith(AndroidJUnit4.class)
public class SQLContextTest {

    private SQLContextProfile sqlContext;

    @Before
    public void setUp() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        sqlContext = new SQLContextProfile(appContext);
    }

    @Test
    public void getCreateQuery() {
        Profile profile = new Profile();
        String sql = sqlContext.getCreateQuery(profile,"id");
        Assert.assertEquals(sql, "CREATE TABLE IF NOT EXISTS Profile (B_MALE INTEGER, C_NAME TEXT, D_DATE TEXT, ID INTEGER64 PRIMARY KEY, N_AGE INTEGER, N_SUM REAL, N_YEAR INTEGER);");
    }

    @Test
    public void isExistsQuery() {
        Profile profile = new Profile();
        String sql = sqlContext.isExistsQuery(profile);
        Assert.assertEquals(sql, "SELECT count(*) FROM sqlite_master WHERE type='table' AND name='Profile';");
    }

    @Test
    public void select() {
        List<Profile> profiles = new ArrayList<>();
        Profile profile = new Profile();
        profile.b_male = true;
        profile.n_age = null;
        profile.c_name = "Шурик";
        profile.d_date = new Date();
        profiles.add(profile);

        sqlContext.insertMany(profiles.toArray());
        Collection<Profile> results = sqlContext.select("select * from Profile;", null, Profile.class);
        Assert.assertEquals(1, results.stream().count());

        Profile resultItem = results.toArray(new Profile[0])[0];

        Assert.assertEquals(profile.c_name, resultItem.c_name);
        profile.c_name = "саша";
        profiles.clear();
        profiles.add(profile);
        sqlContext.insertMany(profiles.toArray());

        results = sqlContext.select("select * from Profile;", null, Profile.class);
        Assert.assertEquals(1, results.stream().count());

        resultItem = results.toArray(new Profile[0])[0];

        Assert.assertEquals(profile.c_name, resultItem.c_name);

        Long count = sqlContext.count("delete from profile;");
        Assert.assertNull(count);
    }

    @Test
    public void insertMany() {
        List<Profile> profiles = new ArrayList<>();
        Profile profile = new Profile();
        profile.b_male = true;
        profile.n_age = null;
        profile.c_name = "Шурик";
        profile.d_date = new Date();
        profiles.add(profile);

        sqlContext.insertMany(profiles.toArray());

        Long count = sqlContext.count("select count(*) from Profile;");
        Assert.assertNotNull(count);
        Assert.assertEquals(1, (long) count);
    }

    @Test
    public void count() {
        Long count = sqlContext.count("select count(*) from Profile;");
        Assert.assertNotNull(count);
        Assert.assertTrue(count >= 0);

        count = sqlContext.count("select count(*) from Test;");
        Assert.assertNull(count);
    }

    @Test
    public void exists() {
        Assert.assertTrue(sqlContext.exists(new Route()));
        Assert.assertFalse(sqlContext.exists(new TestClass()));
    }

    @After
    public void tearDown() {
        sqlContext.trash();
    }

    /**
     * Тестовый класс
     */
    static class TestClass {

    }
}