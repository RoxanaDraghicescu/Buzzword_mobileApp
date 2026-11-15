package com.example.buzzword.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// Week 1: these screens exist so navigation works. Their content comes later.

@Composable
fun TableScreen() = ComingSoonScreen(
    icon = Icons.Outlined.Phone,
    title = "Who's at the table",
    description = "Phones lying on the table will show up here, with a switch between Open and Secure mode.",
    week = "week 6",
)

@Composable
fun WriteScreen() = ComingSoonScreen(
    icon = Icons.Outlined.Edit,
    title = "Write a message",
    description = "Type up to 32 characters, pick who it's for, and flip your phone to send it.",
    week = "week 2",
)

@Composable
fun ReceiveScreen() = ComingSoonScreen(
    icon = Icons.Outlined.Email,
    title = "Nothing received yet",
    description = "Messages felt through the table will appear here letter by letter, with a live graph.",
    week = "week 2",
)

@Composable
fun SettingsScreen() = ComingSoonScreen(
    icon = Icons.Outlined.Settings,
    title = "Settings",
    description = "Choose how errors are fixed: Off, Repetition ×3 or Hamming.",
    week = "week 5",
)

/** Big round icon used on empty screens. */
@Composable
fun IconBadge(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(96.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(44.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun ComingSoonScreen(icon: ImageVector, title: String, description: String, week: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconBadge(icon)
        Spacer(Modifier.height(24.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(
            description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
            Text(
                "Coming in $week",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            )
        }
    }
}