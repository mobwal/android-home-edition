package com.mobwal.home;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.net.URL;
import java.util.Locale;

import com.mobwal.home.models.db.Attachment;
import com.mobwal.home.models.db.Point;
import com.mobwal.home.models.db.Result;
import com.mobwal.home.models.db.Route;
import com.mobwal.home.models.db.Setting;
import com.mobwal.home.models.db.Template;
import com.mobwal.home.utilits.NetworkInfoUtil;
import com.mobwal.home.utilits.SQLContext;

public class WalkerSQLContext extends SQLContext {

    private Context mContext;

    public WalkerSQLContext(Context context) {
        super(context);

        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();

        try {
            db.execSQL(getCreateQuery(new Template(), "id"));
            db.execSQL(getCreateQuery(new Setting(), "id"));
            db.execSQL(getCreateQuery(new Route(), "id"));
            db.execSQL(getCreateQuery(new Point(), "id"));
            db.execSQL(getCreateQuery(new Result(), "id"));
            db.execSQL(getCreateQuery(new Attachment(), "id"));

            db.setTransactionSuccessful();
        } catch (Exception ignored) {

        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
