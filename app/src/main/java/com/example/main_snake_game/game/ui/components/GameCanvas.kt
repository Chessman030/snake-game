package com.example.main_snake_game.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.main_snake_game.game.model.FoodType
import com.example.main_snake_game.game.model.GameState

@Composable
fun GameCanvas(
    gameState: GameState,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .border(2.dp, Color.White)
    ) {
        // Automatically calculate perfectly sized cells based on screen width
        val cellWidth = size.width / gameState.gridWidth
        val cellHeight = size.height / gameState.gridHeight

        // 1. Draw Food
        gameState.food?.let { food ->
            val foodColor = if (food.type == FoodType.BONUS) Color(0xFFFFD700) else Color.Red
            drawRect(
                color = foodColor,
                topLeft = Offset(food.position.x * cellWidth, food.position.y * cellHeight),
                size = Size(cellWidth, cellHeight)
            )
        }

        // 2. Draw Snakes
        gameState.snakes.values.forEach { snake ->
            val snakeColor = when (snake.playerId.hashCode() % 5) {
                0 -> Color.Green
                1 -> Color.Cyan
                2 -> Color.Yellow
                3 -> Color.Magenta
                else -> Color.Red
            }

            snake.segments.forEachIndexed { index, point ->
                // The head is White, the body is the snakeColor
                val segmentColor = if (index == 0) Color.White else snakeColor

                // Draw a slightly smaller rectangle for a "grid" effect (optional but looks nice)
                drawRect(
                    color = segmentColor,
                    topLeft = Offset(
                        x = (point.x * cellWidth) + 1f,
                        y = (point.y * cellHeight) + 1f
                    ),
                    size = Size(cellWidth - 2f, cellHeight - 2f)
                )
            }
        }
    }
}