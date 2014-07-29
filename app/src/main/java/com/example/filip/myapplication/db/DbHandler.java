package com.example.filip.myapplication.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

/**
 * Created by Filip on 2014-07-27 027.
 */
public class DbHandler extends SQLiteOpenHelper {

    public DbHandler(Context context) {
        super(context, "locations.db", null, 1);

    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(
                "create table locations(" +
                        "id integer primary key autoincrement," +
                        "latitude real," +
                        "longitude real," +
                        "accuracy real, " +
                        "time int, " +
                        "time_nano int, " +
                        "altitude real," +
                        "bearing real," +
                        "speed real," +
                        "number_of_sat int);"

        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

    }

    public void addLocation(Location l) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("latitude", l.getLatitude());
        values.put("longitude", l.getLongitude());
        values.put("accuracy", l.getAccuracy());
        values.put("time", l.getTime());
        values.put("time_nano", l.getElapsedRealtimeNanos());
        values.put("altitude", l.getAltitude());
        values.put("bearing", l.getBearing());
        values.put("speed", l.getSpeed());
        values.put("number_of_sat", l.getExtras().getInt("satellites"));

        db.insertOrThrow("locations", null, values);
    }

    public Cursor getDataCursor() {
        SQLiteDatabase db = getReadableDatabase();

        String[] cols = { "id", "latitude",
                "longitude",
                "accuracy",
                "time",
                "time_nano",
                "altitude",
                "bearing",
                "speed",
                "number_of_sat" };

        Cursor cursor = db.query("locations", cols, null, null, null, null, null);
        return cursor;
    }

    public void deleteAll() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("locations", null, null);
    }
}
