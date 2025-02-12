package com.titin.retrogame

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.TextView
import androidx.core.view.isVisible
import com.titin.retrogame.databinding.DialogGameOverBinding

class GameOverDialog(
    context: Context,
    private val score: Int,
    private val onRestart: () -> Unit,
    private val onExit: () -> Unit
) : Dialog(context) {

    private lateinit var binding: DialogGameOverBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogGameOverBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up UI elements using binding
        binding.finalScoreText.text = "Puntaje: $score"

        binding.restartButton.setOnClickListener {
            dismiss()
            onRestart()
        }

        binding.exitButton.setOnClickListener {
            dismiss()
            onExit()
        }

        // Show high score if achieved
        val highScore = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
            .getInt("high_score", 0)

        findViewById<TextView>(R.id.newHighScoreText).isVisible = score > highScore

        if (score > highScore) {
            context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
                .edit()
                .putInt("high_score", score)
                .apply()
        }

        setCancelable(false)
    }
}