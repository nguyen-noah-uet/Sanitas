package com.example.sanitas

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.sanitas.data.LocalAppDatabase
import com.example.sanitas.repositories.TravelRouteRepository

@RequiresApi(Build.VERSION_CODES.O)
class SanitasApp: Application() {
    companion object {
        var userDisplayName: String? = null
        var userEmail: String? = null
        var userPhotoUrl: String? = null
        var measuredHeartBeat = 0.0
        var currentSteps = 0
    }
    private val database by lazy { LocalAppDatabase.getDatabase(this) }
    val travelRouteRepository by lazy { TravelRouteRepository(database.travelRouteDao()) }

    override fun onCreate() {
        super.onCreate()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "location",
                "Location",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}