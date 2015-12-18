package com.findmybike;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Created by simonfischer on 03/12/15.
 */
public class ConvertToOverlappingWindows {

    private ArrayList<String> fileNames;
    private String path = "/Users/simonfischer/Documents/context_data/";
    private String pathEndLocation = "/Users/simonfischer/Documents/context_data/converted/";

    private String ENDFILENAME = "cominedSetTest.arff";
    private Instances _data;
    private FastVector _attributes;
    private Instances _newData;


    private final int windowSize = 32;
    private final int windowSizeHalf = windowSize/2;

    public static void main(String[] args){
        new ConvertToOverlappingWindows();
    }

    public ConvertToOverlappingWindows(){
        fileNames = new ArrayList<String>();
        /*
        TRANING DATA
         */
       /* fileNames.add("cycling_Thu Dec 03 09:10:06 CET 2015.arff");
        fileNames.add("cycling_Wed Dec 02 11:06:08 CET 2015.arff");
        fileNames.add("2_cycling_Thu Dec 03 15:10:41 CET 2015.arff");
        fileNames.add("cycling_Wed Dec 02 13:10:21 CET 2015.arff");
        fileNames.add("walking_Tue Dec 01 17:52:40 CET 2015.arff");
        fileNames.add("walking_Tue Dec 01 18:21:15 CET 2015.arff");*/

        /*
        TEST DATA
         */

        fileNames.add("test_walking_2.arff");
        fileNames.add("test_cycling.arff");


        startConverting();
    }

    private void startConverting(){
        setupWaka();
        for(String fileName : fileNames) {
            try {
                BufferedReader reader = new BufferedReader(
                        new FileReader(path + fileName));
                _data = new Instances(reader);

                reader.close();


                convertFile();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        writeToFile();
    }


    private void convertFile(){

        int length = _data.numInstances();

        boolean stop = false;
        System.out.println("length = " + (length - ((length - 20) % windowSize)) + " actual length " + length);
        int totalMax = 0;
        for(int i = 20; i < length - ((length - 20) % windowSize) - windowSizeHalf; i += windowSizeHalf){

            double max = Double.MIN_VALUE;
            double min = Double.MAX_VALUE;
            double std = 0;
            double euclSum = 0;


            for(int n = i; n < i + windowSizeHalf; n++){

                Instance instance = _data.instance(n);
                totalMax = Math.max(totalMax, n);
                double[] wekaData = instance.toDoubleArray();

                double x = wekaData[1];
                double y = wekaData[2];
                double z = wekaData[3];

                double euclidic = Math.sqrt((x*x)+(y*y)+(z*z));

                max = Math.max(euclidic, max);
                min = Math.min(euclidic, min);

                euclSum += euclidic;

            }
            double mean = euclSum/windowSize;
            double std_dev = Math.sqrt(mean);
            double[] wekaInstance = new double[4];

            wekaInstance[0] = max;
            wekaInstance[1] = min;
            wekaInstance[2] = std_dev;
            if(_data.relationName().equals("cycling")){
                wekaInstance[3] = 0;

            }else{
                wekaInstance[3] = 1;
            }
            _newData.add(new Instance(1.0, wekaInstance));

           // System.out.println("max: " + max + ", min: " + min + " mean: " + euclSum/128 + " std dev: " + std_dev);

        }

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
        _newData = new Instances("sensordata", _attributes, 0);

        System.out.println("new data 1. " + _newData);
    }
    private void writeToFile() {


        FileOutputStream fop = null;
        File file;
        String content = "This is the text content";

        try {

            file = new File(pathEndLocation + ENDFILENAME);
            fop = new FileOutputStream(file);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            // get the content in bytes
            byte[] contentInBytes = _newData.toString().getBytes();

            fop.write(contentInBytes);
            fop.flush();
            fop.close();

            System.out.println("Done");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fop != null) {
                    fop.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



}
