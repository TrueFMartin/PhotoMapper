package com.github.truefmartin.photomapper.NewEditTaskActivity

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.time.LocalDateTime
import java.util.Calendar

class TimePickerFragment(private val onSelectFunction: (LocalDateTime) -> Unit) : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker.
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        // Create a new instance of TimePickerDialog and return it.
        return TimePickerDialog(activity, this, hour, minute, DateFormat.is24HourFormat(activity))
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        onSelectFunction(LocalDateTime.of(2023, 1, 1, hourOfDay, minute))
    }
}

class DatePickerFragment(private val onSelectFunction: (LocalDateTime) -> Unit) : DialogFragment(), DatePickerDialog.OnDateSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker.
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        c.toString()

        // Create a new instance of DatePickerDialog and return it.
        return DatePickerDialog(requireContext(), this, year, month, day)

    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        // Use call back function to set the date for the task
        onSelectFunction(LocalDateTime.of(year, month + 1, day, 0, 0))
    }
}