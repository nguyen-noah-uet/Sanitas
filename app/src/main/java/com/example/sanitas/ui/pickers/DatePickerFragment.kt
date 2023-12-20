package com.example.sanitas.ui.pickers

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.widget.DatePicker
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import java.time.LocalDateTime

class DatePickerFragment(val datePickedCallback: (Int, Int, Int) -> Unit) : DialogFragment(), DatePickerDialog.OnDateSetListener {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker.
        val c = LocalDateTime.now()
        val year = c.year
        val month = c.monthValue - 1
        val day = c.dayOfMonth

        // Create a new instance of DatePickerDialog and return it.
        return DatePickerDialog(requireContext(), this, year, month, day)

    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        // Do something with the date the user picks.
        datePickedCallback(year, month+1, day)
    }
}