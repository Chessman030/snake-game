package com.example.main_snake_game.game.logic

class ScoringEngine {
    companion object {
        const val REGULAR_FOOD_POINTS = 10
        const val BONUS_FOOD_POINTS = 50
        const val BASE_BITE_POINTS = 50
        const val BITE_LENGTH_MULTIPLIER = 5
    }

    fun calculateBitePoints(opponentSnakeLength: Int): Int {
        val extraPoints = (opponentSnakeLength - 3).coerceAtLeast(0) * BITE_LENGTH_MULTIPLIER
        return BASE_BITE_POINTS + extraPoints
    }
}
