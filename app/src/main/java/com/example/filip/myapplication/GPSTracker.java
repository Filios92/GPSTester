package com.example.filip.myapplication;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Filip on 2014-07-16 0016.
 */
public class GPSTracker extends Service implements LocationListener {
    private final Context mContext;

    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    boolean canGetLocation = false;

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude
    private float accuracy;

    private double altitude;
    private float bearing;
    private long time;
    private long elapsedRealTime;
    private String provider;
    private float speed;
    private int numberOfSatellites;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 0; // 10 sec

    // Declaring a Location Manager
    protected LocationManager locationManager;
    private boolean isRequestingLocationUpdates;
    private int updateCounter;
    private long timeOfFirstUpdate;

    // GpsStatus
//    GpsStatus gpsStatus;

    public GPSTracker(Context context) {
        this.mContext = context;
//        getLocation();
        updateCounter = 0;
//        gpsStatus = new GpsStatus();
    }

    /**
     *
     * @param locationListener if null then default from GPSTracker
     * @return Location
     */
    public Location requestLocationSingleUpdate(LocationListener locationListener) {
        if (locationListener == null) {
            locationListener = this;
        }
        try {
            if (locationManager == null)
                locationManager = (LocationManager) mContext
                        .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
//                this.canGetLocation = true;

                // First get location from Network Provider
                if (isNetworkEnabled && !isGPSEnabled) {
                    locationManager.requestSingleUpdate(
                            LocationManager.NETWORK_PROVIDER,
                            locationListener,
                            null);
                    Log.d("Location obtained by", "Network");

                    location = locationManager
                            .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null) {
                        getDataFromLocation();
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    locationManager.requestSingleUpdate(
                            LocationManager.GPS_PROVIDER,
                            locationListener,
                            null);
                    Log.d("Location obtained by", "GPS");

                    location = locationManager
                            .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location != null) {
                        getDataFromLocation();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }

    /**
     *
     * @param locationListener if null then default from GPSTracker
     * @return Location
     */
    public Location requestLocationUpdates(int updateInterval, LocationListener locationListener) {
        if (locationListener == null) {
            locationListener = this;
        }
        try {
            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled && !isGPSEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
                    Log.d("Location obtained by", "Network");
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
                    Log.d("Location obtained by", "GPS");
                }

                isRequestingLocationUpdates = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return location;
    }

    private void getDataFromLocation() {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        accuracy = location.getAccuracy();
        altitude = location.getAltitude();
        bearing = location.getBearing();
        time = location.getTime();
//        elapsedRealTime = location.getElapsedRealtimeNanos();
        provider = location.getProvider();
        speed = location.getSpeed();
        numberOfSatellites = location.getExtras().getInt("satellites");
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        Toast.makeText(mContext, "Location Update\nLat: " + location.getLatitude() + "\nLng: " + location.getLongitude(), Toast.LENGTH_SHORT).show();
        updateCounter++;
        if (timeOfFirstUpdate == 0) {
            timeOfFirstUpdate = location.getTime();
        }

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(mContext, "Provider Disabled\nProvider: " + provider, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(mContext, "Provider Enabled\nProvider: " + provider, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        String statusText = "";
        switch (status) {
            case LocationProvider.AVAILABLE:
                statusText = "Available";
                break;
            case LocationProvider.OUT_OF_SERVICE:
                statusText = "Out of service";
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                statusText = "Temporarily unavailable";
                break;
            default:
                break;
        }
        Toast.makeText(mContext, "Status Changed\nProvider: " + provider + "\nStatus: " + statusText, Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    /**
     * Function to get latitude
     * */
    public double getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }

        // return latitude
        return latitude;
    }

    /**
     * Function to get longitude
     * */
    public double getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }

        // return longitude
        return longitude;
    }

    public float getAccuracy() {
        if(location != null){
            accuracy = location.getAccuracy();
        }
        return accuracy;
    }

    public double getAltitude() {
        if(location != null){
            altitude = location.getAltitude();
        }
        return altitude;
    }

    public float getBearing() {
        if(location != null){
            bearing = location.getBearing();
        }
        return bearing;
    }

    public long getTime() {
        if(location != null){
            time = location.getTime();
        }
        return time;
    }

    public String getProvider() {
        if(location != null){
            provider = location.getProvider();
        }
        return provider;
    }

    public float getSpeed() {
        if(location != null){
            speed = location.getSpeed();
        }
        return speed;
    }

    public int getNumberOfSatellites() {
        if(location != null){
            numberOfSatellites = location.getExtras().getInt("satellites");
        }
        return numberOfSatellites;
    }

    /**
     * Function to check if best network provider
     * @return boolean
     * */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    /**
     * Function to show settings alert dialog
     * */
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // Setting Icon to Dialog
        //alertDialog.setIcon(R.drawable.delete);

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     * */
    public void stopUsingGPS(LocationListener locationListener){
        if (locationListener == null) {
            locationListener = GPSTracker.this;
        }
        if(locationManager != null){
            locationManager.removeUpdates(locationListener);
            isRequestingLocationUpdates = false;
            updateCounter = 0;
        }
    }

    public boolean isRequestingLocationUpdates() { return isRequestingLocationUpdates; }
}
