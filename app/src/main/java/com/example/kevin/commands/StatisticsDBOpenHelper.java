package com.example.kevin.commands;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.example.kevin.commands.FeedReaderContract.FeedEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kev94 on 21.08.2015.
 */

/*********** class has to updated when new commands have been added!! Add new columns **************
 * ********* to the table! Either increment the version number of the database in the **************
 * ********* class FeedReaderContract (not sure if this will work properly) or uninstall ***********
 * ********* and reisntall the app again on the phone (works for sure). In both cases **************
 * ********* the previously recorded statistics cannot be used again in the current version ********
 */

//database contains statistics of how many times commands were not correctly understood (either wrong
// existing command or unknown command) and corrected with the correct command;
// currently only useful if a language model with uni-, bi- and trigrams is used to recognize the
// commands; does not work with a defined grammar containing defined commands;

// columns are named after existing commands and in the first column the unknown commands that have
// been heard so far are listed; every table cell contains the number of times that the unknown command
// x in row y was corrected and the correct command was command a in column b
public class StatisticsDBOpenHelper extends SQLiteOpenHelper {

    private static final String TAG = StatisticsDBOpenHelper.class.getSimpleName();

    /** update the following string when new commands have been added! ****************************/

    //SQL command to create a new database table
    private static final String DATABASE_CREATE = "create table " + FeedEntry.DATABASE_TABLE + " (" + FeedEntry.KEY_ID + " integer primary key autoincrement, "
            + FeedEntry.KEY_UNKNOWN_COMMAND_COLUMN + " text not null, "
            + FeedEntry.KEY_SEARCH_COLUMN + " integer, "
            + FeedEntry.KEY_OPEN_BROWSER_COLUMN + " integer, "
            + FeedEntry.KEY_OPEN_TTSBROSWER_COLUMN + " integer, "
            + FeedEntry.KEY_NEW_EMAIL_COLUMN + " integer, "
            + FeedEntry.KEY_OPEN_MUSIC_PLAYER_COLUMN + " integer, "
            + FeedEntry.KEY_OPEN_CALENDER_COLUMN + " integer, "
            + FeedEntry.KEY_TAKE_A_PICTURE_COLUMN + " integer, "
            + FeedEntry.KEY_TAKE_A_NOTE_COLUMN + " integer, "
            + FeedEntry.KEY_OPEN_MAPS_COLUMN + " integer, "
            + FeedEntry.KEY_SEND_TEXT_MESSAGE_COLUMN + " integer, "
            + FeedEntry.KEY_CALL_SOMEONE_COLUMN + " integer, "
            + FeedEntry.KEY_OPEN_CALCULATOR_COLUMN + " integer, "
            + FeedEntry.KEY_SHOW_CONTACTS_COLUMN + " integer, "
            + FeedEntry.KEY_SHOW_PHOTOS_COLUMN + " integer, "
            + FeedEntry.KEY_SHOW_COMMANDS_COLUMN + " integer, "
            + FeedEntry.KEY_CANCEL_COLUMN + " integer);";



    public StatisticsDBOpenHelper(Context context) {
        super(context, FeedEntry.DATABASE_NAME, null, FeedEntry.DATABASE_VERSION);
        //Toast.makeText(context, context.getDatabasePath(FeedEntry.DATABASE_NAME).getAbsolutePath(), Toast.LENGTH_LONG).show();
    }

