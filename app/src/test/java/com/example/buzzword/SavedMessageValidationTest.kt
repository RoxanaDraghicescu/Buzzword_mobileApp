package com.example.buzzword

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SavedMessageValidationTest {

    @Test
    fun validMessage_isAccepted() {
        assertNull(SavedMessagesViewModel.validate("Wi-Fi", "WIFI: table42"))
    }

    @Test
    fun emptyTitle_isRefused() {
        assertEquals("Title is empty", SavedMessagesViewModel.validate("  ", "hello"))
    }

    @Test
    fun exactly32Characters_isAccepted() {
        assertNull(SavedMessagesViewModel.validate("Long", "a".repeat(32)))
    }

    @Test
    fun over32Characters_isRefused() {
        assertEquals(
            "Message is longer than 32 characters",
            SavedMessagesViewModel.validate("Long", "a".repeat(33)),
        )
    }
}