package com.example.main_snake_game.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.main_snake_game.game.model.GameState

@Composable
fun ScoreBoard(gameState: GameState?, modifier: Modifier = Modifier) {
    if (gameState == null) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.DarkGray)
            .padding(16.dp)
    ) {
        gameState.allPlayers.values.forEach { player ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = player.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Score: ${player.score}",
                    color = Color.Green,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Food: ${player.foodEaten}",
                    color = Color.Yellow,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Bites: ${player.snakeBites}",
                    color = Color.Cyan,
                    fontSize = 12.sp
                )
            }
        }
    }
}
