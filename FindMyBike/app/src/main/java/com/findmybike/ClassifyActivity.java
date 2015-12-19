package com.findmybike;

import android.app.Activity;
import android.util.Log;

import java.io.InputStream;
import java.io.ObjectInputStream;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Created by simonfischer on 08/12/15.
 */
public class ClassifyActivity {


    private FastVector _attributes;
    private Instances _data;
    private Activity _activity;
    private Classifier _classifier;
    private String[] _activities = new String[]{"Cycling", "Walking"};

    public ClassifyActivity(Activity activity){
        _activity = activity;
        setupModel();
        setupWaka();
    }

    public String classifyInstance(Instance instance){
        instance.setDataset(_data);

        try {
            return _activities[(int)_classifier.classifyInstance(instance)];
        } catch (Exception e) {
            e.printStackTrace();
            return "none";
        }

    }

    private void setupWaka(){



        // 1. setup attributes
        _attributes = new FastVector();
        _attributes.addElement(new Attribute("max", Attribute.NUMERIC));
        _attributes.addElement(new Attribute("min", Attribute.NUMERIC));
        _attributes.addElement(new Attribute("std_dev", Attribute.NUMERIC));




        FastVector classes = new FastVector();
        for(int i = 0; i < _activities.length; i++){
            classes.addElement(_activities[i]);
        }
        //classes.addElement("WalkingWithBike");



        _attributes.addElement(new Attribute("activity", classes));

        // 2. create instances object
        _data = new Instances("sensordata", _attributes, 0);
        _data.setClassIndex(3);

    }

    private void setupModel(){
        try {
            //InputStream is = _activity.getResources().openRawResource(R.raw.test);
            InputStream is = _activity.getResources().openRawResource(R.raw.full_data);
            ObjectInputStream ois = new ObjectInputStream(is);

            _classifier =  (Classifier) ois.readObject();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
