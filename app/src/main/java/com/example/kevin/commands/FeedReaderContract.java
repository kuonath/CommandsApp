package com.example.kevin.commands;

import android.provider.BaseColumns;

/**
 * Created by Kev94 on 21.08.2015.
 */

/************** Has to be updated when new commands have been added, since this means **************
 * ************ that there have to be new columns!!! After adding columns, the version *************
 * ************ number should be updated as well or the app should be uninstalled and **************
 * ************ and reinstalled again **************************************************************
 */

//defines table contents and some properties of the database
public final class FeedReaderContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public FeedReaderContract() {}

    /* Inner class that defines the table contents */
    public static abstract class FeedEntry implements BaseColumns {
        public static final String DATABASE_NAME = "suggestions.db";
        public static final String DATABASE_TABLE = "Suggestions";
        public static final int DATABASE_VERSION = 1;
        public static final String KEY_ID = "_id";

        public static final String KEY_UNKNOWN_COMMAND_COLUMN = "UNKNOWN_COMMAND";
        public static final String KEY_SEARCH_COLUMN = "SEARCH";
        public static final String KEY_OPEN_BROWSER_COLUMN = "OPEN_BROWSER";
        public static final String KEY_OPEN_TTSBROSWER_COLUMN = "OPEN_TTSBROWSER";
        public static final String KEY_NEW_EMAIL_COLUMN = "NEW_EMAIL";
        public static final String KEY_OPEN_MUSIC_PLAYER_COLUMN = "OPEN_MUSIC_PLAYER";
        public static final String KEY_OPEN_CALENDER_COLUMN = "OPEN_CALENDER";
        public static final String KEY_TAKE_A_PICTURE_COLUMN = "TAKE_A_PICTURE";
        public static final String KEY_TAKE_A_NOTE_COLUMN = "TAKE_A_NOTE";
        public static final String KEY_OPEN_MAPS_COLUMN = "OPEN_MAPS";
        public static final String KEY_SEND_TEXT_MESSAGE_COLUMN = "SEND_TEXT_MESSAGE";
        public static final String KEY_CALL_SOMEONE_COLUMN = "CALL_SOMEONE";
        public static final String KEY_OPEN_CALCULATOR_COLUMN = "OPEN_CALCULATOR";
        public static final String KEY_SHOW_CONTACTS_COLUMN = "SHOW_CONTACTS";
        public static final String KEY_SHOW_PHOTOS_COLUMN = "SHOW_PHOTOS";
        public static final String KEY_SHOW_COMMANDS_COLUMN = "SHOW_COMMANDS";
        public static final String KEY_CANCEL_COLUMN = "CANCEL";

        public static final String[] ALL_COLUMNS = new String[]{KEY_UNKNOWN_COMMAND_COLUMN,
                KEY_SEARCH_COLUMN,
                KEY_OPEN_BROWSER_COLUMN,
                KEY_OPEN_TTSBROSWER_COLUMN,
                KEY_NEW_EMAIL_COLUMN,
                KEY_OPEN_MUSIC_PLAYER_COLUMN,
                KEY_OPEN_CALENDER_COLUMN,
                KEY_TAKE_A_PICTURE_COLUMN,
                KEY_TAKE_A_NOTE_COLUMN,
                KEY_OPEN_MAPS_COLUMN,
                KEY_SEND_TEXT_MESSAGE_COLUMN,
                KEY_CALL_SOMEONE_COLUMN,
                KEY_OPEN_CALCULATOR_COLUMN,
                KEY_SHOW_CONTACTS_COLUMN,
                KEY_SHOW_PHOTOS_COLUMN,
                KEY_SHOW_COMMANDS_COLUMN,
                KEY_CANCEL_COLUMN};
    }
}


