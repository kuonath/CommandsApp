package com.example.kevin.commands;

/**
 * Created by Kev94 on 27.08.2015.
 */

//custom data type; pair of a string and an integer that reflects that a suggestion contains the
// name of a command and the likelihood that the user wanted to say the suggested command instead
// of the unknown command; values are set in the constructor and class contains two get methods
public class Suggestion<String, Integer> {

    private String mCommand;
    private Integer mFrequency;

    public Suggestion(String command, Integer Frequency) {
        mCommand = command;
        mFrequency = Frequency;
    }

    public String getCommand() {
        return mCommand;
    }

    public Integer getFrequency() {
        return mFrequency;
    }
}
