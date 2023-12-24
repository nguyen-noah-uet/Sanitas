package com.example.sanitas.data.step

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "step_table")
class Steps(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "user_email") val userEmail: String,
    @ColumnInfo(name = "steps") val stepsCount: Int,
    @ColumnInfo(name = "date") val date: LocalDate
)

data class StepTuple(
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "steps") val stepsCount: Int
)