package com.findmybike;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by simonfischer on 01/12/15.
 */
public class WriteToFile {

    private static boolean _append = false;

    public static void writeToFile(String fileName, String content, boolean append){
        _append = append;
        writeToFile(fileName, content);
        _append = false;
    }

    public static void writeToFile(String fileName, String content){
        if(!canWriteOnExternalStorage()) return;

        FileOutputStream outputStream;
        String locationPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        try {


            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);

            FileOutputStream fOut = new FileOutputStream(file, _append);
            fOut.write(content.getBytes());
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
            return true;
        }

        return false;
    }
}
