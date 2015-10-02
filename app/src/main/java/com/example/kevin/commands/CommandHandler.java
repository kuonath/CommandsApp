package com.example.kevin.commands;

import android.app.Activity;
import android.app.SearchManager;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Kev94 on 13.08.2015.
 */

//handles the commands that the ASR in the CommandsActivity recognizes depending on the listening mode
// and the type of command
public class CommandHandler {

    private static final String TAG = CommandHandler.class.getSimpleName();

    private Context mContext;
    private Activity mActivity;
    private ListView mList;

    private PackageManager mPackageManager;

    private Intent mIntentToConfirm = null;

    private Intent mIntent = null;
    private List<ResolveInfo> mActivities = null;
    private ArrayAdapter mAdapter;

    private HashMap<String, String> mPhoneNumberMap;
    private HashMap<String, Integer> mNumberMap;

    private StatisticsDBOpenHelper mDBHelper;
    private DatabaseUpdateTask mDBUpdateTask;
    private SuggestionGenerator mSuggestionGenerator;

    private String mUnknownCommand;

    //ListView is needed for either the suggested commands in case of an unknown command
    // (only with language model, not with defined grammar) or in case of a command that can be
    // executed by multiple apps on the phone; the dbHelper is also needed for the suggestions
    public CommandHandler(Activity activity, Context context, ListView list, StatisticsDBOpenHelper dbHelper) {

        mList = list;
        mContext = context;
        mActivity = activity;

        Log.i(TAG, "list is null: " + Boolean.toString(mList == null));

        mPackageManager = context.getPackageManager();

        mDBHelper = dbHelper;

        //mapping between the strings from the recognizer and the numbers and other characters for the phonenumber
        mPhoneNumberMap = new HashMap();
        mPhoneNumberMap.put("oh", "0");
        mPhoneNumberMap.put("zero", "0");
        mPhoneNumberMap.put("one", "1");
        mPhoneNumberMap.put("two", "2");
        mPhoneNumberMap.put("three", "3");
        mPhoneNumberMap.put("four", "4");
        mPhoneNumberMap.put("five", "5");
        mPhoneNumberMap.put("six", "6");
        mPhoneNumberMap.put("seven", "7");
        mPhoneNumberMap.put("eight", "8");
        mPhoneNumberMap.put("nine", "9");
        mPhoneNumberMap.put("plus", "+");
        mPhoneNumberMap.put("hash", "#");
        mPhoneNumberMap.put("pound", "#");
        mPhoneNumberMap.put("number sign", "#");
        mPhoneNumberMap.put("asterisk", "*");

        //mapping between the strings from the recognizer and the integers used for the selection of an activity
        mNumberMap = new HashMap<>();
        mNumberMap.put("one", 1);
        mNumberMap.put("two", 2);
        mNumberMap.put("three", 3);
        mNumberMap.put("four", 4);
        mNumberMap.put("five", 5);
        mNumberMap.put("six", 6);
        mNumberMap.put("seven", 7);
        mNumberMap.put("eight", 8);
        mNumberMap.put("nine", 9);
    }

