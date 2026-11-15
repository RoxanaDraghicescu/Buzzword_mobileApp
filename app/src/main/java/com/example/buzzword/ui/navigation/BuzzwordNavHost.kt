package com.example.buzzword.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.buzzword.ui.screens.ReceiveScreen
import com.example.buzzword.ui.screens.SavedMessagesScreen
import com.example.buzzword.ui.screens.SettingsScreen
import com.example.buzzword.ui.screens.TableScreen
import com.example.buzzword.ui.screens.WriteScreen

/** The screens of the app. "route" is the name navigation uses to find each one. */
enum class Screen(val route: String, val label: String, val icon: ImageVector) {
    Table("table", "Table", Icons.Default.Home),
    Saved("saved", "Saved", Icons.Default.Star),
    Write("write", "Write", Icons.Default.Edit),
    Receive("receive", "Receive", Icons.Default.Email),
    Settings("settings", "Settings", Icons.Default.Settings),
}

/** "User interface" box from the component diagram: top bar, bottom bar, and the NavHost. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuzzwordNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val currentScreen = Screen.entries.find { it.route == currentRoute } ?: Screen.Table

    Scaffold(
        topBar = { TopAppBar(title = { Text(currentScreen.label) }) },
        bottomBar = {
            NavigationBar {
                Screen.entries.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                // Keep one copy of each screen on the back stack.
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
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