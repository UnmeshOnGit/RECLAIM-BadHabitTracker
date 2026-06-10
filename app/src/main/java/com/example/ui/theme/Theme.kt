package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = Color(0xFF00BFA5),
    secondary = Color(0xFFFF8A65),
    tertiary = Color(0xFFFFD54F),
    background = Color(0xFF101012),
    surface = Color(0xFF1A1A1E),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFFFFFFF)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Color(0xFF386B20),
    secondary = Color(0xFFA44B16),
    tertiary = Color(0xFF386B20),
    background = Color(0xFFF7F9F2),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1A1C18),
    onSurface = Color(0xFF1A1C18)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
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