    //basic handling of the first command; handles the commands that can be carried out by the app or other apps
    //different commands are handled in each if or else-if block; the procedure is the same for most of the commands
    //the else-if block for the command 'open browser' is commented as an example
    public int handleCommand(String command) {

        if (command.equals(Constants.SEARCH))
            return Constants.SEARCH_INTERNET;
        else if (command.equals(Constants.OPEN_BROWSER)) {
            String url = "http://www.google.ie";

            //build the intent to carry out the command; set Flags is needed, because the method
            //startActivity(Intent intent) is called outside of an activity
            Intent browserIntent = new Intent(Intent.ACTION_VIEW);
            browserIntent.setData(Uri.parse(url));
            browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            //is the intent valid
            if (browserIntent.resolveActivity(mPackageManager) != null) {
                List<ResolveInfo> activities = mPackageManager.queryIntentActivities(browserIntent, 0);

                //how many apps are there to carry out the command
                if (activities.size() == 0) {
                    return Constants.ERROR;
                } else if (activities.size() == 1) {
                    //store the intent in a field so that the intent can be carried out later after
                    // the user confirmed that the command was understood correctly (happens in CommandsActivity)
                    mIntentToConfirm = browserIntent;
                    return Constants.EXECUTE_COMMAND;
                } else {
                    //there is more than one activity; store the intent and the list of activities and
                    //tell the CommandsActivity what to do by returning a value
                    mIntent = browserIntent;
                    mActivities = activities;
                    return Constants.SELECT_ACTIVITY;
                }
            } else {
                return Constants.ERROR;
            }
        } else if (command.equals((Constants.OPEN_TTSBROWSER))) {
            Intent ttsbrowserIntent = mPackageManager.getLaunchIntentForPackage("com.example.kev94.ttsbrowser");

            if(ttsbrowserIntent != null) {
                ttsbrowserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mIntentToConfirm = ttsbrowserIntent;
                return Constants.EXECUTE_COMMAND;
            } else {
                return Constants.ERROR;
            }

        } else if (command.equals(Constants.NEW_EMAIL)) {
            Intent mailIntent = new Intent(Intent.ACTION_SENDTO);
            mailIntent.setData(Uri.parse("mailto:"));
            mailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if(mailIntent.resolveActivity(mPackageManager) != null) {
                List<ResolveInfo> activities = mPackageManager.queryIntentActivities(mailIntent, 0);

                if (activities.size() == 0) {
                    return Constants.ERROR;
                } else if (activities.size() == 1) {
                    mIntentToConfirm = mailIntent;
                    return Constants.EXECUTE_COMMAND;
                } else {
                    mIntent = mailIntent;
                    mActivities = activities;
                    return Constants.SELECT_ACTIVITY;
                }
            } else {
                return Constants.ERROR;
            }
        } else if (command.equals(Constants.OPEN_MUSIC_PLAYER)) {
            Intent musicIntent = new Intent(Intent.ACTION_MAIN);
            musicIntent.addCategory(Intent.CATEGORY_APP_MUSIC);
            musicIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if(musicIntent.resolveActivity(mPackageManager) != null) {
                List<ResolveInfo> activities = mPackageManager.queryIntentActivities(musicIntent, 0);

                if (activities.size() == 0) {
                    return Constants.ERROR;
                } else if (activities.size() == 1) {
                    mIntentToConfirm = musicIntent;
                    return Constants.EXECUTE_COMMAND;
                } else {
                    mIntent = musicIntent;
                    mActivities = activities;
                    return Constants.SELECT_ACTIVITY;
                }
            } else {
                return Constants.ERROR;
            }
        } else if (command.equals(Constants.OPEN_CALENDAR)) {
            Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
            builder.appendPath("time");
            ContentUris.appendId(builder, System.currentTimeMillis());
            Intent calendarIntent = new Intent(Intent.ACTION_VIEW).setData(builder.build());
            calendarIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if(calendarIntent.resolveActivity(mPackageManager) != null) {
                List<ResolveInfo> activities = mPackageManager.queryIntentActivities(calendarIntent, 0);

                if (activities.size() == 0) {
                    return Constants.ERROR;
                } else if (activities.size() == 1) {
                    mIntentToConfirm = calendarIntent;
                    return Constants.EXECUTE_COMMAND;
                } else {
                    mIntent = calendarIntent;
                    mActivities = activities;
                    return Constants.SELECT_ACTIVITY;
                }
            } else {
                return Constants.ERROR;
            }
        } else if (command.equals(Constants.TAKE_A_PICTURE)) {

            //store the picture in the standard picture directory
            File destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

            Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(destination));
            pictureIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if(pictureIntent.resolveActivity(mPackageManager) != null) {
                List<ResolveInfo> activities = mPackageManager.queryIntentActivities(pictureIntent, 0);

                if (activities.size() == 0) {
                    return Constants.ERROR;
                } else if (activities.size() == 1) {
                    mIntentToConfirm = pictureIntent;
                    return Constants.EXECUTE_COMMAND;
                } else {
                    mIntent = pictureIntent;
                    mActivities = activities;
                    return Constants.SELECT_ACTIVITY;
                }
            } else {
                return Constants.ERROR;
            }
        } else if (command.equals(Constants.TAKE_A_NOTE)) {
            // TODO: 08.09.2015 currently using Google Keep to take notes; if Google Keep is not installed other apps (e.g. SMemo on Samsung could be used)
            Intent noteIntent = mPackageManager.getLaunchIntentForPackage("com.google.android.keep");

            if(noteIntent != null) {
                noteIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mIntentToConfirm = noteIntent;
                return Constants.EXECUTE_COMMAND;
            } else {
                return Constants.ERROR;
            }
        } else if (command.equals(Constants.OPEN_MAPS)) {
            Uri gmmIntentUri = Uri.parse("geo:37.7749,-122.4194");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if(mapIntent.resolveActivity(mPackageManager) != null) {
                List<ResolveInfo> activities = mPackageManager.queryIntentActivities(mapIntent, 0);

                if (activities.size() == 0) {
                    return Constants.ERROR;
                } else if (activities.size() == 1) {
                    mIntentToConfirm = mapIntent;
                    return Constants.EXECUTE_COMMAND;
                } else {
                    mIntent = mapIntent;
                    mActivities = activities;
                    return Constants.SELECT_ACTIVITY;
                }
            } else {
                return Constants.ERROR;
            }
        } else if (command.equals(Constants.SEND_TEXT_MESSAGE)) {
            //first tell the CommandsActivity that it has to listen for numbers before carrying out any intent
            return Constants.LISTEN_FOR_NUMBER;
        } else if (command.equals(Constants.CALL_SOMEONE)) {
            //first tell the CommandsActivity that it has to listen for numbers before carrying out any intent
            return Constants.LISTEN_FOR_NUMBER;
        } else if (command.equals(Constants.OPEN_CALCULATOR)) {
            Intent calculatorIntent = mPackageManager.getLaunchIntentForPackage("com.android.calculator2");

            if(calculatorIntent != null) {
                calculatorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mIntentToConfirm = calculatorIntent;
                //mContext.startActivity(noteIntent);
                return Constants.EXECUTE_COMMAND;
            } else {
                calculatorIntent = mPackageManager.getLaunchIntentForPackage("com.sec.android.app.popupcalculator");

                if(calculatorIntent != null) {
                    calculatorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mIntentToConfirm = calculatorIntent;
                    //mContext.startActivity(noteIntent);
                    return Constants.EXECUTE_COMMAND;
                } else {
                    return Constants.ERROR;
                }
            }
        } else if (command.equals(Constants.SHOW_CONTACTS)) {
            Intent contactsIntent = new Intent(Intent.ACTION_VIEW);
            contactsIntent.setType(ContactsContract.Contacts.CONTENT_TYPE);
            contactsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if(contactsIntent.resolveActivity(mPackageManager) != null) {
                List<ResolveInfo> activities = mPackageManager.queryIntentActivities(contactsIntent, 0);

                if (activities.size() == 0) {
                    return Constants.ERROR;
                } else if (activities.size() == 1) {
                    mIntentToConfirm = contactsIntent;
                    return Constants.EXECUTE_COMMAND;
                } else {
                    mIntent = contactsIntent;
                    mActivities = activities;
                    return Constants.SELECT_ACTIVITY;
                }
            } else {
                return Constants.ERROR;
            }
        } else if (command.equals(Constants.SHOW_PHOTOS)) {
            Intent galleryIntent = new Intent(Intent.ACTION_VIEW, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if(galleryIntent.resolveActivity(mPackageManager) != null) {
                List<ResolveInfo> activities = mPackageManager.queryIntentActivities(galleryIntent, 0);

                if (activities.size() == 0) {
                    return Constants.ERROR;
                } else if (activities.size() == 1) {
                    mIntentToConfirm = galleryIntent;
                    return Constants.EXECUTE_COMMAND;
                } else {
                    mIntent = galleryIntent;
                    mActivities = activities;
                    return Constants.SELECT_ACTIVITY;
                }
            } else {
                return Constants.ERROR;
            }
        } else if (command.equals(Constants.SHOW_COMMANDS)) {
            //show the dialog that lists all commands
            return Constants.LIST_DIALOG;
        } else {
            //none of the above cases applied -> unknown command (can only happen with a language model
            // that uses uni-, bi- and trigrams, not with a defined grammar that only lists complete commands
            return Constants.UNKNOWN_COMMAND;
        }
    }

