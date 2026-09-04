package com.example.main_snake_game.game.model

import kotlinx.serialization.Serializable

@Serializable
enum class Direction {
    UP, DOWN, LEFT, RIGHT;

    fun opposite(): Direction = when (this) {
        UP -> DOWN
        DOWN -> UP
        LEFT -> RIGHT
        RIGHT -> LEFT
    }
}

@Serializable
data class Snake(
    val playerId: String,
    val segments: List<Point>,
    val direction: Direction = Direction.RIGHT,
    val nextDirection: Direction = Direction.RIGHT,
    val isAlive: Boolean = true,
    val playerName: String = ""
) {
    fun head(): Point = segments.firstOrNull() ?: Point(0, 0)
    fun tail(): Point = segments.lastOrNull() ?: Point(0, 0)
    fun length(): Int = segments.size
}
