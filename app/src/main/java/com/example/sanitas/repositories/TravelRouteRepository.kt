package com.example.sanitas.repositories

import com.example.sanitas.data.position.TravelRoute
import com.example.sanitas.data.position.TravelRouteDAO

class TravelRouteRepository(private val travelRouteDAO: TravelRouteDAO) {
    suspend fun fetchLocalTravelRouteById(routeId: Int) = travelRouteDAO.fetchRouteByRouteID(routeId)

    suspend fun insertLocalRouteLocation(travelRoute: TravelRoute) {
        travelRouteDAO.insertTravelRoute(travelRoute)
    }

    suspend fun getLocalMaxRouteId() = travelRouteDAO.getMaxRouteId()
}