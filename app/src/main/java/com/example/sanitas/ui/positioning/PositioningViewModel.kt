package com.example.sanitas.ui.positioning

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sanitas.data.position.TravelRoute
import com.example.sanitas.repositories.TravelRouteRepository
import com.example.sanitas.services.LocationService
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.Location
import kotlinx.coroutines.launch

class PositioningViewModel(private val repository: TravelRouteRepository) : ViewModel() {

    // Enable/Disable tracking location indicator
    private var isTracking: Boolean = false
    private var currentRouteId: Int? = 0
    private var routeOrder = 0

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

    fun switchTracking() = viewModelScope.launch {
        if (isTracking) {
            isTracking = false
            Log.e("NULL", "DM AO THAT DAY")
            currentRouteId = repository.getLocalMaxRouteId()
            routeOrder = 0

            currentRouteId = if (currentRouteId == null) 0 else currentRouteId!! + 1

            // Implement saving tracked route to database here
            var newTravelRoute: TravelRoute
            _tracked.value?.forEach {
                newTravelRoute = TravelRoute(
                    routeId = currentRouteId!!,
                    ordering = routeOrder,
                    latitude = it.latitude,
                    longitude = it.longitude
                )
                repository.insertLocalRouteLocation(newTravelRoute)
                routeOrder++
            }
        } else {
            isTracking = true
        }

        // Clearing current
        _tracked.value?.clear()
        _tracked.postValue(_tracked.value)
    }


    fun loadHistoryTravelRouteById(id: Int) = viewModelScope.launch {
        _tracked.value?.clear()
        val fetchedData = repository.fetchLocalTravelRouteById(id)
        fetchedData.forEach {
            _tracked.value?.add(GeoCoordinates(it.latitude, it.longitude))
        }
        _tracked.postValue(_tracked.value)
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


class PositioningViewModelFactory(private val repository: TravelRouteRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PositioningViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PositioningViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}