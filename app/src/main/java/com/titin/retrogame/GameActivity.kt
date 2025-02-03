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

class GameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGameBinding

    // Properties - UI Components
    private lateinit var gameView: GameView

    // Properties - Game State
    private val gameState = MutableLiveData<GameState>()
    private var gameRunning = false
    private var score = 0
    private var moveDelay = 500L
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

    // Activity Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupWindow()

        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeViews()
        setupGame()
    }

    override fun onDestroy() {
        super.onDestroy()
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

        // Set up control buttons
        fun setupControls() {
            binding.apply {
                leftButton.setOnClickListener { if (gameRunning) movePlayerLeft() }
                rightButton.setOnClickListener { if (gameRunning) movePlayerRight() }
                startButton.setOnClickListener { startGameWithEffects() }
            }
        }
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
        handler.post(gameLoop)
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

        updateGameState()
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



    private fun updateObstacles() {
        val currentTime = System.currentTimeMillis()
        if ((currentTime - startTime) / moveDelay % 2 == 0L) {
            obstacleX = (obstacleX + 1) % 8
        }
    }

}