package com.example.buzzword

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Checks that the Write screen shows the text the same way Person 1's Encoder will send it. */
class TextAsSentTest {

    @Test
    fun lowercaseBecomesCapitals() {
        assertEquals("SEE YOU AT 8", ChatViewModel.textAsSent("See you at 8"))
    }

    @Test
    fun unknownCharactersBecomeQuestionMarks() {
        assertEquals("M?R", ChatViewModel.textAsSent("măr"))
        assertEquals(setOf('Ă'), ChatViewModel.unsupportedCharacters("măr"))
    }

    @Test
    fun normalTextHasNoUnsupportedCharacters() {
        assertTrue(ChatViewModel.unsupportedCharacters("Door code 4471!").isEmpty())
    }

    @Test
    fun matchesWhatTheDecoderReadsBack() {
        // Encode, cut off start pattern + header, turn the bits back into text: same as the preview.
        val text = "Hi Ana, 42?"
        val bits = Encoder().encode(text, from = 1, to = 2)
        val received = Encoder.bitsToText(bits.drop(Decoder.START_PATTERN.length + Encoder.HEADER_BITS))
        assertEquals(ChatViewModel.textAsSent(text), received)
    }

    @Test
    fun sendTimeGrowsWithTheText() {
        // 8 start bits + 16 header bits + 6 bits per letter, each bit 200 ms
        assertEquals((8 + 16 + 6 * 2) * Decoder.BIT_LENGTH_MILLIS, ChatViewModel.sendMillis("HI"))
    }
}
