package com.example.kevin.commands;

/**
 * Created by Kev94 on 13.08.2015.
 */
public class Constants {

    //Unit conversion
    public static final double NANO_TO_SECONDS = 10E-9;

    //Commands
    public static final int NUMBER_OF_COMMANDS = 15;

    public static final String SEARCH = "search";
    public static final String OPEN_BROWSER = "open browser";
    public static final String OPEN_TTSBROWSER = "open ttsbrowser";
    public static final String NEW_EMAIL = "new email";
    public static final String OPEN_MUSIC_PLAYER = "open music player";
    public static final String OPEN_CALENDAR = "open calendar";
    public static final String TAKE_A_PICTURE = "take a picture";
    public static final String TAKE_A_NOTE = "take a note";
    public static final String OPEN_MAPS = "open maps";
    public static final String SEND_TEXT_MESSAGE = "send text message";
    public static final String CALL_SOMEONE = "call someone";
    public static final String OPEN_CALCULATOR = "open calculator";
    public static final String SHOW_CONTACTS = "show contacts";
    public static final String SHOW_PHOTOS = "show photos";
    public static final String SHOW_COMMANDS = "show commands";
    public static final String CANCEL = "cancel";

    public static final String ALL_COMMANDS = SEARCH + "," +
            OPEN_BROWSER + "," +
            NEW_EMAIL + "," +
            OPEN_MUSIC_PLAYER + "," +
            OPEN_CALENDAR + "," +
            TAKE_A_PICTURE + "," +
            TAKE_A_NOTE + "," +
            OPEN_MAPS + "," +
            SEND_TEXT_MESSAGE + "," +
            CALL_SOMEONE + "," +
            OPEN_CALCULATOR + "," +
            SHOW_CONTACTS + "," +
            SHOW_PHOTOS + "," +
            SHOW_COMMANDS + "," +
            CANCEL;

    //MODE HANDLE UNKNOWN COMMAND
    public static final String REPEAT = "repeat";
    public static final String CORRECT = "correct";

    //MODE CONFIRM
    public static final String YES = "yes";
    public static final String NO = "no";

    //following constants are used as additional Info for TextUpdater

    public static final int NO_ADDITIONAL_INFO = 400;

    //MODE DICTATE NUMBER
    public static final int CALL = 401;
    public static final int TEXT = 402;

    //NUMBER TOO BIG
    public static final int TOO_BIG = 403;

    //MODE_COMMAND
    public static final int SAY_AGAIN = 404;

    //stopBackgroundWork
    public static final int CONTINUE_LISTENING = 0;
    public static final int STOP_LISTENING = 1;

    //Command Handler
    public static final int ERROR = 200;
    public static final int UNKNOWN_COMMAND = 201;
    public static final int EXECUTE_COMMAND = 202;
    public static final int SEARCH_INTERNET = 203;
    public static final int LISTEN_FOR_NUMBER = 204;
    public static final int SELECT_ACTIVITY = 205;
    public static final int LIST_DIALOG = 206;
    public static final int NUMBER_TOO_BIG = 207;
    public static final int SUCCESS = 208;

    //Listening Mode
    public static final int MODE_NOT_LISTENING = 100;
    public static final int MODE_COMMAND = 101;
    public static final int MODE_HANDLE_UNKNOWN_COMMAND = 102;
    public static final int MODE_REPEAT = 103;
    public static final int MODE_CORRECT = 104;
    public static final int MODE_SELECT = 105;
    public static final int MODE_QUERY = 106;
    public static final int MODE_CONFIRM = 107;
    public static final int MODE_DICTATE_NUMBER = 108;

    //Mode Extras
    public static final int NO_EXTRA = 0;
    public static final int EXTRA_SEARCH = SEARCH_INTERNET;
    public static final int EXTRA_SINGLE_ACTIVITY = EXECUTE_COMMAND;
    public static final int EXTRA_MULTIPLE_ACTIVITIES = SELECT_ACTIVITY;
    public static final int EXTRA_LISTEN_FOR_NUMBER = LISTEN_FOR_NUMBER;

    //TextUpdater
    public static final int MESSAGE = 0;
    public static final int INSTRUCTIONS = 1;
    public static final int ASR_STATUS = 2;
    public static final int PARTIAL_RESULT = 3;
    public static final int ERROR_VIEW = 4;

    //Suggestions
    public static final int NUMBER_OF_SUGGESTIONS = 3;

    //number of previous listening modes and commands to remember (currently only two ore three in use)
    public static final int STATES_TO_REMEMBER = 4;

    //Gestures
    public static final int GESTURE_SHAKING = 300;
    public static final int GESTURE_NODDING = 301;
    public static final int GESTURE_COVER = 302;

    //Interrupt TTS or ASR
    public static final int ASR = 600;
    public static final int TTS = 601;

    //Settings
    public static final String PREF_KEY_ASR_ACTIVE = "pref_key_asr_active";
    public static final String PREF_KEY_OPEN_DOMAIN = "pref_key_open_domain";

    public static final String PREF_KEY_TTS_ACTIVE = "pref_key_tts_active";
    public static final String PREF_KEY_SPEECH_RATE = "pref_key_speech_rate";
    public static final String PREF_KEY_PITCH = "pref_key_pitch";
    public static final String PREF_KEY_LANGUAGE = "pref_key_language";
    public static final String PREF_KEY_VOICE = "pref_key_voice";
    public static final String PREF_KEY_ENGINE = "pref_key_engine";
    public static final String PREF_KEY_SENSORS_ACTIVE = "pref_key_sensors_active";
    public static final String PREF_KEY_TOUCH_ACTIVE = "pref_key_touch_active";
    public static final String PREF_KEY_HAPTIC_ACTIVE = "pref_key_haptic_active";
}
