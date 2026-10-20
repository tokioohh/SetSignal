package com.example.setsignal.platform

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Small wrapper around the platform vibrator. It never holds a wake lock and only plays short,
 * one-shot patterns. The caller is responsible for respecting the user's haptics preference.
 */
class HapticFeedback(context: Context) {

    private val appContext = context.applicationContext

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            appContext.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun setCompleted() {
        playPredefined(VibrationEffect.EFFECT_CLICK)
    }

    fun tick() {
        playPredefined(VibrationEffect.EFFECT_TICK)
    }

    /** Two very short pulses when the rest countdown reaches zero. */
    fun restFinished() {
        play(
            VibrationEffect.createWaveform(
                longArrayOf(0L, 70L, 70L, 70L),
                intArrayOf(0, 170, 0, 190),
                -1,
            ),
        )
    }

    /** A compact success pattern for finishing a workout. */
    fun sessionCompleted() {
        play(
            VibrationEffect.createWaveform(
                longArrayOf(0L, 45L, 50L, 45L, 50L, 90L),
                intArrayOf(0, 120, 0, 160, 0, 210),
                -1,
            ),
        )
    }

    private fun playPredefined(effectId: Int) {
        play(VibrationEffect.createPredefined(effectId))
    }

    private fun play(effect: VibrationEffect) {
        val target = vibrator ?: return
        if (!target.hasVibrator()) return
        runCatching { target.vibrate(effect) }
    }
}
