package com.example.sanitas.repositories

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.sanitas.data.step.Steps
import com.example.sanitas.data.step.StepsDAO
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
class StepsRepository(private val stepsDao: StepsDAO) {

    var oldSteps = 0

    suspend fun updateLocalStepById(userEmail: String, newStepsCount: Int, id: Int) {
        stepsDao.updateStepById(userEmail, newStepsCount, id)
    }

    suspend fun insertLocalNewSteps(steps: Steps) {
        stepsDao.insertNewStep(steps)
    }

    suspend fun fetchLocalStepsByDate(userEmail: String, date: LocalDate) = stepsDao.fetchStepsByDate(userEmail, date)

    suspend fun fetchOldSteps(userEmail: String) {
        val old = fetchLocalStepsByDate(userEmail, LocalDate.now())
        oldSteps = old?.stepsCount ?: 0
    }
}