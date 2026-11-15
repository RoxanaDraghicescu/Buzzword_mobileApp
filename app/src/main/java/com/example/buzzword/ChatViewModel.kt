package com.example.buzzword

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Chat state from the class diagram. Week 1: only the shape; send() and pair() come later. */
class ChatViewModel : ViewModel() {

    private val _phones = MutableStateFlow<List<Phone>>(emptyList())
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
}

data class Phone(val id: Int, val name: String)

enum class Mode { OPEN, SECURE }

data class Message(
    val sender: Phone,
    val recipient: Phone,
    val text: String,
    val mode: Mode,
    val counter: Long,
)