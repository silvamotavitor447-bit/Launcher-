package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.LauncherScreen
import com.example.ui.LauncherViewModel
import com.example.ui.theme.MinimalLauncherTheme

class MainActivity : ComponentActivity() {

  private val launcherViewModel: LauncherViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      MinimalLauncherTheme {
        LauncherScreen(viewModel = launcherViewModel)
      }
    }
  }

  override fun onResume() {
    super.onResume()
    // Refresh installed apps whenever launcher returns to foreground
    launcherViewModel.loadApps()
  }
}

