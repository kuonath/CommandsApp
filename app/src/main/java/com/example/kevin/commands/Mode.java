package com.example.kevin.commands;

/**
 * Created by Kev94 on 27.08.2015.
 */

//every listening mode consists of a pair of values: the number of the mode and an additional extra,
// although the extra can be NO_EXTRA (see Constants for a list of possible commands and extras)
public class Mode {

    private int mMode;
    private int mExtra;

    public Mode(int mode, int extra) {
        mMode = mode;
        mExtra = extra;
    }

    public int getMode() {
        return mMode;
    }

    public int getExtra() {
        return mExtra;
    }

    public void setMode(int number) {
        mMode = number;
    }

    public void setExtra(int extra) {
        mExtra = extra;
    }
}
