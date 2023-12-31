package com.example.sanitas.ui.dashboard

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sanitas.SanitasApp
import com.example.sanitas.dataprocessing.StepMonitor
import com.example.sanitas.repositories.StepsRepository


@RequiresApi(Build.VERSION_CODES.O)
class DashboardViewModel(repository: StepsRepository) : ViewModel() {


    private val _steps: MutableLiveData<Int> = MutableLiveData<Int>().apply {
        value = SanitasApp.currentSteps + repository.oldSteps
    }


    init {
        StepMonitor.getInstance().setOnStepDetectedCallback {
            _steps.value = SanitasApp.currentSteps + repository.oldSteps
            _calories.value = _steps.value?.times(0.037);
        }

    }

    val steps: LiveData<Int> = _steps

    private val _calories: MutableLiveData<Double> = MutableLiveData<Double>().apply {
        value = _steps.value?.times(0.037);
    }
    val calories: LiveData<Double> = _calories

    private val _heartBeat: MutableLiveData<Double> = MutableLiveData<Double>().apply {
        value = SanitasApp.measuredHeartBeat
    }
    val heartBeat: LiveData<Double> = _heartBeat
}

class DashboardViewModelFactory(private val repository: StepsRepository) :
    ViewModelProvider.Factory {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}