    //carry out the intent that has been stored in the field mIntentToConfirm; gets called after the user cnfirmed the command
    public void confirmStartOfActivity() {
        mContext.startActivity(mIntentToConfirm);
    }

    //called when there is an unknown command (only for language model, not for grammar) or when the
    //ASR recognized the wrong command
    public int handleWrongCommand(String command, String commandToCorrect) {

        //mUnknownCommand used for updating the database in handleSelection
        mUnknownCommand = commandToCorrect;

        switch(command.toLowerCase()) {
            case Constants.REPEAT:
                //user said 'repeat' and wants to repeat the command -> listening mode command
                return Constants.MODE_COMMAND;
            case Constants.CORRECT:

                //this case only applies in some cases when a language model is used instead of a defined grammar
                //there can be completely new and unknown commands consisting of a mix of words of known commands
                //there might be a pattern that some commands get confused with unknown commands; that is why
                //there is a database that keeps track of these confusions; suggestions get either generated on
                //the basis of this database (confusion matrix) or on the basis of words in the unknown command
                //that also occur in a known command
                List<Suggestion> suggestions;
                List<String> commands = new ArrayList<>();
                List<Integer> frequencies = new ArrayList<>();

                mSuggestionGenerator = new SuggestionGenerator(mDBHelper);

                suggestions = mSuggestionGenerator.generateIntelligentSuggestions(commandToCorrect);

                //break down command-frequency pair in type suggestion to its components to pass the components to the Adapter
                for (Suggestion suggestion : suggestions) {
                    commands.add((String)suggestion.getCommand());
                    frequencies.add((int)suggestion.getFrequency());
                }

                //convert List<String> commands to Array and set the items of the List
                mAdapter = new SuggestionListAdapter(mActivity, commands.toArray(), frequencies);
                mList.setAdapter(mAdapter);
                return Constants.MODE_CORRECT;
            default:
                return Constants.MODE_HANDLE_UNKNOWN_COMMAND;
        }
    }

