package com.example.sanitas.repositories

import com.example.sanitas.data.position.TravelRoute
import com.example.sanitas.data.position.TravelRouteDAO
import java.time.LocalDateTime

class TravelRouteRepository(private val travelRouteDAO: TravelRouteDAO) {
    suspend fun fetchLocalTravelRouteByDate(dateBegin: LocalDateTime, dateEnd: LocalDateTime) = travelRouteDAO.fetchRouteByDate(dateBegin, dateEnd)

    suspend fun insertLocalRouteLocation(travelRoute: TravelRoute) {
        travelRouteDAO.insertTravelRoute(travelRoute)
    }

    suspend fun getLocalMaxRouteId() = travelRouteDAO.getMaxRouteId()
}