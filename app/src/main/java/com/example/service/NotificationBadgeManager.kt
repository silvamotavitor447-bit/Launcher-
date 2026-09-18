package com.example.service

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object NotificationBadgeManager {

  private val _notificationCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
  val notificationCounts: StateFlow<Map<String, Int>> = _notificationCounts.asStateFlow()

  private val _notificationTitles = MutableStateFlow<Map<String, List<String>>>(emptyMap())
  val notificationTitles: StateFlow<Map<String, List<String>>> = _notificationTitles.asStateFlow()

  fun updateNotifications(counts: Map<String, Int>, titles: Map<String, List<String>>) {
    _notificationCounts.value = counts
    _notificationTitles.value = titles
  }

  fun clear() {
    _notificationCounts.value = emptyMap()
    _notificationTitles.value = emptyMap()
  }

  fun getCountForPackage(packageName: String): Int {
    return _notificationCounts.value[packageName] ?: 0
  }

  fun getTitlesForPackage(packageName: String): List<String> {
    return _notificationTitles.value[packageName] ?: emptyList()
  }

  fun isNotificationAccessGranted(context: Context): Boolean {
    return try {
      val enabledPackages = NotificationManagerCompat.getEnabledListenerPackages(context)
      enabledPackages.contains(context.packageName)
    } catch (e: Exception) {
      false
    }
  }

  fun openNotificationAccessSettings(context: Context) {
    try {
      val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
    } catch (e: Exception) {
      try {
        val intent = Intent(Settings.ACTION_SETTINGS).apply {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
      } catch (_: Exception) {}
    }
  }

  fun simulateSampleNotificationsForApps(packageNames: List<String>) {
    val sampleCounts = mutableMapOf<String, Int>()
    val sampleTitles = mutableMapOf<String, List<String>>()
    val sampleNumbers = listOf(2, 5, 1, 3, 9)
    packageNames.take(5).forEachIndexed { index, pkg ->
      val count = sampleNumbers[index % sampleNumbers.size]
      sampleCounts[pkg] = count
      sampleTitles[pkg] = listOf("Notificação recente ($count novas mensagens)", "Alerta de atividade")
    }
    updateNotifications(sampleCounts, sampleTitles)
  }
}
