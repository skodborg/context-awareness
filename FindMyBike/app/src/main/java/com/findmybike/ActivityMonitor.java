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
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;

import weka.core.Instance;

/**
 * Created by simonfischer on 08/12/15.
 */
public class ActivityMonitor {

    private Activity _androidActivity;
    private SensorManager _sensorManager;


    private final int windowSize = 32;
    private final int windowSizeHalf = windowSize/2;

    private ArrayList<Double> dataSet1;
    private ArrayList<Double> dataSet2;
    private Sensor _sensor;
    private SensorEventListener _listener;

    private ClassifyActivity _classifyActivity;
    private LocationListener locationListener;
    private Location _currentLocation;

    public ActivityMonitor(Activity androidActivity){
        _androidActivity = androidActivity;
        _classifyActivity = new ClassifyActivity(_androidActivity);
        startTracking();
    }

    public void startTracking(){
        LocationManager locationManager = (LocationManager) _androidActivity.getSystemService(Context.LOCATION_SERVICE);



        locationListener = new LocationListener() {

            Date date = null;

            @Override
            public void onLocationChanged(Location location) {
                  _currentLocation = location;
                BikePosition.setGPSAccuracy(location.getAccuracy());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override
            public void onProviderEnabled(String provider) {}
            @Override
            public void onProviderDisabled(String provider) {}
        };

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        _sensorManager = (SensorManager)_androidActivity.getSystemService(Context.SENSOR_SERVICE);
        _sensor = _sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        _listener = getListener();

        _sensorManager.registerListener(_listener, _sensor, SensorManager.SENSOR_DELAY_FASTEST);

    }
    private SensorEventListener getListener(){
        dataSet1 = new ArrayList<Double>();
        dataSet2 = new ArrayList<Double>();
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
                if(dataSet1.size() == windowSize){
                    testData();
                }

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
    }

    private void testData(){
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
        double[] wekaInstance = new double[3];



        wekaInstance[0] = max;
        wekaInstance[1] = min;
        wekaInstance[2] = std_dev;

        dataSet1 = new ArrayList<Double>(dataSet2);
        dataSet2 = new ArrayList<Double>();

        String state = _classifyActivity.classifyInstance(new Instance(1.0, wekaInstance));

        writeToFile("You are currently " + state + "\n");
        notifyState(state);
    }

    private int tryToShiftState = 0;

    private String currentState = "none";
    private String nextPossibleState = "none";


    private LatLng currentPosition;

    public void notifyState(String state) {
        if(currentState.equals("none")){
            currentState = state;
            return;
        }
        if(!currentState.equals(state) && nextPossibleState.equals("none")){
            nextPossibleState = state;
            tryToShiftState++;
            return;
        }

        if(!currentState.equals(state) && nextPossibleState.equals(state)){
            if(tryToShiftState <= 6){
                if(tryToShiftState == 1){

                    currentPosition = getCurrentPosition();
                }
                tryToShiftState++;
            }else{
                currentState = state;
                nextPossibleState = "none";
                tryToShiftState = 0;
                if(currentState.equals("Walking")) {


                        updateLocationForBike();
                        Toast.makeText(_androidActivity.getApplicationContext(), "position found", Toast.LENGTH_SHORT);

                }
                WriteToFile.writeToFile("shiftingState.txt", "state shifted.\n", true);
            }
        }



    }

    private void writeToFile(String currentActivity){
        WriteToFile.writeToFile("tracking.txt", currentActivity, true);
    }

    public LatLng getCurrentPosition() {
        if(_currentLocation == null){
            return new LatLng(0,0);
        }
        return new LatLng(_currentLocation.getLatitude(), _currentLocation.getLongitude());
    }

    public void updateLocationForBike(){
        BikePosition.setBikePosition(currentPosition);
    }
}
