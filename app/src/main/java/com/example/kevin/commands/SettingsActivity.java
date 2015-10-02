package com.example.kevin.commands;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;


/**
 * Created by Kev94 on 04.09.2015.
 */

/************ Has to be updated when new settings want to be added or create a new *****************
 * ********** class that extends the Preference class (see PitchPreference or **********************
 * ********** SpeechRatePreference as an example) *************************************************/

//// TODO: 10.09.2015 on first start load preference to reflect the settings that were checked in the features dialog
//displays the current settings and lets the user change them
public class SettingsActivity extends Activity {

    private static final String TAG = SettingsActivity.class.getSimpleName();

    private Intent getIntent;
    private static String[] languages;
    private static String[] voices;
    private static String[] engines;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getIntent = getIntent();

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
        languages = getIntent.getStringArrayExtra("languageEntries");
        voices = getIntent.getStringArrayExtra("voicesEntries");
        engines = getIntent.getStringArrayExtra("enginesEntries");
    }

    //this Fragment contains the Settings
    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        //always set the entries and the entry values when this fragment is being created (maybe the
        // user downloaded new languages and voices)
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

            CheckBoxPreference ASRActivePref = (CheckBoxPreference) findPreference(Constants.PREF_KEY_ASR_ACTIVE);
            CheckBoxPreference openDomainActivePref = (CheckBoxPreference) findPreference(Constants.PREF_KEY_OPEN_DOMAIN);
            CheckBoxPreference TTSActivePref = (CheckBoxPreference) findPreference(Constants.PREF_KEY_TTS_ACTIVE);
            CheckBoxPreference sensorsActivePref = (CheckBoxPreference) findPreference(Constants.PREF_KEY_SENSORS_ACTIVE);
            CheckBoxPreference touchActivePref = (CheckBoxPreference) findPreference(Constants.PREF_KEY_TOUCH_ACTIVE);
            CheckBoxPreference hapticActivePref = (CheckBoxPreference) findPreference(Constants.PREF_KEY_HAPTIC_ACTIVE);
            SpeechRatePreference speechRatePref = (SpeechRatePreference) findPreference(Constants.PREF_KEY_SPEECH_RATE);
            PitchPreference pitchPref = (PitchPreference) findPreference(Constants.PREF_KEY_PITCH);

            boolean ASRActive = prefs.getBoolean(Constants.PREF_KEY_ASR_ACTIVE, false);
            boolean openDomainActive = prefs.getBoolean(Constants.PREF_KEY_OPEN_DOMAIN, false);
            boolean TTSActive = prefs.getBoolean(Constants.PREF_KEY_TTS_ACTIVE, false);
            boolean sensorsActive = prefs.getBoolean(Constants.PREF_KEY_SENSORS_ACTIVE, false);
            boolean touchActive = prefs.getBoolean(Constants.PREF_KEY_TOUCH_ACTIVE, false);
            boolean hapticActive = prefs.getBoolean(Constants.PREF_KEY_HAPTIC_ACTIVE, false);

            openDomainActivePref.setEnabled(ASRActive);
            speechRatePref.setEnabled(TTSActive);
            pitchPref.setEnabled(TTSActive);

            if(ASRActive) {
                ASRActivePref.setSummary(getString(R.string.asr_active));
            } else {
                ASRActivePref.setSummary(getString(R.string.asr_inactive));
            }

            if(openDomainActive) {
                openDomainActivePref.setSummary(getString(R.string.yes));
            } else {
                openDomainActivePref.setSummary(getString(R.string.no));
            }

            if(TTSActive) {
                TTSActivePref.setSummary(getString(R.string.yes));
            } else {
                TTSActivePref.setSummary(getString(R.string.no));
            }

            if(sensorsActive) {
                sensorsActivePref.setSummary(getString(R.string.yes));
            } else {
                sensorsActivePref.setSummary(getString(R.string.no));
            }

            if(touchActive) {
                touchActivePref.setSummary(getString(R.string.yes));
            } else {
                touchActivePref.setSummary(getString(R.string.no));
            }

            if(hapticActive) {
                hapticActivePref.setSummary(getString(R.string.yes));
            } else {
                hapticActivePref.setSummary(getString(R.string.no));
            }

            ListPreference languagesList = (ListPreference) findPreference(Constants.PREF_KEY_LANGUAGE);
            languagesList.setEntries(languages);
            languagesList.setEntryValues(languages);
            languagesList.setSummary(languagesList.getEntry());

            ListPreference voicesList = (ListPreference) findPreference(Constants.PREF_KEY_VOICE);

            /** following is an attempt to dynamically change the entries of the ListPreference to
             * select a voice depending on the selected language (also implemented in OnOptionsItemSelected()
             * in CommandsActivity, but this implementation is too slow and the app needs some seconds to show the settings activity
             */

            /*SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String languageToCompare = sharedPrefs.getString("pref_key_language", "en_US-locale");

            List<String> voicesForLanguage = new ArrayList<>();

            for(String v:voices) {
                if(v.toLowerCase().contains(languageToCompare)) {
                    voicesForLanguage.add(v);
                }
            }

            Object[] voicesListObject = voicesForLanguage.toArray();
            String[] voicesListString = Arrays.copyOf(voicesListObject, voicesListObject.length, String[].class);*/

            voicesList.setEntries(voices);
            voicesList.setEntryValues(voices);
            voicesList.setSummary(voicesList.getEntry());

            ListPreference enginesList = (ListPreference) findPreference(Constants.PREF_KEY_ENGINE);
            enginesList.setEntries(engines);
            enginesList.setEntryValues(engines);
            enginesList.setSummary(enginesList.getEntry());
        }

        //Listens for changes in the preferences; currently this method only changes the summary
        // (text that is displayed below the title of the preference); the speech rate and pitch
        // preferences summary and SeekBar are being handled in the respective classes
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

            switch(key) {
                case Constants.PREF_KEY_ASR_ACTIVE:
                    CheckBoxPreference ASRActivePref = (CheckBoxPreference) findPreference(key);

                    CheckBoxPreference openDomainActivePref = (CheckBoxPreference) findPreference(Constants.PREF_KEY_OPEN_DOMAIN);

                    if(sharedPreferences.getBoolean(key, false) == true) {
                        ASRActivePref.setSummary(getString(R.string.asr_active));
                        openDomainActivePref.setEnabled(true);
                    } else {
                        ASRActivePref.setSummary(getString(R.string.asr_inactive));
                        openDomainActivePref.setEnabled(false);
                    }
                    break;
                case Constants.PREF_KEY_OPEN_DOMAIN:
                    CheckBoxPreference openDomainPref = (CheckBoxPreference) findPreference(key);

                    if(sharedPreferences.getBoolean(key, false) == true) {
                        openDomainPref.setSummary(getString(R.string.yes));
                    } else {
                        openDomainPref.setSummary(getString(R.string.no));
                    }
                    break;
                case Constants.PREF_KEY_TTS_ACTIVE:
                    CheckBoxPreference TTSActivePref = (CheckBoxPreference) findPreference(key);

                    ListPreference languagesListPref = (ListPreference) findPreference(Constants.PREF_KEY_LANGUAGE);
                    ListPreference voicesListPref = (ListPreference) findPreference(Constants.PREF_KEY_VOICE);
                    ListPreference enginesListPref = (ListPreference) findPreference(Constants.PREF_KEY_ENGINE);
                    SpeechRatePreference speechRatePref = (SpeechRatePreference) findPreference(Constants.PREF_KEY_SPEECH_RATE);
                    PitchPreference pitchPref = (PitchPreference) findPreference(Constants.PREF_KEY_PITCH);

                    if(sharedPreferences.getBoolean(key, false) == true) {
                        TTSActivePref.setSummary(getString(R.string.yes));
                        languagesListPref.setEnabled(true);
                        voicesListPref.setEnabled(true);
                        enginesListPref.setEnabled(true);
                        speechRatePref.setEnabled(true);
                        pitchPref.setEnabled(true);
                    } else {
                        TTSActivePref.setSummary(getString(R.string.no));
                        languagesListPref.setEnabled(false);
                        voicesListPref.setEnabled(false);
                        enginesListPref.setEnabled(false);
                        speechRatePref.setEnabled(false);
                        pitchPref.setEnabled(false);

                    }
                    break;
                case Constants.PREF_KEY_SPEECH_RATE:

                    break;
                case Constants.PREF_KEY_PITCH:

                    break;
                case Constants.PREF_KEY_LANGUAGE:
                    ListPreference languagePref = (ListPreference) findPreference(key);
                    languagePref.setSummary(sharedPreferences.getString(key, "US"));
                    break;
                case Constants.PREF_KEY_VOICE:
                    ListPreference voicePref = (ListPreference) findPreference(key);
                    voicePref.setSummary(sharedPreferences.getString(key, "en_US-locale"));
                    break;
                case Constants.PREF_KEY_ENGINE:
                    ListPreference enginePref = (ListPreference) findPreference(key);
                    enginePref.setSummary(sharedPreferences.getString(key, "com.google.android.tts"));
                    break;
                case Constants.PREF_KEY_SENSORS_ACTIVE:
                    CheckBoxPreference sensorsActivePref = (CheckBoxPreference) findPreference(key);

                    if(sharedPreferences.getBoolean(key, false) == true) {
                        sensorsActivePref.setSummary(getString(R.string.yes));
                    } else {
                        sensorsActivePref.setSummary(getString(R.string.no));
                    }
                    break;
                case Constants.PREF_KEY_TOUCH_ACTIVE:
                    CheckBoxPreference touchActivePref = (CheckBoxPreference) findPreference(key);

                    if(sharedPreferences.getBoolean(key, false) == true) {
                        touchActivePref.setSummary(getString(R.string.yes));
                    } else {
                        touchActivePref.setSummary(getString(R.string.no));
                    }
                    break;
                case Constants.PREF_KEY_HAPTIC_ACTIVE:
                    CheckBoxPreference hapticActivePref = (CheckBoxPreference) findPreference(key);

                    if(sharedPreferences.getBoolean(key, false) == true) {
                        hapticActivePref.setSummary(getString(R.string.yes));
                    } else {
                        hapticActivePref.setSummary(getString(R.string.no));
                    }
                    break;
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }
    }
}
