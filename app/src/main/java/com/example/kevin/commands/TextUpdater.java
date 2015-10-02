package com.example.kevin.commands;

import android.content.Context;
import android.widget.TextView;

/**
 * Created by Kev94 on 17.08.2015.
 */

//called when there is a transition from one listening mode to another to update the text on the screen;
// most of the methods take the next mode as an  input argument, but some methods (that are only called
// once (e.g. initialization, error) are simply called after a listening mode and update the text
public class TextUpdater {

    private Context mContext;

    private TextView[] mTextViews = new TextView[5];

    //all the strings that can be displayed

    //empty string
    private static String empty;

    //strings related to state of the recognizer
    private static String prepare;
    private static String initFailed;
    private static String recognizerNotSetup;

    //message TextView
    private static String welcome;

    //instructions TextView (these strings can be read by the TTS engine
    private static String query;
    private static String instructions;
    private static String select;
    private static String repeat;
    private static String unknown;
    private static String numberTooBig;
    private static String correct;
    private static String dictateMessage;
    private static String dictateCall;
    private static String sayAgain;

    //asr status TextView
    private static String listen;

    //takes the TextViews of the CommandsActivity as input arguments so that it can change their text content
    //context is needed to read the strings from the string.xml file
    public TextUpdater (Context context, TextView[] textViews) {

        mContext = context;
        mTextViews = textViews;

        initializeStrings();
    }

    //read the strings in the strings.xml file
    private void initializeStrings() {
        empty = mContext.getString(R.string.empty);
        prepare = mContext.getString(R.string.prepare);
        initFailed = mContext.getString(R.string.init_failed);
        recognizerNotSetup = mContext.getString(R.string.recognizer_not_setup);
        welcome = mContext.getString(R.string.welcome);
        query = mContext.getString(R.string.query);
        instructions = mContext.getString(R.string.instructions);
        listen = mContext.getString(R.string.listen);
        select = mContext.getString(R.string.select);
        repeat = mContext.getString(R.string.repeat);
        unknown = mContext.getString(R.string.unknown);
        numberTooBig = mContext.getString(R.string.number_too_big);
        correct = mContext.getString(R.string.correct);
        dictateMessage = mContext.getString(R.string.dictate_message);
        dictateCall = mContext.getString(R.string.dictate_call);
        sayAgain = mContext.getString(R.string.say_again);
    }

