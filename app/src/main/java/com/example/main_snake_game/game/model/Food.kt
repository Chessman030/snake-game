package com.example.main_snake_game.game.model

import kotlinx.serialization.Serializable

@Serializable
enum class FoodType {
    REGULAR, BONUS
}

@Serializable
data class Food(
    val position: Point,
    val type: FoodType = FoodType.REGULAR
)
