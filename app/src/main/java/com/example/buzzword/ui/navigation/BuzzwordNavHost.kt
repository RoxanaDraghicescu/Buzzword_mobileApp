package com.example.buzzword.ui.navigation

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.buzzword.CaptureActivity
import com.example.buzzword.ui.screens.ReceiveScreen
import com.example.buzzword.ui.screens.SavedMessagesScreen
import com.example.buzzword.ui.screens.SettingsScreen
import com.example.buzzword.ui.screens.TableScreen
import com.example.buzzword.ui.screens.WriteScreen

/** The screens of the app. Filled icon when selected, outlined when not. */
enum class Screen(
    val route: String,
    val label: String,
    val title: String,
    val selectedIcon: ImageVector,
    val icon: ImageVector,
) {
    Table("table", "Table", "Buzzword", Icons.Filled.Home, Icons.Outlined.Home),
    Saved("saved", "Saved", "Saved messages", Icons.Filled.Star, Icons.Outlined.Star),
    Write("write", "Write", "Write", Icons.Filled.Edit, Icons.Outlined.Edit),
    Receive("receive", "Inbox", "Received", Icons.Filled.Email, Icons.Outlined.Email),
    Settings("settings", "Settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings),
}

/** "User interface" box from the component diagram: top bar, bottom bar, and the NavHost. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuzzwordNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val currentScreen = Screen.entries.find { it.route == currentRoute } ?: Screen.Table
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current

    Scaffold(
        containerColor = colors.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    if (currentScreen == Screen.Table) BrandTitle()
                    else Text(currentScreen.title, fontWeight = FontWeight.SemiBold)
                },
                actions = {
                    // Lab tool: record the accelerometer while buzzing
                    IconButton(onClick = { context.startActivity(Intent(context, CaptureActivity::class.java)) }) {
                        Icon(Icons.Outlined.Phone, contentDescription = "Open capture tool")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = colors.background),
            )
        },
        bottomBar = {
            NavigationBar(containerColor = colors.surfaceContainer) {
                Screen.entries.forEach { screen ->
                    val selected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                // Keep one copy of each screen on the back stack.
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                if (selected) screen.selectedIcon else screen.icon,
                                contentDescription = screen.label,
                            )
                        },
                        label = {
                            Text(
                                screen.label,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = colors.primaryContainer,
                            selectedIconColor = colors.onPrimaryContainer,
                            selectedTextColor = colors.onSurface,
                            unselectedIconColor = colors.onSurfaceVariant,
                            unselectedTextColor = colors.onSurfaceVariant,
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Table.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Screen.Table.route) { TableScreen() }
            composable(Screen.Saved.route) { SavedMessagesScreen() }
            composable(Screen.Write.route) { WriteScreen() }
            composable(Screen.Receive.route) { ReceiveScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}

/** App name with a small orange dot, shown on the main screen. */
@Composable
private fun BrandTitle() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(10.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
        )
        Spacer(Modifier.width(8.dp))
        Text("Buzzword", fontWeight = FontWeight.Bold)
    }
}