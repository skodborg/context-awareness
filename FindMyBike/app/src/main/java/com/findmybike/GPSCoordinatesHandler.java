package com.findmybike;

import android.location.Location;

interface GPSCoordinatesHandler {
    void recievedLocation(Location location);
    void recievedWayPoint(int wayPoint);
}
