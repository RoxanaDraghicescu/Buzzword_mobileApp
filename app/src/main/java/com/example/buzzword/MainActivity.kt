package com.example.buzzword

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.buzzword.ui.navigation.BuzzwordNavHost
import com.example.buzzword.ui.theme.BuzzwordTheme

class MainActivity : ComponentActivity() {

    // From the class diagram. Used by the screens later (week 3-4, sending).
    private val viewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BuzzwordTheme {
                BuzzwordNavHost()
            }
        }
    }
}