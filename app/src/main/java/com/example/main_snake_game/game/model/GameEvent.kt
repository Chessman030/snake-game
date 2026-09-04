package com.example.main_snake_game.game.model

import kotlinx.serialization.Serializable

@Serializable
sealed class GameEvent {
    @Serializable
    data class FoodEaten(
        val playerId: String,
        val playerName: String,
        val points: Int,
        val foodEatenCount: Int
    ) : GameEvent()

    @Serializable
    data class SnakeBit(
        val biterPlayerId: String,
        val biterPlayerName: String,
        val bittenPlayerId: String,
        val bittenPlayerName: String,
        val points: Int
    ) : GameEvent()

    @Serializable
    data class PlayerJoined(
        val playerId: String,
        val playerName: String,
        val timestamp: Long
    ) : GameEvent()

    @Serializable
    data class PlayerLeft(
        val playerId: String,
        val playerName: String,
        val timestamp: Long
    ) : GameEvent()

    @Serializable
    data class GameOver(
        val winners: List<String>,
        val timestamp: Long
    ) : GameEvent()

    @Serializable
    data class SnakeStateUpdate(
        val playerId: String,
        val snake: Snake,
        val timestamp: Long
    ) : GameEvent()
}
