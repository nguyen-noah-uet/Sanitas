package com.example.sanitas.repositories

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.sanitas.data.step.Steps
import com.example.sanitas.data.step.StepsDAO
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
class StepsRepository(private val stepsDao: StepsDAO) {

    var oldSteps = 0

    suspend fun updateLocalStepById(newStepsCount: Int, id: Int) {
        stepsDao.updateStepById(newStepsCount, id)
    }

    suspend fun insertLocalNewSteps(steps: Steps) {
        stepsDao.insertNewStep(steps)
    }

    suspend fun fetchLocalStepsByDate(date: LocalDate) = stepsDao.fetchStepsByDate(date)

    suspend fun fetchOldSteps() {
        val old = fetchLocalStepsByDate(LocalDate.now())
        oldSteps = old?.stepsCount ?: 0
    }
}