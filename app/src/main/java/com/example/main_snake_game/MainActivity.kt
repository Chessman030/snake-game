package com.example.main_snake_game

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.main_snake_game.game.ui.screens.AuthenticationScreen
import com.example.main_snake_game.game.ui.screens.MenuScreen
import com.example.main_snake_game.game.ui.screens.MultiplayerLobbyScreen
import com.example.main_snake_game.game.ui.screens.SinglePlayerGameScreen
import com.example.main_snake_game.game.ui.viewmodel.AuthViewModel
import com.example.main_snake_game.game.ui.viewmodel.GameViewModel
import com.example.main_snake_game.game.ui.viewmodel.MultiplayerViewModel
import com.example.main_snake_game.ui.theme.Main_snake_gameTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val gameViewModel: GameViewModel by viewModels()
    private val multiplayerViewModel: MultiplayerViewModel by viewModels()

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        setContent {
            Main_snake_gameTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SnakeGameNavigation(
                        modifier = Modifier.padding(innerPadding),
                        authViewModel = authViewModel,
                        gameViewModel = gameViewModel,
                        multiplayerViewModel = multiplayerViewModel
                    )
                }
            }
        }
    }
}

@Composable
private fun SnakeGameNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    gameViewModel: GameViewModel,
    multiplayerViewModel: MultiplayerViewModel
) {
    val navController = rememberNavController()
    val currentUserId by authViewModel.currentUserId.collectAsState()
    val isAuthLoading by authViewModel.isLoading.collectAsState()
    val authError by authViewModel.errorMessage.collectAsState()
    val gameState by gameViewModel.gameState.collectAsState()
    val userHighScore by gameViewModel.highScore.collectAsState()
    val onlinePlayers by multiplayerViewModel.onlinePlayers.collectAsState()
    val isLocationReady by multiplayerViewModel.isLocationReady.collectAsState()

    var isPaused by remember { mutableStateOf(false) }

    NavHost(
        navController = navController,
        startDestination = if (currentUserId != null) "menu" else "auth",
        modifier = modifier
    ) {
        composable("auth") {
            AuthenticationScreen(
                onSignInAnonymously = { authViewModel.signInAnonymously() },
                onSignInWithEmail = { e, p -> authViewModel.signInWithEmail(e, p) },
                onSignUpWithEmail = { e, p -> authViewModel.signUpWithEmail(e, p) },
                isLoading = isAuthLoading,
                errorMessage = authError
            )

            LaunchedEffect(currentUserId) {
                if (currentUserId != null) {
                    gameViewModel.loadUserHighScore(currentUserId!!)
                    navController.navigate("menu") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            }
        }

        composable("menu") {
            MenuScreen(
                onSinglePlayer = { navController.navigate("singleplayer_game") },
                onMultiPlayer = {
                    currentUserId?.let { userId ->
                        multiplayerViewModel.startTrackingLocation(userId)
                        multiplayerViewModel.loadOnlinePlayers()
                    }
                    navController.navigate("multiplayer_lobby")
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("auth") {
                        popUpTo("menu") { inclusive = true }
                    }
                },
                highScore = userHighScore
            )
        }

        composable("singleplayer_game") {
            if (currentUserId != null) {
                LaunchedEffect(Unit) {
                    if (gameState == null) {
                        gameViewModel.startSinglePlayerGame(
                            playerId = currentUserId!!,
                            playerName = "Player_${currentUserId!!.takeLast(6)}"
                        )
                    }
                }

                SinglePlayerGameScreen(
                    gameState = gameState,
                    onDirectionChange = { gameViewModel.changeDirection(it) },
                    onPause = { gameViewModel.pauseGame(); isPaused = true },
                    onResume = { gameViewModel.resumeGame(); isPaused = false },
                    onStop = {
                        gameViewModel.stopGame()
                        isPaused = false
                        navController.navigate("menu") {
                            popUpTo("singleplayer_game") { inclusive = true }
                        }
                    },
                    isPaused = isPaused
                )
            }
        }

        composable("multiplayer_lobby") {
            MultiplayerLobbyScreen(
                onlinePlayers = onlinePlayers,
                isLocationReady = isLocationReady,
                onStartGame = { navController.navigate("multiplayer_game") },
                onBack = { navController.popBackStack() }
            )
        }

        composable("multiplayer_game") {
            SinglePlayerGameScreen(
                gameState = gameState,
                onDirectionChange = { gameViewModel.changeDirection(it) },
                onPause = { gameViewModel.pauseGame(); isPaused = true },
                onResume = { gameViewModel.resumeGame(); isPaused = false },
                onStop = {
                    gameViewModel.stopGame()
                    isPaused = false
                    navController.navigate("menu") {
                        popUpTo("multiplayer_game") { inclusive = true }
                    }
                },
                isPaused = isPaused
            )
        }
    }
}