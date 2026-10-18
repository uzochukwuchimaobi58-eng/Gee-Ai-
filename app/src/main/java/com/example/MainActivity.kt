package com.example

import android.os.Bundle
import android.util.Log
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
import com.google.firebase.FirebaseApp
import java.io.File

class MainActivity : ComponentActivity() {
  private val viewModel: AppViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Pre-create WebView/Chromium cache directories to prevent simple_file_enumerator / index errors
    try {
      val jsCacheDir = File(cacheDir, "WebView/Default/HTTP Cache/Code Cache/js")
      if (!jsCacheDir.exists()) jsCacheDir.mkdirs()
      val wasmCacheDir = File(cacheDir, "WebView/Default/HTTP Cache/Code Cache/wasm")
      if (!wasmCacheDir.exists()) wasmCacheDir.mkdirs()
    } catch (t: Throwable) {
      Log.w("MainActivity", "WebView cache dir initialization: ${t.message}")
    }

    try {
      if (FirebaseApp.getApps(applicationContext).isEmpty()) {
        FirebaseApp.initializeApp(applicationContext)
      }
    } catch (t: Throwable) {
      Log.w("MainActivity", "FirebaseApp init skipped: ${t.message}")
    }

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
