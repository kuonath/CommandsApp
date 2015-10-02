package com.example.kevin.commands;

import android.annotation.TargetApi;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kevin.commands.DialogListCommandsFragment.iOnCommandSelectedListener;
import com.example.kevin.commands.DialogFeaturesFragment.iOnDialogButtonClickListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

import static android.widget.Toast.makeText;
import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

//Main Activity
public class CommandsActivity extends AppCompatActivity implements RecognitionListener, TextToSpeech.OnInitListener, iOnCommandSelectedListener, iOnDialogButtonClickListener {

    private static final String TAG = CommandsActivity.class.getSimpleName();

    //constants for different searches
    private static final String COMMANDS = "commands";
    private static final String CORRECT = "correct";
    private static final String PHONENUMBER = "phonenumber";
    private static final String DIGITS_MIS = "digits_mis";
    private static final String QUERY = "query";
    private static final String CONFIRM = "confirm";

    //constants for onActivityResult
    private static final int MY_DATA_CHECK_CODE = 0;
    private static final int MY_REQUEST_CODE = 1;
    private static final int SETTINGS_CODE = 2;

    //TextViews
    private TextView mMessage;
    private TextView mInstructions;
    private TextView mASRStatus;
    private TextView mPartialResults;
    private TextView mError;

    //in case voice recognition is turned off
    private EditText mPhonenumberEdit;

    //UI Elements
    private ListView mList;
    private ImageButton mButtonListen;
    private Button mButtonShowCommands;
    private Button mButtonYes;
    private Button mButtonNo;

    //timers for gestures
    private Timer mActivationTimer;
    private Timer mGestureTimer;
    private Timer mCoverTimer;
    private Timer mInterruptTimer;
    private boolean mActivationTimerActive = false;
    private boolean mGestureTimerActive = false;
    private boolean mCoverTimerActive = false;
    private boolean mInterruptTimerActive = false;

    // for haptic feedback
    private Vibrator mV;

    //gestures
    private MotionTrigger mTrigger;
    private long mFirstMovementTime = 0;
    private boolean mFirstSignificantMotion = false;
    private ShakeGesture mShake;
    private NodGesture mNod;
    private CoverGesture mCover;
    private NodGesture mInterruptNod;

    //speech in- and output
    private SpeechRecognizer mRecognizer;
    private boolean mIsRecognizerSetup = false;
    private boolean mOpenDomainModelLoaded = false;
    private TextToSpeech mTextToSpeech;
    private boolean mIsTTSSetup = false;
    private HashMap<String, String> mTTSMap;
    private Bundle mIdParams;

    //task to initialize open-domain search
    private AsyncTask mOpenDomainTask;

    private CommandHandler mCommandHandler;
    private String mCommandToCorrect;

    private TextUpdater mTextUpdater;

    private DatabaseUpdateTask mDBUpdateTask;
    private StatisticsDBOpenHelper mDBHelper;

    //mapping between strings and numbers
    private HashMap<Integer, String> mModeMap;
    private HashMap<Integer, String> mNumberMap;

    //onResume
    private boolean mFirstCall = true;

    //help for mode transitions depending on the last few modes/commands
    private Mode[] mModeHistory = new Mode[Constants.STATES_TO_REMEMBER];
    private String[] mCommandHistory = new String[Constants.STATES_TO_REMEMBER];

    //features
    private Boolean mASRActivated = false;
    private Boolean mQueryActivated = false;
    private Boolean mTTSActivated = false;
    private Boolean mSensorsActivated = false;
    private Boolean mTouchActivated = false;
    private Boolean mHapticFeedbackActivated = false;

    private Boolean mCommandChosenByTouch = false;

