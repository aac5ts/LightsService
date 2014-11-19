package com.example.alexandra.lightsservice;

import android.database.Cursor;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by Alexandra on 11/19/2014.
 */
class MyViewBinder implements SimpleCursorAdapter.ViewBinder {

    @Override
    public boolean setViewValue(View view, Cursor cursor, int index) {
        Log.i("MyViewBinder", "IN SET VIEW VALUE");
        if (index == cursor.getColumnIndex(CalendarContract.Instances.BEGIN)) {
            Log.i("MyViewBinder", "IN BEGIN VIEW BIND");
            // get a locale based string for the date
            DateFormat formatter = new SimpleDateFormat("KK:mm a");
            long date = cursor.getLong(index);
            ((TextView) view).setText(formatter.format(date));
            return true;
        } else {
            return false;
        }
    }
}
