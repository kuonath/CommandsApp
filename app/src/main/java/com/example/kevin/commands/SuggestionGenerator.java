package com.example.kevin.commands;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Kev94 on 18.08.2015.
 */

/******* this class has to updated when new commands have been added; especially *******************
 ******* the part where the suggestions are generated according to occurences **********************
 ******* of words (add more cases when you add new words ******************************************/

//currently only used if there are unknown commands (can only happen if language models with uni-, bi-
// and trigrams are used, not used if there is a defined grammar with defined commands;
// suggestions are either generated with the help of the database that keeps track of
// confusions of commands (e.g. 'open email' was often corrected to 'open browser' -> 'open browser
// will be one of the suggestions) or according to occurrences of words that are part of an existing
// command (only limited number of words -> only words that already occur can be recognized)
public class SuggestionGenerator {

    private static final String TAG = SuggestionGenerator.class.getSimpleName();

    private int mSuggestionCounter;

    List<Suggestion> mOutputCommands = new ArrayList<>();

    private StatisticsDBOpenHelper mDBHelper;
    private DatabaseReadTask mDBReadTask;

    //dbHelper to open/read database
    public SuggestionGenerator(StatisticsDBOpenHelper dbHelper) {
        mSuggestionCounter = 0;
        mDBHelper = dbHelper;
    }

