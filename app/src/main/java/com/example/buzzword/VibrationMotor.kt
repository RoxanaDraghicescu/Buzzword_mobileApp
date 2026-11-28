package com.example.buzzword

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RequiresApi

// Transmitter (class diagram): bits -> buzzes.
class VibrationMotor(context: Context) {
    private val vibrator = context.getSystemService(Vibrator::class.java)

    // pattern = [off, on, off, on, ...] in ms
    // Full strength on every "on", and alarm usage so the phone doesn't weaken it
    @Suppress("DEPRECATION")
    fun play(pattern: List<Long>) {
        val strengths = IntArray(pattern.size) { if (it % 2 == 1) 255 else 0 }
        val effect = VibrationEffect.createWaveform(pattern.toLongArray(), strengths, -1)
        val alarm = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build()
        vibrator.vibrate(effect, alarm)
    }

    // Spin = strong built-in haptic effect (130 ms on the S25), only on Android 11+ phones that have it
    @SuppressLint("InlinedApi")
    private val spinSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            vibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_SPIN)

    private val spinMillis: Long =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            vibrator.getPrimitiveDurations(VibrationEffect.Composition.PRIMITIVE_SPIN)[0].toLong()
        else 130

    // "1" = one spin at the start of the bit, "0" = off. Returns false if it can't play.
    // A phone accepts only a few effects per request (10 on the S25), so we send groups of 8.
    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("InlinedApi")
    @Suppress("DEPRECATION")
    private fun playSpins(bits: String, bitLengthMillis: Long): Boolean {
        if (spinMillis <= 0 || spinMillis > bitLengthMillis * 7 / 10) return false   // must fit in the "on" time
        return try {
            val starts = bits.indices.filter { bits[it] == '1' }.map { it * bitLengthMillis }
            val alarm = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build()
            // build every group first, so nothing plays if one of them fails
            val groups = starts.chunked(8).map { group ->
                val composition = VibrationEffect.startComposition()
                group.forEachIndexed { i, start ->
                    val pause = if (i == 0) 0L else start - group[i - 1] - spinMillis
                    composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_SPIN, 1f, pause.toInt())
                }
                group.first() to composition.compose()
            }
            val handler = Handler(Looper.getMainLooper())
            for ((start, effect) in groups) {
                handler.postDelayed({ vibrator.vibrate(effect, alarm) }, start)   // each group at its own bit
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    // "1" = on 70% of the bit, "0" = off
    fun playBits(bits: String, bitLengthMillis: Long = Decoder.BIT_LENGTH_MILLIS) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && spinSupported && playSpins(bits, bitLengthMillis)) return
        val motorOnMillis = bitLengthMillis * 7 / 10
        val pattern = mutableListOf(0L)
        for (bit in bits) {
            if (bit == '1') {
                pattern.add(motorOnMillis)                    // on
                pattern.add(bitLengthMillis - motorOnMillis)  // short pause
            } else {
                pattern[pattern.lastIndex] += bitLengthMillis // longer pause
            }
        }
        play(pattern)
    }
}