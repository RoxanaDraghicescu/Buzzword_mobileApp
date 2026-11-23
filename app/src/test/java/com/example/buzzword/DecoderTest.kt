package com.example.buzzword

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DecoderTest {

    private val data = "0001001011011101"
    private val ms = Decoder.BIT_LENGTH_MILLIS

    // Fake recording: 100 samples per second, motor on for 70% of every 1-bit
    private fun recording(bits: String, buzzStartMillis: Long, totalMillis: Long = 8000): List<AccelerometerSample> =
        (0..totalMillis step 10).map { t ->
            val index = (t - buzzStartMillis) / ms
            val inside = (t - buzzStartMillis) % ms
            val on = t >= buzzStartMillis && index < bits.length &&
                    bits[index.toInt()] == '1' && inside < ms * 7 / 10
            AccelerometerSample(t * 1_000_000, 0f, 0f, 0f, if (on) 0.2f else 0.005f)
        }

    @Test
    fun readsTheStartPatternAndTheData() {
        val sent = Decoder.START_PATTERN + data
        assertEquals(sent, Decoder().decode(recording(sent, 1200), data.length))
    }

    @Test
    fun worksWhenTheBuzzStartsLate() {
        val sent = Decoder.START_PATTERN + data
        assertEquals(sent, Decoder().decode(recording(sent, 1735), data.length))
    }

    @Test
    fun silence_givesNull() {
        assertNull(Decoder().decode(recording("", 0), data.length))
    }

    @Test
    fun anyOtherStart_givesNull() {
        val sent = "11110000" + data
        assertNull(Decoder().decode(recording(sent, 1200), data.length))
    }

    // Levels from a real recording, with a look-alike ("00101100") of the start pattern later on
    @Test
    fun readsTheRealStartWhenALookAlikeFollows() {
        val real = listOf(0.15, 0.05, 0.15, 0.07, 0.20, 0.19, 0.06, 0.00)
        val lookAlike = listOf(0.00, 0.01, 0.18, 0.06, 0.22, 0.23, 0.04, 0.01)
        val levels = real + List(20) { 0.0 } + lookAlike + List(20) { 0.0 }
        val samples = (0L..14000L step 10).map { t ->
            val index = (t - 1200) / ms
            val level = if (t >= 1200 && index < levels.size) levels[index.toInt()] else 0.0
            AccelerometerSample(t * 1_000_000, 0f, 0f, 0f, level.toFloat())
        }
        val bits = Decoder().decode(samples, 16)
        assertEquals(Decoder.START_PATTERN + "00000000" + "00000000", bits)
    }
}
