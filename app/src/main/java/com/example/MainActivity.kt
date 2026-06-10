package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notification.NotificationHelper
import com.example.ui.screens.MainScreen
import com.example.ui.screens.isDarkThemeGlobal
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.HabitViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Setup and schedule alarms
    NotificationHelper.createNotificationChannel(this)
    NotificationHelper.scheduleDailyReminders(this)

    // Request POST_NOTIFICATIONS permission for API 33+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
      }
    }

    setContent {
      val viewModel: HabitViewModel = viewModel()
      val preferencesList by viewModel.preferences.collectAsStateWithLifecycle(initialValue = emptyList())
      val themePref = preferencesList.find { it.key == "theme_preference" }?.value ?: "light"
      val isDark = themePref == "dark"

      LaunchedEffect(isDark) {
        isDarkThemeGlobal = isDark
      }

      MyApplicationTheme(darkTheme = isDark, dynamicColor = false) {
        MainScreen(viewModel = viewModel)
      }
    }
  }
}

