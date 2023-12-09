package com.example.sanitas.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sanitas.dataprocessing.StepMonitor

class DashboardViewModel() : ViewModel() {

    private val _steps = MutableLiveData<Int>().apply {
        value = StepMonitor.stepCounter
    }
    val steps: LiveData<Int> = _steps

}