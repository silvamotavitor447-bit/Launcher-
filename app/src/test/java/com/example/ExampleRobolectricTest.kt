package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Minimal Launcher", appName)
  }

  @Test
  fun `launcher settings repository persists and updates values`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = com.example.data.LauncherSettingsRepository(context)

    // Initial state should be default true
    org.junit.Assert.assertTrue(repo.settings.value.hapticsEnabled)
    org.junit.Assert.assertTrue(repo.settings.value.gridHapticsEnabled)
    org.junit.Assert.assertTrue(repo.settings.value.gestureHapticsEnabled)

    // Toggle haptics
    repo.setHapticsEnabled(false)
    org.junit.Assert.assertFalse(repo.settings.value.hapticsEnabled)

    repo.setGridHapticsEnabled(false)
    org.junit.Assert.assertFalse(repo.settings.value.gridHapticsEnabled)

    repo.setGestureHapticsEnabled(false)
    org.junit.Assert.assertFalse(repo.settings.value.gestureHapticsEnabled)

    // Recreate repo to verify persistence
    val repo2 = com.example.data.LauncherSettingsRepository(context)
    org.junit.Assert.assertFalse(repo2.settings.value.hapticsEnabled)
    org.junit.Assert.assertFalse(repo2.settings.value.gridHapticsEnabled)
    org.junit.Assert.assertFalse(repo2.settings.value.gestureHapticsEnabled)

    // Re-enable
    repo2.setHapticsEnabled(true)
    org.junit.Assert.assertTrue(repo2.settings.value.hapticsEnabled)
  }

  @Test
  fun `haptic helper handles calls safely`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val settingsOn = com.example.data.LauncherSettings(
        hapticsEnabled = true,
        gridHapticsEnabled = true,
        gestureHapticsEnabled = true,
    )
    val settingsOff = com.example.data.LauncherSettings(
        hapticsEnabled = false,
        gridHapticsEnabled = false,
        gestureHapticsEnabled = false,
    )

    // These should execute without any crash or exception
    com.example.util.HapticHelper.performGridClick(context, settingsOn)
    com.example.util.HapticHelper.performGridLongClick(context, settingsOn)
    com.example.util.HapticHelper.performGestureFeedback(context, settingsOn)
    com.example.util.HapticHelper.performSampleFeedback(context)

    com.example.util.HapticHelper.performGridClick(context, settingsOff)
    com.example.util.HapticHelper.performGridLongClick(context, settingsOff)
    com.example.util.HapticHelper.performGestureFeedback(context, settingsOff)
  }
}
