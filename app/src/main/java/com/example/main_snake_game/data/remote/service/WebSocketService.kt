package com.example.main_snake_game.data.remote.service

import com.example.main_snake_game.game.model.GameEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

class WebSocketService {
    private val eventFlow = MutableSharedFlow<GameEvent>(replay = 0)
    private val json = Json { ignoreUnknownKeys = true }

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private val _connectionError = MutableStateFlow<String?>(null)
    val connectionError: StateFlow<String?> = _connectionError

    private val activeConnections = ConcurrentHashMap<String, ConnectionInfo>()
    private val MAX_RECONNECT_ATTEMPTS = 5
    private var reconnectAttempts = 0

    enum class ConnectionState {
        DISCONNECTED, CONNECTING, CONNECTED, ERROR, RECONNECTING
    }

    data class ConnectionInfo(
        val gameId: String,
        val playerId: String,
        val connectedAt: Long = System.currentTimeMillis(),
        var reconnectAttempts: Int = 0
    )

    fun observeGameEvents(): Flow<GameEvent> = eventFlow

    suspend fun sendGameEvent(event: GameEvent) {
        if (_connectionState.value != ConnectionState.CONNECTED) {
            _connectionError.value = "Cannot send event: Not connected to WebSocket"
            return
        }
        try {
            eventFlow.emit(event)
        } catch (e: Exception) {
            _connectionError.value = "Failed to send game event: ${e.message}"
        }
    }

    suspend fun connect(gameId: String, playerId: String) {
        try {
            _connectionState.value = ConnectionState.CONNECTING
            _connectionError.value = null

            // Check if already connected
            if (activeConnections.containsKey(gameId)) {
                _connectionState.value = ConnectionState.CONNECTED
                return
            }

            // WebSocket connection logic would go here
            // For MVP, using in-memory event flow
            val connectionInfo = ConnectionInfo(
                gameId = gameId,
                playerId = playerId,
                reconnectAttempts = 0
            )

            activeConnections[gameId] = connectionInfo
            _connectionState.value = ConnectionState.CONNECTED
            reconnectAttempts = 0

        } catch (e: Exception) {
            handleConnectionError(gameId, e)
        }
    }

    fun disconnect() {
        try {
            activeConnections.clear()
            _connectionState.value = ConnectionState.DISCONNECTED
            _connectionError.value = null
            reconnectAttempts = 0
        } catch (e: Exception) {
            _connectionError.value = "Error during disconnect: ${e.message}"
        }
    }

    suspend fun disconnect(gameId: String) {
        try {
            activeConnections.remove(gameId)
            if (activeConnections.isEmpty()) {
                _connectionState.value = ConnectionState.DISCONNECTED
            }
        } catch (e: Exception) {
            _connectionError.value = "Error disconnecting from game: ${e.message}"
        }
    }

    private suspend fun handleConnectionError(gameId: String, exception: Exception) {
        val connectionInfo = activeConnections[gameId]
        if (connectionInfo != null) {
            connectionInfo.reconnectAttempts++

            if (connectionInfo.reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                _connectionState.value = ConnectionState.RECONNECTING
                _connectionError.value = "Connection failed, attempting to reconnect (${connectionInfo.reconnectAttempts}/$MAX_RECONNECT_ATTEMPTS)"

                // Exponential backoff: 1s, 2s, 4s, 8s, 16s
                val backoffDelay = 1000L * (1 shl connectionInfo.reconnectAttempts)
                kotlinx.coroutines.delay(backoffDelay)

                // Attempt reconnect
                connect(gameId, connectionInfo.playerId)
            } else {
                _connectionState.value = ConnectionState.ERROR
                _connectionError.value = "WebSocket connection failed after ${MAX_RECONNECT_ATTEMPTS} attempts: ${exception.message}"
                activeConnections.remove(gameId)
            }
        } else {
            _connectionState.value = ConnectionState.ERROR
            _connectionError.value = "Connection error: ${exception.message}"
        }
    }

    fun isConnected(): Boolean = _connectionState.value == ConnectionState.CONNECTED

    fun getActiveConnections(): Int = activeConnections.size
}
