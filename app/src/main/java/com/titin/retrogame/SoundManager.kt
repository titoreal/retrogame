package com.titin.retrogame

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

class SoundManager(context: Context) {
    private val soundPool: SoundPool
    private val sounds = mutableMapOf<SoundEffect, Int>()

    init {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(attributes)
            .build()

        sounds[SoundEffect.COLLISION] = soundPool.load(context, R.raw.collision, 1)
        sounds[SoundEffect.GAME_OVER] = soundPool.load(context, R.raw.game_over, 1)
        sounds[SoundEffect.START_GAME] = soundPool.load(context, R.raw.start_game, 1)
    }

    fun playSound(effect: SoundEffect) {
        sounds[effect]?.let { soundId ->
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        }
    }

    fun release() {
        soundPool.release()
    }
}

