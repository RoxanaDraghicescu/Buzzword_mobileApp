package com.example.buzzword.ui.screens

import android.text.format.DateUtils
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.buzzword.ReceivedMessagesViewModel
import com.example.buzzword.data.ReceivedMessage

/** Receive screen: the history of received messages (kept in the database). */
@Composable
fun ReceiveScreen(
    viewModel: ReceivedMessagesViewModel = viewModel(factory = ReceivedMessagesViewModel.Factory),
) {
    val history by viewModel.history.collectAsStateWithLifecycle()
    var toDelete by remember { mutableStateOf<ReceivedMessage?>(null) }

    Column(Modifier.fillMaxSize()) {
        if (history.isEmpty()) {
            Box(Modifier.weight(1f)) {
                EmptyState(
                    icon = Icons.Outlined.Email,
                    title = "Nothing received yet",
                    description = "Messages felt through the table will appear here and stay after you close the app.",
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Text(
                        if (history.size == 1) "1 message" else "${history.size} messages",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                }
                items(history, key = { it.id }) { message ->
                    ReceivedMessageCard(message, onDeleteClick = { toDelete = message })
                }
            }
        }

        // Test bar: lets us fill the history before the decoder exists.
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Test mode: the decoder isn't connected yet.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                FilledTonalButton(onClick = viewModel::simulateReceived) { Text("Simulate") }
            }
        }
    }

    toDelete?.let { message ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Delete this message?") },
            text = { Text("\"${message.text}\" from ${message.sender} will be removed from the history.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(message)
                    toDelete = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun ReceivedMessageCard(message: ReceivedMessage, onDeleteClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLowest),
        border = BorderStroke(1.dp, colors.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            InitialAvatar(message.sender, colors.secondaryContainer, colors.onSecondaryContainer)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(message.sender, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        // "5 minutes ago", "Yesterday"... in the phone's language
                        DateUtils.getRelativeTimeSpanString(
                            message.receivedAt,
                            System.currentTimeMillis(),
                            DateUtils.MINUTE_IN_MILLIS,
                        ).toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Surface(shape = MaterialTheme.shapes.small, color = colors.surfaceContainer) {
                    Text(
                        message.text,
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = colors.onSurfaceVariant)
            }
        }
    }
}