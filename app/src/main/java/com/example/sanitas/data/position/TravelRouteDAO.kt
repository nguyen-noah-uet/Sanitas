package com.example.sanitas.data.position

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TravelRouteDAO {
    @Query("SELECT lat, long FROM travel_route WHERE routeId = :routeID ORDER BY ordering")
    suspend fun fetchRouteByRouteID(routeID: Int): List<CoordinateTuple>

    @Query("SELECT MAX(routeId) FROM travel_route")
    suspend fun getMaxRouteId(): Int?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTravelRoute(travelRoute: TravelRoute)
}

