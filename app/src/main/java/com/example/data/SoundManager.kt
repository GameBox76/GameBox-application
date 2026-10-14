package com.example.data

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

class SoundManager(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var toneGenerator: ToneGenerator? = null

    var isSoundEnabled: Boolean = true
    var isHapticsEnabled: Boolean = true

    private val vibrator: Vibrator? by lazy {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Exception) {
            null
        }
    }

    init {
        try {
            toneGenerator = ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 70)
        } catch (_: Exception) {
            toneGenerator = null
        }
    }

    fun playClick() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 25)
            } catch (_: Exception) {}
        }
        vibrateLight()
    }

    fun playMove() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 30)
            } catch (_: Exception) {}
        }
        vibrateLight()
    }

    fun playScore() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                playChirp(frequency = 880f, durationMs = 60)
            } catch (_: Exception) {}
        }
        vibrateMedium()
    }

    fun playWin() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                playChirp(523.25f, 70) // C5
                kotlinx.coroutines.delay(80)
                playChirp(659.25f, 70) // E5
                kotlinx.coroutines.delay(80)
                playChirp(783.99f, 70) // G5
                kotlinx.coroutines.delay(80)
                playChirp(1046.50f, 150) // C6
            } catch (_: Exception) {}
        }
        vibrateHeavy()
    }

    fun playGameOver() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                playChirp(440f, 90)
                kotlinx.coroutines.delay(100)
                playChirp(349.23f, 90)
                kotlinx.coroutines.delay(100)
                playChirp(261.63f, 160)
            } catch (_: Exception) {}
        }
        vibrateHeavy()
    }

    fun playHit() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                playChirp(320f, 40)
            } catch (_: Exception) {}
        }
        vibrateMedium()
    }

    private fun playChirp(frequency: Float, durationMs: Int) {
        try {
            val sampleRate = 22050
            val numSamples = (durationMs * sampleRate) / 1000
            val buffer = ShortArray(numSamples)
            val angleStep = (2.0 * Math.PI * frequency) / sampleRate

            for (i in 0 until numSamples) {
                // Envelope decay for crisp game sound
                val decay = 1.0f - (i.toFloat() / numSamples.toFloat())
                val sample = (sin(i * angleStep) * Short.MAX_VALUE * 0.6 * decay).toInt().toShort()
                buffer[i] = sample
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            scope.launch {
                kotlinx.coroutines.delay(durationMs.toLong() + 50)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {
            // Tone generator fallback
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, durationMs)
            } catch (_: Exception) {}
        }
    }

    fun vibrateLight() {
        if (!isHapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(15)
            }
        } catch (_: Exception) {}
    }

    fun vibrateMedium() {
        if (!isHapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(35)
            }
        } catch (_: Exception) {}
    }

    fun vibrateHeavy() {
        if (!isHapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 40, 50, 60), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(70)
            }
        } catch (_: Exception) {}
    }

    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (_: Exception) {}
    }
}
