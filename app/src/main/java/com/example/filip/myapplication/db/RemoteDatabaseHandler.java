package com.example.filip.myapplication.db;

import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Filip on 2014-10-12 012.
 */
public class RemoteDatabaseHandler {
    private JSONParser jsonParser;

    private static String baseURL       = "http://pluton.kt.agh.edu.pl/~fosuch/GPSDbServer/";
    private static String storeURL      = baseURL;
    private static String storeSatURL   = baseURL;
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
            int number_of_sat,
            String provider) {

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
        params.add(new BasicNameValuePair(LocalDatabaseHandler.KEY_PROVIDER,    provider));

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
        if (json != null) {
            Log.d(getClass().getSimpleName(), "got JSON: " + json.toString());
        }
        else {
            Log.e(getClass().getSimpleName(), "json is null");
        }

        return json;
    }

    public void addGPSSatellites(long id, GpsStatus gpsStatus) {
//        int max = gpsStatus.getMaxSatellites();
        Iterable<GpsSatellite> satellites = gpsStatus.getSatellites();
        Iterator<GpsSatellite> sat = satellites.iterator();
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        params.add(new BasicNameValuePair("tag", "storeSatellite"));
        int i = 0;
        while (sat.hasNext()) {
            GpsSatellite s = sat.next();
            params.add(new BasicNameValuePair("sat["+String.valueOf(i)+"][measure_id]", String.valueOf(id)));
            params.add(new BasicNameValuePair("sat["+String.valueOf(i)+"][azimuth]", String.valueOf(s.getAzimuth())));
            params.add(new BasicNameValuePair("sat["+String.valueOf(i)+"][elevation]", String.valueOf(s.getElevation())));
            params.add(new BasicNameValuePair("sat["+String.valueOf(i)+"][prn]", String.valueOf(s.getPrn())));
            params.add(new BasicNameValuePair("sat["+String.valueOf(i)+"][snr]", String.valueOf(s.getSnr())));
            params.add(new BasicNameValuePair("sat["+String.valueOf(i)+"][ephemeris]", String.valueOf(s.hasEphemeris())));
            params.add(new BasicNameValuePair("sat["+String.valueOf(i)+"][usedinfix]", String.valueOf(s.usedInFix())));
            ++i;
        }

        jsonParser.getJSON(storeSatURL, params);

//        return json;
    }
}
