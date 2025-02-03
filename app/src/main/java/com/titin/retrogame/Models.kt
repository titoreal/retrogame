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
