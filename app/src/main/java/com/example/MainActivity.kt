package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.data.AppViewModel
import com.example.ui.GeeAiApp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  private val viewModel: AppViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      var darkTheme by remember { mutableStateOf(true) } // SaaS apps look best in dark theme by default!
      
      MyApplicationTheme(darkTheme = darkTheme) {
        GeeAiApp(
          viewModel = viewModel,
          darkTheme = darkTheme,
          onThemeToggle = { darkTheme = !darkTheme }
        )
      }
    }
  }
}
