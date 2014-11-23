package com.example.filip.myapplication;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.filip.myapplication.db.LocalDatabaseHandler;
import com.example.filip.myapplication.db.RemoteDatabaseHandler;
import com.example.filip.myapplication.fragments.ContinuousUpdateFragment;
import com.example.filip.myapplication.fragments.DbInfoFragment;
import com.example.filip.myapplication.fragments.SingleUpdateFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;


public class MyActivity extends Activity implements ViewPager.OnPageChangeListener {

    DbInfoFragment              dbInfoFragment;
    SingleUpdateFragment        singleUpdateFragment;
    ContinuousUpdateFragment    continuousUpdateFragment;

    MySectionsPagerAdapter      mySectionsPagerAdapter;
    ViewPager                   viewPager;

    Thread t;

    public GPSTracker           gps;
    private LocationListener    locationListener;
    private LocationListener    locationListenerForDebugWithoutWritingToDb;
    private LocationListener    locationListenerForUpdates;
//    private Location            oldLocation;
    private double oldLatitude;
    private double oldLongitude;

    public LocalDatabaseHandler        localDatabaseHandler;
    public RemoteDatabaseHandler       remoteDatabase;

    int currentGroupId;
    int remoteMaxGroupId;
    int localMaxGroupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        currentGroupId = 0;
        remoteDatabase              = new RemoteDatabaseHandler();
        localDatabaseHandler        = new LocalDatabaseHandler(this);
        gps                         = new GPSTracker(this);

        dbInfoFragment              = new DbInfoFragment();
        singleUpdateFragment        = new SingleUpdateFragment();
        continuousUpdateFragment    = new ContinuousUpdateFragment();

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mySectionsPagerAdapter      = new MySectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(mySectionsPagerAdapter);
        viewPager.setOnPageChangeListener(this);

        locationListenerForDebugWithoutWritingToDb = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                gps.onLocationChanged(location);
                singleUpdateFragment.updateViews();
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
                Log.d("onLocationsChanged", String.valueOf(location.getTime()));
//                Location l = new Location(location);
//                Location l = location;

