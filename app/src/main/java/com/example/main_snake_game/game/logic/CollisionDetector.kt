package com.example.main_snake_game.game.logic

import com.example.main_snake_game.game.model.Point
import com.example.main_snake_game.game.model.Food
import com.example.main_snake_game.game.model.Snake

class CollisionDetector {

    fun checkFoodCollision(snakeHead: Point, food: Food): Boolean {
        return snakeHead == food.position
    }

    fun checkSelfCollision(snake: Snake): Boolean {
        if (snake.segments.size < 4) return false

        val head = snake.head()
        val body = snake.segments.drop(1)

        return body.contains(head)
    }

    fun checkSnakeCollision(head: Point, otherSnake: Snake): Boolean {
        return otherSnake.segments.any { it == head }
    }

    fun checkWallCollision(head: Point, gridWidth: Int, gridHeight: Int): Boolean {
        return head.x < 0 || head.x >= gridWidth || head.y < 0 || head.y >= gridHeight
    }
}
