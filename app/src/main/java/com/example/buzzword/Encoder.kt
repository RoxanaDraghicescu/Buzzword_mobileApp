package com.example.buzzword

// Sender side (class diagram): text -> frame bits.
class Encoder {

    companion object {
        // 64 symbols -> 6 bits per letter (capitals only)
        const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 .,!?:;-_'\"@#\$%&*+=/()<>[]~|"
        const val TYPE_DATA = 1      // 0 HELLO, 1 DATA, 2 ACK, 3 NACK, 4 PAIR, 5 CONFIRM
        const val HEADER_BITS = 16   // type 3 + from 4 + to 4 + length 5

        // Number -> fixed-width bits: (5, 4) -> "0101"
        fun toBits(number: Int, width: Int) = Integer.toBinaryString(number).padStart(width, '0')

        // Every 6 bits -> one letter
        fun bitsToText(bits: String) = bits.chunked(6).map { ALPHABET[it.toInt(2)] }.joinToString("")
    }

    // Frame in order: start pattern, type, from, to, length, text
    fun encode(text: String, from: Int, to: Int, type: Int = TYPE_DATA): String {
        val letters = text.uppercase().take(32).ifEmpty { " " }   // 1..32 letters
        val textBits = letters.map { letter ->
            val index = ALPHABET.indexOf(letter)
            toBits(if (index >= 0) index else ALPHABET.indexOf('?'), 6)   // unknown -> '?'
        }.joinToString("")
        return Decoder.START_PATTERN +
                toBits(type, 3) + toBits(from, 4) + toBits(to, 4) +
                toBits(letters.length - 1, 5) +   // length stored minus 1
                textBits
    }
}