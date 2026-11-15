package com.example.buzzword.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.buzzword.SavedMessagesViewModel
import com.example.buzzword.data.SavedMessage

/** Saved messages screen: Create (Add button) and Read (the list). Person 1 adds Update/Delete. */
@Composable
fun SavedMessagesScreen(
    viewModel: SavedMessagesViewModel = viewModel(factory = SavedMessagesViewModel.Factory),
) {
    val messages by viewModel.savedMessages.collectAsStateWithLifecycle()
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        SavedMessagesList(
            messages = messages,
            onMessageClick = { /* Person 1 (Day 2): open the message to edit or delete it */ },
        )

        ExtendedFloatingActionButton(
            onClick = { showAddDialog = true },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("Add") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        )
    }

    if (showAddDialog) {
        AddSavedMessageDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, text ->
                viewModel.add(title, text)
                showAddDialog = false
            },
        )
    }
}

@Composable
private fun SavedMessagesList(
    messages: List<SavedMessage>,
    onMessageClick: (SavedMessage) -> Unit,
) {
    if (messages.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(
                "No saved messages yet.\nTap Add to save one, like \"WIFI: table42\".",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }
    // LazyColumn only draws the rows that are on screen.
    LazyColumn(contentPadding = PaddingValues(bottom = 88.dp)) { // room for the Add button
        items(messages, key = { it.id }) { message ->
            ListItem(
                headlineContent = { Text(message.title) },
                supportingContent = { Text(message.text) },
                modifier = Modifier.clickable { onMessageClick(message) },
            )
            HorizontalDivider()
        }
    }
}

/** A dialog (not a separate screen) with title and message text. */
@Composable
private fun AddSavedMessageDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, text: String) -> Unit,
) {
    // rememberSaveable keeps what you typed if the phone is rotated.
    var title by rememberSaveable { mutableStateOf("") }
    var text by rememberSaveable { mutableStateOf("") }
    val error = SavedMessagesViewModel.validate(title, text)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New saved message") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { if (it.length <= SavedMessagesViewModel.MAX_TITLE_LENGTH) title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { if (it.length <= SavedMessagesViewModel.MAX_TEXT_LENGTH) text = it },
                    label = { Text("Message") },
                    supportingText = { Text("${text.length} / ${SavedMessagesViewModel.MAX_TEXT_LENGTH}") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(title, text) }, enabled = error == null) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}