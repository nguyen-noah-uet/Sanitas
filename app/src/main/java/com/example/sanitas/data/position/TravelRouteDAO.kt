package com.example.sanitas.data.position

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.time.LocalDateTime

@Dao
interface TravelRouteDAO {
    @Query("SELECT lat, long, routeId FROM travel_route WHERE date BETWEEN :dateBegin AND :dateEnd ORDER BY routeId, ordering")
    suspend fun fetchRouteByDate(dateBegin: LocalDateTime, dateEnd: LocalDateTime): List<CoordinateTuple>

    @Query("SELECT MAX(routeId) FROM travel_route")
    suspend fun getMaxRouteId(): Int?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTravelRoute(travelRoute: TravelRoute)
}

