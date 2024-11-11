package com.example.moneymanager;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.widget.EditText;

import java.util.Calendar;

public class calenderUI {
    public static void showCalendar(Activity activity, EditText dateInput) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(activity,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
                    dateInput.setText(date);
                }, year, month, day);

        datePickerDialog.show();
    }
}
