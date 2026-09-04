package com.example.main_snake_game.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PlayerDTO(
    val id: String,
    val name: String,
    val score: Int = 0,
    val foodEaten: Int = 0,
    val snakeBites: Int = 0,
    val isOnline: Boolean = false,
    val isAlive: Boolean = true,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val geohash: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
)

@Serializable
data class GameEventDTO(
    val type: String,
    val playerId: String,
    val playerName: String,
    val data: Map<String, String> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class ScoreDTO(
    val playerId: String,
    val playerName: String,
    val score: Int,
    val rank: Int
)
