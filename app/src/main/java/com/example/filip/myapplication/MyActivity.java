package com.example.filip.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.filip.myapplication.db.DbHandler;


public class MyActivity extends Activity {

    Button buttonSingle;
    Button buttonCont;
    Button buttonStop;
    Button buttonShow;
    EditText editTextUpdateInterval;
    GPSTracker gps;
    private LocationListener locationListener;
    private LocationListener locationListenerForDebugWithoutWritingToDb;
    private LocationListener locationListenerForUpdates;
    private LocationListener locationListenerForContinuousSingleUpdate;
    DbHandler dbHandler;
    Thread t;
    Handler locationHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        dbHandler = new DbHandler(this);
        buttonSingle = ((Button) findViewById(R.id.buttonSingle));
        buttonCont = (Button) findViewById(R.id.buttonCont);
        buttonStop = (Button) findViewById(R.id.buttonStop);
        buttonShow = (Button) findViewById(R.id.buttonShow);
        editTextUpdateInterval = (EditText) findViewById(R.id.editTextUpdateInterval);
        gps = new GPSTracker(this);

        locationListenerForDebugWithoutWritingToDb = new LocationListener() {
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

        locationListenerForUpdates = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                dbHandler.addLocation(location);
                Log.d("Got it", String.valueOf(location.getTime()));
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {}

            @Override
            public void onProviderEnabled(String s) {}

            @Override
            public void onProviderDisabled(String s) {}
        };

        locationListenerForContinuousSingleUpdate = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                dbHandler.addLocation(location);
                Log.d("Got it", String.valueOf(location.getTime()));
                locationHandler.sendMessage(Message.obtain());
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {}

            @Override
            public void onProviderEnabled(String s) {}

            @Override
            public void onProviderDisabled(String s) {}
        };

        buttonSingle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationListener = locationListenerForDebugWithoutWritingToDb;
                gps.requestLocationSingleUpdate(locationListener);
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
                locationListener = locationListenerForUpdates;
                gps.requestLocationUpdates(updateInterval, locationListener);

                // Wersja z wątkiem i singleUpdate
//                locationListener = locationListenerForContinuousSingleUpdate;
//                if (t == null || !t.isAlive()) {
//                    Log.d("Thred", "start");
//                    t = new Thread(new Runnable() {
//                        public Handler handler;
//                        @Override
//                        public void run() {
//                            Log.d("In Thread", "running");
//                            if(!Thread.currentThread().isInterrupted()) {
//                                Looper.prepare();
////                    while (!Thread.currentThread().isInterrupted()) {
////                        gps.requestLocationSingleUpdate(locationListener2);
////                        Thread.sleep(3000);
////                    }
//                                locationHandler = new Handler() {
//                                    @Override
//                                    public void handleMessage(Message msg) {
//                                        gps.requestLocationSingleUpdate(locationListener3);
//                                    }
//                                };
//                                gps.requestLocationSingleUpdate(locationListener);
//                                Looper.loop();
//                                Log.d("In Thread", "after loop");
//                            }
//                            Log.d("In Thread", "after try/catch");
//                        }
//                    });
//                    t.start();
//                }

                buttonStop.setEnabled(true);
                buttonCont.setEnabled(false);
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Wersja z wątkiem i singleUpdate
//                if (t != null && t.isAlive()) {
//                    locationHandler.getLooper().quit();
//                    t.interrupt();
//                }
                locationListener = locationListenerForUpdates;
                gps.stopUsingGPS(locationListener);
                buttonStop.setEnabled(false);
                buttonCont.setEnabled(true);
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
            buttonStop.setEnabled(true);
            buttonCont.setEnabled(false);
        } else {
            buttonStop.setEnabled(false);
            buttonCont.setEnabled(true);
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
