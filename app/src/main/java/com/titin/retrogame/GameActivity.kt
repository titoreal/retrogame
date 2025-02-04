package com.titin.retrogame

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.titin.retrogame.databinding.ActivityGameBinding
import java.util.Collections
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class GameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGameBinding

    // Properties - UI Components
    private lateinit var gameView: GameView

    // Properties - Game State
    private val gameState = MutableLiveData<GameState>()
    private var gameRunning = false
    private var score = 0
    private var moveDelay = 500L
    private val maxTargets = 15
    private var remainingTargets = maxTargets
    private var startTime = 0L

    // Properties - Player and Obstacles
    private var playerX = 4
    private var playerY = 10
    private var obstacleX = 0
    private var lastPlayerX = 4
    private var lastPlayerY = 10

    // Properties - Collections and Handlers
    private val handler = Handler(Looper.getMainLooper())
    private val targets = Collections.synchronizedList(mutableListOf<Target>())


    // Game Loop
    private val gameLoop = object : Runnable {
        override fun run() {
            if (gameRunning) {
                updateGame()
                handler.postDelayed(this, moveDelay)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupWindow()

        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeViews()
        setupGame()
    }

    // Initialization Methods
    private fun setupWindow() {
        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = ContextCompat.getColor(this@GameActivity, R.color.background_color)
        }
    }

    private fun initializeViews() {
        gameView = binding.gameView

    }

    private fun showStartButton() {
        binding.startButton.visibility = View.VISIBLE
    }

    private fun hideStartButton() {
        binding.startButton.visibility = View.GONE
    }

    private fun setupGame() {
        setupGameState()
        setupControls()
        showStartScreen()
    }

    private fun setupGameState() {
        gameState.observe(this) { state ->
            gameView.updateState(state)
            updateUI(state)
        }
    }

    private fun setupControls() {
        binding.apply {
            leftButton.setOnClickListener { if (gameRunning) movePlayerLeft() }
            rightButton.setOnClickListener { if (gameRunning) movePlayerRight() }
            startButton.setOnClickListener { startGameWithEffects() }
        }
    }

    // Game State Management
    private fun startGameWithEffects() {
        hideStartButton()  // Usar el método auxiliar
        startGame()
    }

    private fun startGame() {
        initializeGameState()
        generateTargets()
        handler.post(gameLoop)
    }
    private fun generateTargets() {
        synchronized(targets) {
            targets.clear()
            val availablePositions = generateAvailablePositions()
            availablePositions.shuffle()
            repeat(maxTargets) {
                if (availablePositions.isNotEmpty()) {
                    val pos = availablePositions.removeAt(0)
                    targets.add(Target(x = pos.first, y = pos.second))
                }
            }
            remainingTargets = maxTargets
        }
    }

    private fun generateAvailablePositions(): MutableList<Pair<Int, Int>> {
        val positions = mutableListOf<Pair<Int, Int>>()
        for (x in 0..7) {
            for (y in 2..10) {
                positions.add(Pair(x, y))
            }
        }
        return positions
    }
    private fun initializeGameState() {
        gameRunning = true
        score = 0
        moveDelay = 500L
        playerX = 4
        playerY = 10
        startTime = System.currentTimeMillis()
    }

    private fun showStartScreen() {
        resetGameState()
        gameView.resetAnimations()
    }

    private fun resetGameState() {
        gameRunning = false
        showStartButton()  // Usar el método auxiliar
        score = 0
        playerX = 4
        playerY = 10
        obstacleX = 0
        targets.clear()
        updateGameState()
    }

    // Game Logic - Movement and Updates
    private fun updateGame() {
        val movement = calculateNextMovement()

        checkCollisionsWithMovement(movement)
        updateLastPosition()
        updateGameState()
    }

    private fun calculateNextMovement(): MovementVector {
        val previousX = playerX
        val previousY = playerY
        playerY = if (playerY <= 0) 10 else playerY - 1
        return MovementVector(previousX, previousY, playerX, playerY)
    }

    private fun updateLastPosition() {
        lastPlayerX = playerX
        lastPlayerY = playerY
    }

    private fun movePlayerLeft() {
        if (!gameRunning) return
        val previousX = playerX
        playerX = (playerX - 1 + 8) % 8
        handlePlayerMovement(MovementVector(previousX, playerY, playerX, playerY))
    }

    private fun movePlayerRight() {
        if (!gameRunning) return
        val previousX = playerX
        playerX = (playerX + 1) % 8
        handlePlayerMovement(MovementVector(previousX, playerY, playerX, playerY))
    }

    private fun handlePlayerMovement(movement: MovementVector) {
        checkCollisionsWithMovement(movement)
        updateGameState()
    }

    // Collision Detection
    private fun checkCollisionsWithMovement(movement: MovementVector) {
        checkTargetCollisions(movement)
    }

    private fun checkTargetCollisions(movement: MovementVector) {
        val targetsToDeactivate = synchronized(targets) {
            targets.filter { it.isActive && isCollisionOnPath(movement, it) }
        }
        targetsToDeactivate.forEach { handleTargetCollision(it) }
    }

    private fun isCollisionOnPath(movement: MovementVector, target: Target): Boolean {
        if (movement.toX == target.x && movement.toY == target.y) return true
        if (movement.fromX == movement.toX) return false

        val adjustedDeltaX = calculateAdjustedDeltaX(movement.toX - movement.fromX)
        return isPointOnDiagonalPath(
            movement.fromX, movement.fromY,
            movement.toX, movement.toY,
            target.x, target.y,
            adjustedDeltaX
        )
    }
    private fun isPointOnDiagonalPath(
        fromX: Int, fromY: Int,
        toX: Int, toY: Int,
        targetX: Int, targetY: Int,
        adjustedDeltaX: Int
    ): Boolean {

        val adjustedTargetX = when {
            targetX - fromX > 4 -> targetX - 8
            targetX - fromX < -4 -> targetX + 8
            else -> targetX
        }
        val minX = min(fromX, fromX + adjustedDeltaX)
        val maxX = max(fromX, fromX + adjustedDeltaX)
        val minY = min(fromY, toY)
        val maxY = max(fromY, toY)

        if (adjustedTargetX < minX || adjustedTargetX > maxX ||
            targetY < minY || targetY > maxY) {
            return false
        }

        val moveRatioX = (adjustedTargetX - fromX).toFloat() / adjustedDeltaX
        val expectedY = fromY + (toY - fromY) * moveRatioX

        return abs(targetY - expectedY) <= 0.5f
    }

    private fun calculateAdjustedDeltaX(deltaX: Int): Int = when {
        deltaX > 4 -> deltaX - 8
        deltaX < -4 -> deltaX + 8
        else -> deltaX
    }

    private fun handleTargetCollision(target: Target) {
        updateScore()
        deactivateTarget(target)
        showCollisionEffects()
        checkAndRegenerateTargets()
    }

    private fun updateScore() {
        score += 10
        remainingTargets--
    }

    private fun deactivateTarget(target: Target) {
        target.isActive = false
    }

    private fun showCollisionEffects() {

        val collisionX = playerX * gameView.getCellWidth() + gameView.getCellWidth() / 2
        val collisionY = playerY * gameView.getCellHeight() + gameView.getCellHeight() / 2
        gameView.showCollisionAnimation(collisionX, collisionY)
    }

    private fun checkAndRegenerateTargets() {
        if (remainingTargets == 0) {
            moveDelay = max(100L, moveDelay - 50)
            generateTargets()
        }
    }

    // Game State Updates
    private fun updateGameState() {
        gameState.value = GameState(
            playerX = playerX,
            playerY = playerY,
            obstacleX = obstacleX,
            targets = targets.toList(),
            score = score,
            elapsedTime = (System.currentTimeMillis() - startTime) / 1000
        )
    }

    private fun updateUI(state: GameState) {
        binding.apply {
            scoreText.text = "Score: ${state.score}"
            timeText.text = "Time: ${state.elapsedTime}s"
        }
    }



}