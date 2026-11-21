package com.example.buzzword

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator

// Transmitter (class diagram): bits -> buzzes.
class VibrationMotor(context: Context) {
    private val vibrator = context.getSystemService(Vibrator::class.java)

    // pattern = [off, on, off, on, ...] in ms
    fun play(pattern: List<Long>) =
        vibrator.vibrate(VibrationEffect.createWaveform(pattern.toLongArray(), -1))

    // "1" = on 70% of the bit, "0" = off
    fun playBits(bits: String, bitLengthMillis: Long = Decoder.BIT_LENGTH_MILLIS) {
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