package com.example.filip.myapplication.fragments;



import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.filip.myapplication.GPSTracker;
import com.example.filip.myapplication.MyActivity;
import com.example.filip.myapplication.R;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class SingleUpdateFragment extends Fragment {

    MyActivity myActivity;
    GPSTracker gps;

    public SingleUpdateFragment() {
        // Required empty public constructor
        // or move to onCreateView
//        myActivity = (MyActivity) getActivity();
//        gps = myActivity.gps;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myActivity = (MyActivity) getActivity();
        gps = myActivity.gps;

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_single_update, container, false);
    }

    public void updateViews() {
        ((TextView) getView().findViewById(R.id.single_update_table_latitude)).setText(String.valueOf(gps.getLatitude()));
        ((TextView) getView().findViewById(R.id.single_update_table_longitude)).setText(String.valueOf(gps.getLongitude()));
        ((TextView) getView().findViewById(R.id.single_update_table_accuracy)).setText(String.valueOf(gps.getAccuracy()));
        ((TextView) getView().findViewById(R.id.single_update_table_time)).setText(String.valueOf(gps.getTime()));
        ((TextView) getView().findViewById(R.id.single_update_table_time_nano)).setText(String.valueOf(gps.getElapsedRealtimeNanos()));
        ((TextView) getView().findViewById(R.id.single_update_table_altitude)).setText(String.valueOf(gps.getAltitude()));
        ((TextView) getView().findViewById(R.id.single_update_table_bearing)).setText(String.valueOf(gps.getBearing()));
        ((TextView) getView().findViewById(R.id.single_update_table_speed)).setText(String.valueOf(gps.getSpeed()));
        ((TextView) getView().findViewById(R.id.single_update_table_sat)).setText(String.valueOf(gps.getNumberOfSatellites()));
        ((TextView) getView().findViewById(R.id.single_update_table_provider)).setText(String.valueOf(gps.getProvider()));
    }

}
