package com.example.kevin.commands;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Kev94 on 28.08.2015.
 */

//class that describes a NodGesture and implements the gesture interface; logic is implemented in
// GestureSensorListener
public class CoverGesture implements iGestures, SensorEventListener {

    private static final String TAG = CoverGesture.class.getSimpleName();

    private Sensor mProximitySensor = null;
    private Sensor mLightSensor = null;
    private SensorManager mSensorManager;

    private double mPreviousValue = 0;

    private boolean mGestureCompleted = false;

    public CoverGesture(Context context) {

        mSensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);

        //some devices are not equipped with a proximity sensor, if that is the case, use the light sensor instead
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

    @Override
    public void registerListener() {
        if(mProximitySensor != null) {
            mSensorManager.registerListener(this, mProximitySensor, SensorManager.SENSOR_DELAY_FASTEST);
            Log.i(TAG, "Proximity Sensor registered");
        }
        if(mLightSensor != null) {
            mSensorManager.registerListener(this, mLightSensor, SensorManager.SENSOR_DELAY_FASTEST);
            Log.i(TAG, "Light Sensor registered");
        }
    }

    @Override
    public void unregisterListener() {
        if(mProximitySensor != null) {
            Log.i(TAG, "unregister Proximity Sensor");
            mSensorManager.unregisterListener(this, mProximitySensor);
        }
        if(mLightSensor != null) {
            Log.i(TAG, "unregister Light Sensor");
            mSensorManager.unregisterListener(this, mLightSensor);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            if(event.values[0] < (event.sensor.getMaximumRange()/4)) {
                mGestureCompleted = true;
            }
        } else if(event.sensor.getType() == Sensor.TYPE_LIGHT) {
            //Log.i(TAG, "Difference: " +  (mPreviousValue - event.values[0]) + ", previous value: " + mPreviousValue);
            if((mPreviousValue - event.values[0]) > (0.85 * mPreviousValue)) {
                mGestureCompleted = true;
            }
            mPreviousValue = event.values[0];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public boolean gestureCompleted() {
        return mGestureCompleted;
    }
}
