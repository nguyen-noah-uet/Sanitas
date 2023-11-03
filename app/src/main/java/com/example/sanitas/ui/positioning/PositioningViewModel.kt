package com.example.sanitas.ui.positioning

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PositioningViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    private val _text =  MutableLiveData<String>().apply { value = "This is Positioning Fragment" }
    val text : LiveData<String> = _text
}