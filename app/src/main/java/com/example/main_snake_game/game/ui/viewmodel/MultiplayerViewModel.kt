package com.example.main_snake_game.game.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.main_snake_game.data.repository.LocationRepository
import com.example.main_snake_game.data.repository.PlayerRepository
import com.example.main_snake_game.data.local.entity.PlayerEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import kotlin.math.min

@HiltViewModel
class MultiplayerViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _onlinePlayers = MutableStateFlow<List<PlayerEntity>>(emptyList())
    val onlinePlayers: StateFlow<List<PlayerEntity>> = _onlinePlayers

    private val _currentPlayerLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val currentPlayerLocation: StateFlow<Pair<Double, Double>?> = _currentPlayerLocation

    private val _isLocationReady = MutableStateFlow(false)
    val isLocationReady: StateFlow<Boolean> = _isLocationReady

    private val _locationError = MutableStateFlow<String?>(null)
    val locationError: StateFlow<String?> = _locationError

    private val MAX_RETRIES = 3
    private val TIMEOUT_MS = 10000L // 10 second timeout for location acquisition
    private val PLAYER_REFRESH_INTERVAL = 2000L // Refresh online players every 2 seconds
    private var locationRetryCount = 0

    fun startTrackingLocation(playerId: String) {
        viewModelScope.launch {
            locationRetryCount = 0
            attemptLocationTracking(playerId)
        }
    }

    private suspend fun attemptLocationTracking(playerId: String) {
        try {
            _locationError.value = null

            // Try to get initial location with timeout
            val initialLocation = withTimeoutOrNull(TIMEOUT_MS) {
                locationRepository.getLastKnownLocation()
            }

            if (initialLocation != null) {
                _currentPlayerLocation.value = initialLocation
                _isLocationReady.value = true
                locationRetryCount = 0 // Reset retry count on success
            } else {
                handleLocationTimeout(playerId)
                return
            }

            // Collect continuous location updates with error handling
            locationRepository.getLocationUpdates()
                .catch { exception ->
                    _locationError.value = "Location update failed: ${exception.message}"
                    if (locationRetryCount < MAX_RETRIES) {
                        locationRetryCount++
                        delay(1000L * locationRetryCount) // Exponential backoff
                        attemptLocationTracking(playerId)
                    } else {
                        _locationError.value = "Location service unavailable after $MAX_RETRIES retries"
                        _isLocationReady.value = false
                    }
                }
                .collect { (locationPair, geohash) ->
                    val loc = locationRepository.getLastKnownLocation()
                    if (loc != null) {
                        _currentPlayerLocation.value = loc
                        _isLocationReady.value = true
                        _locationError.value = null

                        playerRepository.updatePlayerLocation(
                            playerId = playerId,
                            latitude = loc.first,
                            longitude = loc.second,
                            geohash = geohash
                        )
                    }
                }
        } catch (e: Exception) {
            _locationError.value = "Fatal error in location tracking: ${e.message}"
            _isLocationReady.value = false
        }
    }

    private suspend fun handleLocationTimeout(playerId: String) {
        if (locationRetryCount < MAX_RETRIES) {
            locationRetryCount++
            val backoffDelay = min(1000L * locationRetryCount, 5000L) // Max 5 second delay
            _locationError.value = "Location timeout - Retry attempt $locationRetryCount/$MAX_RETRIES"
            delay(backoffDelay)
            attemptLocationTracking(playerId)
        } else {
            _locationError.value = "Unable to acquire GPS signal after $MAX_RETRIES attempts. Check permissions and GPS settings."
            _isLocationReady.value = false
        }
    }

    fun loadOnlinePlayers() {
        viewModelScope.launch {
            try {
                // Start continuous polling with error handling
                while (true) {
                    try {
                        playerRepository.getAllPlayers().collect { players ->
                            _onlinePlayers.value = players.filter {
                                it.latitude != 0.0 && it.longitude != 0.0
                            }
                        }
                    } catch (e: Exception) {
                        _locationError.value = "Failed to load players: ${e.message}"
                    }
                    delay(PLAYER_REFRESH_INTERVAL)
                }
            } catch (e: Exception) {
                _locationError.value = "Player loading service error: ${e.message}"
            }
        }
    }

    fun stopTracking() {
        _isLocationReady.value = false
        locationRetryCount = 0
    }
}
