package com.example.sanitas.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    private val _text= MutableLiveData<String>().apply {
        value = "This is profile fragment"
    }
    val text: LiveData<String> = _text
}