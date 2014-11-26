package com.example.filip.myapplication.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by Filip on 2014-07-27 027.
 */
public class LocalDatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION   = 2;
    private static final String DATABASE_NAME   = "locations.db";
    private static final String TABLE_LOCATIONS = "locations";
    private static final String TABLE_GROUPS = "groups";

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
    public static final String KEY_PROVIDER     = "provider";

    private static final String[] KEY_ALL_COLUMNS = {
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
            KEY_NUMBER_OF_SAT,
            KEY_PROVIDER
    };

    public static final String KEY_GROUP_DESC   = "group_desc";

    public static final String KEY_AVG_LATITUDE   = "avg_latitude";
    public static final String KEY_AVG_LONGITUDE   = "avg_longitude";
    public static final String KEY_GROUP_TIME_START   = "start";
    public static final String KEY_GROUP_TIME_END = "end";
    public static final String KEY_GROUP_COUNT = "group_count";

    private static final String QUERY_GROUP_INFO =
            "SELECT " +
                    KEY_GROUP_ID + " AS " + KEY_ID + ", " +
                    KEY_GROUP_DESC + ", " +
                    "count(*) AS " + KEY_GROUP_COUNT + ", " +
                    "avg(" + KEY_LATITUDE  + ") AS " + KEY_AVG_LATITUDE  + ", " +
                    "avg(" + KEY_LONGITUDE + ") AS " + KEY_AVG_LONGITUDE + ", " +
                    "min(" + KEY_TIME + ") AS " + KEY_GROUP_TIME_START + ", " +
                    "max(" + KEY_TIME + ") AS " + KEY_GROUP_TIME_END   + " " +
            "FROM " + TABLE_LOCATIONS + " INNER JOIN " + TABLE_GROUPS + " ON (" + TABLE_LOCATIONS+"."+KEY_GROUP_ID+"="+TABLE_GROUPS+"."+KEY_ID+") "+
            "GROUP BY " + KEY_GROUP_ID + "," + KEY_GROUP_DESC + " " +
            "ORDER BY " + KEY_GROUP_ID;

    private static final String QUERY_DISTINCT_ROWS =
            "SELECT " +
                    TextUtils.join(", ", KEY_ALL_COLUMNS) +
            "FROM " + TABLE_LOCATIONS + " l1 " +
            "WHERE NOT EXISTS(" +
                    "SELECT 1 FROM " + TABLE_LOCATIONS + " l2 " +
                    "WHERE " +
                    "l2." + KEY_ID          + " > l1."      + KEY_ID        + " AND " +
                    "l2." + KEY_LATITUDE    + " = " + "l1." + KEY_LATITUDE  + " AND " +
                    "l2." + KEY_LONGITUDE   + " = " + "l1." + KEY_LONGITUDE +
            " )";

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
                        KEY_NUMBER_OF_SAT  + " int," +
                        KEY_PROVIDER    + " text" +
                        ");"
        );

        db.execSQL(
                "create table " + TABLE_GROUPS + "(" +
                        KEY_ID          + " integer primary key, " +
                        KEY_GROUP_DESC  + " text" +
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

    public void addLocation(Location l, int group_id) {
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
        values.put(KEY_PROVIDER, l.getProvider());
        values.put(KEY_GROUP_ID, group_id);

        Log.d(getClass().getSimpleName(), "Writing to local db... " + l.getTime());
        db.insertOrThrow(TABLE_LOCATIONS, null, values);
//        db.close(); // ?
    }

    public void addGroup(int id, String group_description) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, id);
        values.put(KEY_GROUP_DESC, group_description);

        Log.d(getClass().getSimpleName(), "Writing to local db... group: " + id + ". " + group_description);
        db.insertOrThrow(TABLE_GROUPS, null, values);
    }

    public double getStd(String col, long group_id) {
        SQLiteDatabase db = getReadableDatabase();
        double avg = getAvg(col, group_id);
        db.execSQL("DROP TABLE IF EXISTS tmp");
        db.execSQL("CREATE TABLE tmp (c real)");
        db.execSQL("INSERT INTO tmp(c) SELECT (" + col + "-"+avg+") FROM " + TABLE_LOCATIONS + " WHERE " + KEY_GROUP_ID + " = " + group_id);
        Cursor c = db.rawQuery("SELECT * from tmp", null);

        int size = c.getCount();
        c = db.rawQuery("SELECT sum(c*c) as d FROM tmp", null);
        c.moveToFirst();

        double sum = c.getDouble(c.getColumnIndex("d"));

        double ret = Math.sqrt(sum / (size-1));

        db.execSQL("DROP TABLE IF EXISTS tmp");
        return ret;
    }

    public double getAvg(String col, long group_id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT avg(" + col + ") AS tmp FROM " + TABLE_LOCATIONS + " WHERE " + KEY_GROUP_ID + " = " + group_id, null);
        c.moveToFirst();
        return c.getDouble(c.getColumnIndex("tmp"));
    }

    public Cursor getDataCursor() {
        SQLiteDatabase db = getReadableDatabase();

        return db.query(TABLE_LOCATIONS, KEY_ALL_COLUMNS, null, null, null, null, null);
    }

    public Cursor getGroupsInfoCursor() {
        SQLiteDatabase db = getReadableDatabase();
        Log.d(getClass().getSimpleName(), "rawQuery of: " + QUERY_GROUP_INFO);

        return db.rawQuery(QUERY_GROUP_INFO, null);
    }

    public Cursor getDataForGroup(long groupId) {
        SQLiteDatabase db = getReadableDatabase();

        String[] cols = {
                KEY_ID,
                KEY_GROUP_ID,
                KEY_LATITUDE,
                KEY_LONGITUDE,
                KEY_ACCURACY,
                KEY_NUMBER_OF_SAT
        };

        String where = KEY_GROUP_ID + " = ?";
        String[] whereArgs = new String[] {
                String.valueOf(groupId)
        };
        String orderBy = KEY_ID;

        return db.query(TABLE_LOCATIONS, cols, where, whereArgs, null, null, orderBy, null);
    }

    public Cursor getOptimizedDataForGroup(long group_id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c;
        double stdLat, avgLat, stdLng, avgLng;

        stdLat = getStd(KEY_LATITUDE, group_id);
        avgLat = getAvg(KEY_LATITUDE, group_id);
        stdLng = getStd(KEY_LONGITUDE, group_id);
        avgLng = getAvg(KEY_LONGITUDE, group_id);

        return db.rawQuery("SELECT " + KEY_LATITUDE + ", "+KEY_LONGITUDE+" FROM "
                + TABLE_LOCATIONS
                + " WHERE " + KEY_GROUP_ID + "=" + group_id
                + " AND " + KEY_LATITUDE + ">" + avgLat + "-" + stdLat
                + " AND " + KEY_LATITUDE + "<" + avgLat + "+" + stdLat
                + " AND " + KEY_LONGITUDE + ">" + avgLng + "-" + stdLng
                + " AND " + KEY_LONGITUDE + "<" + avgLng + "+" + stdLng, null);

    }

    public Cursor getUniqueDataForGroup(long groupId) {
        SQLiteDatabase db = getReadableDatabase();

        return db.rawQuery(QUERY_DISTINCT_ROWS, null);
    }

    public void deleteAll() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_LOCATIONS, null, null);
    }

    public int getLastGroupId() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT max(" + KEY_ID + ") FROM " + TABLE_GROUPS, null);
        c.moveToFirst();
        return c.getInt(c.getColumnIndex("max("+KEY_ID+")"));
    }

    public double getOptimizedLatitude(long group_id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c;
        double stdLat, avgLat, stdLng, avgLng;

        stdLat = getStd(KEY_LATITUDE, group_id);
        avgLat = getAvg(KEY_LATITUDE, group_id);
        stdLng = getStd(KEY_LONGITUDE, group_id);
        avgLng = getAvg(KEY_LONGITUDE, group_id);

        c = db.rawQuery("SELECT avg(" + KEY_LATITUDE + ") as a FROM "
                + TABLE_LOCATIONS
                + " WHERE " + KEY_GROUP_ID + "=" + group_id
                + " AND " + KEY_LATITUDE + ">" + avgLat + "-" + stdLat
                + " AND " + KEY_LATITUDE + "<" + avgLat + "+" + stdLat
                + " AND " + KEY_LONGITUDE + ">" + avgLng + "-" + stdLng
                + " AND " + KEY_LONGITUDE + "<" + avgLng + "+" + stdLng, null);
        c.moveToFirst();
        return c.getDouble(c.getColumnIndex("a"));
    }

    public double getOptimizedLongitude(long group_id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c;
        double stdLat, avgLat, stdLng, avgLng;

        stdLat = getStd(KEY_LATITUDE, group_id);
        avgLat = getAvg(KEY_LATITUDE, group_id);
        stdLng = getStd(KEY_LONGITUDE, group_id);
        avgLng = getAvg(KEY_LONGITUDE, group_id);

        c = db.rawQuery("SELECT avg(" + KEY_LONGITUDE + ") as a FROM "
                + TABLE_LOCATIONS
                + " WHERE " + KEY_GROUP_ID + "=" + group_id
                + " AND " + KEY_LATITUDE + ">" + avgLat + "-" + stdLat
                + " AND " + KEY_LATITUDE + "<" + avgLat + "+" + stdLat
                + " AND " + KEY_LONGITUDE + ">" + avgLng + "-" + stdLng
                + " AND " + KEY_LONGITUDE + "<" + avgLng + "+" + stdLng, null);
        c.moveToFirst();
        return c.getDouble(c.getColumnIndex("a"));
    }
}
