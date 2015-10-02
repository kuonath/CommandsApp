package com.example.kevin.commands;

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kev94 on 24.08.2015.
 */

//asynchronous task to read the content (values in one row) of the database specified by the input command that is set
// in the setCommand() method
public class DatabaseReadTask extends AsyncTask<Void, Void, Exception> {

    private static final String TAG = DatabaseReadTask.class.getSimpleName();

    private StatisticsDBOpenHelper mDBHelper;

    private String mInputCommand;

    private List<Suggestion> suggestions = new ArrayList<>();

    public DatabaseReadTask(StatisticsDBOpenHelper dbHelper) {
        mDBHelper = dbHelper;
    }

    @Override
    protected Exception doInBackground(Void... voids) {
        try {
            Log.i(TAG, "now in async task");
            if (mDBHelper.rowExisting(mInputCommand)) {
                Log.i(TAG, "unknown currentCommand exists -> generate suggestions according to frequency");

                suggestions = mDBHelper.getMostProbableCommands(mInputCommand);

            } else {
                Log.i(TAG, "unknown currentCommand does not exist -> generate suggestions according to occuring words");
            }
        } catch (Exception e) {
            return e;
        }
        return null;
    }

    //setter method to supply the class with the input command (unknown command)
    public void setCommand(String inputCommand) {
        mInputCommand = inputCommand;
    }
    
    public List<Suggestion> getSuggestions() {
        return suggestions;
    }
}
