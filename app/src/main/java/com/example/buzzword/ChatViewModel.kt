package com.example.buzzword

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Chat state from the class diagram. Week 1: only the shape; send() and pair() come later. */
class ChatViewModel : ViewModel() {

    // TODO week 6: replace with the phones that announce themselves on the table.
    private val _phones = MutableStateFlow(
        listOf(Phone(1, "Ana"), Phone(2, "Bob"), Phone(3, "Cris")),
    )
    val phones: StateFlow<List<Phone>> = _phones.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    var mode: Mode = Mode.OPEN

    fun send(text: String, recipient: Phone) {
        // TODO week 3-4: build a Message and pass it to TableManager.send()
    }

    fun pair(code: String) {
        // TODO week 5: call Pairing.pair(code)
    }

    companion object {
        /**
         * What the receiver will see: Person 1's Encoder only knows capital letters and the
         * symbols in Encoder.ALPHABET, anything else (ă, emoji...) arrives as '?'.
         */
        fun textAsSent(text: String): String =
            text.uppercase().take(SavedMessagesViewModel.MAX_TEXT_LENGTH)
                .map { if (it in Encoder.ALPHABET) it else '?' }
                .joinToString("")

        /** Characters the Encoder can't send, each listed once (for a warning on screen). */
        fun unsupportedCharacters(text: String): Set<Char> =
            text.uppercase().filter { it !in Encoder.ALPHABET }.toSet()

        /** About how long the phone buzzes to send this text: every bit of the frame takes BIT_LENGTH_MILLIS. */
        fun sendMillis(text: String): Long =
            Encoder().encode(text, from = 0, to = 0).length * Decoder.BIT_LENGTH_MILLIS
    }
}

/** id = seat number at the table, sent in 4 bits by the Encoder, so it must be 0..15. */
data class Phone(val id: Int, val name: String)

enum class Mode { OPEN, SECURE }

data class Message(
    val sender: Phone,
    val recipient: Phone,
    val text: String,
    val mode: Mode,
    val counter: Long,
)