    //there are two cases where the user has to choose between options:
    //  1.the ASR recognized an unknown command and the user said 'correct' -> suggestions are shown and the user can choose
    //  2.there is more than one app that can execute the command -> the user can select one of the apps
    public int handleSelection(String number, int listeningMode) {

        //in case the correct command is not shown in the suggestions, the user can say missing and
        //the app will be waiting for any inout again
        if(number.equals("missing")) {
            return Constants.MODE_COMMAND;
        }

        String[] nums;

        //it could be that the ASR recognizes more than one number
        //in that case use only the last number
        nums = number.split(" ", 2);

        int num = mNumberMap.get(nums[nums.length-1]);

        int numberOfSuggestions = mAdapter.getCount();

        if(num > numberOfSuggestions) {
            return Constants.NUMBER_TOO_BIG;
        }

        switch (listeningMode) {
            case Constants.MODE_CORRECT:

                //execute the command that the user selected out of the suggestions and update the database
                String command = (String) mAdapter.getItem(num-1);
                Log.i(TAG, "selected command: " + command);

                mDBUpdateTask = new DatabaseUpdateTask(mDBHelper);
                mDBUpdateTask.setCommands(command, mUnknownCommand);
                mDBUpdateTask.execute();

                int nextStep = handleCommand(command);

                //do not ask for confirmation, this time the selection of the user should be alright
                if(nextStep == Constants.EXECUTE_COMMAND) {
                    mContext.startActivity(mIntentToConfirm);
                    return Constants.SUCCESS;
                } else if(nextStep == Constants.SELECT_ACTIVITY){
                    return Constants.MODE_SELECT;
                }
                break;
            case Constants.MODE_SELECT:
                //select one of the apps that can execute the command
                ResolveInfo info = (ResolveInfo) mAdapter.getItem(num-1);

                Intent intent = mIntent;
                intent.setClassName(info.activityInfo.packageName, info.activityInfo.name);
                mContext.startActivity(intent);
                return Constants.SUCCESS;
        }
        return Constants.ERROR;
    }

    public int nameNumber(String input, int textOrCall) {

        String[] words = input.split(" ");
        String firstWord = words[0];

        Log.i(TAG, "first word: " + words[0]);

        int returnValue;

        if(mPhoneNumberMap.containsKey(firstWord) || firstWord.equals("number") || firstWord.equals("double") || firstWord.equals("triple")) {
            returnValue = handleNumber(input, textOrCall);
        } else {
            returnValue = handleName(input, textOrCall);
        }

        return returnValue;
    }

