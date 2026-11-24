package com.example.buzzword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.buzzword.data.AppDatabase
import com.example.buzzword.data.ReceivedMessage
import com.example.buzzword.data.ReceivedMessageDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Holds the history of received messages for the Receive screen. */
class ReceivedMessagesViewModel(private val dao: ReceivedMessageDao) : ViewModel() {

    val history: StateFlow<List<ReceivedMessage>> = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun delete(message: ReceivedMessage) {
        viewModelScope.launch { dao.delete(message) }
    }

    /**
     * TEST ONLY, until TableManager is connected to Person 1's Decoder: adds a fake received message.
     * Later, TableManager.onFrameReceived() calls dao.add() with the real message.
     */
    fun simulateReceived() {
        val (sender, text) = listOf(
            // Capitals only, like the real Decoder gives them (Encoder.ALPHABET).
            "Ana" to "WIFI: TABLE42",
            "Bob" to "SEE YOU AT 8",
            "Cris" to "PASS THE SALT",
            "Dana" to "DOOR CODE 4471",
        ).random()
        viewModelScope.launch { dao.add(ReceivedMessage(sender = sender, text = text)) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                ReceivedMessagesViewModel(AppDatabase.getInstance(app).receivedMessageDao())
            }
        }
    }
}