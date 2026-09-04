package com.example.main_snake_game.game.model

import kotlinx.serialization.Serializable

@Serializable
data class Point(
    val x: Int,
    val y: Int
) {
    operator fun plus(other: Point) = Point(x + other.x, y + other.y)
    operator fun minus(other: Point) = Point(x - other.x, y - other.y)

    fun distance(other: Point): Float {
        return kotlin.math.sqrt(
            ((x - other.x) * (x - other.x) + (y - other.y) * (y - other.y)).toFloat()
        )
    }
}
