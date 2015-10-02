package com.example.kevin.commands;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

/**
 * Created by Kev94 on 08.09.2015.
 */

//shows in the beginning of the app and lets the user choose different settings (activate ASR,
// open-domain queries, TTS, Sensors, Touch), so the app does not have to set up unused features;
// these settings should also be added to the main settings menu (preference screen; with maybe
// more possibilities to select features (e.g. only detect shaking gestures but not gestures
// related to light/proximity sensor)
public class DialogFeaturesFragment extends DialogFragment {

    private static final String TAG = DialogFeaturesFragment.class.getSimpleName();

    private CheckBox mASRBox;
    private CheckBox mQueryBox;
    private CheckBox mTTSBox;
    private CheckBox mSensorBox;
    private CheckBox mTouchBox;
    private CheckBox mHapticBox;

    private boolean mActivateASR = false;
    private boolean mActivateQuery = false;
    private boolean mActivateTTS = false;
    private boolean mActivateSensors = false;
    private boolean mActivateTouch = false;
    private boolean mActivateHaptic = false;

    //Listener that is implemented by CommandsActivity to inform the Activity which button was clicked
    //(information about the button is stored in the title of the button
    // (OK or close; maybe settings when implemented))
    public interface iOnDialogButtonClickListener {
        public void onFinishFeaturesDialog(String buttonClicked);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View content = inflater.inflate(R.layout.dialog_features, null);

        mASRBox = (CheckBox) content.findViewById(R.id.asr_box);
        mQueryBox = (CheckBox) content.findViewById(R.id.query_box);
        mTTSBox = (CheckBox) content.findViewById(R.id.tts_box);
        mSensorBox = (CheckBox) content.findViewById(R.id.sensor_box);
        mTouchBox = (CheckBox) content.findViewById(R.id.touch_box);
        mHapticBox = (CheckBox) content.findViewById(R.id.haptic_box);

        // read shared preferences and check CheckBoxes accordingly so that the CheckBoxes show the settings
        // of the last use of the app when the app gets opened for the next time
        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mActivateASR = sharedPrefs.getBoolean(Constants.PREF_KEY_ASR_ACTIVE, false);
        mActivateQuery = sharedPrefs.getBoolean(Constants.PREF_KEY_OPEN_DOMAIN, false);
        mActivateTTS = sharedPrefs.getBoolean(Constants.PREF_KEY_TTS_ACTIVE, false);
        mActivateSensors = sharedPrefs.getBoolean(Constants.PREF_KEY_SENSORS_ACTIVE, false);
        mActivateTouch = sharedPrefs.getBoolean(Constants.PREF_KEY_TOUCH_ACTIVE, false);
        mActivateHaptic = sharedPrefs.getBoolean(Constants.PREF_KEY_HAPTIC_ACTIVE, false);

        mASRBox.setChecked(mActivateASR);
        mQueryBox.setChecked(mActivateQuery);
        mTTSBox.setChecked(mActivateTTS);
        mSensorBox.setChecked(mActivateSensors);
        mTouchBox.setChecked(mActivateTouch);
        mHapticBox.setChecked(mActivateHaptic);

        //onClickListener for CheckBoxes to check/uncheck them and activate/deactivate their features

        mASRBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = !mASRBox.isChecked();
                if (checked) {
                    Log.i(TAG, "checked");
                    mASRBox.setChecked(false);
                    mActivateASR = false;
                    mQueryBox.setChecked(false);
                    mQueryBox.setEnabled(false);
                    mActivateQuery = false;
                } else {
                    Log.i(TAG, "not checked");
                    mASRBox.setChecked(true);
                    mActivateASR = true;
                    mQueryBox.setEnabled(true);
                }
            }
        });

        mQueryBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = !mQueryBox.isChecked();
                if (checked) {
                    mQueryBox.setChecked(false);
                    mActivateQuery = false;
                } else {
                    mQueryBox.setChecked(true);
                    mActivateQuery = true;
                }
            }
        });

        mTTSBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = !mTTSBox.isChecked();
                if (checked) {
                    mTTSBox.setChecked(false);
                    mActivateTTS = false;
                } else {
                    mTTSBox.setChecked(true);
                    mActivateTTS = true;
                }
            }
        });

        mSensorBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = !mSensorBox.isChecked();
                if (checked) {
                    mSensorBox.setChecked(false);
                    mActivateSensors = false;
                } else {
                    mSensorBox.setChecked(true);
                    mActivateSensors = true;
                }
            }
        });

        mTouchBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = !mTouchBox.isChecked();
                if (checked) {
                    mTouchBox.setChecked(false);
                    mActivateTouch = false;
                } else {
                    mTouchBox.setChecked(true);
                    mActivateTouch = true;
                }
            }
        });

        mHapticBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = !mHapticBox.isChecked();
                if (checked) {
                    mHapticBox.setChecked(false);
                    mActivateHaptic = false;
                } else {
                    mHapticBox.setChecked(true);
                    mActivateHaptic = true;
                }
            }
        });

        builder.setView(content)

                .setMessage(getString(R.string.select_features))

                //save values to Shared Preferences so they can be read again in the CommandsActivity and the selected features can be activated
                .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = sharedPrefs.edit();
                        editor.putBoolean(Constants.PREF_KEY_ASR_ACTIVE, mActivateASR);
                        editor.putBoolean(Constants.PREF_KEY_OPEN_DOMAIN, mActivateQuery);
                        editor.putBoolean(Constants.PREF_KEY_TTS_ACTIVE, mActivateTTS);
                        editor.putBoolean(Constants.PREF_KEY_SENSORS_ACTIVE, mActivateSensors);
                        editor.putBoolean(Constants.PREF_KEY_TOUCH_ACTIVE, mActivateTouch);
                        editor.putBoolean(Constants.PREF_KEY_HAPTIC_ACTIVE, mActivateHaptic);
                        editor.commit();

                        iOnDialogButtonClickListener activity = (iOnDialogButtonClickListener) getActivity();
                        activity.onFinishFeaturesDialog(getString(R.string.button_ok));
                    }
                })

                //neutral Button could be used to open settings to enable or disable smaller features before the start (e.g. use sensors for shake detection, but not for canceling)

                /*.setNeutralButton(R.string.button_settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })*/

                //dialog dismissed automatically, close activity (action taken in implementation of
                // iOnDialogButtonClickListener in CommandsActivity itself)
                .setNegativeButton(R.string.button_close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        iOnDialogButtonClickListener activity = (iOnDialogButtonClickListener) getActivity();
                        activity.onFinishFeaturesDialog(getString(R.string.button_close));
                    }
                });

        return builder.create();
    }
}
