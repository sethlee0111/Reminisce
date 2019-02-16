package com.sethlee0111.reminiscence;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TimePicker;

public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {
    OnDataPass dataPasser;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dataPasser = (OnDataPass) context;
    }
    public void passTime(int hourOfDay, int minute) {
        dataPasser.onTimePass(hourOfDay, minute);
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Do something with the time chosen by the user
        passTime(hourOfDay, minute);
        Log.d("Time Set!!! ", "" + hourOfDay + " : " + minute);
    }
}