    //needed to assure that a mixture of input modalities works
    private boolean mSkipOnResult = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commands);

        //Mapping between listening mode and mode of the recognizer
        mModeMap = new HashMap<>();
        mModeMap.put(Constants.MODE_COMMAND, COMMANDS);
        mModeMap.put(Constants.MODE_HANDLE_UNKNOWN_COMMAND, CORRECT);
        mModeMap.put(Constants.MODE_REPEAT, COMMANDS);
        mModeMap.put(Constants.MODE_CORRECT, DIGITS_MIS);
        mModeMap.put(Constants.MODE_SELECT, DIGITS_MIS);
        mModeMap.put(Constants.MODE_QUERY, QUERY);
        mModeMap.put(Constants.MODE_CONFIRM, CONFIRM);
        mModeMap.put(Constants.MODE_DICTATE_NUMBER, PHONENUMBER);

        mNumberMap = new HashMap<>();
        mNumberMap.put(1, "one");
        mNumberMap.put(2, "two");
        mNumberMap.put(3, "three");
        mNumberMap.put(4, "four");
        mNumberMap.put(5, "five");
        mNumberMap.put(6, "six");
        mNumberMap.put(7, "seven");
        mNumberMap.put(8, "eight");
        mNumberMap.put(9, "nine");

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        mMessage = (TextView) findViewById(R.id.message);
        mInstructions = (TextView) findViewById(R.id.instructions);
        mASRStatus = (TextView) findViewById(R.id.asr_status);
        mPartialResults = (TextView) findViewById(R.id.partial_results);
        mError = (TextView) findViewById(R.id.error);

        mPhonenumberEdit = (EditText) findViewById(R.id.phonenumber_edit);

        mTextUpdater = new TextUpdater(this.getApplicationContext(), new TextView[]{mMessage, mInstructions, mASRStatus, mPartialResults, mError});

        mTextUpdater.prepare();

        mButtonListen = (ImageButton) findViewById(R.id.button_listen);

        //color filter to create an effect of pressing the button; visual feedback
        mButtonListen.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        ImageButton view = (ImageButton) v;
                        view.setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                        v.invalidate();
                        break;
                    case MotionEvent.ACTION_UP:
                        view = (ImageButton) v;
                        view.clearColorFilter();
                        view.invalidate();
                        v.performClick();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        view = (ImageButton) v;
                        view.clearColorFilter();
                        view.invalidate();
                        break;
                }
                return true;
            }
        });

        //start listening on click; but only if recognizer is setup
        mButtonListen.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (mASRActivated && mIsRecognizerSetup) {
                    //haptic feedback
                    if(mHapticFeedbackActivated) {
                        mV.vibrate(500);
                    }
                    modeTransition(Constants.MODE_COMMAND, Constants.NO_EXTRA, "Button Listen");
                    mTextUpdater.updateText(mModeHistory[0].getMode(), Constants.NO_ADDITIONAL_INFO);
                    switchSearch(mModeMap.get(mModeHistory[0].getMode()));
                }
            }
        });

        mButtonShowCommands = (Button) findViewById(R.id.button_commands_list);

        mButtonShowCommands.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mTouchActivated) {
                    if (mTTSActivated && mIsTTSSetup && (mTextToSpeech != null)) {
                        mTextToSpeech.stop();
                    }
                    modeTransition(Constants.MODE_COMMAND, Constants.NO_EXTRA, "Button Show");
                    stateMachine(Constants.SHOW_COMMANDS);
                } else {
                    makeText(getApplicationContext(), getString(R.string.toast_feature_not_activated), Toast.LENGTH_SHORT).show();
                }
            }
        });

        mButtonYes = (Button) findViewById(R.id.button_confirm_yes);

        mButtonNo = (Button) findViewById(R.id.button_confirm_no);

        mButtonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mTouchActivated) {
                    mSkipOnResult = true;
                    stateMachine(Constants.YES);
                } else {
                    makeText(getApplicationContext(), getString(R.string.toast_feature_not_activated), Toast.LENGTH_SHORT).show();
                }
                /*mV.setVisibility(View.INVISIBLE);
                mButtonNo.setVisibility(View.INVISIBLE);*/
            }
        });

        mButtonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mTouchActivated) {
                    mSkipOnResult = true;
                    stateMachine(Constants.NO);
                } else {
                    makeText(getApplicationContext(), getString(R.string.toast_feature_not_activated), Toast.LENGTH_SHORT).show();
                }
                /*mV.setVisibility(View.INVISIBLE);
                mButtonYes.setVisibility(View.INVISIBLE);*/
            }
        });

        //List for app selection in case there is more than one app to carry out a command and for suggestions (language model for commands and unknown commands)
        mList = (ListView) findViewById(R.id.list);

        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i("View", "Click on the adapter. Current Mode: " + mModeHistory[0].getMode());
                mCommandHandler.handleSelection(mNumberMap.get(i + 1), mModeHistory[0].getMode());
            }
        });

        Log.i("view", "list is null: " + Boolean.toString(mList == null));

        mDBHelper = new StatisticsDBOpenHelper(this.getApplicationContext());
        mCommandHandler = new CommandHandler(CommandsActivity.this, this.getApplicationContext(), mList, mDBHelper);

        for(int i = 0; i < Constants.STATES_TO_REMEMBER; i++) {
            mModeHistory[i] = new Mode(Constants.MODE_NOT_LISTENING, Constants.NO_EXTRA);
            mCommandHistory[i] = "";
        }

        //dialog for first settings; shows some preferences that can be changed in the beginning
        DialogFragment featuresDialog = new DialogFeaturesFragment();
        featuresDialog.show(getFragmentManager(), "featuresDialog");
    }

    //implementation of Listener that listens for the button that was clicked on DialogFeaturesFragment
    @Override
    public void onFinishFeaturesDialog(String buttonClicked) {
        if (buttonClicked.equals(getString(R.string.button_close))) {
            finish();
        } else if (buttonClicked.equals(getString(R.string.button_ok))) {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

            if(sharedPrefs.getBoolean(Constants.PREF_KEY_ASR_ACTIVE, false)) {

                Log.i("Settings", "use ASR");
                mASRActivated = true;

                if(sharedPrefs.getBoolean(Constants.PREF_KEY_OPEN_DOMAIN, false)) {

                    Log.i("Settings", "use open-domain");
                    mQueryActivated = true;
                    startRecognizerInit(true);
                } else {
                    startRecognizerInit(false);
                }
            }

            if(sharedPrefs.getBoolean(Constants.PREF_KEY_TTS_ACTIVE, false)) {

                Log.i("Settings", "use TTS");
                mTTSActivated = true;

                //Intent to set up Text-To-Speech
                Log.i("TTS", "start intent for tts");
                Intent checkTTSIntent = new Intent();
                checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
                startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);

            }

            if(sharedPrefs.getBoolean(Constants.PREF_KEY_SENSORS_ACTIVE, false)) {

                Log.i("Settings", "use sensors");
                mSensorsActivated = true;

                //gestures
                mTrigger = new MotionTrigger(this.getApplicationContext());
                mShake = new ShakeGesture(this.getApplicationContext());
                mNod = new NodGesture(this.getApplicationContext());
                mCover = new CoverGesture(this.getApplicationContext());

            }

            if(sharedPrefs.getBoolean(Constants.PREF_KEY_TOUCH_ACTIVE, false)) {

                Log.i("Settings", "use touch");
                mTouchActivated = true;
            }

            if(sharedPrefs.getBoolean(Constants.PREF_KEY_TOUCH_ACTIVE, false)) {

                Log.i("Settings", "use haptics");
                mHapticFeedbackActivated = true;

                mV = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
            }
        }
    }

    private void addOpenDomainSearch() {

        mOpenDomainTask = new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(CommandsActivity.this);
                    File assetsDir = assets.syncAssets();
                    File languageModelQuery = new File(assetsDir, "cmusphinx-5.0-en-us.lm.dmp");
                    mRecognizer.addNgramSearch(QUERY, languageModelQuery);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    mTextUpdater.initFailed(result);
                } else{
                    mOpenDomainModelLoaded = true;
                    Toast.makeText(getApplicationContext(), getString(R.string.toast_open_domain_ready), Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    private void startRecognizerInit(Boolean useOpenDomain) {
        // Recognizer initialization is time-consuming and it involves IO,
        // so we execute it in an async task

        final Boolean openDomain = useOpenDomain;

        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(CommandsActivity.this);
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir, openDomain);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    mTextUpdater.initFailed(result);
                } else{
                    Toast.makeText(getApplicationContext(), getString(R.string.toast_ready), Toast.LENGTH_LONG).show();
                    waitForInput();
                }
            }
        }.execute();
    }

    //OptionsMenu, currently only contains settings
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_commands, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.settings:
                Intent settingsActivity = new Intent(this, SettingsActivity.class);

                String[] languagesListString;
                String[] voicesListString;
                String[] enginesListString;

                if(mTTSActivated) {
                    //create list of languages and voices and engines available on the phone; different
                    // in different phones and can change on a phone when the user downloads a new language/engine
                    Set<Locale> languagesLoc = mTextToSpeech.getAvailableLanguages();
                    List<String> languagesList = new ArrayList<>();

                    Set<Voice> voices = mTextToSpeech.getVoices();
                    List<String> voicesList = new ArrayList<>();

                    List<TextToSpeech.EngineInfo> engines = mTextToSpeech.getEngines();
                    List<String> enginesList = new ArrayList<>();

                    for (Locale loc : languagesLoc) {
                        //somehow there are some empty languages
                        if (!loc.getCountry().equals("")) {
                            languagesList.add(loc.getCountry());
                        }
                    }

                    //convert from list to string array
                    Object[] languagesListObject = languagesList.toArray();
                    languagesListString = Arrays.copyOf(languagesListObject, languagesListObject.length, String[].class);

                    //add all voices
                    // TODO: 09.09.2015 only add voices that correspond to the selected language; already tried an implementation checking if the voice contains the short name of a language with two letters (US, GB, DE, ...), but that was too slow
                    for (Voice v : voices) {
                        voicesList.add(v.getName());
                    }

                    //convert from list to string array
                    Object[] voicesListObject = voicesList.toArray();
                    voicesListString = Arrays.copyOf(voicesListObject, voicesListObject.length, String[].class);

                    for (TextToSpeech.EngineInfo engine : engines) {
                        enginesList.add(engine.name);
                    }

                    //convert from list to string array
                    Object[] enginesListObject = enginesList.toArray();
                    enginesListString = Arrays.copyOf(enginesListObject, enginesListObject.length, String[].class);
                } else {
                    languagesListString = new String[]{"activate TTS first"};
                    voicesListString = new String[]{"activate TTS first"};
                    enginesListString = new String[]{"activate TTS first"};
                }

                //add the arrays to the intent as extras and start the settings activity
                settingsActivity.putExtra("languageEntries", languagesListString);
                settingsActivity.putExtra("voicesEntries", voicesListString);
                settingsActivity.putExtra("enginesEntries", enginesListString);
                startActivityForResult(settingsActivity, SETTINGS_CODE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //used to receive results from other activities; in this cas from the intent that initializes
    // the TTS engine and from the settings activity
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                //TTS OK
                mTextToSpeech = new TextToSpeech(this, this);
                Log.i("TTS", "Activity Result");
            }
            else {
                //no TTS data available -> install data
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        } else if(requestCode == SETTINGS_CODE) {

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

            if(mTTSActivated && mIsTTSSetup && (mTextToSpeech != null)) {
                setTextToSpeechParameters(prefs);
            }

            //this if-statement has to stay here after the previous statement!!! Don't change the order!!!
            if(prefs.getBoolean(Constants.PREF_KEY_TTS_ACTIVE, false)) {

                if(mTTSActivated == false) {
                    //mTTSActivated = true; will be set in onInit()

                    //Intent to set up Text-To-Speech
                    Log.i("TTS", "start intent for tts");
                    Intent checkTTSIntent = new Intent();
                    checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
                    startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
                }

            } else {
                mTTSActivated = false;
            }

            if(prefs.getBoolean(Constants.PREF_KEY_OPEN_DOMAIN, false) && !mQueryActivated) {

                mQueryActivated = true;

                if(mASRActivated && !mOpenDomainModelLoaded) {
                    addOpenDomainSearch();
                }
            } else {
                mQueryActivated = false;
            }

            if(prefs.getBoolean(Constants.PREF_KEY_ASR_ACTIVE, false)) {
                if (!mASRActivated) {
                    mASRActivated = true;
                    startRecognizerInit(mQueryActivated);
                }
            } else {
                mASRActivated = false;
            }

            if(prefs.getBoolean(Constants.PREF_KEY_SENSORS_ACTIVE, false)) {
                mSensorsActivated = true;
            } else {
                mSensorsActivated = false;
            }

            if(prefs.getBoolean(Constants.PREF_KEY_TOUCH_ACTIVE, false)) {
                mTouchActivated = true;
            } else {
                mTouchActivated = false;
            }

            if(prefs.getBoolean(Constants.PREF_KEY_HAPTIC_ACTIVE, false)) {
                if(!mHapticFeedbackActivated) {
                    mHapticFeedbackActivated = true;
                    mV = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
                }
            } else {
                mHapticFeedbackActivated = false;
            }
        }
    }

    //on TTS initalized
    @Override
    public void onInit(int status) {
        Log.i("TTS", "onInit()");
        if (status == TextToSpeech.SUCCESS) {

            mIsTTSSetup = true;
            mTTSActivated = true;

            makeText(this, getString(R.string.toast_tts_init), Toast.LENGTH_LONG);

            mTTSMap = new HashMap<>();
            mTTSMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");

            mIdParams = new Bundle();
            mIdParams.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            setTextToSpeechParameters(prefs);

            Log.i("TTS", "setOnUtteranceProgressListener");
            //recognizer should not listen while TTS is speaking -> set this listener
            //// TODO: 09.09.2015 somehow this does not get called when for example the utterance is done
            mTextToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(final String utteranceId) {
                    Log.i("TTS", "start speaking");
                    //if recognizer is listening -> stop listening
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            //startInterruptTimer(Constants.TTS);

                            //you can't click the button when the voice started speaking
                            mButtonListen.setClickable(false);
                            mButtonListen.setEnabled(false);

                            mButtonShowCommands.setClickable(false);
                            mButtonShowCommands.setEnabled(false);

                            mButtonYes.setClickable(false);
                            mButtonYes.setEnabled(false);

                            mButtonNo.setClickable(false);
                            mButtonNo.setEnabled(false);

                            if (mASRActivated && (mRecognizer != null) && mIsRecognizerSetup) {
                                mRecognizer.stop();
                                mASRStatus.setText(getString(R.string.speak));
                            }
                        }
                    });
                }

                @Override
                public void onDone(String utteranceId) {
                    Log.i("TTS", "Done");

                    //somehow only works when explicitly running on UI Thread, although this is not
                    // the case for the text update in onStart(), but still used for onStart() as well
                    // maybe because onInit is called from the Intent that is started to initialize
                    // the TTS engine

                    onSpeechEnd();
                }

                @Override
                public void onError(String utteranceId) {
                    Toast.makeText(getApplicationContext(), getString(R.string.toast_err_speaking), Toast.LENGTH_LONG).show();
                }
            });

        } else if (status == TextToSpeech.ERROR) {
            Toast.makeText(this, getString(R.string.toast_tts_failed), Toast.LENGTH_LONG).show();
        }
    }

    private void onSpeechEnd() {

        if(mInterruptTimerActive) {
            //stopInterruptTimer();
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if ((mModeHistory[0].getMode() == Constants.MODE_NOT_LISTENING) || (mModeHistory[0].getMode() == Constants.MODE_COMMAND)) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mButtonShowCommands.setClickable(true);
                            mButtonShowCommands.setEnabled(true);
                        }
                    });
                }

                if (mModeHistory[0].getMode() == Constants.MODE_CONFIRM) {
                    mButtonYes.setClickable(true);
                    mButtonYes.setEnabled(true);

                    mButtonNo.setClickable(true);
                    mButtonNo.setEnabled(true);
                }

                if (mModeHistory[0].getMode() != Constants.MODE_NOT_LISTENING) {
                    //when done with speaking -> listen
                    switchSearch(mModeMap.get(mModeHistory[0].getMode()));
                    startGestureTimer(mModeHistory[0].getMode());
                } else {
                    mASRStatus.setText(getString(R.string.empty));
                    if (mTouchActivated) {
                        mButtonListen.setClickable(true);
                        mButtonListen.setEnabled(true);
                    }
                }
            }
        });
    }

    //called after initialization and after settings activity is closed to update settings
    private void setTextToSpeechParameters(SharedPreferences prefs) {
        mTextToSpeech.setSpeechRate(((float) prefs.getInt(Constants.PREF_KEY_SPEECH_RATE, 10) / 10));
        mTextToSpeech.setPitch(((float) prefs.getInt(Constants.PREF_KEY_PITCH, 10) / 10));

        String language = prefs.getString(Constants.PREF_KEY_LANGUAGE, "US").toUpperCase();
        mTextToSpeech.setLanguage(Locale.forLanguageTag(language));

        //mTextToSpeech.setLanguage(Locale.forLanguageTag(prefs.getString("pref_key_language", "US")));
        Set<Voice> voicesList = mTextToSpeech.getVoices();

        for(Voice v:voicesList) {
            if(v.getName().equals(prefs.getString(Constants.PREF_KEY_VOICE, "en_US-locale"))) {
                mTextToSpeech.setVoice(v);
                Log.i("TTS", "voice");
                break;
            }
        }
    }

    @TargetApi(21)
    public void speakSelection(String selection) {
        if(mTTSActivated && mIsTTSSetup && (mTextToSpeech != null)) {
            if (android.os.Build.VERSION.SDK_INT < 19) {
                mTextToSpeech.speak(selection, TextToSpeech.QUEUE_FLUSH, mTTSMap);
            } else {
                mTextToSpeech.speak(selection, TextToSpeech.QUEUE_FLUSH, mIdParams, "UniqueID");
            }
        }
    }

    //what happens when you click the back button? standard functionality overridden
    @Override
    public void onBackPressed() {

        mPhonenumberEdit.setText(getString(R.string.empty));
        mPhonenumberEdit.setVisibility(View.INVISIBLE);

        mButtonYes.setVisibility(View.INVISIBLE);
        mButtonNo.setVisibility(View.INVISIBLE);

        //if you are not listening -> close app; otherwise stop listening
        if(mModeHistory[0].getMode() != Constants.MODE_NOT_LISTENING) {
            if(mASRActivated && (mRecognizer != null) && mIsRecognizerSetup) {
                mRecognizer.stop();
            }

            modeTransition(Constants.MODE_NOT_LISTENING, Constants.NO_EXTRA, "Button Back");
            mTextUpdater.updateText(mModeHistory[0].getMode(), Constants.NO_ADDITIONAL_INFO);
            speakSelection(mInstructions.getText().toString());

            if(mGestureTimerActive) {
                mGestureTimer.cancel();
                mGestureTimerActive = false;
            }

            waitForInput();
        }
        else {
            finish();
        }
    }

    public void modeTransition(int nextMode, int nextExtra, String whereFrom) {

        Log.i("Mode", "Mode transition from " + mModeHistory[0].getMode() + " to " + nextMode + " in function " + whereFrom);

        for(int i = (Constants.STATES_TO_REMEMBER - 1); i > 0; i--) {
            mModeHistory[i].setMode(mModeHistory[i - 1].getMode());
            mModeHistory[i].setExtra(mModeHistory[i-1].getExtra());
        }
        mModeHistory[0].setMode(nextMode);
        mModeHistory[0].setExtra(nextExtra);
    }

    public void commandTransition(String currentCommand) {

        for(int i = (Constants.STATES_TO_REMEMBER - 1); i > 0; i--) {
            mCommandHistory[i] = mCommandHistory[i-1];
        }
        mCommandHistory[0] = currentCommand;
    }

    //called when the app waits for any input (click on ButtonListen/ButtonShowCommands or gesture activation of ASR through movement)
    public void waitForInput() {

        if(mASRActivated) {
            mButtonListen.setEnabled(true);
            mButtonListen.setClickable(true);
        }

        mList.setAdapter(null);

        if((mModeHistory[1].getMode() == Constants.MODE_COMMAND) || (mCommandHistory[0].equals(Constants.CANCEL))) {// || (mModeHistory[1] == Constants.MODE_CONFIRM)) {
            mTextUpdater.updateText(Constants.MODE_NOT_LISTENING, Constants.NO_ADDITIONAL_INFO);
            speakSelection(mInstructions.getText().toString());
        }

        if(mASRActivated && mSensorsActivated && !mActivationTimerActive && mIsRecognizerSetup) {
            startActivationTimer();
        }

        modeTransition(Constants.MODE_NOT_LISTENING, Constants.NO_EXTRA, "wait for input");
    }

    //// TODO: 09.09.2015 Think about also using a smaller dictionary when useOpenDomain is false; this way it would be possible to use language models instead of grammars (but that's not necessary)
    private void setupRecognizer(File assetsDir, Boolean useOpenDomain) throws IOException {
        // The mRecognizer can be configured to perform multiple searches
        // of different kind and switch between them

        mRecognizer = defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))

                        // To disable logging of raw audio comment out this call (takes a lot of space on the device)
                //.setRawLogDir(assetsDir)

                        // Threshold to tune for keyphrase to balance between false alarms and misses
                .setKeywordThreshold(1e-45f)

                        // Use context-independent phonetic search, context-dependent is too slow for mobile
                .setBoolean("-allphone_ci", true)

                //.setFloat("-vad_threshold", 3.0) //can be used to set the threshold for voice activity detection

                .getRecognizer();
        mRecognizer.addListener(this);

        File grammarPhonenumber = new File(assetsDir, "phonenumber.gram");
        mRecognizer.addGrammarSearch(PHONENUMBER, grammarPhonenumber);

        File grammarDigitsMis = new File(assetsDir, "numbers_mis.gram");
        mRecognizer.addGrammarSearch(DIGITS_MIS, grammarDigitsMis);

        File grammarCorrect = new File(assetsDir, "correct.gram");
        mRecognizer.addGrammarSearch(CORRECT, grammarCorrect);

        File grammarConfirm = new File(assetsDir, "confirm.gram");
        mRecognizer.addGrammarSearch(CONFIRM, grammarConfirm);

        File grammarCommands = new File(assetsDir, "commands.gram");
        mRecognizer.addGrammarSearch(COMMANDS, grammarCommands);

        if(useOpenDomain) {
            addOpenDomainSearch();
        }

        //views (also TextViews) have to be updated on the UI thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTextUpdater.updateText(mModeHistory[0].getMode(), Constants.NO_ADDITIONAL_INFO);
                speakSelection(mInstructions.getText().toString());
            }
        });

        mIsRecognizerSetup = true;
    }

    @Override
    public void onBeginningOfSpeech() {

    }

    //called when app is closed; cancel Timers, shutdown recognizer and TTS
    @Override
    public void onDestroy() {
        super.onDestroy();

        stopBackgroundWork(Constants.STOP_LISTENING);

        if(mGestureTimerActive) {
            stopGestureTimer();
        }

        if(mActivationTimer != null) {
            stopActivationTimer();
        }

        if(mInterruptTimerActive) {
            stopInterruptTimer();
        }

        if(mRecognizer != null) {
            mRecognizer.cancel();
            mRecognizer.shutdown();
            mIsRecognizerSetup = false;
        }

        if(mTextToSpeech != null) {
            mTextToSpeech.shutdown();
        }

        if(mOpenDomainTask != null) {
            mOpenDomainTask.cancel(true);
        }
    }

    //called when activity is in background (dialog is opened/another activity or app is started)
    @Override
    public void onPause() {
        super.onPause();

        if(mASRActivated && (mRecognizer != null) && !mIsRecognizerSetup) {
            mRecognizer.shutdown();
        }
        stopBackgroundWork(Constants.STOP_LISTENING);

    }

    //called when activity is in foreground again (after closing other activity/app/dialog) and also after onCreate when the app is first started
    @Override
    public void onResume() {
        super.onResume();

        //everything set to beginning
        if(mASRActivated && mIsRecognizerSetup) {
            waitForInput();
        } else {
            //does not work properly
            if(!mFirstCall) {
                //mTextUpdater.recognizerNotSetup();
            }
        }

        mPartialResults.setText(R.string.empty);

        if (mIsRecognizerSetup) {
            mModeHistory[0].setMode(Constants.MODE_NOT_LISTENING);
            mModeHistory[0].setExtra(Constants.NO_EXTRA);
            mTextUpdater.updateText(mModeHistory[0].getMode(), Constants.NO_ADDITIONAL_INFO);
            speakSelection(mInstructions.getText().toString());
        }

        mList.setAdapter(null);

        mFirstCall = false;

        mButtonNo.setVisibility(View.INVISIBLE);
        mButtonYes.setVisibility(View.INVISIBLE);
    }

    //used to listen for the MotionTrigger through method checkForMotion() at intervals of 50ms
    public void startActivationTimer() {

        if(mSensorsActivated) {

            if(mTrigger != null) {
                //mTrigger = new MotionTrigger(this);
                mTrigger.registerListener();
            }

            mActivationTimer = new Timer();

            Log.i("Timer", "Activation Timer started");

            mActivationTimerActive = true;
            mFirstMovementTime = 0;

            mActivationTimer.scheduleAtFixedRate(new TimerTask() {

                @Override
                public void run() {
                    if(mTrigger != null) {
                        checkForMotion();
                    }
                }
            }, 0, 50);
        } else {
            Log.i("Timer", "Did not start timer. Sensors are not activated in Settings");
        }
    }

    public void stopActivationTimer() {

        if(mSensorsActivated) {

            mActivationTimer.cancel();
            mActivationTimerActive = false;

            if(mTrigger != null) {
                mTrigger.unregisterListener();
            }

            mFirstMovementTime = 0;
        }
    }

    //used to listen for a cover gesture at intervals of 50ms (e.g. move hand close to proximity sensor or light sensor on phone) and cancel current operation if detected
    public void startCoverTimer() {

        if(mSensorsActivated) {
            mCover = new CoverGesture(this.getApplicationContext());
            mCover.registerListener();

            mCoverTimer = new Timer();

            Log.i("Timer", "Cancel Timer started");

            mCoverTimerActive = true;

            mCoverTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (mCover.gestureCompleted()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.i("Timer", "cancel listening");
                                stateMachine(Constants.CANCEL);
                            }
                        });
                    }
                }
            }, 0, 50);
        } else {
            Log.i("Timer", "Did not start timer. Sensors are not activated in Settings");
        }
    }

    public void stopCoverTimer() {

        mCoverTimer.cancel();

        if(mCover != null) {
            mCover.unregisterListener();
            mCover = null;
        }

        mCoverTimerActive = false;
    }

    public void startInterruptTimer(final int ASRorTTS) {

        if (mSensorsActivated) {
            mInterruptNod = new NodGesture(this.getApplicationContext());
            mInterruptTimer = new Timer();

            Log.i("Timer", "Interrupt Timer started");

            mInterruptTimerActive = true;

            mInterruptNod.registerListener();

            mInterruptTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if ((mInterruptNod != null) && (mInterruptNod.gestureCompleted())) {
                        Log.i("Timer", "InterruptNod");

                        if(ASRorTTS == Constants.ASR) {
                            mRecognizer.stop();
                        } else if (ASRorTTS == Constants.TTS){
                            mTextToSpeech.stop();
                            onSpeechEnd();
                        }
                    }
                }
            }, 0, 50);
        }
    }

    public void stopInterruptTimer() {

        mInterruptTimer.cancel();

        if(mInterruptNod != null) {
            mInterruptNod.unregisterListener();
            mInterruptNod = null;
        }

        mInterruptTimerActive = false;
    }

    //listen for nodding or shaking gestures at intervals of 50ms
    //// TODO: 09.09.2015 add gesture for commands 'repeat' and 'correct'
    public void startGestureTimer(int listeningMode) {

        if(mSensorsActivated) {
            mShake = new ShakeGesture(this.getApplicationContext());
            mNod = new NodGesture(this.getApplicationContext());
            mGestureTimer = new Timer();

            Log.i("Timer", "Gesture Timer started");

            mGestureTimerActive = true;

            switch (listeningMode) {
                case Constants.MODE_HANDLE_UNKNOWN_COMMAND:
                        //in case this mode will be used at some time, there could also be a timer to listen for gestures
                    break;
                case Constants.MODE_CONFIRM:
                    mShake.registerListener();
                    mNod.registerListener();

                    mGestureTimer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            if ((mShake != null) && mShake.gestureCompleted()) {
                                mSkipOnResult = true;
                                Log.i("Timer", "Shake");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        stateMachine(Constants.NO);
                                    }
                                });
                            } else if ((mNod != null) && mNod.gestureCompleted()) {
                                mSkipOnResult = true;
                                Log.i("Timer", "Nod");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        stateMachine(Constants.YES);
                                    }
                                });
                            }
                        }
                    }, 0, 50);
                    break;
            }
        } else {
            Log.i("Timer", "Did not start timer. Sensors are not activated in Settings");
        }
    }

    public void stopGestureTimer() {

        mGestureTimer.cancel();

        if(mNod != null) {
            mNod.unregisterListener();
            mNod = null;
        }

        if(mShake != null) {
            mShake.unregisterListener();
            mShake = null;
        }

        mGestureTimerActive = false;
    }


    public void stopBackgroundWork(int stopListening) {

        if(mActivationTimerActive) {
            stopActivationTimer();
        }

        if(mGestureTimerActive) {
            stopGestureTimer();
        }

        if(mCoverTimerActive) {
           stopCoverTimer();
        }

        if(mInterruptTimerActive) {
            stopInterruptTimer();
        }

        if(stopListening == 1) {
            modeTransition(Constants.MODE_NOT_LISTENING, Constants.NO_EXTRA, "stopBackGroundWork");
        }

        if(mASRActivated && (mRecognizer != null) && mIsRecognizerSetup) {
            mRecognizer.stop();
        }

        if(mTTSActivated && (mIsTTSSetup) && (mTextToSpeech!= null) && (mTextToSpeech.isSpeaking())) {
            mTextToSpeech.stop();
        }
    }

    //listens for movement of the phone in the opposite direction of gravity (lift the phone and stop; move the phone close to your mouth)
    public void checkForMotion() {

        if(mTrigger.firstSignificantMotion()) {
            mFirstSignificantMotion = true;
            mFirstMovementTime = System.currentTimeMillis();
            //Log.i(TAG, "First: " + Long.toString(System.currentTimeMillis()) + ", " + "Dist: " + mTrigger.getDistance());

        }
        //too slow
        else if((System.currentTimeMillis() - mFirstMovementTime > 400)) {
            mFirstMovementTime = 0;
            mFirstSignificantMotion = false;
        }

        //// TODO: 09.09.2015 distance 50cm; not accurate; test on different phones
        //gesture complete -> start listening
        if(mFirstSignificantMotion && mTrigger.secondSignificantMotion() && mTrigger.getDistance() > 0.5) {

            //haptic feedback
            if(mHapticFeedbackActivated) {
                mV.vibrate(500);
            }

            stopActivationTimer();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    modeTransition(Constants.MODE_COMMAND, Constants.NO_EXTRA, "checkForMotion");
                    mTextUpdater.updateText(mModeHistory[0].getMode(), Constants.NO_ADDITIONAL_INFO);
                    switchSearch(mModeMap.get(mModeHistory[0].getMode()));

                    mFirstSignificantMotion = false;
                    mFirstMovementTime = 0;
                }
            });
        }

        if(System.currentTimeMillis() - mFirstMovementTime > 400 && mTrigger.noSignificantMotion()) {
            mFirstMovementTime = 0;
        }
    }

    //user does not speak anymore -> stop to listen
    @Override
    public void onEndOfSpeech() {
        if(mASRActivated && mRecognizer != null) {
            mRecognizer.stop();
        }
    }

    //show partial results on TextView
    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;

        String command = hypothesis.getHypstr().toLowerCase();

        mPartialResults.setText(command);
    }

    //if there is an hypothesis, get the string and pass it as an argument to the "stateMachine"
    @Override
    public void onResult(Hypothesis hypothesis) {

        if(mInterruptTimerActive) {
           stopInterruptTimer();
        }

        //used for gesture input for confirmation; after gesture input the recognizer stops and has a
        //hypothesis; without this if-statement stateMachine() would be called twice which does not
        //lead to the desired behaviour, because the part where the user can repeat what he wanted to
        //say after saying 'no' is skipped -> therefore skip onResult and the second call of stateMachine
        //in the same state/mode
        if (mSkipOnResult) {
            mSkipOnResult = false;
            return;
        }

        Log.i(TAG, "on Result() previous mode: " + Integer.toString(mModeHistory[1].getMode()));
        if (hypothesis != null) {
            final String currentCommand = hypothesis.getHypstr().toLowerCase();
            Log.i("Command", currentCommand);

            stateMachine(currentCommand);
        } else {
            //in mode command or when you said 'repeat' after an unknown command keep listening
            // although the recognizer didn't understand anything until the recognizer understands something
            if((mModeHistory[0].getMode() != Constants.MODE_COMMAND) || ((mModeHistory[0].getMode() == Constants.MODE_COMMAND) && (mModeHistory[1].getMode() == Constants.MODE_HANDLE_UNKNOWN_COMMAND))) {
                if(mModeMap.containsKey(mModeHistory[0].getMode())) {
                    makeText(this.getApplicationContext(), getString(R.string.toast_not_understood_continue), Toast.LENGTH_SHORT).show();
                    switchSearch(mModeMap.get(mModeHistory[0].getMode()));
                }
            } else if(mModeHistory[0].getMode() == Constants.MODE_COMMAND) {  //in mode command quit listening and wait for any input to activate the ASR/click on the Button
                makeText(this.getApplicationContext(), getString(R.string.toast_not_understood_stop), Toast.LENGTH_SHORT).show();
                stopBackgroundWork(Constants.STOP_LISTENING);
                waitForInput();
            } else{
                Log.i("Strange", "You shouldn't be here.");
            }
        }
    }

    //called when a command is selected on the list of commands that gets shown when the user clicks on the button 'Show commands'
    @Override
    public void onCommandSelected(String command) {
        mCommandChosenByTouch = true;
        stateMachine(command);
    }

    //handles commands and the transition from one state to another
    //// TODO: 09.09.2015 got quite long and complicated -> maybe split into different methods or think of another structure keeping the case statements
    public void stateMachine(String currentCommand) {

        Log.i("Mode", "Mode0: " + mModeHistory[0].getMode() + " Mode1: " + mModeHistory[1].getMode() + " Mode2: " + mModeHistory[2].getMode() + " Mode3: " + mModeHistory[3].getMode());
        Log.i("Mode", "command " + currentCommand + " in Mode: " + mModeHistory[0].getMode());

        if (currentCommand.isEmpty()) {

        }
        else {  //command not empty

            //update list of last commands
            commandTransition(currentCommand);

            mPartialResults.setText(mCommandHistory[0]);

            if (mCommandHistory[0].equals(Constants.CANCEL)) {
                Log.i("Command", "you said cancel");
                mButtonYes.setVisibility(View.INVISIBLE);
                mButtonNo.setVisibility(View.INVISIBLE);
                stopBackgroundWork(Constants.STOP_LISTENING);
                waitForInput();
            } else {
                switch (mModeHistory[0].getMode()) {
                    //Mode Command means you can say any command that is listed in the Constants class
                    case Constants.MODE_COMMAND:

                        //there are certain types of commands:
                        // 1.unknown 2. queries 3. commands that are followed by a dictation of numbers 4. commands that can simply be executed 5. commands that can be executed by more than one app 6. show commands
                        // extra as a result of handleCommand -> determines next step
                        mModeHistory[0].setExtra(mCommandHandler.handleCommand(mCommandHistory[0]));

                        switch (mModeHistory[0].getExtra()) {
                            case Constants.ERROR:
                                modeTransition(Constants.MODE_NOT_LISTENING, Constants.NO_EXTRA, "stateMachine");
                                makeText(this.getApplicationContext(), R.string.toast_command_error, Toast.LENGTH_LONG).show();
                                stopBackgroundWork(Constants.STOP_LISTENING);
                                waitForInput();
                                break;
                            case Constants.UNKNOWN_COMMAND:
                                //can only occur when a language model with uni-, bi- and trigrams is used for recognition of commands, currently a defined grammar is used that can only recognize defined commands
                                mCommandToCorrect = mCommandHistory[0];
                                modeTransition(Constants.MODE_HANDLE_UNKNOWN_COMMAND, Constants.NO_EXTRA, "stateMachine");
                                mTextUpdater.updateText(mModeHistory[0].getMode(), Constants.NO_ADDITIONAL_INFO);
                                speakSelection(mInstructions.getText().toString());

                                if(!mTTSActivated || !mIsTTSSetup) {
                                    switchSearch(mModeMap.get(mModeHistory[0].getMode()));
                                }
                                break;
                            case Constants.SEARCH_INTERNET:

                                if (mQueryActivated && mOpenDomainModelLoaded) {
                                    modeTransition(Constants.MODE_CONFIRM, Constants.NO_EXTRA, "stateMachine");
                                    mTextUpdater.confirm(mCommandHistory[0]);
                                    speakSelection(mInstructions.getText().toString());

                                    if(!mTTSActivated || !mIsTTSSetup) {
                                        switchSearch(mModeMap.get(mModeHistory[0].getMode()));
                                        startGestureTimer(mModeHistory[0].getMode());
                                    }
                                } else {
                                    makeText(this, getString(R.string.toast_feature_not_activated), Toast.LENGTH_LONG);
                                    modeTransition(Constants.MODE_NOT_LISTENING, Constants.NO_EXTRA, "stateMachine");
                                    mTextUpdater.updateText(mModeHistory[0].getMode(), Constants.NO_EXTRA);
                                    stopBackgroundWork(Constants.STOP_LISTENING);
                                    waitForInput();
                                }
                                break;
                            case Constants.LISTEN_FOR_NUMBER:

                                //both commands 'send text message' and 'call someone' get treated the same;
                                // later the command history will be used to determine whether to call the number or whether to send a text message
                                if(mASRActivated && mIsRecognizerSetup) {
                                    mTextUpdater.confirm(mCommandHistory[0]);
                                    speakSelection(mInstructions.getText().toString());
                                    modeTransition(Constants.MODE_CONFIRM, Constants.NO_EXTRA, "stateMachine");

                                    if(!mTTSActivated || !mIsTTSSetup) {
                                        switchSearch(mModeMap.get(mModeHistory[0].getMode()));
                                        startGestureTimer(mModeHistory[0].getMode());
                                    }
                                }
                                break;
                            case Constants.EXECUTE_COMMAND:
                                //do not simply execute the command, first wait for user confirmation
                                mTextUpdater.confirm(mCommandHistory[0]);
                                modeTransition(Constants.MODE_CONFIRM, Constants.NO_EXTRA, "stateMachine");
                                speakSelection(mInstructions.getText().toString());

                                if(!mTTSActivated || !mIsTTSSetup) {
                                    switchSearch(mModeMap.get(mModeHistory[0].getMode()));
                                    startGestureTimer(mModeHistory[0].getMode());
                                }

                                break;
                            case Constants.SELECT_ACTIVITY:
                                //first confirm and then select an app
                                mTextUpdater.confirm(mCommandHistory[0]);
                                speakSelection(mInstructions.getText().toString());
                                modeTransition(Constants.MODE_CONFIRM, Constants.NO_EXTRA, "stateMachine");

                                if(!mTTSActivated || !mIsTTSSetup) {
                                    switchSearch(mModeMap.get(mModeHistory[0].getMode()));
                                    startGestureTimer(mModeHistory[0].getMode());
                                }

                                break;
                            case Constants.LIST_DIALOG:
                                DialogFragment listCommandsDialog = new DialogListCommandsFragment();
                                listCommandsDialog.show(getFragmentManager(), "listCommandsDialogFragment");
                                break;
                        }
                        break;
                    case Constants.MODE_HANDLE_UNKNOWN_COMMAND:
                        //user said either 'correct' (suggestions are displayed) or 'repeat' (user can repeat what he actually wanted to say
                        modeTransition(mCommandHandler.handleWrongCommand(mCommandHistory[0], mCommandToCorrect), Constants.NO_EXTRA, "stateMachine");
                        Log.i("Command", "unknown currentCommand previous mode: " + Integer.toString(mModeHistory[1].getMode()));
                        mTextUpdater.updateText(mModeHistory[0].getMode(), Constants.NO_ADDITIONAL_INFO);
                        speakSelection(mInstructions.getText().toString());

                        if(!mTTSActivated || !mIsTTSSetup) {
                            switchSearch(mModeMap.get(mModeHistory[0].getMode()));
                        }

                        break;
                    case Constants.MODE_CORRECT:
                        //a list of suggestions is displayed and the user selected one of the suggestions by saying a number

                        //determines next step; if the next step is to simply execute the command, this action will be taken by the command handler
                        int selectionResultCorrect = mCommandHandler.handleSelection(mCommandHistory[0], mModeHistory[0].getMode());

                        if (selectionResultCorrect == Constants.NUMBER_TOO_BIG) {                       //number does not exist -> user can repeat the number
                            mTextUpdater.updateText(mModeHistory[0].getMode(), Constants.TOO_BIG);
                            speakSelection(mInstructions.getText().toString());

                            if(!mTTSActivated || !mIsTTSSetup) {
                                switchSearch(mModeMap.get(mModeHistory[0].getMode()));
                            }

                        } else if (selectionResultCorrect == Constants.MODE_COMMAND) {                  //the correct command is missing in the suggestions -> listen again for the correct command
                            modeTransition(Constants.MODE_COMMAND, Constants.NO_EXTRA, "stateMachine");
                            mTextUpdater.updateText(mModeHistory[0].getMode(), Constants.NO_ADDITIONAL_INFO);
                            speakSelection(mInstructions.getText().toString());

                            if(!mTTSActivated || !mIsTTSSetup) {
                                switchSearch(mModeMap.get(mModeHistory[0].getMode()));
                            }

                        } else if (selectionResultCorrect == Constants.ERROR) {
                            waitForInput();
                            makeText(this.getApplicationContext(), getString(R.string.toast_dont_know), Toast.LENGTH_LONG);
                        } else if (selectionResultCorrect == Constants.MODE_SELECT) {                   //more than one app that can execute the selected command
                            modeTransition(Constants.MODE_SELECT, Constants.NO_EXTRA, "stateMachine");
                            mTextUpdater.updateText(mModeHistory[0].getMode(), Constants.NO_ADDITIONAL_INFO);
                            speakSelection(mInstructions.getText().toString());

                            if(!mTTSActivated || !mIsTTSSetup) {
                                switchSearch(mModeMap.get(mModeHistory[0].getMode()));
                            }
                        }
                        break;
                    case Constants.MODE_SELECT:
                        //user selected app that he wants to execute the command
                        int selectionResultSelect = mCommandHandler.handleSelection(mCommandHistory[0], mModeHistory[0].getMode());

                        if (selectionResultSelect == Constants.NUMBER_TOO_BIG) {
                            mTextUpdater.updateText(mModeHistory[0].getMode(), Constants.TOO_BIG);
                            speakSelection(mInstructions.getText().toString());

                            if(!mTTSActivated || !mIsTTSSetup) {
                                switchSearch(mModeMap.get(mModeHistory[0].getMode()));
                            }

                        } else if (selectionResultSelect == Constants.ERROR) {
                            waitForInput();
                            makeText(this.getApplicationContext(), getString(R.string.toast_dont_know), Toast.LENGTH_LONG);
                        } else { //success
                            if(mHapticFeedbackActivated) {
                                mV.vibrate(500);
                            }
                        }
                        break;
                    case Constants.MODE_CONFIRM:
                        //user said either yes or no; stop listening for gestures
                        if (mGestureTimerActive) {
                            stopGestureTimer();
                        }

                        //ensure that this is running on the UI thread
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mButtonYes.setVisibility(View.INVISIBLE);
                                mButtonNo.setVisibility(View.INVISIBLE);
                            }
                        });


                        if (mCommandHistory[0].equals(Constants.YES)) {
                            Log.i("Command", "yes for command: " + mCommandHistory[1]);

                            Log.i("Mode", "Mode0: " + mModeHistory[0].getMode() + " Mode1: " + mModeHistory[1].getMode() + " Mode2: " + mModeHistory[2].getMode() + " Mode3: " + mModeHistory[3].getMode());
                            Log.i("Mode", "Mode0: " + mModeHistory[0].getExtra() + " Mode1: " + mModeHistory[1].getExtra() + " Mode2: " + mModeHistory[2].getExtra() + " Mode3: " + mModeHistory[3].getExtra());
                            Log.i("Command", "Command0: " + mCommandHistory[0] + " Command1: " + mCommandHistory[1] + " Command2: " + mCommandHistory[2] + " Command3: " + mCommandHistory[3]);

                            //do the database stuff; only of use when using a language model for the commands, not when using a defined grammar
                            //// TODO: 09.09.2015 since a defined grammar is currently used and this part could not be tested it could be that this is not working properly after adding some more functionality to the app; test again when using a language model
                            if (mModeHistory[2].getMode() == Constants.MODE_HANDLE_UNKNOWN_COMMAND) {
                                Log.i("Database", "Update unknown command");
                                mDBUpdateTask = new DatabaseUpdateTask(mDBHelper);
                                mDBUpdateTask.setCommands(mCommandHistory[1], mCommandHistory[3]);
                                mDBUpdateTask.execute();
                            } else if ((mModeHistory[3].getMode() == Constants.MODE_COMMAND) && (mCommandHistory[2] == Constants.NO)) {
                                Log.i("Databse", "Update wrong command");
                                mDBUpdateTask = new DatabaseUpdateTask(mDBHelper);
                                mDBUpdateTask.setCommands(mCommandHistory[1], mCommandHistory[3]);
                                mDBUpdateTask.execute();
                            } else {
                                Log.i("Database", "Update");
                                mDBUpdateTask = new DatabaseUpdateTask(mDBHelper);
                                mDBUpdateTask.setCommands(mCommandHistory[1], mCommandHistory[1]);
                                mDBUpdateTask.execute();
                            }

                            //here the added extra in mode command comes into play again; is used after confirmation to carry out next steps
                            if(mModeHistory[1].getMode() == Constants.MODE_COMMAND) {

                                switch(mModeHistory[1].getExtra()) {
                                    case Constants.EXTRA_MULTIPLE_ACTIVITIES:
                                        mCommandHandler.fillAdapter();
                                        modeTransition(Constants.MODE_SELECT, Constants.NO_EXTRA, "stateMachine");
                                        mTextUpdater.updateText(mModeHistory[0].getMode(), Constants.NO_ADDITIONAL_INFO);
                                        speakSelection(mInstructions.getText().toString());

                                        if(!mTTSActivated || !mIsTTSSetup) {
                                            switchSearch(mModeMap.get(mModeHistory[0].getMode()));
                                        }

                                        break;
                                    case Constants.EXTRA_SINGLE_ACTIVITY:
                                        mCommandHandler.confirmStartOfActivity();

                                        if(mHapticFeedbackActivated) {
                                            mV.vibrate(500);
                                        }
                                        break;
                                    case Constants.EXTRA_SEARCH:
                                        modeTransition(Constants.MODE_QUERY, Constants.NO_EXTRA, "stateMachine");
                                        mTextUpdater.updateText(mModeHistory[0].getMode(), Constants.NO_ADDITIONAL_INFO);
                                        speakSelection(mInstructions.getText().toString());

                                        if(!mTTSActivated || !mIsTTSSetup) {
                                            switchSearch(mModeMap.get(mModeHistory[0].getMode()));
                                        }

                                        break;
                                    case Constants.EXTRA_LISTEN_FOR_NUMBER:
                                        //whether user wants to call someone or send a text message after dictating a phonenumber is determined by the last command (not yes or no; before that)
                                        if(mCommandHistory[1].equals(Constants.CALL_SOMEONE)) {

                                            if(mCommandChosenByTouch) {

                                                //show EditText and Keyboard
                                                enterPhoneNumber(Constants.CALL);

                                            } else {
                                                modeTransition(Constants.MODE_DICTATE_NUMBER, Constants.CALL, "stateMachine");
                                                mTextUpdater.updateText(mModeHistory[0].getMode(), Constants.CALL);
                                                speakSelection(mInstructions.getText().toString());

                                                if(!mTTSActivated || !mIsTTSSetup) {
                                                    switchSearch(mModeMap.get(mModeHistory[0].getMode()));
                                                }
                                            }
                                        } else if(mCommandHistory[1].equals(Constants.SEND_TEXT_MESSAGE)) {

                                            if(mCommandChosenByTouch) {

                                                //show EditText and Keyboard
                                                enterPhoneNumber(Constants.TEXT);

                                            } else {
                                                modeTransition(Constants.MODE_DICTATE_NUMBER, Constants.TEXT, "stateMachine");
                                                mTextUpdater.updateText(mModeHistory[0].getMode(), Constants.TEXT);
                                                speakSelection(mInstructions.getText().toString());

                                                if(!mTTSActivated || !mIsTTSSetup) {
                                                    switchSearch(mModeMap.get(mModeHistory[0].getMode()));
                                                }
                                            }
                                        }
                                        break;
                                }
                            }
                        } else if (mCommandHistory[0].equals(Constants.NO)) {
                            if(mASRActivated && mIsRecognizerSetup) {                                                            //give the user tha chance to repeat what he wanted to say
                                modeTransition(Constants.MODE_COMMAND, Constants.NO_EXTRA, "stateMachine");
                                mTextUpdater.updateText(mModeHistory[0].getMode(), Constants.SAY_AGAIN);
                                speakSelection(mInstructions.getText().toString());

                                if(!mTTSActivated || !mIsTTSSetup) {
                                    switchSearch(mModeMap.get(mModeHistory[0].getMode()));
                                }

                            } else {
                                modeTransition(Constants.MODE_NOT_LISTENING, Constants.NO_EXTRA, "stateMachine");
                                mTextUpdater.updateText(mModeHistory[0].getMode(), Constants.NO_ADDITIONAL_INFO);
                                speakSelection(mInstructions.getText().toString());
                            }
                        }
                        break;
                    case Constants.MODE_QUERY:
                        mCommandHandler.handleQuery(mCommandHistory[0]);

                        if(mHapticFeedbackActivated) {
                            mV.vibrate(500);
                        }
                        break;
                    case Constants.MODE_DICTATE_NUMBER:

                        int selectionResult;

                        if(!mCommandChosenByTouch) {
                            selectionResult = mCommandHandler.nameNumber(mCommandHistory[0], mModeHistory[0].getExtra());
                        } else {

                            mPhonenumberEdit.setText(getString(R.string.empty));
                            if(mPhonenumberEdit.getVisibility() == View.VISIBLE) {
                                mPhonenumberEdit.setVisibility(View.INVISIBLE);
                            }

                            mCommandChosenByTouch = false;

                            selectionResult = mCommandHandler.handleNumber(mCommandHistory[0], mModeHistory[0].getExtra());
                        }

                        if (selectionResult == Constants.MODE_SELECT) {
                            modeTransition(Constants.MODE_SELECT, Constants.NO_EXTRA, "stateMachine");
                            mTextUpdater.updateText(mModeHistory[0].getMode(), Constants.NO_ADDITIONAL_INFO);
                            speakSelection(mInstructions.getText().toString());

                            if(!mTTSActivated || !mIsTTSSetup) {
                                switchSearch(mModeMap.get(mModeHistory[0].getMode()));
                            }

                        } else if (selectionResult == Constants.ERROR) {
                            waitForInput();
                            makeText(this.getApplicationContext(), getString(R.string.toast_dont_know), Toast.LENGTH_LONG);
                        } else { //success
                            if(mHapticFeedbackActivated) {
                                mV.vibrate(500);
                            }
                        }
                        break;
                    default:
                        if (mModeMap.containsKey(mModeHistory[0].getMode())) {
                            switchSearch(mModeMap.get(mModeHistory[0].getMode()));
                        }
                        break;
                }

                //current mode is the next mode, because this is the end of the method and the
                // transition has already been made
                if(mModeHistory[0].getMode() == Constants.MODE_CONFIRM) {
                    if(mTouchActivated) {
                        mButtonYes.setVisibility(View.VISIBLE);
                        mButtonNo.setVisibility(View.VISIBLE);
                    }
                }
                
                makeText(this.getApplicationContext(), mCommandHistory[0], Toast.LENGTH_SHORT).show();
            }
        }
    }

    //show EditText and keyboard; when the user clicks ENTER (done) transition to the next state and
    //pass the phone number as an input argument to the stateMachine so a text message will be sent
    //to this number or this number will be called
    private void enterPhoneNumber(final int textOrCall) {

        mPhonenumberEdit.setVisibility(View.VISIBLE);
        mPhonenumberEdit.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mPhonenumberEdit, InputMethodManager.SHOW_IMPLICIT);

        mPhonenumberEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    mTextUpdater.updateText(mModeHistory[0].getMode(), textOrCall);
                    modeTransition(Constants.MODE_DICTATE_NUMBER, textOrCall, "stateMachine");
                    stateMachine(mPhonenumberEdit.getText().toString());
                }
                return false;
            }
        });
    }

    //used to manage different recognition modes and the different grammars and models
    private void switchSearch(String searchName) {

        stopBackgroundWork(Constants.CONTINUE_LISTENING);
        mButtonListen.setClickable(false);
        mButtonListen.setEnabled(false);

        startCoverTimer();

        Log.i("TTS", "startListening");

        if(mASRActivated && (mRecognizer != null) && mIsRecognizerSetup) {
            mASRStatus.setText(getString(R.string.listen));
            mRecognizer.startListening(searchName);
            //startInterruptTimer(Constants.ASR);
            Log.i("ASR", "startListening");
        }
    }

    @Override
    public void onError(Exception error) {
        mTextUpdater.error(error);
        speakSelection(mInstructions.getText().toString());
        stopBackgroundWork(Constants.STOP_LISTENING);
        waitForInput();
    }

    @Override
    public void onTimeout() {

    }
}
