package com.example.kevin.commands;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kev94 on 03.09.2015.
 */

//Lists all the possible commands; the user can click on any command to execute the command;
// Dialog is shown either when the user clicks on the 'Show commands' button or when the user
// says 'show commands'
public class DialogListCommandsFragment extends DialogFragment {

    private String[] commandsList;

    //interface that is implemented by CommandsActivity; passes the selected command back to the
    // activity; CommandsActivity executes the selected command
    public interface iOnCommandSelectedListener {
        public void onCommandSelected(String command);
    }

    private iOnCommandSelectedListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        mListener = (iOnCommandSelectedListener) getActivity();

        //list of all commands; commands are listed in one string constant in the class Constants and
        // separated by commas; here this string constant is split into the single commands to list
        // the commands in an array and set the items of the dialog
        commandsList = Constants.ALL_COMMANDS.split(",");

        builder.setItems(commandsList, new DialogInterface.OnClickListener() {

            //onItemClickListener; passess the selected command on to the CommandsActivity via the
            // interface iOnCommandSelectedListener and the activity executes the command
            public void onClick(DialogInterface dialog, int which) {

                mListener.onCommandSelected(commandsList[which]);
            }
        });

        return builder.create();
    }
}
