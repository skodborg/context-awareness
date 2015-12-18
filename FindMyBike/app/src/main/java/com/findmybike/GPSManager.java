package com.findmybike;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;

/**
 * Created by simonfischer on 28/09/15.
 */
public class GPSManager {

    private Activity _activity;
    private SensorManager _sensorManager;
    private Sensor _sensor;

    public GPSManager(Activity activity){
        _activity = activity;
    }

    public void startGPSByTime(final int timer, final GPSCoordinatesHandler locationHandler){
        LocationManager locationManager = (LocationManager) _activity.getSystemService(Context.LOCATION_SERVICE);



        LocationListener locationListener = new LocationListener() {

            Date date = null;

            @Override
            public void onLocationChanged(Location location) {
                if(date == null) {
                    date = new Date();
                    locationHandler.recievedLocation(location);
                }else{
                    if(new Date().getTime() - date.getTime() > timer){
                        date = new Date();
                        locationHandler.recievedLocation(location);
                    }
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override
            public void onProviderEnabled(String provider) {}
            @Override
            public void onProviderDisabled(String provider) {}
        };

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

    }

    public void startGPSByDistance(final int distance, final GPSCoordinatesHandler locationHandler){

        LocationManager locationManager = (LocationManager) _activity.getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {

            private Location _location = null;

            @Override
            public void onLocationChanged(Location location) {
                if(_location == null){
                    _location = location;
                    locationHandler.recievedLocation(location);
                    return;
                }

                if(_location.distanceTo(location) >= distance){
                    _location = location;
                    locationHandler.recievedLocation(location);
                }

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override
            public void onProviderEnabled(String provider) {}
            @Override
            public void onProviderDisabled(String provider) {}
        };


        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

    }

    public void startGPSByDistance(final int distance, final float distancePrMilliSec, final GPSCoordinatesHandler locationHandler){

        final int timeBetweenGPSFixes = Math.round(((float)distance)/distancePrMilliSec)*1000;

        LocationManager locationManager = (LocationManager) _activity.getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {

            Date date = null;

            @Override
            public void onLocationChanged(Location location) {
                if(date == null) {
                    date = new Date();
                    locationHandler.recievedLocation(location);
                }else{
                    if(new Date().getTime() - date.getTime() > timeBetweenGPSFixes){
                        date = new Date();
                        locationHandler.recievedLocation(location);
                    }
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override
            public void onProviderEnabled(String provider) {}
            @Override
            public void onProviderDisabled(String provider) {}
        };

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

    }

    public void startGPSWithSensor(final int distance, final GPSCoordinatesHandler locationHandler){
        _sensorManager = (SensorManager)_activity.getSystemService(Context.SENSOR_SERVICE);
        _sensor = _sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        _sensorManager.registerListener(new SensorEventListener() {

            private float totalOld = 10000;
            LocationListener locationListener = null;

            @Override
            public void onSensorChanged(SensorEvent event) {

                float[] values = event.values;
                float total = values[0] + values[1] + values[2];

                float max = Math.max(total, totalOld);
                float min = Math.min(total, totalOld);
                totalOld = total;

                if(max - min > 0.5){
                    if(locationListener == null) {
                        locationListener = new LocationListener() {

                            private Location _location = null;

                            @Override
                            public void onLocationChanged(Location location) {
                                if (_location == null) {
                                    _location = location;
                                    locationHandler.recievedLocation(location);
                                    return;
                                }

                                if (_location.distanceTo(location) >= distance) {
                                    _location = location;
                                    locationHandler.recievedLocation(location);
                                }

                            }

                            @Override
                            public void onStatusChanged(String provider, int status, Bundle extras) {
                            }

                            @Override
                            public void onProviderEnabled(String provider) {
                            }

                            @Override
                            public void onProviderDisabled(String provider) {
                            }
                        };
                    }
                    LocationManager locationManager = (LocationManager) _activity.getSystemService(Context.LOCATION_SERVICE);
                    locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);


                }

            }


            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        }, _sensor, SensorManager.SENSOR_DELAY_NORMAL);


    }
    private int wayPointCounter = 1;
    public void wayPoint(final GPSCoordinatesHandler locationHandler){
        locationHandler.recievedWayPoint(wayPointCounter);
        wayPointCounter++;
    }

}
