package com.example.sanitas.data.position

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.time.LocalDateTime

@Dao
interface TravelRouteDAO {
    @Query("SELECT lat, long, routeId FROM travel_route " +
            "WHERE date BETWEEN :dateBegin AND :dateEnd AND user_email = :email " +
            "ORDER BY routeId, ordering")
    suspend fun fetchRouteByDate(email: String, dateBegin: LocalDateTime, dateEnd: LocalDateTime): List<CoordinateTuple>

    @Query("SELECT MAX(routeId) FROM travel_route WHERE user_email = :email")
    suspend fun getMaxRouteId(email: String): Int?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTravelRoute(travelRoute: TravelRoute)
}

