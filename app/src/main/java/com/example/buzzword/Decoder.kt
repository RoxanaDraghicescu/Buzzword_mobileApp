package com.example.buzzword

// Receiver (class diagram): buzz strength -> bits.
class Decoder(private val bitLengthMillis: Long = BIT_LENGTH_MILLIS) {

    companion object {
        const val START_PATTERN = "10101100"
        const val BIT_LENGTH_MILLIS = 200L   // chosen from tests (spec suggests 100)
    }

    // Returns start pattern + bitCount bits, or null if no start pattern
    fun decode(samples: List<AccelerometerSample>, bitCount: Int): String? {
        if (samples.isEmpty()) return null
        val firstNanos = samples.first().timeNanos
        val times = samples.map { (it.timeNanos - firstNanos) / 1e6 }   // ms
        val strengths = samples.map { it.buzzStrength.toDouble() }

        // Average buzz in the middle 60% of one bit
        fun bitStrength(start: Double, bitIndex: Int): Double {
            val from = start + (bitIndex + 0.2) * bitLengthMillis
            val to = start + (bitIndex + 0.8) * bitLengthMillis
            val inside = times.indices.filter { times[it] >= from && times[it] < to }.map { strengths[it] }
            return if (inside.isEmpty()) 0.0 else inside.average()
        }

        // Average strength of the start pattern's 1s (or 0s)
        fun averageOf(start: Double, bit: Char) =
            START_PATTERN.indices.filter { START_PATTERN[it] == bit }.map { bitStrength(start, it) }.average()

        // 1) Find where 1010 1100 fits best (1s much stronger than 0s), trying every 5 ms
        val lastStart = times.last() - (START_PATTERN.length + bitCount) * bitLengthMillis
        val start = generateSequence(0.0) { it + 5 }.takeWhile { it <= lastStart }
            .maxByOrNull { averageOf(it, '1') - averageOf(it, '0') } ?: return null
        if (averageOf(start, '1') - averageOf(start, '0') < 0.01) return null   // no buzz

        // 2) Line between strong and weak = halfway between 1s and 0s
        val threshold = (averageOf(start, '1') + averageOf(start, '0')) / 2

        // 3) Strong = 1, weak = 0
        val bits = (0 until START_PATTERN.length + bitCount).joinToString("") {
            if (bitStrength(start, it) >= threshold) "1" else "0"
        }

        // 4) Only accept it if it really starts with 1010 1100
        return if (bits.startsWith(START_PATTERN)) bits else null
    }
}