    //updates the text in the TextViews depending on the listening mode and some additional info;
    // can be the info that the command is not first spoken but repeated, because the first time
    // the wrong command was understood or the info that the number for the selection of an activity
    // out of multiple possible activities was too high
    public void updateText(int listeningMode, int addInfo) {

        switch(listeningMode) {
            case Constants.MODE_NOT_LISTENING:
                mTextViews[Constants.MESSAGE].setText(welcome);
                mTextViews[Constants.INSTRUCTIONS].setText(instructions);
                mTextViews[Constants.ASR_STATUS].setText(empty);
                mTextViews[Constants.ERROR_VIEW].setText(empty);
                break;
            case Constants.MODE_COMMAND:
                if(addInfo == Constants.SAY_AGAIN) {
                    mTextViews[Constants.MESSAGE].setText(welcome);
                    mTextViews[Constants.INSTRUCTIONS].setText(sayAgain);
                    mTextViews[Constants.PARTIAL_RESULT].setText(empty);
                    mTextViews[Constants.ERROR_VIEW].setText(empty);
                } else {
                    mTextViews[Constants.MESSAGE].setText(welcome);
                    mTextViews[Constants.INSTRUCTIONS].setText(empty);
                    mTextViews[Constants.PARTIAL_RESULT].setText(empty);
                    mTextViews[Constants.ERROR_VIEW].setText(empty);
                }
                break;
            case Constants.MODE_HANDLE_UNKNOWN_COMMAND:
                mTextViews[Constants.MESSAGE].setText(welcome);
                mTextViews[Constants.INSTRUCTIONS].setText(unknown);

                mTextViews[Constants.ERROR_VIEW].setText(empty);
                break;
            case Constants.MODE_SELECT:
                if(addInfo == Constants.NO_ADDITIONAL_INFO) {
                    mTextViews[Constants.MESSAGE].setText(welcome);
                    mTextViews[Constants.INSTRUCTIONS].setText(select);
                    mTextViews[Constants.PARTIAL_RESULT].setText(empty);
                    mTextViews[Constants.ERROR_VIEW].setText(empty);
                } else if(addInfo == Constants.TOO_BIG) {
                    mTextViews[Constants.MESSAGE].setText(welcome);
                    mTextViews[Constants.INSTRUCTIONS].setText(numberTooBig);
                    mTextViews[Constants.PARTIAL_RESULT].setText(empty);
                    mTextViews[Constants.ERROR_VIEW].setText(empty);
                }
                break;
            case Constants.MODE_CORRECT:
                if(addInfo == Constants.NO_ADDITIONAL_INFO) {
                    mTextViews[Constants.MESSAGE].setText(welcome);
                    mTextViews[Constants.INSTRUCTIONS].setText(correct);
                    mTextViews[Constants.PARTIAL_RESULT].setText(empty);
                    mTextViews[Constants.ERROR_VIEW].setText(empty);
                } else if(addInfo == Constants.NUMBER_TOO_BIG) {
                    mTextViews[Constants.MESSAGE].setText(welcome);
                    mTextViews[Constants.INSTRUCTIONS].setText(numberTooBig);
                    mTextViews[Constants.PARTIAL_RESULT].setText(empty);
                    mTextViews[Constants.ERROR_VIEW].setText(empty);
                }
                break;
            case Constants.MODE_QUERY:
                mTextViews[Constants.MESSAGE].setText(welcome);
                mTextViews[Constants.INSTRUCTIONS].setText(query);
                mTextViews[Constants.PARTIAL_RESULT].setText(empty);
                mTextViews[Constants.ERROR_VIEW].setText(empty);
                break;
            case Constants.MODE_DICTATE_NUMBER:
                if(addInfo == Constants.CALL) {
                    mTextViews[Constants.MESSAGE].setText(welcome);
                    mTextViews[Constants.INSTRUCTIONS].setText(dictateCall);
                    mTextViews[Constants.PARTIAL_RESULT].setText(empty);
                    mTextViews[Constants.ERROR_VIEW].setText(empty);
                } else if (addInfo == Constants.TEXT) {
                    mTextViews[Constants.MESSAGE].setText(welcome);
                    mTextViews[Constants.INSTRUCTIONS].setText(dictateMessage);
                    mTextViews[Constants.PARTIAL_RESULT].setText(empty);
                    mTextViews[Constants.ERROR_VIEW].setText(empty);
                }
                break;
        }
    }


    public void prepare() {
        mTextViews[Constants.MESSAGE].setText(prepare);
        mTextViews[Constants.INSTRUCTIONS].setText(empty);
        mTextViews[Constants.ASR_STATUS].setText(empty);
        mTextViews[Constants.PARTIAL_RESULT].setText(empty);
        mTextViews[Constants.ERROR_VIEW].setText(empty);
    }

    public void initFailed(Exception result) {
        mTextViews[Constants.MESSAGE].setText(initFailed + " " + result);
        mTextViews[Constants.INSTRUCTIONS].setText(empty);
        mTextViews[Constants.ASR_STATUS].setText(empty);
        mTextViews[Constants.PARTIAL_RESULT].setText(empty);
        mTextViews[Constants.ERROR_VIEW].setText(empty);
    }

    public void recognizerNotSetup() {
        mTextViews[Constants.MESSAGE].setText(recognizerNotSetup);
        mTextViews[Constants.INSTRUCTIONS].setText(empty);
        mTextViews[Constants.ASR_STATUS].setText(empty);
        mTextViews[Constants.PARTIAL_RESULT].setText(empty);
        mTextViews[Constants.ERROR_VIEW].setText(empty);
    }

    //displayed when the user has to confirm
    public void confirm(String command) {
        mTextViews[Constants.MESSAGE].setText(welcome);
        mTextViews[Constants.INSTRUCTIONS].setText(mContext.getString(R.string.confirm, command));

        mTextViews[Constants.ERROR_VIEW].setText(empty);
    }

    public void error(Exception error) {
        mTextViews[Constants.MESSAGE].setText(welcome);
        mTextViews[Constants.INSTRUCTIONS].setText(empty);
        mTextViews[Constants.ASR_STATUS].setText(empty);

        mTextViews[Constants.ERROR_VIEW].setText(error.getMessage());
    }
}
