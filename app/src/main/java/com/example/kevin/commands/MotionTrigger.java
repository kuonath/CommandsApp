package com.example.kevin.commands;

import android.content.Context;
import android.hardware.SensorManager;

/**
 * Created by Kev94 on 13.08.2015.
 */

/******** an older version of the MotionTrigger used the time between the first significant ********
 * ****** motion event and the second significant motion event to decide whether the recorded ******
 * ****** triggers the ASR or not; now the values of the accelerometer are being integrated ********
 * ****** in order to estimate the covered distance (has to be tested with different devices *******
 * ****** if this is robust enough!!!) *************************************************************
 */

public class MotionTrigger {

    private static final String TAG = MotionTrigger.class.getSimpleName();

    private static final float THRESHOLD_ACCEL = 1.5f;

    private static final int LOWER_THRESHOLD_TIME = 150;
    private static final int UPPER_THRESHOLD_TIME = 400;

    private SensorManager mSensorManager;

    private ActivationSensorListener mListener;

    private long mFirstTimestamp = 0;

    public MotionTrigger(Context context) {

        mSensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);

        //listens for both, gravity sensor and linear accelerometer, records the signals and computes some values like magnitude and direction of maximum acceleration
        mListener = new ActivationSensorListener(context, mSensorManager, true, true);
    }

    //checks for the first significant motion in the acceleration data: the phone starts to move in the opposite direction of gravity
    public boolean firstSignificantMotion() {

        if(mListener.getSignOfBiggestValueGrav() == 1) {
            if ((mListener.getMagnitudeAccel() > THRESHOLD_ACCEL) && (mListener.getBiggestValueAccel() > 0) && (mListener.getBiggestValueIndexAccel() == mListener.getBiggestValueIndexGrav())) {

                if (mFirstTimestamp == 0) {
                    mFirstTimestamp = System.currentTimeMillis();
                    mListener.integrate(true);
                    //Log.i(TAG, "First Check");
                }
                return true;
            } else {
                return false;
            }
        }
        else if(mListener.getSignOfBiggestValueGrav() == -1) {
            if ((mListener.getMagnitudeAccel() > THRESHOLD_ACCEL) && (mListener.getBiggestValueAccel() < 0) && (mListener.getBiggestValueIndexAccel() == mListener.getBiggestValueIndexGrav())) {

                if (mFirstTimestamp == 0) {
                    mFirstTimestamp = System.currentTimeMillis();
                    mListener.integrate(true);
                    //Log.i(TAG, "First Check");
                }
                return true;
            } else {
                return false;
            }
        }
        else {
            return false;
        }
    }

    //checks for the first significant motion in the acceleration data: the movement of the phone in the opposite direction of gravity is getting slower again, the phone decelerates
    public boolean secondSignificantMotion() {

        if (mListener.getSignOfBiggestValueGrav() == 1) {
            if ((mListener.getMagnitudeAccel() > THRESHOLD_ACCEL) && (mListener.getBiggestValueAccel() < 0) && (mListener.getBiggestValueIndexAccel() == mListener.getBiggestValueIndexGrav()) && (mFirstTimestamp > 0)) {

                //Log.i(TAG, Long.toString(System.currentTimeMillis() - mFirstTimestamp));
                //Log.i(TAG, Double.toString(mListener.getDistance()));

                mListener.integrate(false);
                return true;

                /*if ((System.currentTimeMillis() - mFirstTimestamp >= LOWER_THRESHOLD_TIME) && (System.currentTimeMillis() - mFirstTimestamp <= UPPER_THRESHOLD_TIME)) {
                    return true;
                } else {
                    mFirstTimestamp = 0;
                    return false;
                }*/
            } else {
                //mFirstTimestamp = 0;
                return false;
            }
        }
        else if (mListener.getSignOfBiggestValueGrav() == -1) {
            if ((mListener.getMagnitudeAccel() > THRESHOLD_ACCEL) && (mListener.getBiggestValueAccel() > 0) && (mListener.getBiggestValueIndexAccel() == mListener.getBiggestValueIndexGrav()) && (mFirstTimestamp > 0)) {

                //Log.i(TAG, Long.toString(System.currentTimeMillis() - mFirstTimestamp));

                mListener.integrate(false);
                return true;

                /*if ((System.currentTimeMillis() - mFirstTimestamp >= LOWER_THRESHOLD_TIME) && (System.currentTimeMillis() - mFirstTimestamp <= UPPER_THRESHOLD_TIME)) {
                    return true;
                } else {
                    mFirstTimestamp = 0;
                    return false;
                }*/
            } else {
                //mFirstTimestamp = 0;
                return false;
            }
        }
        else {
            return false;
        }
    }

    //no significant motion can be detected (happens when there is no motion, constant motion or motion in a wrong direction)
    public boolean noSignificantMotion() {
        if((mListener.getMagnitudeAccel() < 2 && mFirstTimestamp != 0)) {
            mFirstTimestamp = 0;
            mListener.integrate(false);
            return true;
        }
        else {
            return false;
        }
    }

    public void registerListener() {
        if(mListener != null) {
            mListener.registerListener();
        }
    }

    public void unregisterListener() {
        if(mListener != null) {
            mListener.unregisterListener();
        }
    }

    //covered distance between first and second significant motion event
    public double getDistance() {
        return mListener.getDistance();
    }
}
