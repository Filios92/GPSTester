package com.example.filip.myapplication.db;

import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Filip on 2014-10-12 012.
 */
public class RemoteDatabaseHandler {
    private JSONParser jsonParser;

    private static String baseURL       = "http://pluton.kt.agh.edu.pl/~fosuch/GPSDbServer/";
    private static String storeURL      = baseURL;
    private static String nextGroupURL  = baseURL + "?rt=index/nextGroup";

    public RemoteDatabaseHandler() {
        jsonParser = new JSONParser();
    }

    public JSONObject addLocation(
            int group_id,
            double latitude,
            double longitude,
            float accuracy,
            long time,
            long time_nano,
            double altitude,
            float bearing,
            float speed,
            int number_of_sat) {

        List<NameValuePair> params = new ArrayList<NameValuePair>();

        params.add(new BasicNameValuePair("tag", "store"));
        params.add(new BasicNameValuePair(LocalDatabaseHandler.KEY_GROUP_ID,    String.valueOf(group_id)));
        params.add(new BasicNameValuePair(LocalDatabaseHandler.KEY_LATITUDE,    String.valueOf(latitude)));
        params.add(new BasicNameValuePair(LocalDatabaseHandler.KEY_LONGITUDE,   String.valueOf(longitude)));
        params.add(new BasicNameValuePair(LocalDatabaseHandler.KEY_ACCURACY,    String.valueOf(accuracy)));
        params.add(new BasicNameValuePair(LocalDatabaseHandler.KEY_TIME,        String.valueOf(time)));
        params.add(new BasicNameValuePair(LocalDatabaseHandler.KEY_TIME_NANO,   String.valueOf(time_nano)));
        params.add(new BasicNameValuePair(LocalDatabaseHandler.KEY_ALTITUDE,    String.valueOf(altitude)));
        params.add(new BasicNameValuePair(LocalDatabaseHandler.KEY_BEARING,     String.valueOf(bearing)));
        params.add(new BasicNameValuePair(LocalDatabaseHandler.KEY_SPEED,       String.valueOf(speed)));
        params.add(new BasicNameValuePair(LocalDatabaseHandler.KEY_NUMBER_OF_SAT, String.valueOf(number_of_sat)));

        JSONObject json = jsonParser.getJSON(storeURL, params);
        if (json != null) {
            Log.d(getClass().getSimpleName(), "got JSON: " + json.toString());
        } else {
            Log.e(getClass().getSimpleName(), "Didn't get JSON for time: " + time);
        }
        return json;
    }

    public JSONObject getNextGroup(String group_description) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        //params.add(new BasicNameValuePair("tag", "store"));
        params.add(new BasicNameValuePair("desc", group_description));

        JSONObject json = jsonParser.getJSON(nextGroupURL, params);
        Log.d(getClass().getSimpleName(), "got JSON: " + json.toString());
        return json;
    }
}
