package com.example.filip.myapplication.fragments;



import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.example.filip.myapplication.MapsActivity;
import com.example.filip.myapplication.MyActivity;
import com.example.filip.myapplication.R;
import com.example.filip.myapplication.db.LocalDatabaseHandler;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class DbInfoFragment extends Fragment {

    MyActivity myActivity;
    LocalDatabaseHandler db;
    SimpleCursorAdapter mAdapter;
    ListView listViewOfSavedLocations;

    public DbInfoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myActivity = (MyActivity) getActivity();
        db = myActivity.localDatabaseHandler;

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_db_info, container, false);

        listViewOfSavedLocations = (ListView) v.findViewById(R.id.listViewOfSavedGroups);

        try {
            updateViews(v);
        } catch (NullPointerException e) {
            Log.e("er", "null");
        }

        listViewOfSavedLocations.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                long groupId = l;
                Intent intent = new Intent(view.getContext(), MapsActivity.class);
                Bundle b = new Bundle();
                b.putLong("groupId", groupId);
                intent.putExtras(b);
                startActivity(intent);
            }
        });

        return v;
    }

    public void updateViews(View v) {

//        ListView listViewOfSavedLocations = (ListView) v.findViewById(R.id.listViewOfSavedGroups);

        // For the cursor adapter, specify which columns go into which views
        final String[] fromColumns = {
                LocalDatabaseHandler.KEY_ID,
                LocalDatabaseHandler.KEY_GROUP_DESC,
                LocalDatabaseHandler.KEY_GROUP_COUNT,
                LocalDatabaseHandler.KEY_AVG_LATITUDE,
                LocalDatabaseHandler.KEY_AVG_LONGITUDE,
                LocalDatabaseHandler.KEY_GROUP_TIME_START,
                LocalDatabaseHandler.KEY_GROUP_TIME_END
        };
        int[] toViews = {
                R.id.groupInfoId,
                R.id.groupInfoDesc,
                R.id.groupInfoCount,
                R.id.groupInfoAvgLatitude,
                R.id.groupInfoAvgLongitude,
                R.id.groupInfoStartTime,
                R.id.groupInfoEndTime
        };

        Cursor c = db.getGroupsInfoCursor();

        mAdapter = new SimpleCursorAdapter(myActivity, R.layout.list_item_group_info, c, fromColumns, toViews, 0);
        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int column) {
                TextView tv;
                switch (column) {
                    case 3:
                    case 4:
                        tv = (TextView) view;
                        double avg = cursor.getDouble(cursor.getColumnIndex(fromColumns[column]));
                        tv.setText(String.valueOf(avg));
                        return true;
                    case 5:
                    case 6:
                        tv = (TextView) view;
                        long date = cursor.getLong(cursor.getColumnIndex(fromColumns[column]));
                        DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(date);
                        String formattedDate = format.format(calendar.getTime());
                        tv.setText(formattedDate);
                        return true;
                    default:
                        break;
                }
                return false;
            }
        });

        listViewOfSavedLocations.setAdapter(mAdapter);
    }

}
