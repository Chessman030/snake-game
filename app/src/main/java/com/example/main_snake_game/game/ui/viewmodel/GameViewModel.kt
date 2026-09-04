package com.example.main_snake_game.game.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.main_snake_game.data.repository.GameRepository
import com.example.main_snake_game.data.repository.LocationRepository
import com.example.main_snake_game.data.repository.PlayerRepository
import com.example.main_snake_game.game.logic.GameEngine
import com.example.main_snake_game.game.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameEngine: GameEngine,
    private val gameRepository: GameRepository,
    private val playerRepository: PlayerRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    companion object {
        private const val SINGLE_PLAYER_TICK_MS = 150L
        private const val GRID_WIDTH = 30
        private const val GRID_HEIGHT = 30
    }

    private val random = Random(System.currentTimeMillis())

    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState?> = _gameState

    private val _gameEvents = MutableStateFlow<List<GameEvent>>(emptyList())
    val gameEvents: StateFlow<List<GameEvent>> = _gameEvents

    private val _isGameRunning = MutableStateFlow(false)
    val isGameRunning: StateFlow<Boolean> = _isGameRunning

    private val _gameMode = MutableStateFlow<GameMode?>(null)
    val gameMode: StateFlow<GameMode?> = _gameMode

    private val _highScore = MutableStateFlow<Int?>(null)
    val highScore: StateFlow<Int?> = _highScore

    private var gameLoopJob: Job? = null
    private var currentPlayerId: String = ""

    fun loadUserHighScore(playerId: String) {
        currentPlayerId = playerId
        viewModelScope.launch {
            try {
                // Fetch score from Firebase via Repository
                val score = playerRepository.getUserHighScore(playerId)
                _highScore.value = score
            } catch (e: Exception) {
                _highScore.value = 0
            }
        }
    }

    // THIS FUNCTION WAS MISSING ITS SIGNATURE IN YOUR PREVIOUS CODE
    fun startSinglePlayerGame(playerId: String, playerName: String) {
        currentPlayerId = playerId
        _gameMode.value = GameMode.SINGLE_PLAYER

        val initialSnake = Snake(
            playerId = playerId,
            segments = listOf(
                Point(15, 15),
                Point(14, 15),
                Point(13, 15)
            ),
            playerName = playerName,
            direction = Direction.RIGHT,
            nextDirection = Direction.RIGHT,
            isAlive = true
        )

        val initialFood = generateRandomFood(listOf(initialSnake))

        val initialState = GameState(
            gameId = "single_${System.currentTimeMillis()}",
            mode = GameMode.SINGLE_PLAYER,
            snakes = mapOf(playerId to initialSnake),
            allPlayers = mapOf(playerId to Player(
                id = playerId,
                name = playerName,
                isAlive = true,
                isOnline = true
            )),
            food = initialFood,
            isGameRunning = true,
            gridWidth = GRID_WIDTH,
            gridHeight = GRID_HEIGHT,
            gameStartTime = System.currentTimeMillis()
        )

        _gameState.value = initialState
        _isGameRunning.value = true
        startSinglePlayerGameLoop()
    }

    private fun startSinglePlayerGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            while (_isGameRunning.value) {
                val currentState = _gameState.value
                if (currentState == null || !currentState.isGameRunning) break

                val updatedState = updateSinglePlayerGameState(currentState)
                _gameState.value = updatedState

                if (!updatedState.isGameRunning) {
                    _isGameRunning.value = false
                    endGame(updatedState)
                }
                delay(SINGLE_PLAYER_TICK_MS)
            }
        }
    }

    private fun updateSinglePlayerGameState(gameState: GameState): GameState {
        var snakes = gameState.snakes.toMutableMap()
        var allPlayers = gameState.allPlayers.toMutableMap()
        var food = gameState.food ?: generateRandomFood(snakes.values.toList())
        val events = mutableListOf<GameEvent>()

        val snake = snakes[currentPlayerId] ?: return gameState
        val movedSnake = moveSnake(snake)

        val head = movedSnake.head()
        val hitSelf = movedSnake.segments.drop(1).contains(head)

        if (hitSelf) {
            snakes[currentPlayerId] = movedSnake.copy(isAlive = false)
            return gameState.copy(isGameRunning = false, snakes = snakes)
        }

        if (head == food.position) {
            val lengthenedSnake = movedSnake.copy(
                segments = movedSnake.segments + snake.segments.last()
            )
            snakes[currentPlayerId] = lengthenedSnake

            val pointsEarned = if (food.type == FoodType.BONUS) 50 else 10
            allPlayers[currentPlayerId]?.let {
                allPlayers[currentPlayerId] = it.copy(
                    score = it.score + pointsEarned,
                    foodEaten = it.foodEaten + 1
                )
            }
            food = generateRandomFood(snakes.values.toList())
        } else {
            snakes[currentPlayerId] = movedSnake
        }

        return gameState.copy(
            snakes = snakes,
            allPlayers = allPlayers,
            food = food,
            currentTick = gameState.currentTick + 1
        )
    }

    private fun moveSnake(snake: Snake): Snake {
        val head = snake.head()
        val dir = snake.nextDirection

        val newHead = when (dir) {
            Direction.UP -> Point(head.x, head.y - 1)
            Direction.DOWN -> Point(head.x, head.y + 1)
            Direction.LEFT -> Point(head.x - 1, head.y)
            Direction.RIGHT -> Point(head.x + 1, head.y)
        }

        val wrappedHead = Point(
            x = (newHead.x + GRID_WIDTH) % GRID_WIDTH,
            y = (newHead.y + GRID_HEIGHT) % GRID_HEIGHT
        )

        val newSegments = listOf(wrappedHead) + snake.segments.dropLast(1)
        return snake.copy(segments = newSegments, direction = dir)
    }

    private fun generateRandomFood(snakes: List<Snake>): Food {
        val occupied = snakes.flatMap { it.segments }.toSet()
        var pos: Point
        do {
            pos = Point(random.nextInt(GRID_WIDTH), random.nextInt(GRID_HEIGHT))
        } while (occupied.contains(pos))

        return Food(pos, if (random.nextInt(100) < 20) FoodType.BONUS else FoodType.REGULAR)
    }

    fun changeDirection(newDir: Direction) {
        val state = _gameState.value ?: return
        val snake = state.snakes[currentPlayerId] ?: return

        val isOpposite = when {
            newDir == Direction.UP && snake.direction == Direction.DOWN -> true
            newDir == Direction.DOWN && snake.direction == Direction.UP -> true
            newDir == Direction.LEFT && snake.direction == Direction.RIGHT -> true
            newDir == Direction.RIGHT && snake.direction == Direction.LEFT -> true
            else -> false
        }

        if (!isOpposite) {
            val updatedSnakes = state.snakes.toMutableMap()
            updatedSnakes[currentPlayerId] = snake.copy(nextDirection = newDir)
            _gameState.value = state.copy(snakes = updatedSnakes)
        }
    }

    fun pauseGame() {
        _isGameRunning.value = false
    }

    fun resumeGame() {
        if (_gameState.value?.isGameRunning == true && !_isGameRunning.value) {
            _isGameRunning.value = true
            startSinglePlayerGameLoop()
        }
    }

    private fun endGame(finalState: GameState) {
        gameLoopJob?.cancel()

        // Calculate final score and update high score
        viewModelScope.launch {
            try {
                val finalScore = finalState.allPlayers[currentPlayerId]?.score ?: 0
                // Update high score in Firebase if current score is higher
                playerRepository.updateUserHighScore(currentPlayerId, finalScore)

                // Immediately refresh the local high score so it shows correctly next time
                val newHighScore = playerRepository.getUserHighScore(currentPlayerId)
                _highScore.value = newHighScore
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stopGame() {
        _isGameRunning.value = false
        gameLoopJob?.cancel()
        _gameState.value = null
    }
}