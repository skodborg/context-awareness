package com.findmybike;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.InputStream;
import java.io.ObjectInputStream;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private Activity _this;
    private Tracker _walkingTracker;
    private Tracker _cyclingTracker;
    private Tracker _walkBikeTracker;
    private FastVector _attributes;
    private Instances _data;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _this = this;
        setListeners();
        new ActivityMonitor(this);
    }



    private void setListeners() {

        _walkingTracker = new Tracker(this, "walking");
        _cyclingTracker = new Tracker(this, "cycling");
        _walkBikeTracker = new Tracker(this, "walkBike");

        final Button walkingButton =  (Button) findViewById(R.id.walkingButton);
        walkingButton.setOnClickListener(new View.OnClickListener() {

            boolean active = false;

            @Override
            public void onClick(View v) {
                active = !active;
                if(active){
                    _walkingTracker.startTracking();
                }else{
                    _walkingTracker.stopTracking();
                }
            }
        });

        final Button cyclingButton =  (Button) findViewById(R.id.cyclingButton);
        cyclingButton.setOnClickListener(new View.OnClickListener() {

            boolean active = false;

            @Override
            public void onClick(View v) {
                active = !active;
                if(active){
                    _cyclingTracker.startTracking();
                }else{
                    _cyclingTracker.stopTracking();
                }
            }
        });

        final Button walkBikeButton =  (Button) findViewById(R.id.walkWithCycleButton);
        walkBikeButton.setOnClickListener(new View.OnClickListener() {

            boolean active = false;

            @Override
            public void onClick(View v) {
                active = !active;
                if (active) {
                    _walkBikeTracker.startTracking();
                } else {
                    _walkBikeTracker.stopTracking();
                }
            }
        });

        final Button openGoogleMaps = (Button)findViewById(R.id.findBikeOnGoogleMaps);
        openGoogleMaps.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                startActivity(new Intent(_this, MapsActivity.class));
            }
        });
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
