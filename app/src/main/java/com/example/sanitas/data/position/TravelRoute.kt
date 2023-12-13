package com.example.sanitas.data.position

import android.location.Location
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "travel_route")
class TravelRoute(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "routeId") val routeId: Int,
    @ColumnInfo(name = "order") val order: Int,
    @ColumnInfo(name = "location") val position: Location
)