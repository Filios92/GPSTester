package com.example.filip.myapplication;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.filip.myapplication.R;
import com.example.filip.myapplication.db.DbHandler;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LocationsFromDbList extends Activity {

    DbHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations_from_db_list);

        TextView t = (TextView) findViewById(R.id.textViewList);
        db = new DbHandler(this);
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
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.locations_from_db_list, menu);
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
}