                if(oldLatitude == 0.0 // should be changed probably, cause 0.0 could be valid
                        || (   oldLatitude  != location.getLatitude()
                            && oldLongitude != location.getLongitude() )
                        ) {
                    gps.setLocation(location);
//                    localDatabaseHandler.addLocation(l, currentGroupId); // moved to WriteToDb

//                    if (isNetworkAvailable()) {
                        new WriteToDb().execute(location);
//                    }

//                    if (oldLocation == null) {
//                        oldLocation = new Location(location);
//                    } else {
//                        oldLocation.set(location);
//                    }

                    oldLatitude  = location.getLatitude();
                    oldLongitude = location.getLongitude();
                } else {
                    Log.d("location changed", "not saving the same location");
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {}

            @Override
            public void onProviderEnabled(String s) {}

            @Override
            public void onProviderDisabled(String s) {}
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
//
//        Button btn = ((Button) findViewById(R.id.buttonToggleUpdate));
//
//        if (gps.isRequestingLocationUpdates()) {
//            btn.setText(R.string.button_stop_continuous_update);
//        } else {
//            btn.setText(R.string.button_start_continuous_update);
//        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        gps.stopUsingGPS(locationListener);
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {

    }

    @Override
    public void onPageSelected(int i) {
        invalidateOptionsMenu();
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
//        return super.onPrepareOptionsMenu(menu);
        int pageNum = viewPager.getCurrentItem();
        if (pageNum != 0) {
            menu.findItem(R.id.action_drop_db).setEnabled(false);
            menu.findItem(R.id.action_drop_db).setVisible(false);
            menu.findItem(R.id.action_delete_db).setEnabled(false);
            menu.findItem(R.id.action_delete_db).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.database_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_delete_db:
                localDatabaseHandler.deleteAll();
                return true;
            case R.id.action_drop_db:
                deleteDatabase(localDatabaseHandler.getDatabaseName());
                localDatabaseHandler = new LocalDatabaseHandler(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class WriteToDb extends AsyncTask <Location, Void, Location> {

        @Override
        protected Location doInBackground(Location... locations) {
            Location location = locations[0];
            Log.d(getClass().getSimpleName(), "background... time: " + location.getTime());

            localDatabaseHandler.addLocation(location, currentGroupId);

            if (remoteMaxGroupId != 0) {
                JSONObject json = remoteDatabase.addLocation(
                        remoteMaxGroupId,
                        location.getLatitude(),
                        location.getLongitude(),
                        location.getAccuracy(),
                        location.getTime(),
                        location.getElapsedRealtimeNanos(),
                        location.getAltitude(),
                        location.getBearing(),
                        location.getSpeed(),
                        location.getExtras().getInt("satellites"),
                        location.getProvider());

                try {
                    if (Integer.parseInt(json.getString("success")) == 1) {
                        long _id = Integer.parseInt(json.getJSONObject("added").getString("_id"));
                        remoteDatabase.addGPSSatellites(_id, gps.gpsStatus);
                    }
                } catch (Exception e) {
                    Log.e("Saving satellites error", e.toString());
                }
            } // else {
//                gps.setLocation(location); // if no internet connection, setLocation for view update
//            }

            return location;
        }

        @Override
        protected void onPostExecute(Location location) {
            super.onPostExecute(location);

            Log.d("Got it", String.valueOf(location.getTime()));

            continuousUpdateFragment.updateViews();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager=(ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE); // change 'this' to 'context'
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void getSingleUpdate(View v) {
        locationListener = locationListenerForDebugWithoutWritingToDb;
        gps.requestLocationSingleUpdate(locationListener);
    }

    public void toggleContinuousUpdate(final View v) {

        locationListener = locationListenerForUpdates;

        if (gps.isRequestingLocationUpdates()) {
            gps.stopUsingGPS(locationListener);
            v.setKeepScreenOn(false);
            ((Button)v).setText(R.string.button_start_continuous_update);
        }
        else {

            if (!gps.canGetLocation()) {
                gps.showSettingsAlert();
                return;
            }
            remoteMaxGroupId = 0;
            localMaxGroupId = 0;
            currentGroupId = 0;
            oldLatitude = 0.0;

            final String groupDesc;
            String tmpGroupDesc = ((EditText)findViewById(R.id.edit_text_group_description)).getText().toString();
            if (tmpGroupDesc.isEmpty()) {
                tmpGroupDesc = getString(R.string.group_desc_default);
            }
            groupDesc = tmpGroupDesc;

            if(isNetworkAvailable()) {
                t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject json = remoteDatabase.getNextGroup(groupDesc);
                        try {
                            if (json.getString("success") != null) {
                                if (Integer.parseInt(json.getString("success")) == 1) {
                                    remoteMaxGroupId = Integer.parseInt(json.getJSONObject("nextGroup").getString("_id"));
//                                    localDatabaseHandler.addGroup(currentGroupId, groupDesc);
                                }
                                else {
                                    remoteMaxGroupId = 0;
                                }
                            }
                        } catch (Exception e) {
                            // TODO
//                            e.printStackTrace();
                        }
                    }
                });
                t.start();
            }

            if (t.isAlive()) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    Log.e("Getting group id thread", e.getMessage());
                }
            }

            localMaxGroupId = localDatabaseHandler.getLastGroupId() + 1;
            currentGroupId = (localMaxGroupId > remoteMaxGroupId) ? localMaxGroupId : remoteMaxGroupId ;

            localDatabaseHandler.addGroup(currentGroupId, groupDesc);

            gps.requestLocationUpdates(locationListener);
            v.setKeepScreenOn(true);
            ((Button)v).setText(R.string.button_stop_continuous_update);
        }
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class MySectionsPagerAdapter extends FragmentPagerAdapter {

        public MySectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            //return PlaceholderFragment.newInstance(position + 1);
            //return DestinationChooserFragment.newInstance(getApplicationContext(), position);
            switch (position) {
                case 0:
                    return dbInfoFragment;
                case 1:
                    return singleUpdateFragment;
                case 2:
                    return continuousUpdateFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_fragment_db_info).toUpperCase(l);
                case 1:
                    return getString(R.string.title_fragment_single_update).toUpperCase(l);
                case 2:
                    return getString(R.string.title_fragment_continuous_update).toUpperCase(l);
            }
            return null;
        }
    }
}
