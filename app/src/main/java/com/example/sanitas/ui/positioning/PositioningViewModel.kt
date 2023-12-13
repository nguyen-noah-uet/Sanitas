package com.example.sanitas.ui.positioning

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sanitas.services.LocationService
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.Location

class PositioningViewModel : ViewModel() {

    // Enable/Disable tracking location indicator
    private var isTracking: Boolean = false


    // Current location livedata
    private val _location = MutableLiveData<Location>().apply {
        value = null
    }
    val location: LiveData<Location> = _location


    // Tracked location arraylist livedata
    private val _tracked = MutableLiveData<ArrayList<GeoCoordinates>>().apply {
        //value = _location.value?.let { arrayListOf(it.coordinates) }
        value = arrayListOf()
    }
    val tracked: LiveData<ArrayList<GeoCoordinates>> = _tracked


    fun startLocation(context: Context, activity: Activity) {
        LocationService.updateCallback = { updateLocation(it) }
        activity.startService(Intent(context, LocationService::class.java).apply {
            action = LocationService.ACTION_START
        })
    }

    fun switchTracking() {
        isTracking = !isTracking

        // Implement saving tracked route to database here

        // Clearing current
        _tracked.value?.clear()
    }


    // Callback trigger when new location update
    // Need to pass as an parameter to LocationService
    private fun updateLocation(new: Location) {
        _location.postValue(new)
        if (isTracking) {
            _tracked.value?.add(new.coordinates)
            _tracked.postValue(_tracked.value)
        }
    }

}