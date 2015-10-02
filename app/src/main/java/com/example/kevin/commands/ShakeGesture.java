package com.example.kevin.commands;

import android.content.Context;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Created by Kev94 on 26.08.2015.
 */

//class that describes a ShakeGesture and implements the gesture interface; logic is implemented in
// GestureSensorListener; used to tell the phone that it did not understand the command correctly
// and that the user wants to repeat the command
public class ShakeGesture implements iGestures {

    private static final String TAG = ShakeGesture.class.getSimpleName();

    private SensorManager mSensorManager;

    private GestureSensorListener mListener;

    public ShakeGesture(Context context) {

        mSensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);

        //this gesture only needs to listen for the accelerometer
        mListener = new GestureSensorListener(context, mSensorManager, Constants.GESTURE_SHAKING, true, false, false);
    }

    @Override
    public void registerListener() {
        if(mListener != null) {
            mListener.registerListener();
            Log.i(TAG, "Shake listener registered");
        }
    }

    @Override
    public void unregisterListener() {
        if(mListener != null) {
            mListener.unregisterListener();
        }
    }

    @Override
    public boolean gestureCompleted() {
        return mListener.getGesture();
    }
}
