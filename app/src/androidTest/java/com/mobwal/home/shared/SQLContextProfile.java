package com.mobwal.home.shared;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.mobwal.home.utilits.SQLContext;

public class SQLContextProfile extends SQLContext {
    public SQLContextProfile(Context context) {
        super(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(getCreateQuery(new Profile(), "id"));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
