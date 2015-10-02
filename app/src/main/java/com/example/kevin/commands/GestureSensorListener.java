package com.example.kevin.commands;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Kev94 on 26.08.2015.
 */

/******** Does not listen for the activation gesture for the ASR (see ActivationSensorListener) ****
 * ****** has to updated when a new gesture will be added ******************************************
 */

//Listens for and handles different gestures
public class GestureSensorListener implements SensorEventListener {

    private static final String TAG = GestureSensorListener.class.getSimpleName();

    //constants for the shake gesture
    public static final double SHAKE_THRESHOLD = 5;
    public static final long SHAKE_MAX_EVENT_TIME = 200000000;
    public static final long SHAKE_MAX_TIME_BETWEEN_EVENTS = 1000000000;

    //constants for the nod gesture
    public static final double NOD_THRESHOLD = 5;
    public static final long NOD_MAX_EVENT_TIME = 250000000;
    public static final long NOD_MAX_TIME_BETWEEN_EVENTS = 500000000;

    private Sensor mAccelSensor = null;
    private Sensor mGravSensor = null;
    private Sensor mProximitySensor = null;
    private Sensor mLightSensor = null;
    private SensorManager mSensorManager;

    private int mGesture;

    private boolean mGestureCompleted = false;

    //general
    private long mCurrentTimestamp = 0;
    private long mPreviousTimestamp = 0;

    //Shaking
    private int mShakeCount = 0;

    //Nodding
    private int mNodCount = 0;

    //Cover
    private double mPreviousValue;

    //constructor also takes arguments that tell the class, which sensors will be used for the gesture,
    // and only these listeners will be registered, since this class handles different gestures
    public GestureSensorListener(Context context, SensorManager manager, int gesture, boolean listenForAccel, boolean listenForGrav, boolean listenForCover) {

        mSensorManager = manager;

        mGesture = gesture;

        if(listenForAccel) {
            if (mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
                mAccelSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            } else {
                Toast.makeText(context, context.getString(R.string.toast_no_accel), Toast.LENGTH_LONG).show();
            }
        }

        if(listenForGrav) {
            if (mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null) {
                mGravSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
            } else {
                Toast.makeText(context, context.getString(R.string.toast_no_grav), Toast.LENGTH_LONG).show();
            }
        }

        //some devices are not equipped with a proximity sensor, if that is the case, use the light sensor instead
        if(listenForCover) {
            if(mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null) {
                mProximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            } else {
                if(mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null) {
                    mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

                    mPreviousValue = 0;

                } else {
                    Toast.makeText(context, context.getString(R.string.toast_no_cover), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void registerListener() {
        if(mAccelSensor != null) {
            mSensorManager.registerListener(this, mAccelSensor, SensorManager.SENSOR_DELAY_FASTEST);
            Log.i(TAG, "Accelerometer registered");
        }
        if(mGravSensor != null) {
            mSensorManager.registerListener(this, mGravSensor, SensorManager.SENSOR_DELAY_FASTEST);
            Log.i(TAG, "Gravity Sensor registered");
        }
        if(mProximitySensor != null) {
            mSensorManager.registerListener(this, mProximitySensor, SensorManager.SENSOR_DELAY_FASTEST);
            Log.i(TAG, "Proximity Sensor registered");
        }
        if(mLightSensor != null) {
            mSensorManager.registerListener(this, mLightSensor, SensorManager.SENSOR_DELAY_FASTEST);
            Log.i(TAG, "Light Sensor registered");
        }
    }

    public void unregisterListener() {
        if(mAccelSensor != null) {
            Log.i(TAG, "unregister Accelerometer");
            mSensorManager.unregisterListener(this, mAccelSensor);
        }
        if(mGravSensor != null) {
            Log.i(TAG, "unregister Gravity Sensor");
            mSensorManager.unregisterListener(this, mGravSensor);
        }
        if(mProximitySensor != null) {
            Log.i(TAG, "unregister Proximity Sensor");
            mSensorManager.unregisterListener(this, mProximitySensor);
        }
        if(mLightSensor != null) {
            Log.i(TAG, "unregister Light Sensor");
            mSensorManager.unregisterListener(this, mLightSensor);
        }
    }

    //listens for all the different sensor changes and calls the appropriated method to handle the events according to the type of the sensor that changed its values
    @Override
    public void onSensorChanged(SensorEvent event) {

        switch(event.sensor.getType()) {
            case Sensor.TYPE_LINEAR_ACCELERATION:

                switch(mGesture) {
                    case Constants.GESTURE_SHAKING:
                        detectShaking(event);
                        break;
                    case Constants.GESTURE_NODDING:
                        detectNodding(event);
                        break;
                }
                break;
            case Sensor.TYPE_GRAVITY:
                break;
            case Sensor.TYPE_PROXIMITY:
                detectCover(event);
                break;
            case Sensor.TYPE_LIGHT:
                detectCover(event);
                mPreviousValue = event.values[0];
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    //this method can be improved, but it works
    public void detectShaking(SensorEvent event) {

        if((event.timestamp - mPreviousTimestamp) > SHAKE_MAX_TIME_BETWEEN_EVENTS) {
            mShakeCount = 0;
            mCurrentTimestamp = 0;
            mPreviousTimestamp = 0;
        }

        if(Math.abs(event.values[0]) > SHAKE_THRESHOLD)  {
            if ((event.timestamp - mCurrentTimestamp) > SHAKE_MAX_EVENT_TIME) {
                Log.i(TAG, "Value: " + Double.toString(event.values[0]) + " shakes: " + Integer.toString(mShakeCount) + " time: " + Long.toString(event.timestamp - mCurrentTimestamp));
                mCurrentTimestamp = event.timestamp;
                mShakeCount++;
                mPreviousTimestamp = mCurrentTimestamp;
            }
        }

        if(mShakeCount > 2) {
            mGestureCompleted = true;
        }
    }

    //this method can be improved, but it works
    public void detectNodding(SensorEvent event) {

        if((event.timestamp - mPreviousTimestamp) > NOD_MAX_TIME_BETWEEN_EVENTS) {
            Log.i(TAG, "too much time passed");
            mNodCount = 0;
            mCurrentTimestamp = 0;
            mPreviousTimestamp = 0;
        }

        if(Math.abs(event.values[2]) > NOD_THRESHOLD)  {
            if ((event.timestamp - mCurrentTimestamp) > NOD_MAX_EVENT_TIME) {
                Log.i(TAG, "Value: " + Double.toString(event.values[0]) + " nods: " + Integer.toString(mNodCount) + " time: " + Long.toString(event.timestamp - mCurrentTimestamp));
                mCurrentTimestamp = event.timestamp;
                mNodCount++;
                mPreviousTimestamp = mCurrentTimestamp;
            }
        }

        if(mNodCount > 1) {
            mGestureCompleted = true;
        }
    }

    //uses either the proximity sensor or changes of the light sensor
    public void detectCover(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            if(event.values[0] < (event.sensor.getMaximumRange()/4)) {
                mGestureCompleted = true;
            }
        } else if(event.sensor.getType() == Sensor.TYPE_LIGHT) {
            //Log.i(TAG, "Difference: " +  (mPreviousValue - event.values[0]) + ", previous value: " + mPreviousValue);
            if((mPreviousValue - event.values[0]) > (0.85 * mPreviousValue)) {
                mGestureCompleted = true;
            }
        }
    }

    public boolean getGesture() {
        return mGestureCompleted;
    }
}
