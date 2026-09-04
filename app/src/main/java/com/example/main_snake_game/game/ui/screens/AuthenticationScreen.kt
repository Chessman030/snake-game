package com.example.main_snake_game.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AuthenticationScreen(
    onSignInAnonymously: () -> Unit,
    onSignInWithEmail: (String, String) -> Unit,
    onSignUpWithEmail: (String, String) -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUpMode by remember { mutableStateOf(false) }
    var useEmail by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SNAKE GAME",
            color = Color.Green,
            fontSize = 40.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        if (isLoading) {
            CircularProgressIndicator(color = Color.Green)
        } else if (useEmail) {
            // Email Authentication UI
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.padding(8.dp)
            )

            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.padding(8.dp)
            )

            Button(
                onClick = {
                    if (isSignUpMode) {
                        onSignUpWithEmail(email, password)
                    } else {
                        onSignInWithEmail(email, password)
                    }
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text(if (isSignUpMode) "Sign Up" else "Sign In")
            }

            TextButton(
                onClick = { isSignUpMode = !isSignUpMode },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = if (isSignUpMode) "Already have an account? Sign In" else "Don't have an account? Sign Up",
                    color = Color.Cyan,
                    fontSize = 12.sp
                )
            }

            Button(
                onClick = {
                    useEmail = false
                    email = ""
                    password = ""
                    isSignUpMode = false
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Back")
            }
        } else {
            // Main menu with Guest and Email options
            Button(
                onClick = onSignInAnonymously,
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Play as Guest", fontSize = 16.sp)
            }

            Button(
                onClick = { useEmail = true },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Sign In with Email", fontSize = 16.sp)
            }
        }

        errorMessage?.let {
            Text(
                text = it,
                color = Color.Red,
                modifier = Modifier.padding(top = 16.dp),
                fontSize = 12.sp
            )
        }
    }
}
