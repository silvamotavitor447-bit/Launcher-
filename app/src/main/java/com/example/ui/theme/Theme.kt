package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AmoledDarkColorScheme =
  darkColorScheme(
    primary = AccentWhite,
    onPrimary = AmoledBlack,
    primaryContainer = SurfaceElevated,
    onPrimaryContainer = TextPrimary,
    secondary = TextSecondary,
    onSecondary = AmoledBlack,
    background = AmoledBlack,
    onBackground = TextPrimary,
    surface = AmoledBlack,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceBorder,
    outlineVariant = AccentSubtle,
  )

@Composable
fun MinimalLauncherTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = AmoledDarkColorScheme,
    typography = Typography,
    content = content,
  )
}

// Kept for backward compatibility if referenced elsewhere
@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MinimalLauncherTheme(content = content)
}

