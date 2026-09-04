package com.example.main_snake_game.data.repository

import android.location.Location
import com.example.main_snake_game.data.remote.service.LocationService
import com.example.main_snake_game.util.GeoHashUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay

class LocationRepository(
    private val locationService: LocationService
) {

    companion object {
        // DEBUG MODE: Set to true to use mock location for testing
        var DEBUG_MODE = false
        var MOCK_LATITUDE = 37.7749
        var MOCK_LONGITUDE = -122.4194
    }

    fun getLocationUpdates(): Flow<Pair<Pair<Double, Double>, String>> {
        return if (DEBUG_MODE) {
            // Debug mode: emit mock location updates every 1 second for continuous testing
            flow {
                while (true) {
                    val mockLocation = Pair(MOCK_LATITUDE, MOCK_LONGITUDE)
                    val geohash = GeoHashUtils.encode(MOCK_LATITUDE, MOCK_LONGITUDE, precision = 6)
                    emit(Pair(mockLocation, geohash))
                    delay(1000) // Emit update every second
                }
            }
        } else {
            // Production mode: use real GPS from location service
            locationService.getLocationUpdates().map { location ->
                val geohash = GeoHashUtils.encode(location.latitude, location.longitude, precision = 6)
                Pair(Pair(location.latitude, location.longitude), geohash)
            }
        }
    }

    suspend fun getLastKnownLocation(): Pair<Double, Double>? {
        return if (DEBUG_MODE) {
            // Debug mode: return mock location immediately
            Pair(MOCK_LATITUDE, MOCK_LONGITUDE)
        } else {
            // Production mode: get real location
            val location = locationService.getLastKnownLocation()
            location?.let {
                Pair(it.latitude, it.longitude)
            }
        }
    }

    // Helper function to enable debug mode for testing
    fun enableDebugMode(latitude: Double = 37.7749, longitude: Double = -122.4194) {
        DEBUG_MODE = true
        MOCK_LATITUDE = latitude
        MOCK_LONGITUDE = longitude
    }

    // Helper function to disable debug mode and use real GPS
    fun disableDebugMode() {
        DEBUG_MODE = false
    }
}

