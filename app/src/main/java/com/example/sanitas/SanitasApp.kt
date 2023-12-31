package com.example.sanitas

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.sanitas.data.LocalAppDatabase
import com.example.sanitas.data.step.Steps
import com.example.sanitas.repositories.StepsRepository
import com.example.sanitas.repositories.TravelRouteRepository
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
class SanitasApp: Application() {
    companion object {
        var userDisplayName: String? = null
        var userEmail: String? = "local"
        var userPhotoUrl: String? = null
        var measuredHeartBeat = 0.0
        var currentSteps = 0
    }
    private val database by lazy { LocalAppDatabase.getDatabase(this) }
    val travelRouteRepository by lazy { TravelRouteRepository(database.travelRouteDao()) }
    val stepsRepository by lazy { StepsRepository(database.stepsDao()) }

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

        startDataInsertCoroutine()
    }


    @OptIn(DelicateCoroutinesApi::class)
    private fun startDataInsertCoroutine() {
        GlobalScope.launch(Dispatchers.Main) {
            val today = LocalDate.now()
            while (true) {
                delay(5000)
                if (userEmail != null) {
                    val old = stepsRepository.fetchLocalStepsByDate(userEmail!!, today)
                    if (old == null) {
                        val newSteps = Steps(
                            stepsCount = currentSteps,
                            date = today,
                            userEmail = userEmail!!
                        )
                        stepsRepository.insertLocalNewSteps(newSteps)
                    } else {
                        stepsRepository.updateLocalStepById(
                            userEmail!!,
                            currentSteps + stepsRepository.oldSteps,
                            old.id
                        )
                    }
                }
            }
        }
    }
}