    //handle a phonenumber and either call someone or send a text message
    public int handleNumber(String number, int textOrCall) {

        String phoneNumber = "pn";

        char firstCharacter = number.charAt(0);

        Log.i(TAG, "Is digit: " + Integer.toString(Character.digit(firstCharacter,10)) + "is sign: " + Boolean.toString(!((firstCharacter != '#') || (firstCharacter != '*') || (firstCharacter != '+'))));

        //// TODO: 08.09.2015 there is something wrong with this if-else statement; why do I check if the first character is a digit?
        if((Character.digit(firstCharacter,10) == -1) && (firstCharacter != '#') && (firstCharacter != '*') && (firstCharacter != '+'))  {
            boolean wasPreviousDouble = false;
            boolean wasPreviousTriple = false;
            boolean wasPreviousNumber = false;

            String[] digits = number.split(" ");
            phoneNumber = "";

            for (String digit : digits) {

                if(digit == null) {
                    continue;
                } else if (phoneNumber == null) {
                    phoneNumber = "";
                }

                Log.i(TAG, "digit: " + digit);

                if (digit.equals("double")) {
                    wasPreviousDouble = true;
                } else if (digit.equals("triple")) {
                    wasPreviousTriple = true;
                } else if (digit.equals("number")) {
                    wasPreviousNumber = true;
                } else {

                    if(mPhoneNumberMap.get(digit) != null) {
                        if (wasPreviousDouble) {
                            phoneNumber = phoneNumber.concat(mPhoneNumberMap.get(digit));
                            phoneNumber = phoneNumber.concat(mPhoneNumberMap.get(digit));
                        } else if (wasPreviousTriple) {
                            phoneNumber = phoneNumber.concat(mPhoneNumberMap.get(digit));
                            phoneNumber = phoneNumber.concat(mPhoneNumberMap.get(digit));
                            phoneNumber = phoneNumber.concat(mPhoneNumberMap.get(digit));
                        } else if (wasPreviousNumber) {
                            if (digit.equals("sign")) {
                                phoneNumber = phoneNumber.concat(mPhoneNumberMap.get("number sign"));
                            }
                        } else {
                            phoneNumber = phoneNumber.concat(mPhoneNumberMap.get(digit));
                        }
                    }

                    wasPreviousDouble = false;
                    wasPreviousTriple = false;
                    wasPreviousNumber = false;
                }
            }
        } else {
            phoneNumber = number;
        }

        Toast.makeText(mContext, phoneNumber, Toast.LENGTH_LONG).show();

        //either call the number or send a text message to this number
        if (textOrCall == Constants.CALL) {
            Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if(callIntent.resolveActivity(mPackageManager) != null) {
                mContext.startActivity(callIntent);
                return Constants.SUCCESS;
            } else {
                return Constants.ERROR;
            }
        } else if(textOrCall == Constants.TEXT){
            Intent messageIntent = new Intent(Intent.ACTION_VIEW);
            messageIntent.putExtra("address", phoneNumber);
            messageIntent.setData(Uri.parse("smsto:" + phoneNumber));
            messageIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            List<ResolveInfo> activities = mPackageManager.queryIntentActivities(messageIntent, 0);

            if (activities.size() == 0) {
                return Constants.ERROR;
            } else if (activities.size() == 1) {
                mContext.startActivity(messageIntent);
                return Constants.SUCCESS;
            } else {
                mIntent = messageIntent;
                Log.i(TAG, "list is null: " + Boolean.toString(mList == null));
                mAdapter = new ActivityListAdapter(mActivity, activities.toArray());
                Log.i(TAG, "adapter is null: " + Boolean.toString(mAdapter == null));
                mList.setAdapter(mAdapter);
                return Constants.MODE_SELECT;
            }
        } else {
            return Constants.ERROR;
        }
    }

    public int handleName(String name, int textOrCall) {

        //http://stackoverflow.com/questions/6330151/how-to-get-a-contacts-number-from-contact-name-in-android

        String number;

        String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" like'%" + name +"%'";
        String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor c = mContext.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection, selection, null, null);
        if (c.moveToFirst()) {
            number = c.getString(0);
        } else {
            number = "000";
        }
        c.close();

        Log.i("Name", "number: " + number);

        //either call the number or send a text message to this number
        if (textOrCall == Constants.CALL) {
            Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if(callIntent.resolveActivity(mPackageManager) != null) {
                mContext.startActivity(callIntent);

                return Constants.SUCCESS;
            } else {

                return Constants.ERROR;
            }
        } else if(textOrCall == Constants.TEXT){
            Intent messageIntent = new Intent(Intent.ACTION_VIEW);
            messageIntent.putExtra("address", number);
            messageIntent.setData(Uri.parse("smsto:" + number));
            messageIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            List<ResolveInfo> activities = mPackageManager.queryIntentActivities(messageIntent, 0);

            if (activities.size() == 0) {

                return Constants.ERROR;
            } else if (activities.size() == 1) {
                mContext.startActivity(messageIntent);

                return Constants.SUCCESS;
            } else {
                mIntent = messageIntent;
                Log.i(TAG, "list is null: " + Boolean.toString(mList == null));
                mAdapter = new ActivityListAdapter(mActivity, activities.toArray());
                Log.i(TAG, "adapter is null: " + Boolean.toString(mAdapter == null));
                mList.setAdapter(mAdapter);

                return Constants.MODE_SELECT;
            }
        } else {
            return Constants.ERROR;
        }
    }

    //search the Internet for the query
    public void handleQuery(String query) {

        Intent searchIntent = new Intent(Intent.ACTION_WEB_SEARCH);
        searchIntent.putExtra(SearchManager.QUERY, query);
        searchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(searchIntent);
    }

    public void fillAdapter() {
        Log.i(TAG, "list is null: " + Boolean.toString(mList == null));
        mAdapter = new ActivityListAdapter(mActivity, mActivities.toArray());
        mList.setAdapter(mAdapter);
    }
}
