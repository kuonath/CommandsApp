package com.example.kevin.commands;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Toast;

//Listens for the sensors that are used to detect the movement which activates the ASR
// (linear accelerometer and gravity sensor); it also computes some values (e.g. magnitude)
// that are used by the MotionTrigger to detect significant movements
public class ActivationSensorListener implements SensorEventListener {

    private static final String TAG = ActivationSensorListener.class.getSimpleName();

    private Sensor mAccelSensor;
    private Sensor mGravSensor;
    private SensorManager mSensorManager;

    private boolean mListenForAccel;
    private boolean mListenForGrav;

    private double mMagnitudeAccel;
    private double mBiggestValueAccel;
    private int mBiggestValueIndexAccel;

    private double mMagnitudeGrav;
    private double mBiggestValueGrav;
    private int mBiggestValueIndexGrav;
    private int mSignOfBiggestValueGrav;

    private boolean mIntegrating = false;
    private boolean mFirstValue = true;
    private long mPreviousTimeStamp;
    private double mDistance = 0;

    //constructor takes two arguments which indicate if the listener should listen for the
    // accelerometer and the gravity sensor (usually both should be true)
    public ActivationSensorListener(Context context, SensorManager manager, boolean listenForAccel, boolean listenForGrav) {

        mSensorManager = manager;

        mListenForAccel = listenForAccel;
        mListenForGrav = listenForGrav;

        if(mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            mAccelSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        }
        else {
            Toast.makeText(context, context.getString(R.string.toast_no_accel), Toast.LENGTH_LONG).show();
        }

        if(mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null) {
            mGravSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        }
        else {
            Toast.makeText(context, context.getString(R.string.toast_no_grav), Toast.LENGTH_LONG).show();
        }
    }

    public void registerListener() {
        if((mAccelSensor != null) && mListenForAccel) {
            mSensorManager.registerListener(this, mAccelSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
        if((mGravSensor != null) && mListenForGrav) {
            mSensorManager.registerListener(this, mGravSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    public void unregisterListener() {
        if((mAccelSensor != null) && mListenForAccel) {
            mSensorManager.unregisterListener(this, mAccelSensor);
        }
        if((mGravSensor != null) && mListenForGrav) {
            mSensorManager.unregisterListener(this, mGravSensor);
        }
    }

    //reacts on changes of sensors and carries out code in an if-else block according to the type
    //of the sensors whose values have changed; also computes the magnitude, the direction with the
    //maximum sensor value and integrates the values of the acceleration twice sensor over time
    //to estimate the covered distance of the movement
    @Override
    public void onSensorChanged(SensorEvent event) {

        if(event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

            mMagnitudeAccel = Math.sqrt(Math.pow(event.values[0],2) + Math.pow(event.values[1],2) + Math.pow(event.values[2],2));

            computeBiggestValueAndIndex(event.values, event.sensor.getType());

            if(mIntegrating) {
                if(mFirstValue) {
                    mPreviousTimeStamp =  event.timestamp;
                    mFirstValue = false;
                }
                else {
                    double dT = (event.timestamp - mPreviousTimeStamp) * Constants.NANO_TO_SECONDS;
                    mDistance = mDistance + (mMagnitudeAccel * dT * dT);
                    mPreviousTimeStamp = event.timestamp;
                }
            }
        }
        else if(event.sensor.getType() == Sensor.TYPE_GRAVITY) {

            mMagnitudeGrav = Math.sqrt(Math.pow(event.values[0],2) + Math.pow(event.values[1],2) + Math.pow(event.values[2],2));

            computeBiggestValueAndIndex(event.values, event.sensor.getType());
        }
    }

    // TODO: 08.09.2015  there are more efficient implementations
    public void computeBiggestValueAndIndex(float[] values, int type) {

        double[] absValues = new double[3];

        for(int i=0; i<3; i++) {
            absValues[i] = Math.abs(values[i]);
        }

        switch(type) {
            case Sensor.TYPE_LINEAR_ACCELERATION:
                if((absValues[0] > absValues[1]) && (absValues[0] > absValues[2])) {
                    mBiggestValueAccel = values[0];
                    mBiggestValueIndexAccel = 0;
                }
                else if((absValues[1] > absValues[0]) && (absValues[0] > absValues[2])) {
                    mBiggestValueAccel = values[1];
                    mBiggestValueIndexAccel = 1;
                }
                else if((absValues[2] > absValues[0]) && (absValues[0] > absValues[1])) {
                    mBiggestValueAccel = values[2];
                    mBiggestValueIndexAccel = 2;
                }
                break;
            case Sensor.TYPE_GRAVITY:
                if((absValues[0] > absValues[1]) && (absValues[0] > absValues[2])) {
                    mBiggestValueGrav = values[0];
                    mSignOfBiggestValueGrav = (int) Math.signum(values[0]);
                    mBiggestValueIndexGrav = 0;
                }
                else if((absValues[1] > absValues[0]) && (absValues[0] > absValues[2])) {
                    mBiggestValueGrav = values[1];
                    mSignOfBiggestValueGrav = (int) Math.signum(values[1]);
                    mBiggestValueIndexGrav = 1;
                }
                else if((absValues[2] > absValues[0]) && (absValues[0] > absValues[1])) {
                    mBiggestValueGrav = values[2];
                    mSignOfBiggestValueGrav = (int) Math.signum(values[2]);
                    mBiggestValueIndexGrav = 2;
                }
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    //tells the class whether it should integrate the values or not (set by the MotionTrigger
    // between the first and second significant event
    public void integrate(boolean start) {
        mIntegrating = start;

        mPreviousTimeStamp = 0;
        mFirstValue = start;

        if(start) {
            mDistance = 0;
        }
    }

    //some methods to read the computed and stored values of this class
    public double getMagnitudeAccel() {
        return mMagnitudeAccel;
    }

    public double getBiggestValueAccel() {
        return mBiggestValueAccel;
    }

    public int getBiggestValueIndexAccel() {
        return mBiggestValueIndexAccel;
    }

    public double getMagnitudeGrav() {
        return mMagnitudeGrav;
    }

    public double getBiggestValueGrav() {
        return mBiggestValueGrav;
    }

    public int getBiggestValueIndexGrav() {
        return mBiggestValueIndexGrav;
    }

    public int getSignOfBiggestValueGrav() {
        return mSignOfBiggestValueGrav;
    }

    public double getDistance() {
        return mDistance;
    }
}
