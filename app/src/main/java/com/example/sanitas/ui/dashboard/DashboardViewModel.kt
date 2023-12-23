package com.example.sanitas.ui.dashboard

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sanitas.SanitasApp
import com.example.sanitas.dataprocessing.StepMonitor

class DashboardViewModel() : ViewModel() {

    @RequiresApi(Build.VERSION_CODES.O)
    private val _steps = MutableLiveData<Int>().apply {
        value = SanitasApp.currentSteps
    }
    @RequiresApi(Build.VERSION_CODES.O)
    val steps: LiveData<Int> = _steps

}