package com.titin.retrogame

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Properties
    private var cellWidth = 0f
    private var cellHeight = 0f
    private var gameState: GameState? = null

    // Animation Properties
    private var playerRotation = 0f
    private var playerScale = 1f

    // Visual Elements Collections
    private val playerPath = Path()
    private val paints = setupPaints()

    init {
        setupPlayerPath()
    }

    // Paint Setup
    private fun setupPaints() = object {
        val player = Paint().apply {
            style = Paint.Style.FILL
            shader = LinearGradient(
                0f, -Constants.PLAYER_PATH_SIZE, 0f, Constants.PLAYER_PATH_SIZE,
                Color.parseColor("#4CAF50"),
                Color.parseColor("#81C784"),
                Shader.TileMode.CLAMP
            )
        }
        val playerGlow = Paint().apply {
            style = Paint.Style.STROKE
            color = Color.parseColor("#A5D6A7")
            strokeWidth = 2f
            maskFilter = BlurMaskFilter(10f, BlurMaskFilter.Blur.NORMAL)
        }

        val grid = Paint().apply {
            style = Paint.Style.STROKE
            color = Color.parseColor("#30FFFFFF")
            strokeWidth = 1f
        }
    }

    private fun setupPlayerPath() {
        playerPath.apply {
            moveTo(0f, -Constants.PLAYER_PATH_SIZE)
            lineTo(15f, Constants.PLAYER_PATH_SIZE)
            lineTo(0f, 15f)
            lineTo(-15f, Constants.PLAYER_PATH_SIZE)
            close()
        }
    }

    fun updateState(state: GameState) {
        gameState = state
        invalidate()
    }

    // Drawing Methods
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawGrid(canvas)

        gameState?.let { state ->
            val playerPixelX = state.playerX * cellWidth + cellWidth / 2
            val playerPixelY = state.playerY * cellHeight + cellHeight / 2
            drawPlayer(canvas, playerPixelX, playerPixelY)
        }
    }

    private fun drawGrid(canvas: Canvas) {
        for (i in 0..Constants.GRID_COLUMNS) {
            canvas.drawLine(
                i * cellWidth, 0f,
                i * cellWidth, height.toFloat(),
                paints.grid
            )
        }

        for (i in 0..Constants.GRID_ROWS) {
            canvas.drawLine(
                0f, i * cellHeight,
                width.toFloat(), i * cellHeight,
                paints.grid
            )
        }
    }

    private fun drawPlayer(canvas: Canvas, x: Float, y: Float) {
        canvas.save()
        canvas.translate(x, y)
        canvas.scale(playerScale * 1.8f, playerScale * 1.8f)
        canvas.rotate(playerRotation)
        canvas.drawPath(playerPath, paints.playerGlow)
        canvas.drawPath(playerPath, paints.player)
        canvas.restore()
    }

    // Lifecycle Methods
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cellWidth = w / Constants.GRID_COLUMNS.toFloat()
        cellHeight = h / Constants.GRID_ROWS.toFloat()
    }
}























