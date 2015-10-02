package com.example.kevin.commands;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;


/**
 * Created by Kev94 on 07.09.2015.
 */

//one possible option that can be changed in the settings; responsible for the speech rate of the TTS engine;
// a caption ('Speech rate') and a SeekBar that allows the user to tune the speech rate
// to the desired value between 0.1 and 2 with intervals of 0.1
public class SpeechRatePreference extends Preference implements SeekBar.OnSeekBarChangeListener {

    public static int maximum    = 20;
    public static int interval   = 1;

    private float oldValue = 1;
    private TextView monitorBox;

    public SpeechRatePreference(Context context) {
        super(context);
    }

    public SpeechRatePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SpeechRatePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    //create the Relative Layout programmatically
    @Override
    protected View onCreateView(ViewGroup parent){
        super.onCreateView(parent);

        TextView title = new TextView(getContext());
        title.setText(getTitle());
        title.setTextSize(15);
        title.setPadding(10, 10, 10, 10);
        //xml files sliderlayout are only created to get an id for the corresponding view
        title.setId(R.id.pref_speech_rate);

        SeekBar bar = new SeekBar(getContext());
        bar.setMax(maximum);
        bar.setProgress((int) this.oldValue);
        //xml files sliderlayout are only created to get an id for the corresponding view
        bar.setId(R.id.pref_speech_rate_bar);
        bar.setPadding(25, 5, 25, 5);

        this.monitorBox = new TextView(getContext());
        this.monitorBox.setTextSize(12);
        this.monitorBox.setTypeface(Typeface.MONOSPACE, Typeface.ITALIC);
        this.monitorBox.setId(R.id.pref_speech_rate_monitor);
        this.monitorBox.setPadding(0, 0, 25, 0);

        RelativeLayout relLayout = new RelativeLayout(getContext());

        RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams (
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        titleParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        RelativeLayout.LayoutParams barParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        barParams.addRule(RelativeLayout.BELOW, title.getId());
        barParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        RelativeLayout.LayoutParams monitorParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        monitorParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        title.setLayoutParams(titleParams);

        bar.setLayoutParams(barParams);
        bar.setOnSeekBarChangeListener(this);

        this.monitorBox.setLayoutParams(monitorParams);
        this.monitorBox.setPadding(2, 5, 0, 0);
        this.monitorBox.setText(Float.toString(((float) bar.getProgress()) / 10));

        relLayout.addView(title);
        relLayout.addView(bar);
        relLayout.addView(this.monitorBox);
        //xml files sliderlayout are only created to get an id for the corresponding view
        relLayout.setId(android.R.id.widget_frame);

        return relLayout;
    }

    //Listens for changes of the SeekBar
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        progress = Math.round(((float)progress)/interval)*interval;

        if(progress == 0) {
            progress = 1;
        }

        if(!callChangeListener(progress)){
            seekBar.setProgress((int)this.oldValue);
            return;
        }

        seekBar.setProgress(progress);
        this.oldValue = progress;
        this.monitorBox.setText(progress + "");
        updatePreference(progress);

        notifyChanged();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    protected Object onGetDefaultValue(TypedArray ta,int index){

        int dValue = (int)ta.getInt(index,50);

        return validateValue(dValue);
    }


    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

        int temp = restoreValue ? getPersistedInt(50) : (Integer)defaultValue;

        if(!restoreValue)
            persistInt(temp);

        this.oldValue = temp;
    }


    private int validateValue(int value){

        if(value > maximum)
            value = maximum;
        else if(value < 0)
            value = 0;
        else if(value % interval != 0)
            value = Math.round(((float)value)/interval)*interval;


        return value;
    }

    private void updatePreference(int newValue){

        SharedPreferences.Editor editor =  getEditor();
        editor.putInt(getKey(), newValue);
        editor.commit();
    }
}
