package com.example.main_snake_game.game.logic

import com.example.main_snake_game.game.model.Direction
import com.example.main_snake_game.game.model.Food
import com.example.main_snake_game.game.model.FoodType
import com.example.main_snake_game.game.model.GameEvent
import com.example.main_snake_game.game.model.GameState
import com.example.main_snake_game.game.model.Point
import com.example.main_snake_game.game.model.Player
import com.example.main_snake_game.game.model.Snake
import kotlin.random.Random

class GameEngine(
    private val gridWidth: Int = 30,
    private val gridHeight: Int = 30,
    private val scoringEngine: ScoringEngine = ScoringEngine(),
    private val collisionDetector: CollisionDetector = CollisionDetector()
) {
    private val random = Random(System.currentTimeMillis())
    private val gameEvents = mutableListOf<GameEvent>()

    fun updateGameState(gameState: GameState): Pair<GameState, List<GameEvent>> {
        if (!gameState.isGameRunning || gameState.snakes.isEmpty()) {
            return gameState to emptyList()
        }

        gameEvents.clear()
        var updatedState = gameState.copy(currentTick = gameState.currentTick + 1)
        var snakes = updatedState.snakes.toMutableMap()
        var allPlayers = updatedState.allPlayers.toMutableMap()
        var food = updatedState.food

        // Move all snakes
        snakes = snakes.mapValues { (_, snake) ->
            moveSnake(snake)
        }.toMutableMap()

        // Check collisions and food eating
        val (newSnakes, newPlayers, newFood, events) = processCollisions(
            snakes,
            allPlayers,
            food ?: generateFood(snakes)
        )

        snakes = newSnakes.toMutableMap()
        allPlayers = newPlayers.toMutableMap()
        food = newFood
        gameEvents.addAll(events)

        // Remove dead snakes
        snakes = snakes.filterValues { it.isAlive }.toMutableMap()

        // Check if game is over
        val aliveSnakes = snakes.count { it.value.isAlive }
        val isGameOver = aliveSnakes <= 1

        updatedState = updatedState.copy(
            snakes = snakes,
            allPlayers = allPlayers,
            food = food,
            isGameRunning = !isGameOver
        )

        return updatedState to gameEvents
    }

    private fun moveSnake(snake: Snake): Snake {
        if (!snake.isAlive || snake.segments.isEmpty()) {
            return snake
        }

        val head = snake.head()
        val newHead = head + getDirectionVector(snake.nextDirection)
        val wrappedHead = wrapCoordinates(newHead)

        val newSegments = listOf(wrappedHead) + snake.segments.dropLast(1)

        return snake.copy(
            segments = newSegments,
            direction = snake.nextDirection
        )
    }

    private fun getDirectionVector(direction: Direction): Point {
        return when (direction) {
            Direction.UP -> Point(0, -1)
            Direction.DOWN -> Point(0, 1)
            Direction.LEFT -> Point(-1, 0)
            Direction.RIGHT -> Point(1, 0)
        }
    }

    private fun wrapCoordinates(point: Point): Point {
        return Point(
            x = ((point.x % gridWidth) + gridWidth) % gridWidth,
            y = ((point.y % gridHeight) + gridHeight) % gridHeight
        )
    }

    private fun processCollisions(
        snakes: Map<String, Snake>,
        players: Map<String, Player>,
        food: Food
    ): ProcessedCollisionsResult {
        var updatedSnakes = snakes
        var updatedPlayers = players
        var updatedFood = food
        val events = mutableListOf<GameEvent>()

        for ((playerId, snake) in snakes) {
            if (!snake.isAlive) continue

            val head = snake.head()

            // Check food collision
            if (collisionDetector.checkFoodCollision(head, updatedFood)) {
                val newSnake = snake.copy(segments = snake.segments + snake.tail())
                updatedSnakes = updatedSnakes + (playerId to newSnake)

                val points = if (updatedFood.type == FoodType.BONUS) 50 else 10
                val player = updatedPlayers[playerId]
                if (player != null) {
                    val updatedPlayer = player.copy(
                        score = player.score + points,
                        foodEaten = player.foodEaten + 1
                    )
                    updatedPlayers = updatedPlayers + (playerId to updatedPlayer)

                    events.add(
                        GameEvent.FoodEaten(
                            playerId = playerId,
                            playerName = snake.playerName,
                            points = points,
                            foodEatenCount = updatedPlayer.foodEaten
                        )
                    )
                }

                updatedFood = generateFood(updatedSnakes)
            }

            // Check self collision
            if (collisionDetector.checkSelfCollision(snake)) {
                val deadSnake = snake.copy(isAlive = false)
                updatedSnakes = updatedSnakes + (playerId to deadSnake)
                continue
            }

            // Check collision with other snakes
            for ((otherPlayerId, otherSnake) in snakes) {
                if (otherPlayerId == playerId || !otherSnake.isAlive) continue

                if (collisionDetector.checkSnakeCollision(head, otherSnake)) {
                    val points = 50 + (otherSnake.length() - 3).coerceAtLeast(0) * 5

                    val biterPlayer = updatedPlayers[playerId]
                    val bittenPlayer = updatedPlayers[otherPlayerId]

                    if (biterPlayer != null) {
                        val updatedBiter = biterPlayer.copy(
                            score = biterPlayer.score + points,
                            snakeBites = biterPlayer.snakeBites + 1
                        )
                        updatedPlayers = updatedPlayers + (playerId to updatedBiter)
                    }

                    if (bittenPlayer != null) {
                        val updatedBitten = bittenPlayer.copy(isAlive = false)
                        updatedPlayers = updatedPlayers + (otherPlayerId to updatedBitten)
                    }

                    val deadSnake = otherSnake.copy(isAlive = false)
                    updatedSnakes = updatedSnakes + (otherPlayerId to deadSnake)

                    events.add(
                        GameEvent.SnakeBit(
                            biterPlayerId = playerId,
                            biterPlayerName = snake.playerName,
                            bittenPlayerId = otherPlayerId,
                            bittenPlayerName = otherSnake.playerName,
                            points = points
                        )
                    )
                }
            }
        }

        return ProcessedCollisionsResult(updatedSnakes, updatedPlayers, updatedFood, events)
    }

    private fun generateFood(snakes: Map<String, Snake>): Food {
        val occupiedPositions = snakes.values.flatMap { it.segments }.toSet()
        var newPosition: Point
        do {
            newPosition = Point(
                random.nextInt(gridWidth),
                random.nextInt(gridHeight)
            )
        } while (occupiedPositions.contains(newPosition))

        val isBonusFood = random.nextFloat() < 0.2f
        return Food(
            position = newPosition,
            type = if (isBonusFood) FoodType.BONUS else FoodType.REGULAR
        )
    }

    fun changeDirection(playerId: String, gameState: GameState, newDirection: Direction): GameState {
        val snake = gameState.snakes[playerId] ?: return gameState

        // Prevent reversing into itself
        if (newDirection.opposite() == snake.direction) {
            return gameState
        }

        val updatedSnake = snake.copy(nextDirection = newDirection)
        return gameState.copy(
            snakes = gameState.snakes + (playerId to updatedSnake)
        )
    }

    private data class ProcessedCollisionsResult(
        val snakes: Map<String, Snake>,
        val players: Map<String, Player>,
        val food: Food,
        val events: List<GameEvent>
    )
}
