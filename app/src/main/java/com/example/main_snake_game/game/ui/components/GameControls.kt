package com.example.main_snake_game.game.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.main_snake_game.game.model.Direction

@Composable
fun GameControls(
    onDirectionChange: (Direction) -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    isPaused: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Direction controls - D-Pad style
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { onDirectionChange(Direction.UP) },
                modifier = Modifier.size(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
            ) {
                Text("↑", color = Color.White)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { onDirectionChange(Direction.LEFT) },
                modifier = Modifier.size(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
            ) {
                Text("←", color = Color.White)
            }
            Button(
                onClick = { onDirectionChange(Direction.DOWN) },
                modifier = Modifier
                    .size(60.dp)
                    .padding(horizontal = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
            ) {
                Text("↓", color = Color.White)
            }
            Button(
                onClick = { onDirectionChange(Direction.RIGHT) },
                modifier = Modifier.size(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
            ) {
                Text("→", color = Color.White)
            }
        }

        // Game controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (isPaused) {
                Button(onClick = onResume) {
                    Text("Resume")
                }
            } else {
                Button(onClick = onPause) {
                    Text("Pause")
                }
            }

            Button(
                onClick = onStop,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Stop", color = Color.White)
            }
        }
    }
}