    //'intelligent' refers to the use of statistics stored in a database; with continued use of the
    // app there is 'learning effect' (suggestions are more accurate, since unknown commands have
    // been corrected a lot and there should be a pattern in the confusion of the different commands
    public List<Suggestion> generateIntelligentSuggestions(String inputCommand) {

        mDBReadTask = new DatabaseReadTask(mDBHelper);
        mDBReadTask.setCommand(inputCommand);
        mDBReadTask.execute();

        //read database in an asynchronous task, but wait at least 2 seconds, because the info
        // is needed in the next steps
        try {
            mDBReadTask.get(2000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {

        }

        //get suggestions according to likelihood that the unknown command is confused with one of
        // the existing commands
        List<Suggestion> intelligentSuggestions = mDBReadTask.getSuggestions();

        for(Suggestion suggestion : intelligentSuggestions) {
            mOutputCommands.add(suggestion);
            mSuggestionCounter++;
        }

        Log.i(TAG, Integer.toString(mSuggestionCounter) + " intelligent suggestions");

        //there have to be three suggestions; so if there is not enough info in the database, fill
        // the list of suggestions with commands that contain words that occur in the unknown command as well
        if(mSuggestionCounter < Constants.NUMBER_OF_SUGGESTIONS) {
            generateSuggestionReplace(inputCommand);
        }

        Log.i(TAG, mSuggestionCounter + " suggestions in total");

        //mOutputCommands is a global variable! It might have been changed in function generateSuggestionReplace
        //not a very nice way to do it, should be changed in the future
        return mOutputCommands;
    }

    /**update this method when new commands have been added**/

    //check if the unknown command contains a word that already occurs in an existing command
    // and replace the word with an empty string, so that the word will not be considered again
    public void generateSuggestionReplace(String inputCommand) {

        int loopCounter = mSuggestionCounter;

        List<String> commands = new ArrayList<>();

        for (Suggestion suggestion : mOutputCommands) {
            commands.add((String)suggestion.getCommand());
        }

        while(loopCounter < Constants.NUMBER_OF_SUGGESTIONS){

            Log.i(TAG, "loop counter: " + loopCounter);
            if (inputCommand.contains("search")) {

                if(!commands.contains(Constants.SEARCH)) {
                    Suggestion<String, Integer> suggestion = new Suggestion<>(Constants.SEARCH, 0);
                    mOutputCommands.add(suggestion);
                    mSuggestionCounter++;
                }
                inputCommand = inputCommand.replace("search", " ");

            } else if (inputCommand.contains("browser")) {

                if(!commands.contains(Constants.OPEN_BROWSER)) {
                    Suggestion<String, Integer> suggestion = new Suggestion<>(Constants.OPEN_BROWSER, 0);
                    mOutputCommands.add(suggestion);
                    mSuggestionCounter++;
                }
                inputCommand = inputCommand.replace("browser", " ");

            } else if (inputCommand.contains("e-mail")) {

                if(!commands.contains(Constants.NEW_EMAIL)) {
                    Suggestion<String, Integer> suggestion = new Suggestion<>(Constants.NEW_EMAIL, 0);
                    mOutputCommands.add(suggestion);
                    mSuggestionCounter++;
                }
                inputCommand = inputCommand.replace("e-mail", " ");
                inputCommand = inputCommand.replace("new", " ");

            } else if (inputCommand.contains("new")) {

                if(!commands.contains(Constants.NEW_EMAIL)) {
                    Suggestion<String, Integer> suggestion = new Suggestion<>(Constants.NEW_EMAIL, 0);
                    mOutputCommands.add(suggestion);
                    mSuggestionCounter++;
                }
                inputCommand = inputCommand.replace("e-mail", " ");
                inputCommand = inputCommand.replace("new", " ");

            } else if (inputCommand.contains("music")) {

                if(!commands.contains(Constants.OPEN_MUSIC_PLAYER)) {
                    Suggestion<String, Integer> suggestion = new Suggestion<>(Constants.OPEN_MUSIC_PLAYER, 0);
                    mOutputCommands.add(suggestion);
                    mSuggestionCounter++;
                }
                inputCommand = inputCommand.replace("music", " ");
                inputCommand = inputCommand.replace("player", " ");

            } else if (inputCommand.contains("player")) {

                if(!commands.contains(Constants.OPEN_MUSIC_PLAYER)) {
                    Suggestion<String, Integer> suggestion = new Suggestion<>(Constants.OPEN_MUSIC_PLAYER, 0);
                    mOutputCommands.add(suggestion);
                    mSuggestionCounter++;
                }
                inputCommand = inputCommand.replace("music", " ");
                inputCommand = inputCommand.replace("player", " ");

            } else if (inputCommand.contains("calendar")) {

                if(!commands.contains(Constants.OPEN_CALENDAR)) {
                    Suggestion<String, Integer> suggestion = new Suggestion<>(Constants.OPEN_CALENDAR, 0);
                    mOutputCommands.add(suggestion);
                    mSuggestionCounter++;
                }
                inputCommand = inputCommand.replace("calendar", " ");

            } else if (inputCommand.contains("maps")) {

                if(!commands.contains(Constants.OPEN_MAPS)) {
                    Suggestion<String, Integer> suggestion = new Suggestion<>(Constants.OPEN_MAPS, 0);
                    mOutputCommands.add(suggestion);
                    mSuggestionCounter++;
                }
                inputCommand = inputCommand.replace("maps", " ");

            } else if (inputCommand.contains("picture")) {

                if(!commands.contains(Constants.TAKE_A_PICTURE)) {
                    Suggestion<String, Integer> suggestion = new Suggestion<>(Constants.TAKE_A_PICTURE, 0);
                    mOutputCommands.add(suggestion);
                    mSuggestionCounter++;
                }
                inputCommand = inputCommand.replace("picture", " ");

            } else if (inputCommand.contains("note")) {

                if(!commands.contains(Constants.TAKE_A_NOTE)) {
                    Suggestion<String, Integer> suggestion = new Suggestion<>(Constants.TAKE_A_NOTE, 0);
                    mOutputCommands.add(suggestion);
                    mSuggestionCounter++;
                }
                inputCommand = inputCommand.replace("note", " ");

            } else if (inputCommand.contains("send")) {

                if(!commands.contains(Constants.SEND_TEXT_MESSAGE)) {
                    Suggestion<String, Integer> suggestion = new Suggestion<>(Constants.SEND_TEXT_MESSAGE, 0);
                    mOutputCommands.add(suggestion);
                    mSuggestionCounter++;
                }
                inputCommand = inputCommand.replace("send", " ");
                inputCommand = inputCommand.replace("text", " ");
                inputCommand = inputCommand.replace("message", " ");

            } else if (inputCommand.contains("text")) {

                if(!commands.contains(Constants.SEND_TEXT_MESSAGE)) {
                    Suggestion<String, Integer> suggestion = new Suggestion<>(Constants.SEND_TEXT_MESSAGE, 0);
                    mOutputCommands.add(suggestion);
                    mSuggestionCounter++;
                }
                inputCommand = inputCommand.replace("send", " ");
                inputCommand = inputCommand.replace("text", " ");
                inputCommand = inputCommand.replace("message", " ");

            } else if (inputCommand.contains("message")) {

                if(!commands.contains(Constants.SEND_TEXT_MESSAGE)) {
                    Suggestion<String, Integer> suggestion = new Suggestion<>(Constants.SEND_TEXT_MESSAGE, 0);
                    mOutputCommands.add(suggestion);
                    mSuggestionCounter++;
                }
                inputCommand = inputCommand.replace("send", " ");
                inputCommand = inputCommand.replace("text", " ");
                inputCommand = inputCommand.replace("message", " ");

            } else if (inputCommand.contains("call")) {

                if(!commands.contains(Constants.CALL_SOMEONE)) {
                    Suggestion<String, Integer> suggestion = new Suggestion<>(Constants.CALL_SOMEONE, 0);
                    mOutputCommands.add(suggestion);
                    mSuggestionCounter++;
                }
                inputCommand = inputCommand.replace("someone", " ");
                inputCommand = inputCommand.replace("call", " ");

            } else if (inputCommand.contains("someone")) {

                if(!commands.contains(Constants.CALL_SOMEONE)) {
                    Suggestion<String, Integer> suggestion = new Suggestion<>(Constants.CALL_SOMEONE, 0);
                    mOutputCommands.add(suggestion);
                    mSuggestionCounter++;
                }
                inputCommand = inputCommand.replace("someone", " ");
                inputCommand = inputCommand.replace("call", " ");

            }

            loopCounter++;
        }
    }
}
