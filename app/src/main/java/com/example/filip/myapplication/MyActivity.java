package com.example.filip.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.filip.myapplication.db.DbHandler;


public class MyActivity extends Activity {

    Button buttonCont;
    Button buttonStop;
    Button buttonShow;
    EditText editTextUpdateInterval;
    GPSTracker gps;
    private LocationListener locationListener;
    private LocationListener locationListener2;

    DbHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        dbHandler = new DbHandler(this);

        buttonCont = (Button) findViewById(R.id.buttonCont);
        buttonStop = (Button) findViewById(R.id.buttonStop);
        buttonShow = (Button) findViewById(R.id.buttonShow);
        editTextUpdateInterval = (EditText) findViewById(R.id.editTextUpdateInterval);

        gps = new GPSTracker(this);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                gps.onLocationChanged(location);
                updateView();
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                gps.onStatusChanged(s, i, bundle);
            }

            @Override
            public void onProviderEnabled(String s) {
                gps.onProviderEnabled(s);
            }

            @Override
            public void onProviderDisabled(String s) {
                gps.onProviderDisabled(s);
            }
        };

        locationListener2 = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                dbHandler.addLocation(location);
                Log.d("Got it", String.valueOf(location.getTime()));
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        ((Button) findViewById(R.id.buttonSingle)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gps.requestLocationSingleUpdate(locationListener);
                /*
                if (gps.canGetLocation()) {
                    ((TextView) findViewById(R.id.textViewLat)).setText(String.valueOf(gps.getLatitude()));
                    ((TextView) findViewById(R.id.textViewLng)).setText(String.valueOf(gps.getLongitude()));
                    ((TextView) findViewById(R.id.textViewAcc)).setText(String.valueOf(gps.getAccuracy()));
                    ((TextView) findViewById(R.id.textView17)).setText(String.valueOf(gps.getAltitude()));
                    ((TextView) findViewById(R.id.textView18)).setText(String.valueOf(gps.getBearing()));
                    ((TextView) findViewById(R.id.textViewTime)).setText(String.valueOf(gps.getTime()));
                    ((TextView) findViewById(R.id.textViewProvider)).setText(String.valueOf(gps.getProvider()));
                    ((TextView) findViewById(R.id.textViewSpeed)).setText(String.valueOf(gps.getSpeed()));
//                    ((TextView) findViewById(R.id.textViewSat)).setText(gps.getNumberOfSatellites());
                } else {
                    gps.showSettingsAlert();
                }
                */
            }
        });

        buttonCont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int updateInterval = 3000;
                try {
                    updateInterval = Integer.parseInt(editTextUpdateInterval.getText().toString());
                } catch (NumberFormatException e) {

                }
                gps.requestLocationUpdates(updateInterval, locationListener2);
                buttonStop.setActivated(true);
                buttonCont.setActivated(false);
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gps.stopUsingGPS(locationListener2);
                buttonStop.setActivated(false);
                buttonCont.setActivated(true);
            }
        });

        buttonShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), LocationsFromDbList.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (gps.isRequestingLocationUpdates()) {
            buttonStop.setActivated(true);
            buttonCont.setActivated(false);
        } else {
            buttonStop.setActivated(false);
            buttonCont.setActivated(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        gps.stopUsingGPS(locationListener);
    }

    public void updateView() {
        ((TextView) findViewById(R.id.textViewLat)).setText(String.valueOf(gps.getLatitude()));
        ((TextView) findViewById(R.id.textViewLng)).setText(String.valueOf(gps.getLongitude()));
        ((TextView) findViewById(R.id.textViewAcc)).setText(String.valueOf(gps.getAccuracy()));
        ((TextView) findViewById(R.id.textView17)).setText(String.valueOf(gps.getAltitude()));
        ((TextView) findViewById(R.id.textView18)).setText(String.valueOf(gps.getBearing()));
        ((TextView) findViewById(R.id.textViewTime)).setText(String.valueOf(gps.getTime()));
        ((TextView) findViewById(R.id.textViewProvider)).setText(String.valueOf(gps.getProvider()));
        ((TextView) findViewById(R.id.textViewSpeed)).setText(String.valueOf(gps.getSpeed()));
        ((TextView) findViewById(R.id.textViewSat)).setText(String.valueOf(gps.getNumberOfSatellites()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
