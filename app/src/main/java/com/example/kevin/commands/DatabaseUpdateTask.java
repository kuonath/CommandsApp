package com.example.kevin.commands;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by Kev94 on 24.08.2015.
 */

//asynchronous task to update the database (either add a new column when a new and unknown command
// has been heard or increment the value in the table cell that corresponds to the row containing
// the unknown command and the column containing the correct command
public class DatabaseUpdateTask extends AsyncTask<Void, Void, Exception> {

    private static final String TAG = DatabaseUpdateTask.class.getSimpleName();

    private StatisticsDBOpenHelper mDBHelper;

    private String mCurrentCommand;
    private String mCommandToCorrect;

    public DatabaseUpdateTask(StatisticsDBOpenHelper dbHelper) {
        mDBHelper = dbHelper;
    }

    @Override
    protected Exception doInBackground(Void... voids) {
        try {
            Log.i(TAG, "now in asynch task");
            if (mDBHelper.rowExisting(mCommandToCorrect)) {
                Log.i(TAG, "unknown command " + mCommandToCorrect  + " exists");

                mDBHelper.incrementValue(mCommandToCorrect, mCurrentCommand);

            } else {
                Log.i(TAG, "unknown command " + mCommandToCorrect + " does not exist");
                mDBHelper.insertNewRow(mCommandToCorrect, mCurrentCommand);
            }
        } catch (Exception e) {
            return e;
        }
        return null;
    }

    //setter method to supply the class with the unknown and the correct command
    public void setCommands(String currentCommand, String commandToCorrect) {
        mCurrentCommand = currentCommand;
        mCommandToCorrect = commandToCorrect;
    }
}
