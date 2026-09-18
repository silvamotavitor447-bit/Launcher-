package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LauncherSettings(
    val hapticsEnabled: Boolean = true,
    val gridHapticsEnabled: Boolean = true,
    val gestureHapticsEnabled: Boolean = true,
    val clockShowSeconds: Boolean = true,
    val clockIs24Hour: Boolean = true,
)

class LauncherSettingsRepository(context: Context) {
  private val prefs: SharedPreferences =
      context.getSharedPreferences("pixelos_launcher_settings", Context.MODE_PRIVATE)

  private val _settings =
      MutableStateFlow(
          LauncherSettings(
              hapticsEnabled = prefs.getBoolean(KEY_HAPTICS_ENABLED, true),
              gridHapticsEnabled = prefs.getBoolean(KEY_GRID_HAPTICS_ENABLED, true),
              gestureHapticsEnabled = prefs.getBoolean(KEY_GESTURE_HAPTICS_ENABLED, true),
              clockShowSeconds = prefs.getBoolean(KEY_CLOCK_SHOW_SECONDS, true),
              clockIs24Hour = prefs.getBoolean(KEY_CLOCK_IS_24_HOUR, true),
          )
      )
  val settings: StateFlow<LauncherSettings> = _settings.asStateFlow()

  fun setHapticsEnabled(enabled: Boolean) {
    prefs.edit().putBoolean(KEY_HAPTICS_ENABLED, enabled).apply()
    _settings.value = _settings.value.copy(hapticsEnabled = enabled)
  }

  fun setGridHapticsEnabled(enabled: Boolean) {
    prefs.edit().putBoolean(KEY_GRID_HAPTICS_ENABLED, enabled).apply()
    _settings.value = _settings.value.copy(gridHapticsEnabled = enabled)
  }

  fun setGestureHapticsEnabled(enabled: Boolean) {
    prefs.edit().putBoolean(KEY_GESTURE_HAPTICS_ENABLED, enabled).apply()
    _settings.value = _settings.value.copy(gestureHapticsEnabled = enabled)
  }

  fun setClockShowSeconds(enabled: Boolean) {
    prefs.edit().putBoolean(KEY_CLOCK_SHOW_SECONDS, enabled).apply()
    _settings.value = _settings.value.copy(clockShowSeconds = enabled)
  }

  fun setClockIs24Hour(enabled: Boolean) {
    prefs.edit().putBoolean(KEY_CLOCK_IS_24_HOUR, enabled).apply()
    _settings.value = _settings.value.copy(clockIs24Hour = enabled)
  }

  companion object {
    private const val KEY_HAPTICS_ENABLED = "key_haptics_enabled"
    private const val KEY_GRID_HAPTICS_ENABLED = "key_grid_haptics_enabled"
    private const val KEY_GESTURE_HAPTICS_ENABLED = "key_gesture_haptics_enabled"
    private const val KEY_CLOCK_SHOW_SECONDS = "key_clock_show_seconds"
    private const val KEY_CLOCK_IS_24_HOUR = "key_clock_is_24_hour"
  }
}
