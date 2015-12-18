package com.findmybike;


import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

/**
 * Created by simonfischer on 01/12/15.
 */
public class Tracker {

    private String _activity;
    private String wakaData;
    private SensorManager _sensorManager;
    private Activity _androidActivity;
    private Sensor _sensor;
    private SensorEventListener _listener;
    private LocationListener _locationListener;
    private Object _lock = new Object();

    private ArrayList<Double> dataSet1;
    private ArrayList<Double> dataSet2;

    private final int windowSize = 32;
    private final int windowSizeHalf = windowSize/2;



    private FastVector _attributes;
    private Instances _data;

    private float _speed = 0;

    public Tracker(Activity androidActivity, String activity) {
        _activity = activity;
        _androidActivity = androidActivity;
        wakaData = "";

        setupWaka();
        startSpeedTracking();

        dataSet1 = new ArrayList<Double>();
        dataSet2 = new ArrayList<Double>();

    }

    private void setupWaka(){



        // 1. setup attributes
        _attributes = new FastVector();
        _attributes.addElement(new Attribute("max", Attribute.NUMERIC));
        _attributes.addElement(new Attribute("min", Attribute.NUMERIC));
        _attributes.addElement(new Attribute("std_dev", Attribute.NUMERIC));




        FastVector classes = new FastVector();
        classes.addElement("Cycling");
        classes.addElement("Walking");
        classes.addElement("WalkingWithBike");



        _attributes.addElement(new Attribute("activity", classes));

        // 2. create instances object
        _data = new Instances("sensordata", _attributes, 0);

    }

    public void startTracking(){
        _sensorManager = (SensorManager)_androidActivity.getSystemService(Context.SENSOR_SERVICE);
        _sensor = _sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        _listener = getListener();

        _sensorManager.registerListener(_listener, _sensor, SensorManager.SENSOR_DELAY_NORMAL);

    }

    private void startSpeedTracking() {
        LocationManager locationManager = (LocationManager) _androidActivity.getSystemService(Context.LOCATION_SERVICE);

        _locationListener = new LocationListener() {

            private Location _location = null;

            @Override
            public void onLocationChanged(Location location) {
                synchronized (_lock) {
                    _speed = location.getSpeed();
                }

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override
            public void onProviderEnabled(String provider) {}
            @Override
            public void onProviderDisabled(String provider) {}
        };


        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, _locationListener);
    }

    public void stopTracking(){
        WriteToFile.writeToFile(_activity  + "_" + new Date().toString() + ".arff", _data.toString());
        _sensorManager.unregisterListener(_listener);
        _listener = null;
    }

    private SensorEventListener getListener(){
        return new SensorEventListener() {

            @Override
            public void onSensorChanged(SensorEvent event) {
                float[] values = event.values;
                    double x = values[0];
                    double y = values[1];
                    double z = values[2];

                    double euclidic = Math.sqrt((x*x)+(y*y)+(z*z));

                    if(dataSet1.size() >= windowSizeHalf){
                        dataSet2.add(euclidic);
                    }

                    dataSet1.add(euclidic);

                  Log.w("dataset1.size()", "" + dataSet1.size());
                    if(dataSet1.size() == windowSize){
                        saveToDataObject();
                    }

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
    }

    private void saveToDataObject(){
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        double std = 0;
        double totalMax = 0;
        double euclSum = 0;


        for(double dataPoint : dataSet1){

            max = Math.max(dataPoint, max);
            min = Math.min(dataPoint, min);

            euclSum += dataPoint;

        }
        double mean = euclSum/windowSize;
        double std_dev = Math.sqrt(mean);
        double[] wekaInstance = new double[4];



        wekaInstance[0] = max;
        wekaInstance[1] = min;
        wekaInstance[2] = std_dev;

        if(_activity.equals("cycling")){
            wekaInstance[3] = 0;

        }else if(_activity.equals("walking")){
            wekaInstance[3] = 1;
        }else{
            wekaInstance[3] = 2;
        }


        _data.add(new Instance(1.0, wekaInstance));

        dataSet1 = new ArrayList<Double>(dataSet2);
        dataSet2 = new ArrayList<Double>();
    }

}
