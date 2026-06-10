package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Full Edge-to-Edge display
        enableEdgeToEdge()
        
        val app = application as SIMMonitorApp

        setContent {
            // Hot loading dark mode preferences from DataStore config
            val settings by app.settingsDataStore.settingsFlow.collectAsStateWithLifecycle(initialValue = null)
            val isDarkTheme = settings?.enableDarkMode ?: false

            MyApplicationTheme(darkTheme = isDarkTheme) {
                AppNavigation()
            }
        }
    }
}
