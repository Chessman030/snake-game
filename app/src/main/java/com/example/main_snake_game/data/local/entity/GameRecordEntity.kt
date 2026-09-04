package com.example.main_snake_game.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_records")
data class GameRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val gameId: String,
    val playerId: String,
    val playerName: String,
    val score: Int,
    val foodEaten: Int,
    val snakeBites: Int,
    val gameMode: String,
    val duration: Long,
    val isWinner: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
