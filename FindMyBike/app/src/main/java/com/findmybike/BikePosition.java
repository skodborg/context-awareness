package com.findmybike;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by simonfischer on 17/12/15.
 */
public class BikePosition {

    private static LatLng _bikePosition;
    private static float _accuracy = 0;

    public static boolean bikeIsParked(){
        return _bikePosition != null;
    }

    public static LatLng getBikePosition(){
        if(_bikePosition == null){
            return new LatLng(0,0);
        }

        return _bikePosition;
    }

    public static void setBikePosition(final LatLng position){
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                LatLng bikePosition = new MapWindowFetcher().getBestPositionFit(position.latitude, position.longitude, _accuracy);

                _bikePosition = position;
            }});
        thread.start();
    }

    public static void setGPSAccuracy(float accuracy){
        _accuracy = accuracy;
    }

    public static float getAccuracy(){

        return _accuracy;
    }
}
