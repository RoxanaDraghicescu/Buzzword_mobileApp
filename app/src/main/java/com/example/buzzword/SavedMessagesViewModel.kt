package com.example.buzzword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.buzzword.data.AppDatabase
import com.example.buzzword.data.SavedMessage
import com.example.buzzword.data.SavedMessageDao

/** Holds the data for the Saved messages screen. The screen never touches the database directly. */
class SavedMessagesViewModel(private val dao: SavedMessageDao) : ViewModel() {

    /** Read: the list shown on screen. Starts empty, then fills in from the database. */
    val savedMessages: StateFlow<List<SavedMessage>> = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Create: saves a new message in a coroutine, so the screen doesn't freeze. */
    fun add(title: String, text: String) {
        if (validate(title, text) != null) return
        viewModelScope.launch {
            dao.add(SavedMessage(title = title.trim(), text = text.trim()))
        }
    }

    /** Update: saves the edited message (same id, new title and text). */
    fun update(message: SavedMessage) {
        if (validate(message.title, message.text) != null) return
        viewModelScope.launch {
            dao.update(message.copy(title = message.title.trim(), text = message.text.trim()))
        }
    }

    /** Delete: removes the message. */
    fun delete(message: SavedMessage) {
        viewModelScope.launch { dao.delete(message) }
    }

    companion object {
        const val MAX_TEXT_LENGTH = 32 // project doc: messages are at most 32 characters
        const val MAX_TITLE_LENGTH = 40

        /** Returns an error to show, or null if the message can be saved. */
        fun validate(title: String, text: String): String? = when {
            title.isBlank() -> "Title is empty"
            text.isBlank() -> "Message is empty"
            title.trim().length > MAX_TITLE_LENGTH -> "Title is longer than $MAX_TITLE_LENGTH characters"
            text.trim().length > MAX_TEXT_LENGTH -> "Message is longer than $MAX_TEXT_LENGTH characters"
            else -> null
        }

        /** Tells Compose how to build this ViewModel, giving it the DAO. */
        val Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                SavedMessagesViewModel(AppDatabase.getInstance(app).savedMessageDao())
            }
        }
    }
}