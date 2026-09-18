package com.example.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.data.LauncherSettings

object HapticHelper {

  enum class VibrationType {
    LIGHT_CLICK,
    HEAVY_CLICK,
    GESTURE_TICK,
    SAMPLE_PREVIEW,
  }

  fun performGridClick(context: Context, settings: LauncherSettings) {
    if (settings.hapticsEnabled && settings.gridHapticsEnabled) {
      vibrate(context, VibrationType.LIGHT_CLICK)
    }
  }

  fun performGridLongClick(context: Context, settings: LauncherSettings) {
    if (settings.hapticsEnabled && settings.gridHapticsEnabled) {
      vibrate(context, VibrationType.HEAVY_CLICK)
    }
  }

  fun performGestureFeedback(context: Context, settings: LauncherSettings) {
    if (settings.hapticsEnabled && settings.gestureHapticsEnabled) {
      vibrate(context, VibrationType.GESTURE_TICK)
    }
  }

  fun performSampleFeedback(context: Context) {
    vibrate(context, VibrationType.SAMPLE_PREVIEW)
  }

  private fun vibrate(context: Context, type: VibrationType) {
    try {
      val vibrator =
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
          } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
          } ?: return

      if (!vibrator.hasVibrator()) return

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val effect =
            when (type) {
              VibrationType.LIGHT_CLICK ->
                  VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
              VibrationType.HEAVY_CLICK ->
                  VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
              VibrationType.GESTURE_TICK ->
                  VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
              VibrationType.SAMPLE_PREVIEW ->
                  VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
            }
        vibrator.vibrate(effect)
      } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val (ms, amplitude) =
            when (type) {
              VibrationType.LIGHT_CLICK -> Pair(15L, 100)
              VibrationType.HEAVY_CLICK -> Pair(45L, 200)
              VibrationType.GESTURE_TICK -> Pair(20L, 140)
              VibrationType.SAMPLE_PREVIEW -> Pair(35L, 220)
            }
        vibrator.vibrate(VibrationEffect.createOneShot(ms, amplitude))
      } else {
        @Suppress("DEPRECATION")
        val ms =
            when (type) {
              VibrationType.LIGHT_CLICK -> 15L
              VibrationType.HEAVY_CLICK -> 45L
              VibrationType.GESTURE_TICK -> 20L
              VibrationType.SAMPLE_PREVIEW -> 35L
            }
        @Suppress("DEPRECATION")
        vibrator.vibrate(ms)
      }
    } catch (_: Throwable) {
      // Gracefully ignore devices without vibrator hardware or restricted vibration
    }
  }
}
