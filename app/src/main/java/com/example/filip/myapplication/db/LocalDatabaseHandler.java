package com.example.filip.myapplication.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

/**
 * Created by Filip on 2014-07-27 027.
 */
public class LocalDatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION   = 1;
    private static final String DATABASE_NAME   = "locations.db";
    private static final String TABLE_LOCATIONS = "locations";

    // Table Columns names
    public static final String KEY_ID           = "_id";
    public static final String KEY_GROUP_ID     = "group_id";
    public static final String KEY_MEASURE_ID   = "measure_id";
    public static final String KEY_LATITUDE     = "latitude";
    public static final String KEY_LONGITUDE    = "longitude";
    public static final String KEY_ACCURACY     = "accuracy";
    public static final String KEY_TIME         = "time";
    public static final String KEY_TIME_NANO    = "time_nano";
    public static final String KEY_ALTITUDE     = "altitude";
    public static final String KEY_BEARING      = "bearing";
    public static final String KEY_SPEED        = "speed";
    public static final String KEY_NUMBER_OF_SAT = "number_of_sat";

    public LocalDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table " + TABLE_LOCATIONS + "(" +
                        KEY_ID          + " integer primary key autoincrement, " +
                        KEY_GROUP_ID    + " integer , " +
                        KEY_MEASURE_ID  + " integer , " +
                        KEY_LATITUDE    + " real," +
                        KEY_LONGITUDE   + " real," +
                        KEY_ACCURACY    + " real, " +
                        KEY_TIME        + " int, " +
                        KEY_TIME_NANO   + " int, " +
                        KEY_ALTITUDE    + " real," +
                        KEY_BEARING     + " real," +
                        KEY_SPEED       + " real," +
                        KEY_NUMBER_OF_SAT  + " int" +
                        ");"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        // Drop older table if existed
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);

        // Create tables again
        onCreate(sqLiteDatabase);
    }

    public void addLocation(Location l) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_LATITUDE,    l.getLatitude());
        values.put(KEY_LONGITUDE,   l.getLongitude());
        values.put(KEY_ACCURACY,    l.getAccuracy());
        values.put(KEY_TIME,        l.getTime());
        values.put(KEY_TIME_NANO,   l.getElapsedRealtimeNanos());
        values.put(KEY_ALTITUDE,    l.getAltitude());
        values.put(KEY_BEARING,     l.getBearing());
        values.put(KEY_SPEED,       l.getSpeed());
        values.put(KEY_NUMBER_OF_SAT, l.getExtras().getInt("satellites"));

        Log.d(getClass().getSimpleName(), "Writing to local db... " + l.getTime());
        db.insertOrThrow(TABLE_LOCATIONS, null, values);
//        db.close(); // ?
    }

    public Cursor getDataCursor() {
        SQLiteDatabase db = getReadableDatabase();

        String[] cols = {
                KEY_ID,
                KEY_GROUP_ID,
                KEY_MEASURE_ID,
                KEY_LATITUDE,
                KEY_LONGITUDE,
                KEY_ACCURACY,
                KEY_TIME,
                KEY_TIME_NANO,
                KEY_ALTITUDE,
                KEY_BEARING,
                KEY_SPEED,
                KEY_NUMBER_OF_SAT };

        return db.query(TABLE_LOCATIONS, cols, null, null, null, null, null);
    }

    public void deleteAll() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_LOCATIONS, null, null);
    }
}
