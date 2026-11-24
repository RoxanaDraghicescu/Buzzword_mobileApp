@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.buzzword.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.buzzword.ChatViewModel
import com.example.buzzword.Phone
import com.example.buzzword.SavedMessagesViewModel
import com.example.buzzword.data.SavedMessage
import kotlinx.coroutines.launch
import java.util.Locale

/** Write screen: "Type message and pick recipient" from the activity diagram. */
@Composable
fun WriteScreen(
    chatViewModel: ChatViewModel,
    savedViewModel: SavedMessagesViewModel = viewModel(factory = SavedMessagesViewModel.Factory),
) {
    val phones by chatViewModel.phones.collectAsStateWithLifecycle()
    val saved by savedViewModel.savedMessages.collectAsStateWithLifecycle()

    var text by rememberSaveable { mutableStateOf("") }
    var recipientId by rememberSaveable { mutableStateOf<Int?>(null) }
    var showPicker by rememberSaveable { mutableStateOf(false) }
    val recipient = phones.find { it.id == recipientId }
    val max = SavedMessagesViewModel.MAX_TEXT_LENGTH

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionLabel("Message")
        OutlinedTextField(
            value = text,
            onValueChange = { if (it.length <= max) text = it },
            placeholder = { Text("Type up to $max characters") },
            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
            minLines = 2,
            shape = MaterialTheme.shapes.medium,
            supportingText = {
                Text(
                    "${text.length} / $max",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                    color = if (text.length == max) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            modifier = Modifier.fillMaxWidth(),
        )
        val unsupported = ChatViewModel.unsupportedCharacters(text)
        if (unsupported.isNotEmpty()) {
            Text(
                "Can't be sent by vibration, will arrive as '?': ${unsupported.joinToString(" ")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
        OutlinedButton(
            onClick = { showPicker = true },
            enabled = saved.isNotEmpty(),
            shape = MaterialTheme.shapes.medium,
        ) {
            Icon(Icons.Outlined.Star, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (saved.isEmpty()) "No saved messages yet" else "Pick a saved message")
        }

        Spacer(Modifier.height(8.dp))
        SectionLabel("Send to")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(phones, key = { it.id }) { phone ->
                val selected = phone.id == recipientId
                FilterChip(
                    selected = selected,
                    onClick = { recipientId = phone.id },
                    label = { Text(phone.name) },
                    leadingIcon = if (selected) {
                        { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null,
                )
            }
        }
        Text(
            "Demo phones until the table can list real ones (week 6).",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(8.dp))
        ReadyCard(text, recipient)
    }

    if (showPicker) {
        PickSavedMessageSheet(
            saved = saved,
            onPick = { text = it.text.take(max) },
            onDismiss = { showPicker = false },
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp),
    )
}

/** Shows whether the message is ready: text written and a recipient chosen. */
@Composable
private fun ReadyCard(text: String, recipient: Phone?) {
    val ready = text.isNotBlank() && recipient != null
    if (!ready) {
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainer) {
            Text(
                "Write a message and choose who it's for.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            )
        }
        return
    }
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    "Ready to send to ${recipient!!.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                // Person 1's Encoder sends capitals only, so show what the other phone will read.
                Text(
                    "\"${ChatViewModel.textAsSent(text)}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    "Buzzes for about ${"%.1f".format(Locale.US, ChatViewModel.sendMillis(text) / 1000.0)} s",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    "Flip the phone face-down to send · coming in week 4",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

/** Bottom sheet with the saved messages; tapping one fills in the text. */
@Composable
private fun PickSavedMessageSheet(
    saved: List<SavedMessage>,
    onPick: (SavedMessage) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Text(
            "Pick a saved message",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 8.dp),
        )
        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
            items(saved, key = { it.id }) { message ->
                ListItem(
                    headlineContent = { Text(message.title, fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text(message.text, fontFamily = FontFamily.Monospace) },
                    leadingContent = {
                        InitialAvatar(
                            message.title,
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.onPrimaryContainer,
                            size = 40.dp,
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable {
                        onPick(message)
                        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                    },
                )
            }
        }
    }
}