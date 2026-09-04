package com.example.main_snake_game.data.repository

import com.example.main_snake_game.data.local.dao.PlayerDao
import com.example.main_snake_game.data.local.entity.PlayerEntity
import com.example.main_snake_game.data.remote.service.FirebaseService
import kotlinx.coroutines.flow.Flow

class PlayerRepository(
    private val playerDao: PlayerDao,
    private val firebaseService: FirebaseService
) {

    fun getTopPlayers(limit: Int): Flow<List<PlayerEntity>> {
        return playerDao.getTopPlayers(limit)
    }

    fun getAllPlayers(): Flow<List<PlayerEntity>> {
        return playerDao.getAllPlayers()
    }

    suspend fun savePlayer(player: PlayerEntity) {
        playerDao.insertPlayer(player)
    }

    suspend fun updatePlayer(player: PlayerEntity) {
        playerDao.updatePlayer(player)
    }

    suspend fun getPlayerById(playerId: String): PlayerEntity? {
        return playerDao.getPlayerById(playerId)
    }

    suspend fun updatePlayerOnlineStatus(playerId: String, isOnline: Boolean) {
        firebaseService.updatePlayerOnlineStatus(playerId, isOnline)
    }

    suspend fun updatePlayerLocation(playerId: String, latitude: Double, longitude: Double, geohash: String) {
        firebaseService.updatePlayerLocation(playerId, latitude, longitude, geohash)
        val player = playerDao.getPlayerById(playerId)
        if (player != null) {
            playerDao.updatePlayer(
                player.copy(latitude = latitude, longitude = longitude, geohash = geohash)
            )
        }
    }

    /**
     * Create or update user profile in Firebase with high score
     */
    suspend fun createUserProfile(
        userId: String,
        name: String,
        email: String? = null,
        highScore: Int = 0
    ) {
        firebaseService.createOrUpdateUserProfile(userId, name, email, highScore)
    }

    /**
     * Get user's current high score from Firebase
     */
    suspend fun getUserHighScore(userId: String): Int {
        return firebaseService.getUserHighScore(userId)
    }

    /**
     * Update user's high score if new score is higher
     */
    suspend fun updateUserHighScore(userId: String, newScore: Int): Boolean {
        return firebaseService.updateUserHighScore(userId, newScore)
    }
}
