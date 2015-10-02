package com.example.kevin.commands;

import android.app.Activity;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Kev94 on 17.08.2015.
 */

//sometimes there is more than one app that can carry out an Intent; this adapter contains a list
// of all the available apps installed on device that can carry out the command; the user can choose
// the app
//
// (idea: in settings you could select whether you want this behaviour or you want to use the
// default app)
public class ActivityListAdapter extends ArrayAdapter {

    private final Activity context;
    Object[] items;

    //constructor takes an array of the activities that are available on the phone and can execute the command as an input argument
    public ActivityListAdapter(Activity context, Object[] items) {

        super(context, R.layout.activity_list, items);
        this.context = context;
        this.items = items;
    }

    //create the view of the adapter; see activity_list.xml for the layout; each row has the same
    //layout, but contains different activities (icons, names)
    @Override
    public View getView(int position, View view, ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();

        View rowView = inflater.inflate(R.layout.activity_list, null, true);

        TextView activityNumber = (TextView) rowView.findViewById(R.id.activity_number);
        TextView activityName = (TextView) rowView.findViewById(R.id.activity_name);
        ImageView activityIcon = (ImageView) rowView.findViewById(R.id.activity_icon);

        activityNumber.setText(Integer.toString(position+1));
        activityName.setText(((ResolveInfo)items[position]).activityInfo.applicationInfo.loadLabel(context.getPackageManager()).toString());
        activityIcon.setImageDrawable(((ResolveInfo)items[position]).activityInfo.applicationInfo.loadIcon(context.getPackageManager()));

        return rowView;
    }
}
