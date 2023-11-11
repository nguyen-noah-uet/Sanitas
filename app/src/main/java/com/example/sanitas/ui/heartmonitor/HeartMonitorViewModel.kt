package com.example.sanitas.ui.heartmonitor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HeartMonitorViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    private val _text = MutableLiveData<String>().apply {
        value = "This is HeartMonitor Fragment"
    }
    val text : LiveData<String> = _text
}