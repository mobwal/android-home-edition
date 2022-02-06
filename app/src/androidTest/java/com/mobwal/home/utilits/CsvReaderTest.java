package com.mobwal.home.utilits;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CsvReaderTest {

    @Before
    public void setUp() {
    }

    @Test
    public void normalValue() {
        Assert.assertEquals(CsvReader.normalValue("point\r"), "point");
    }

    @Test
    public void getRows() {
        String data = "point1;1\r\npoint2;2";
        CsvReader csvReader = new CsvReader(data);
        String[][] rows = csvReader.getRows();
        Assert.assertEquals(rows.length, 2);
        Assert.assertEquals(rows[0][1], "1");

        data = "name;index;\r\npoint1;1\r\npoint2;2";
        csvReader = new CsvReader(data);
        rows = csvReader.getRows(true);
        Assert.assertEquals(rows.length, 2);
        Assert.assertEquals(rows[0][1], "1");

        csvReader = new CsvReader(data);
        rows = csvReader.getRows(null);
        Assert.assertEquals(rows.length, 3);
        Assert.assertEquals(rows[1][1], "1");
    }

    @After
    public void tearDown() {
    }
}