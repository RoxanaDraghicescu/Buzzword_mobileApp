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

        // Running totals, so the average of any time window is quick (phones give 100-460 samples/s)
        val totals = DoubleArray(strengths.size + 1)
        for (i in strengths.indices) totals[i + 1] = totals[i] + strengths[i]
        fun indexAt(ms: Double): Int = times.binarySearch(ms).let { if (it >= 0) it else -it - 1 }
        fun average(from: Double, to: Double): Double {
            val a = indexAt(from)
            val b = indexAt(to)
            return if (b > a) (totals[b] - totals[a]) / (b - a) else 0.0
        }

        // Average buzz in the middle 60% of one bit
        fun bitStrength(start: Double, bitIndex: Int) =
            average(start + (bitIndex + 0.2) * bitLengthMillis, start + (bitIndex + 0.8) * bitLengthMillis)

        // Average strength of the start pattern's 1s (or 0s)
        fun averageOf(start: Double, bit: Char) =
            START_PATTERN.indices.filter { START_PATTERN[it] == bit }.map { bitStrength(start, it) }.average()

        // 1) Rough start = first strong buzz. "Loud" = 95th percentile, so one hard tap doesn't count
        val loud = strengths.sorted()[(strengths.size * 0.95).toInt()]
        if (loud < 0.015) return null   // no buzz (noise is about 0.006, even a faint buzz is above this)
        val rough = times.firstOrNull { average(it, it + 100) >= 0.5 * loud } ?: return null

        // 2) Exact start = where 1010 1100 fits best (1s much stronger than 0s), within 150 ms of the rough one
        val start = (-150..150 step 5).map { rough + it }
            .maxByOrNull { averageOf(it, '1') - averageOf(it, '0') } ?: return null
        if (start + (START_PATTERN.length + bitCount) * bitLengthMillis > times.last()) return null   // recording too short

        // 3) Line between strong and weak = halfway between the start pattern's 1s and 0s
        val threshold = (averageOf(start, '1') + averageOf(start, '0')) / 2

        // 4) Strong = 1, weak = 0
        val bits = (0 until START_PATTERN.length + bitCount).joinToString("") {
            if (bitStrength(start, it) >= threshold) "1" else "0"
        }

        // 5) Only accept it if it really starts with 1010 1100
        return if (bits.startsWith(START_PATTERN)) bits else null
    }
}
