package com.example.filip.myapplication;

import android.database.Cursor;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.filip.myapplication.db.LocalDatabaseHandler;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.LinkedList;
import java.util.List;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    long groupId;

    enum MapDrawOptions {
        DRAW_AS_LINE, DRAW_AS_DOTS
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            groupId = b.getLong("groupId");
        }

        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
//        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        new LocationsFromDbDrawer().execute(MapDrawOptions.DRAW_AS_LINE);
//        new OptimizedLocationDrawer().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_change_map_line:
                mMap.clear();
                new LocationsFromDbDrawer().execute(MapDrawOptions.DRAW_AS_LINE);
//                new OptimizedLocationDrawer().execute();
                return true;
            case R.id.action_change_map_dots:
                mMap.clear();
                new LocationsFromDbDrawer().execute(MapDrawOptions.DRAW_AS_DOTS);
//                new OptimizedLocationDrawer().execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class LocationsFromDbDrawer extends AsyncTask<MapDrawOptions, Void, List<Location>> {

        MapDrawOptions mOpt;
        List<LatLng> stdLocations;
        LatLng optimizedLocation;

        @Override
        protected List<Location> doInBackground(MapDrawOptions... opt) {
            mOpt = opt[0];
            LocalDatabaseHandler db = new LocalDatabaseHandler(getApplicationContext());
            Cursor c = db.getDataForGroup(groupId);
            List<Location> poss = new LinkedList<Location>();
            stdLocations = new LinkedList<LatLng>();

            while (c.moveToNext()) {
                Location pos = new Location("gps");

                pos.setLatitude(c.getDouble(c.getColumnIndex(LocalDatabaseHandler.KEY_LATITUDE)));
                pos.setLongitude(c.getDouble(c.getColumnIndex(LocalDatabaseHandler.KEY_LONGITUDE)));
                pos.setAccuracy(c.getFloat(c.getColumnIndex(LocalDatabaseHandler.KEY_ACCURACY)));
                pos.setTime(c.getLong(c.getColumnIndex(LocalDatabaseHandler.KEY_NUMBER_OF_SAT)));

                poss.add(pos);
            }

            c = db.getOptimizedDataForGroup(groupId);
            while (c.moveToNext()) {

                stdLocations.add(new LatLng(
                        c.getDouble(c.getColumnIndex(LocalDatabaseHandler.KEY_LATITUDE)),
                        c.getDouble(c.getColumnIndex(LocalDatabaseHandler.KEY_LONGITUDE))));
            }

            optimizedLocation = new LatLng(
                    db.getOptimizedLatitude(groupId),
                    db.getOptimizedLongitude(groupId)
            );

            Log.d("data normal size = ", String.valueOf(poss.size()));
            Log.d("data optimized size = ", String.valueOf(stdLocations.size()));

            return poss;
        }

        @Override
        protected void onPostExecute(List<Location> poss) {
            switch (mOpt) {
                case DRAW_AS_LINE:
                    List<LatLng> p = new LinkedList<LatLng>();
                    for (Location pos : poss) {
                        p.add(new LatLng(pos.getLatitude(), pos.getLongitude()));
                    }
                    mMap.addPolyline(new PolylineOptions()
                            .addAll(p)
                            .color(0xff77aaff)
                            .width(1)
                            .geodesic(true)
                            .zIndex(1));

                    mMap.addPolyline(new PolylineOptions()
                            .addAll(stdLocations)
                            .color(0xffff0000)
                            .width(1)
                            .geodesic(true)
                            .zIndex(2));

                    break;
                case DRAW_AS_DOTS:
                    CircleOptions c = new CircleOptions()
                            .fillColor(0x0f001199)
                            .strokeColor(0x30001199)
                            .strokeWidth(0.05f);
                    for(Location pos : poss) {
                        if (pos.getAccuracy() < 8 && pos.getTime() > 6) {
                            c.center(new LatLng(pos.getLatitude(), pos.getLongitude()))
                                    .radius(pos.getAccuracy());
                            mMap.addCircle(c);
                        }
                    }
                    break;
                default:
                    break;
            }
            mMap.addCircle(new CircleOptions()
                    .center(optimizedLocation)
                    .fillColor(0xffffaaaa)
                    .radius(1.0)
                    .strokeWidth(0.1f)
                    .strokeColor(0xffcaca));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(poss.get(1).getLatitude(), poss.get(0).getLongitude()), 18));
        }
    }

    private class OptimizedLocationDrawer extends AsyncTask<Void, Void, LatLng> {

        @Override
        protected LatLng doInBackground(Void... voids) {
            LocalDatabaseHandler db = new LocalDatabaseHandler(getApplicationContext());
            LatLng latLng = new LatLng(db.getOptimizedLatitude(groupId), db.getOptimizedLongitude(groupId));
            return latLng;
        }

        @Override
        protected void onPostExecute(LatLng latLng) {
            mMap.addCircle(new CircleOptions()
                .center(latLng)
                .fillColor(0xffffaaaa)
                .radius(1.0)
                .strokeWidth(0.1f)
                .strokeColor(0xffcaca));
        }
    }
}
