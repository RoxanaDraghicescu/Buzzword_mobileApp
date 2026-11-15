package com.example.buzzword.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// Week 1: these screens exist so navigation works. Their content comes later.

@Composable
fun TableScreen() = PlaceholderScreen(
    title = "Who is at the table",
    comingNext = "List of phones and the Open / Secure switch (weeks 5-6).",
)

@Composable
fun WriteScreen() = PlaceholderScreen(
    title = "Write a message",
    comingNext = "Message box with a 32-character counter, recipient, pick a saved message (week 2).",
)

@Composable
fun ReceiveScreen() = PlaceholderScreen(
    title = "Received messages",
    comingNext = "History of received messages and the live buzz graph (weeks 2-3).",
)

@Composable
fun SettingsScreen() = PlaceholderScreen(
    title = "Settings",
    comingNext = "Choose error correction: Off, Repetition x3, Hamming (week 5).",
)

@Composable
private fun PlaceholderScreen(title: String, comingNext: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Text(
            comingNext,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}