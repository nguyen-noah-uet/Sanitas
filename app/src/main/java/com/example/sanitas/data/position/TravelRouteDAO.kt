package com.example.sanitas.data.position

import android.location.Location
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TravelRouteDAO {
    @Query("SELECT location FROM travel_route WHERE routeId = routeID")
    suspend fun fetchRouteByRouteID(routeID: Int): ArrayList<Location>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLocationForRoute()
}