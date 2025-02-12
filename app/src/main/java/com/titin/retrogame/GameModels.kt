package com.titin.retrogame

data class MovementVector(
    val fromX: Int,
    val fromY: Int,
    val toX: Int,
    val toY: Int
)

// Data Classes
data class GameState(
    val playerX: Int,
    val playerY: Int,
    val obstacleX: Int,
    val targets: List<Target>,
    val score: Int,
    val elapsedTime: Long
)

data class Target(
    val x: Int,
    val y: Int,
    var isActive: Boolean = true
)

data class Star(
    var x: Float,
    var y: Float,
    var size: Float,
    var alpha: Int,
    var speed: Float
)

data class Nebula(
    var x: Float,
    var y: Float,
    var radius: Float,
    var color: Int,
    var alpha: Int
)
enum class SoundEffect {
    COLLISION,
    GAME_OVER,
    START_GAME
}
