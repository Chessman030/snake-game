package com.example.main_snake_game.game.model

import kotlinx.serialization.Serializable

@Serializable
data class Player(
    val id: String,
    val name: String,
    val score: Int = 0,
    val foodEaten: Int = 0,
    val snakeBites: Int = 0,
    val isOnline: Boolean = false,
    val isAlive: Boolean = true,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val geohash: String = ""
)
