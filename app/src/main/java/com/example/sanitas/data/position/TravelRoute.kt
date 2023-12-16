package com.example.sanitas.data.position

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "travel_route")
class TravelRoute(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "routeId") val routeId: Int,
    @ColumnInfo(name = "ordering") val ordering: Int,
    @ColumnInfo(name = "lat") val latitude: Double,
    @ColumnInfo(name = "long") val longitude: Double
)

data class CoordinateTuple(
    @ColumnInfo(name = "lat") val latitude: Double,
    @ColumnInfo(name = "long") val longitude: Double
)