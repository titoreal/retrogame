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
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import java.util.Random

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {


    // Properties
    private var cellWidth = 0f
    private var cellHeight = 0f
    private var gameState: GameState? = null
    private val random = Random()

    // Animation Properties
    private var playerRotation = 0f
    private var playerScale = 1f
    private var obstacleRotation = 0f
    private var collisionRadius = 0f
    private var collisionAlpha = 0
    private var lastCollisionX = 0f
    private var lastCollisionY = 0f

    // Visual Elements Collections
    private val stars = mutableListOf<Star>()
    private val nebulas = mutableListOf<Nebula>()
    private val playerPath = Path()

    // Animators
    private var starAnimator: ValueAnimator? = null
    private var playerAnimator: ValueAnimator? = null
    private var collisionAnimator: ValueAnimator? = null
    private var obstacleAnimator: ValueAnimator? = null

    // Paints
    private val paints = setupPaints()

    init {
        setupBackground()
        setupPlayerPath()
        setupAnimations()
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

        val target = Paint().apply {
            style = Paint.Style.FILL
            color = Color.parseColor("#FFD700")
        }

        val targetGlow = Paint().apply {
            style = Paint.Style.STROKE
            color = Color.parseColor("#FFEB3B")
            strokeWidth = 4f
            maskFilter = BlurMaskFilter(15f, BlurMaskFilter.Blur.NORMAL)
        }

        val obstacle = Paint().apply {
            style = Paint.Style.FILL
            shader = RadialGradient(
                0f, 0f, 40f,
                Color.parseColor("#FF5252"),
                Color.parseColor("#FF1744"),
                Shader.TileMode.CLAMP
            )
        }

        val grid = Paint().apply {
            style = Paint.Style.STROKE
            color = Color.parseColor("#30FFFFFF")
            strokeWidth = 1f
        }
    }

    // Setup Methods
    private fun setupBackground() {
        generateStars()
        generateNebulas()
        setupStarAnimation()
    }

    private fun generateStars() {
        repeat(Constants.STAR_COUNT) {
            stars.add(createRandomStar())
        }
    }

    private fun createRandomStar() = Star(
        x = random.nextFloat() * width,
        y = random.nextFloat() * height,
        size = random.nextFloat() * 3f + 0.5f,
        alpha = random.nextInt(155) + 100,
        speed = random.nextFloat() * 2f + 0.5f
    )

    private fun generateNebulas() {
        repeat(5) {
            val nebulaColors = arrayOf(
                Color.parseColor("#3949AB"),
                Color.parseColor("#5E35B1"),
                Color.parseColor("#512DA8")
            )
            nebulas.add(
                Nebula(
                    x = random.nextFloat() * width,
                    y = random.nextFloat() * height,
                    radius = random.nextFloat() * 200f + 100f,
                    color = nebulaColors[random.nextInt(nebulaColors.size)],
                    alpha = random.nextInt(50) + 20
                )
            )
        }
    }

    private fun createRandomNebula(colors: Array<String>) = Nebula(
        x = random.nextFloat() * width,
        y = random.nextFloat() * height,
        radius = random.nextFloat() * 200f + 100f,
        color = Color.parseColor(colors[random.nextInt(colors.size)]),
        alpha = random.nextInt(50) + 20
    )

    private fun setupPlayerPath() {
        playerPath.apply {
            moveTo(0f, -Constants.PLAYER_PATH_SIZE)
            lineTo(15f, Constants.PLAYER_PATH_SIZE)
            lineTo(0f, 15f)
            lineTo(-15f, Constants.PLAYER_PATH_SIZE)
            close()
        }
    }

    private fun setupStarAnimation() {
        starAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 16 // 60 FPS
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                updateStars()
                invalidate()
            }
            start()
        }
    }

    private fun setupAnimations() {
        obstacleAnimator = ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 3000
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener { animator ->
                obstacleRotation = animator.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    // Update Methods
    fun updateState(state: GameState) {
        gameState = state
        invalidate()
    }

    private fun updateStars() {
        stars.forEach { star ->
            star.y += star.speed
            if (star.y > height) {
                star.y = 0f
                star.x = random.nextFloat() * width
            }
        }
    }

    // Animation Methods
    fun resetAnimations() {
        playerScale = 1f
        collisionRadius = 0f
        collisionAlpha = 0
        playerAnimator?.cancel()
        collisionAnimator?.cancel()
    }

    fun showCollisionAnimation(x: Float, y: Float) {
        lastCollisionX = x
        lastCollisionY = y

        collisionAnimator?.cancel()
        collisionAnimator = ValueAnimator.ofFloat(0f, cellWidth).apply {
            duration = Constants.COLLISION_ANIMATION_DURATION
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animator ->
                collisionRadius = animator.animatedValue as Float
                collisionAlpha = ((1f - animator.animatedFraction) * 255).toInt()
                invalidate()
            }
            start()
        }
    }

    fun startGameOverAnimation() {
        playerAnimator?.cancel()
        playerAnimator = ValueAnimator.ofFloat(1f, 3f, 1f).apply {
            duration = Constants.GAME_OVER_ANIMATION_DURATION
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animator ->
                playerScale = animator.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    // Drawing Methods
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBackground(canvas)
        drawGrid(canvas)

        gameState?.let { state ->
            val playerPixelX = state.playerX * cellWidth + cellWidth / 2
            val playerPixelY = state.playerY * cellHeight + cellHeight / 2

            drawPlayer(canvas, playerPixelX, playerPixelY)
            drawTargets(canvas, state)
            if (state.score > 200) {
                drawObstacles(canvas, state)
            }
            drawCollisionEffect(canvas)
        }
    }

    private fun drawBackground(canvas: Canvas) {
        // Background gradient
        val gradientPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                intArrayOf(
                    Color.parseColor("#000033"),
                    Color.parseColor("#000066"),
                    Color.parseColor("#000044")
                ),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), gradientPaint)

        // Nebulas
        nebulas.forEach { nebula ->
            val nebulaPaint = Paint().apply {
                color = nebula.color
                alpha = nebula.alpha
                maskFilter = BlurMaskFilter(50f, BlurMaskFilter.Blur.NORMAL)
            }
            canvas.drawCircle(nebula.x, nebula.y, nebula.radius, nebulaPaint)
        }

        // Stars
        stars.forEach { star ->
            val starPaint = Paint().apply {
                color = Color.WHITE
                alpha = star.alpha
            }
            canvas.drawCircle(star.x, star.y, star.size, starPaint)
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

    private fun drawTargets(canvas: Canvas, state: GameState) {
        state.targets.forEach { target ->
            if (target.isActive) {
                val centerX = target.x * cellWidth + cellWidth / 2
                val centerY = target.y * cellHeight + cellHeight / 2
                val radius = cellWidth / 4
                canvas.drawCircle(centerX, centerY, radius * 1.2f, paints.targetGlow)
                canvas.drawCircle(centerX, centerY, radius, paints.target)
            }
        }
    }

    private fun drawObstacles(canvas: Canvas, state: GameState) {
        val obstacleSize = cellWidth * 0.75f

        for (i in 0..1) {
            val obstacleX = ((state.obstacleX + i) % Constants.GRID_COLUMNS) * cellWidth + cellWidth / 2
            val obstacleY = cellHeight / 2

            canvas.save()
            canvas.translate(obstacleX, obstacleY)
            canvas.rotate(obstacleRotation)

            val path = Path().apply {
                moveTo(-obstacleSize/2, -obstacleSize/2)
                lineTo(obstacleSize/2, -obstacleSize/3)
                lineTo(obstacleSize/2, obstacleSize/3)
                lineTo(-obstacleSize/3, obstacleSize/2)
                close()
            }

            canvas.drawPath(path, paints.obstacle)
            canvas.restore()
        }
    }

    private fun drawCollisionEffect(canvas: Canvas) {
        if (collisionRadius > 0 && collisionAlpha > 0) {
            val collisionPaint = Paint(paints.target).apply {
                alpha = collisionAlpha
                maskFilter = BlurMaskFilter(15f, BlurMaskFilter.Blur.NORMAL)
            }
            canvas.drawCircle(lastCollisionX, lastCollisionY, collisionRadius, collisionPaint)
        }
    }

    // Lifecycle Methods
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cellWidth = w / Constants.GRID_COLUMNS.toFloat()
        cellHeight = h / Constants.GRID_ROWS.toFloat()
    }

    // Getter Methods
    fun getCellWidth(): Float = cellWidth
    fun getCellHeight(): Float = cellHeight
}
