package com.findmybike;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Time;
import java.util.Date;

/**
 * Created by simonfischer on 28/09/15.
 */
public class LogGPSCoordinatesToFile implements GPSCoordinatesHandler {

    private String _fileName;
    private Activity _activity;

    public LogGPSCoordinatesToFile(String fileName, Activity activity){
        _activity = activity;
        _fileName = fileName;

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);

        if(file != null){
            file.delete();
        }
    }



    @Override
    public void recievedLocation(Location location) {
        if(!canWriteOnExternalStorage()) return;
        FileOutputStream outputStream;
        String locationPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        try {

            Log.w("LOG RESULT", location.toString());

            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), _fileName);

            FileOutputStream fOut = new FileOutputStream(file, true);
            fOut.write((location.getLatitude() + ", " + location.getLongitude() + ", " + new Date().getTime() + "\n").getBytes());
            fOut.close();
        } catch (FileNotFoundException e) {

            Log.d("ERROR", "FileNotFoundException occured " + e.getMessage());

        } catch (IOException e) {
            e.printStackTrace();
            Log.d("ERROR", "IOException occured when writing to file " + e.getMessage());

        }
    }

    private static boolean canWriteOnExternalStorage() {
        // get the state of your external storage
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // if storage is mounted return true
            Log.d("sTag", "Yes, can write to external storage.");
            return true;
        }

        Log.d("sTag", "No, cannot write to external storage.");
        return false;
    }


    @Override
    public void recievedWayPoint(int wayPoint) {
        if(!canWriteOnExternalStorage()) return;
        FileOutputStream outputStream;
        String locationPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        try {

            Log.w("LOG RESULT", "At waypoint " + wayPoint);

            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), _fileName);

            FileOutputStream fOut = new FileOutputStream(file, true);

            fOut.write(("WayPoint: " + wayPoint + " : " +  new Date().getTime() + "\n").getBytes());
            fOut.close();
            Toast.makeText(_activity, "At waypoint " + wayPoint, Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {

            Log.d("ERROR", "FileNotFoundException occured " + e.getMessage());

        } catch (IOException e) {
            e.printStackTrace();
            Log.d("ERROR", "IOException occured when writing to file " + e.getMessage());

        }
    }
}
