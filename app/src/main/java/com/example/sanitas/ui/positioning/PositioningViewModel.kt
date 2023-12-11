package com.example.sanitas.ui.positioning

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sanitas.domain.position.PositioningProvider
import com.example.sanitas.services.DefaultLocationClient
import com.google.android.gms.location.LocationServices
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.Location

class PositioningViewModel : ViewModel() {
    private val TAG = "PositioningViewModel"
    init {
        DefaultLocationClient.getInstance().setOnLocationChanged(::updateLocation)
    }

    // New PositionProvider instance
    private var positioningProvider: PositioningProvider? = null

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


    fun setupProvider(context: Context) {
        positioningProvider =
            PositioningProvider(context, LocationServices.getFusedLocationProviderClient(context))

        positioningProvider!!.startLocating { updateLocation(it) }
    }


    fun switchTracking() {
        isTracking = !isTracking

        // Implement saving tracked route to database here

        // Clearing current
        _tracked.value?.clear()
    }


    // Callback trigger when new location update
    // Need to pass as an argument to PositioningListener
    private fun updateLocation(new: android.location.Location) {
        Log.i(TAG, "Tracking: $isTracking")
        val convertedLocation = convertLocation(new)
        _location.value = convertedLocation
        tracked.value?.add(convertedLocation.coordinates)
//        _tracked.value = _tracked.value
        Log.i(TAG, "${_tracked.value?.size}")
    }


    // Convert from android.location.Location to HERE SDK Location
    private fun convertLocation(nativeLocation: android.location.Location): Location {
        val geoCoordinates = GeoCoordinates(
            nativeLocation.latitude,
            nativeLocation.longitude,
            nativeLocation.altitude
        )
        val location = Location(geoCoordinates)
        if (nativeLocation.hasBearing()) {
            location.bearingInDegrees = nativeLocation.bearing.toDouble()
        }
        if (nativeLocation.hasSpeed()) {
            location.speedInMetersPerSecond = nativeLocation.speed.toDouble()
        }
        if (nativeLocation.hasAccuracy()) {
            location.horizontalAccuracyInMeters = nativeLocation.accuracy.toDouble()
        }
        return location
    }
}