@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.buzzword.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.buzzword.SavedMessagesViewModel
import com.example.buzzword.data.SavedMessage
import kotlinx.coroutines.launch
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton

/** Saved messages screen: Create (the "New message" button) and Read (the cards). */
@Composable
fun SavedMessagesScreen(
    viewModel: SavedMessagesViewModel = viewModel(factory = SavedMessagesViewModel.Factory),
) {
    val messages by viewModel.savedMessages.collectAsStateWithLifecycle()
    var showAddSheet by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }
    val editing = messages.find { it.id == editingId }

    Box(Modifier.fillMaxSize()) {
        if (messages.isEmpty()) {
            EmptySavedMessages(onAddClick = { showAddSheet = true })
        } else {
            SavedMessagesList(
                messages = messages,
                onMessageClick = { editingId = it.id },
            )
            ExtendedFloatingActionButton(
                onClick = { showAddSheet = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("New message") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            )
        }
    }

    if (showAddSheet) {
        AddSavedMessageSheet(
            onDismiss = { showAddSheet = false },
            onSave = { title, text -> viewModel.add(title, text) },
        )
    }

    if (editing != null) {
        EditSavedMessageSheet(
            message = editing,
            onDismiss = { editingId = null },
            onSave = { title, text -> viewModel.update(editing.copy(title = title, text = text)) },
            onDelete = { viewModel.delete(editing) },
        )
    }
}

@Composable
private fun SavedMessagesList(
    messages: List<SavedMessage>,
    onMessageClick: (SavedMessage) -> Unit,
) {
    // LazyColumn only draws the rows that are on screen.
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                if (messages.size == 1) "1 message" else "${messages.size} messages",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
        items(messages, key = { it.id }) { message ->
            SavedMessageCard(message, onClick = { onMessageClick(message) })
        }
    }
}

/** One saved message: a round letter, the title, and the text in a code-style pill. */
@Composable
private fun SavedMessageCard(message: SavedMessage, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLowest),
        border = BorderStroke(1.dp, colors.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).background(colors.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    message.title.trim().take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onPrimaryContainer,
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    message.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
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
            Spacer(Modifier.width(12.dp))
            Text(
                "${message.text.length}/${SavedMessagesViewModel.MAX_TEXT_LENGTH}",
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmptySavedMessages(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconBadge(Icons.Outlined.Star)
        Spacer(Modifier.height(24.dp))
        Text(
            "No saved messages yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Save messages you send often, like a Wi-Fi password, and send them with one flip.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onAddClick, shape = MaterialTheme.shapes.medium) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Add your first message")
        }
    }
}

/** A sheet that slides up from the bottom (not a separate screen) with title and message. */
@Composable
private fun AddSavedMessageSheet(
    onDismiss: () -> Unit,
    onSave: (title: String, text: String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    // rememberSaveable keeps what you typed if the phone is rotated.
    var title by rememberSaveable { mutableStateOf("") }
    var text by rememberSaveable { mutableStateOf("") }
    val canSave = SavedMessagesViewModel.validate(title, text) == null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column {
                Text(
                    "New saved message",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Something you send often, like a Wi-Fi password.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            OutlinedTextField(
                value = title,
                onValueChange = { if (it.length <= SavedMessagesViewModel.MAX_TITLE_LENGTH) title = it },
                label = { Text("Title") },
                placeholder = { Text("e.g. Wi-Fi") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = text,
                // Don't let the user type past 32 characters
                onValueChange = { if (it.length <= SavedMessagesViewModel.MAX_TEXT_LENGTH) text = it },
                label = { Text("Message") },
                placeholder = { Text("e.g. table42") },
                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
                supportingText = {
                    Text(
                        "${text.length} / ${SavedMessagesViewModel.MAX_TEXT_LENGTH}",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                    )
                },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = {
                    onSave(title, text)
                    // Slide the sheet down, then remove it.
                    scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                },
                enabled = canSave,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Text("Save message")
            }
        }
    }
}

/** Edit sheet: same form as the add sheet, plus a Delete button that asks first. */
@Composable
private fun EditSavedMessageSheet(
    message: SavedMessage,
    onDismiss: () -> Unit,
    onSave: (title: String, text: String) -> Unit,
    onDelete: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var title by rememberSaveable { mutableStateOf(message.title) }
    var text by rememberSaveable { mutableStateOf(message.text) }
    var askingToDelete by rememberSaveable { mutableStateOf(false) }
    val canSave = SavedMessagesViewModel.validate(title, text) == null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Edit message",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            OutlinedTextField(
                value = title,
                onValueChange = { if (it.length <= SavedMessagesViewModel.MAX_TITLE_LENGTH) title = it },
                label = { Text("Title") },
                placeholder = { Text("e.g. Wi-Fi") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = text,
                onValueChange = { if (it.length <= SavedMessagesViewModel.MAX_TEXT_LENGTH) text = it },
                label = { Text("Message") },
                placeholder = { Text("e.g. table42") },
                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
                supportingText = {
                    Text(
                        "${text.length} / ${SavedMessagesViewModel.MAX_TEXT_LENGTH}",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                    )
                },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = {
                    onSave(title, text)
                    scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                },
                enabled = canSave,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Text("Save changes")
            }
            OutlinedButton(
                onClick = { askingToDelete = true },
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Delete message")
            }
        }
    }

    if (askingToDelete) {
        AlertDialog(
            onDismissRequest = { askingToDelete = false },
            title = { Text("Delete this message?") },
            text = { Text("\"${message.title}\" will be removed from your saved messages.") },
            confirmButton = {
                TextButton(onClick = {
                    askingToDelete = false
                    onDelete()
                    onDismiss()
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { askingToDelete = false }) { Text("Cancel") }
            },
        )
    }
}