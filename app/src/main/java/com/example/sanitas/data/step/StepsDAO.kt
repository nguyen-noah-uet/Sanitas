package com.example.sanitas.data.step

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.time.LocalDate

@Dao
interface StepsDAO {
    @Query("UPDATE step_table SET steps = :newSteps WHERE id = :stepId")
    suspend fun updateStepById(newSteps: Int, stepId: Int)

    @Insert
    suspend fun insertNewStep(steps: Steps)

    @Query("SELECT id, steps FROM step_table WHERE date = :thisDate")
    suspend fun fetchStepsByDate(thisDate: LocalDate): StepTuple?
}