package com.example.filip.myapplication;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.example.filip.myapplication.db.LocalDatabaseHandler;

public class LocationsFromDb extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

    LocalDatabaseHandler db;
    SimpleCursorAdapter mAdapter;

    // These are the Contacts rows that we will retrieve
    static final String[] PROJECTION = new String[] {ContactsContract.Data._ID,
            ContactsContract.Data.DISPLAY_NAME};

    // This is the select criteria
    static final String SELECTION = "((" +
            ContactsContract.Data.DISPLAY_NAME + " NOTNULL) AND (" +
            ContactsContract.Data.DISPLAY_NAME + " != '' ))";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations_from_db_list);

        ListView listViewOfSavedLocations = (ListView) findViewById(R.id.listViewOfSavedLocations1);

        // For the cursor adapter, specify which columns go into which views
        String[] fromColumns = {LocalDatabaseHandler.KEY_ID};
        int[] toViews = {android.R.id.text1}; // The TextView in simple_list_item_1

        db = new LocalDatabaseHandler(this);
        mAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, db.getDataCursor(), fromColumns, toViews, 0);

        listViewOfSavedLocations.setAdapter(mAdapter);

//        getLoaderManager().initLoader(0, null, this);
/*
        TextView t = (TextView) findViewById(R.id.textViewList);
        db = new LocalDatabaseHandler(this);
        Cursor c = db.getDataCursor();
        t.setText("Data:\n");
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
        Calendar calendar = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        while (c.moveToNext()) {
            calendar.setTimeInMillis(c.getLong(4));
            calendar2.setTimeInMillis(c.getLong(5) / 1000000);
            t.append(c.getInt(0) + ". \nLat " + c.getDouble(1) +
                    " \nLng " + c.getDouble(2) +
                    " \nAcc " + c.getDouble(3) +
                    " \nTime " + format.format(calendar.getTime()) +
                    " \nTime nano " + format.format(calendar2.getTime()) +
                    " \nAlt " + c.getDouble(6) +
                    " \nBer " + c.getDouble(7) +
                    " Spe " + c.getDouble(8) +
                    " Sat " + c.getInt(9) + "\n");
        }
        c.close();
*/
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
                db.deleteAll();
                return true;
            case R.id.action_drop_db:
                deleteDatabase("locations.db");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(this, ContactsContract.Data.CONTENT_URI,
                PROJECTION, SELECTION, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
    }
}