    //if database does not yet exist, create it
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "create database");
        db.execSQL(DATABASE_CREATE);
    }

    //called when the version number of the database has been updated
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.w("TaskDBAdapter", "Upgrading from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");

        db.execSQL("DROP TABLE IF IT EXISTS " + FeedEntry.DATABASE_TABLE);
        onCreate(db);
    }

    //checks if the unknown command has already been heard or if it is new
    public boolean rowExisting(String commandToCorrect) {
        SQLiteDatabase db = this.getReadableDatabase();
        Log.i(TAG, "readable database for checking if unknown command " + commandToCorrect + " exists");

        String[] resultColumns = new String[]{FeedEntry.KEY_UNKNOWN_COMMAND_COLUMN};
        String where = FeedEntry.KEY_UNKNOWN_COMMAND_COLUMN + "=?";
        String[] selectionArgs = new String[]{commandToCorrect.toUpperCase().replace(" ", "_")};

        Log.i(TAG, "where: " + where);
        Log.i(TAG, "selectionArgs: " + selectionArgs[0]);

        Cursor cursor = db.query(FeedEntry.DATABASE_TABLE, resultColumns, where, selectionArgs, null, null, null);

        return cursor.moveToFirst();
    }

    //reads the value in one row corresponding to the unknown command and in one column corresponding to the correct command
    private int readValue(String commandToCorrect, String columnName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Log.i(TAG, "readable database for reading values");

        String[] resultColumns = FeedEntry.ALL_COLUMNS;
        String where = FeedEntry.KEY_UNKNOWN_COMMAND_COLUMN + "=?";
        String[] selectionArgs = new String[]{commandToCorrect.toUpperCase().replace(" ", "_")};

        Cursor cursor = db.query(FeedEntry.DATABASE_TABLE, resultColumns, where, selectionArgs, null, null, null);

        //Don't forget the following command to get an Integer
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndexOrThrow(columnName);
        int oldValue = cursor.getInt(columnIndex);
        Log.i(TAG, "old value: " + Integer.toString(oldValue));

        cursor.close();
        return oldValue;
    }

    //increment the value in the table cell in the row of the unknown command and in the column with the correct command
    public void incrementValue(String commandToCorrect, String command) {
        SQLiteDatabase db = this.getWritableDatabase();

        String where = FeedEntry.KEY_UNKNOWN_COMMAND_COLUMN + "=?";
        String columnName = command.toUpperCase().replace(" ", "_");

        Log.i(TAG, "column name: " + columnName);

        int oldValue = readValue(commandToCorrect, columnName);

        ContentValues updatedValue = new ContentValues();
        updatedValue.put(columnName, oldValue + 1);

        Log.i(TAG, "Update Value for row " + commandToCorrect.toUpperCase().replace(" ", "_") + " and column " + columnName + " to " + Integer.toString(oldValue + 1));
        db.update(FeedEntry.DATABASE_TABLE, updatedValue, where, new String[]{commandToCorrect.toUpperCase().replace(" ", "_")});
    }

    //called if a new unknown commend has been heard; creates a new row for this command
    public void insertNewRow(String commandToCorrect, String command) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] resultColumns = FeedEntry.ALL_COLUMNS;
        String columnName = command.toUpperCase().replace(" ", "_");
        Log.i(TAG, "column name: " + columnName);

        ContentValues newValues = new ContentValues();

        List<String> zeroColumns = new ArrayList<>();

        int commandIndex = 0;

        newValues.put(resultColumns[0], commandToCorrect.toUpperCase().replace(" ", "_"));

        Log.i(TAG, "going to add new row for command " + commandToCorrect.toUpperCase());

        for (int i = 1; i < resultColumns.length; i++) {

            if(resultColumns[i].equals(columnName)) {
                Log.i(TAG, "command index: " + i);
                commandIndex = i;
            } else {
                zeroColumns.add(resultColumns[i]);
            }
        }

        for(String column : zeroColumns) {
            newValues.put(column, 0);
        }

        newValues.put(resultColumns[commandIndex], 1);

        db.insert(FeedEntry.DATABASE_TABLE, null, newValues);
        Log.i(TAG, "Value for column " + resultColumns[commandIndex] + " set to one");
    }

    //scans all the numbers in one row and returns the three commands (column names) with the highest numbers
    public List<Suggestion> getMostProbableCommands(String inputCommand) {
        List<Suggestion> mostProbableCommands = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Log.i(TAG, "readable database for reading values");

        String[] resultColumns = FeedEntry.ALL_COLUMNS;
        String where = FeedEntry.KEY_UNKNOWN_COMMAND_COLUMN + "=?";
        String[] selectionArgs = new String[]{inputCommand.toUpperCase().replace(" ", "_")};

        Cursor cursor = db.query(FeedEntry.DATABASE_TABLE, resultColumns, where, selectionArgs, null, null, null);

        Log.i(TAG, "move cursor to first element");
        //Don't forget the following command to get an Integer
        cursor.moveToFirst();

        int[] frequencies = new int[Constants.NUMBER_OF_COMMANDS];
        int[] indices = new int[Constants.NUMBER_OF_COMMANDS];

        //frequencies is a 0 based array, while the column names start with the index 1 (the column zero contains the names of the unknown commands)
        for(int i=0; i<Constants.NUMBER_OF_COMMANDS; i++) {
            Log.i(TAG, "Value of column " + cursor.getColumnName(i+1) + ": " + Integer.toString(cursor.getInt(i+1)));
            frequencies[i] = cursor.getInt(i+1);
            indices[i] = i+1;
        }

        for(int i=1; i<Constants.NUMBER_OF_COMMANDS; i++) {
            int j=i;
            int value = frequencies[i];
            int index = indices[i];

            while((j > 0) && (frequencies[j-1] > value)) {
                frequencies[j] = frequencies[j-1];
                indices[j] = indices[j-1];
                j=j-1;
            }
            frequencies[j] = value;
            indices[j] = index;
        }

        for(int i=1; i<=3; i++) {

            if(frequencies[Constants.NUMBER_OF_COMMANDS-i] != 0) {
                //String command = cursor.getColumnName(indices[Constants.NUMBER_OF_COMMANDS - i]).replace("_", " ").toLowerCase();
                //Log.i(TAG, "Command " + command + " with frequency " + Integer.toString(frequencies[Constants.NUMBER_OF_COMMANDS - i]));
                Suggestion<String, Integer> command = new Suggestion<>(cursor.getColumnName(indices[Constants.NUMBER_OF_COMMANDS - i]).replace("_", " ").toLowerCase(), frequencies[Constants.NUMBER_OF_COMMANDS - i]);
                mostProbableCommands.add(command);
            }
        }

        cursor.close();

        return mostProbableCommands;
    }
}
