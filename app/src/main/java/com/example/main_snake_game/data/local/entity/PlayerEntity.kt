package com.example.main_snake_game.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val totalScore: Int = 0,
    val totalFoodEaten: Int = 0,
    val totalBites: Int = 0,
    val gamesPlayed: Int = 0,
    val gameWins: Int = 0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val geohash: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
)
