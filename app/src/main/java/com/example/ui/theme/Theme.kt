package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = SleekOrangeDark,
    onPrimary = Color.White,
    secondary = Color(0xFF38BDF8),
    onSecondary = Color(0xFF0C4A6E),
    tertiary = SleekPeachDark,
    onTertiary = SleekTextLight,
    background = SleekBgDark,
    onBackground = SleekTextLight,
    surface = SleekSurfaceDark,
    onSurface = SleekTextLight,
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = SleekGrayDark,
    outline = SleekSandstoneDark,
    outlineVariant = Color(0xFF475569),
    secondaryContainer = Color(0xFF1E3A8A),
    onSecondaryContainer = Color(0xFFBFDBFE),
    tertiaryContainer = Color(0xFF451A03),
    onTertiaryContainer = Color(0xFFFED7AA)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = SleekOrange,
    onPrimary = Color.White,
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    tertiary = SleekPeach,
    onTertiary = SleekBrandDark,
    background = SleekBgLight,
    onBackground = SleekTextDark,
    surface = Color.White,
    onSurface = SleekTextDark,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = SleekSoftText,
    outline = SleekSandstone,
    outlineVariant = Color(0xFFCBD5E1),
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    tertiaryContainer = Color(0xFFFFEDD5),
    onTertiaryContainer = Color(0xFF9A3412)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
