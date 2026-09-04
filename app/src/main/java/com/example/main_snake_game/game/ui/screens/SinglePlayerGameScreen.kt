package com.example.main_snake_game.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.main_snake_game.game.model.Direction
import com.example.main_snake_game.game.model.GameState
import com.example.main_snake_game.game.ui.components.GameCanvas
import com.example.main_snake_game.game.ui.components.GameControls
import com.example.main_snake_game.game.ui.components.ScoreBoard

@Composable
fun SinglePlayerGameScreen(
    gameState: GameState?,
    onDirectionChange: (Direction) -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    isPaused: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(8.dp)
    ) {
        ScoreBoard(gameState, modifier = Modifier.padding(8.dp))

        if (gameState != null) {
            // This wrapper Box ensures the Canvas stays perfectly centered and square
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                GameCanvas(
                    gameState = gameState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f) // Forces the grid to be a perfect square
                        .padding(8.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Waiting for game to start...",
                    color = Color.White,
                    fontSize = 20.sp
                )
            }
        }

        GameControls(
            onDirectionChange = onDirectionChange,
            onPause = onPause,
            onResume = onResume,
            onStop = onStop,
            isPaused = isPaused
        )
    }
}