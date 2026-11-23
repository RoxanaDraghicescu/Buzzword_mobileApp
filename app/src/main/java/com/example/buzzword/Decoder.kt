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

        // 1) The message starts at the first strong buzz (30% of the loudest)
        val peak = strengths.max()
        if (peak < 0.05) return null   // no buzz
        val start = times[strengths.indexOfFirst { it >= 0.3 * peak }]
        if (start + (START_PATTERN.length + bitCount) * bitLengthMillis > times.last()) return null   // recording too short

        // Average buzz in the middle 60% of one bit
        fun bitStrength(bitIndex: Int): Double {
            val from = start + (bitIndex + 0.2) * bitLengthMillis
            val to = start + (bitIndex + 0.8) * bitLengthMillis
            val inside = times.indices.filter { times[it] >= from && times[it] < to }.map { strengths[it] }
            return if (inside.isEmpty()) 0.0 else inside.average()
        }

        // 2) Line between strong and weak = halfway between the start pattern's 1s and 0s
        fun averageOf(bit: Char) =
            START_PATTERN.indices.filter { START_PATTERN[it] == bit }.map { bitStrength(it) }.average()
        val threshold = (averageOf('1') + averageOf('0')) / 2

        // 3) Strong = 1, weak = 0
        val bits = (0 until START_PATTERN.length + bitCount).joinToString("") {
            if (bitStrength(it) >= threshold) "1" else "0"
        }

        // 4) Only accept it if it really starts with 1010 1100
        return if (bits.startsWith(START_PATTERN)) bits else null
    }
}
