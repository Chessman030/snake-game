package com.example.main_snake_game.game.model

import kotlinx.serialization.Serializable

@Serializable
enum class GameMode {
    SINGLE_PLAYER, MULTI_PLAYER
}

@Serializable
data class GameState(
    val gameId: String = "",
    val mode: GameMode = GameMode.SINGLE_PLAYER,
    val snakes: Map<String, Snake> = emptyMap(),
    val food: Food? = null,
    val allPlayers: Map<String, Player> = emptyMap(),
    val isGameRunning: Boolean = false,
    val gridWidth: Int = 30,
    val gridHeight: Int = 30,
    val currentTick: Long = 0,
    val gameStartTime: Long = 0,
    val winner: Player? = null
)
