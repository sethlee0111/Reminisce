package com.sethlee0111.reminiscence;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TimePicker;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    private OnDataPass dataPasser;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dataPasser = (OnDataPass) context;
    }
    public void passDate(int year, int month, int dayOfMonth) {
        dataPasser.onDatePass(year, month, dayOfMonth);
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of TimePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, dayOfMonth);
    }

    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        // Do something with the time chosen by the user
        passDate(year, month, dayOfMonth);
        Log.d("Date Set!!! ", "" + year + " " + month + " " + dayOfMonth);
    }
}