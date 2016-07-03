package com.doors.thegrid;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {

    private Profile mUser;
    private LocationContentProvider mAdapter;
    private ExpandableListView mListView;
    private TextView mLocationsText;
    private ImageButton mLocationsButton;
    volatile private boolean dropped = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mUser = getIntent().getParcelableExtra("user");

        mLocationsButton =  (ImageButton) findViewById(R.id.locations_button);
        mLocationsText = (TextView) findViewById(R.id.location_text);
        mLocationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dropLocationsList();
            }
        });
        mLocationsText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dropLocationsList();
            }
        });

        LinkedList<Location> mLocs = mUser.getLocations();
        LinkedList<String> locationNames = getNamesOfLocations(mLocs);
        buildListView(locationNames);
    }

    private void dropLocationsList() {
        if(!dropped) {
            mListView.setVisibility(View.VISIBLE);
        } else {
            mListView.setVisibility(View.VISIBLE);
        }
    }

    public LinkedList<String> getNamesOfLocations (LinkedList<Location> locs) {
        LinkedList<String> locationNames = new LinkedList<>();
        for(Location l : locs) {
            locationNames.add(l.getName());
        }
        locationNames.add("add location...");
        return locationNames;
    }

    private void buildListView(LinkedList<String> mLocs) {
        mListView = (ExpandableListView) findViewById(R.id.locations);
        mAdapter = new LocationContentProvider(this, mLocs);
        mListView.setAdapter(mAdapter);
        mListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                Toast.makeText(
                        getApplicationContext(),
                        mUser.getLocations().get(childPosition).getName(), Toast.LENGTH_SHORT)
                        .show();
                return false;
            }
        });
    }

}
