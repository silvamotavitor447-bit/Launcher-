package com.example.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class LauncherNotificationListenerService : NotificationListenerService() {

  override fun onListenerConnected() {
    super.onListenerConnected()
    Log.d("LauncherNotifService", "NotificationListenerService connected")
    refreshNotifications()
  }

  override fun onNotificationPosted(sbn: StatusBarNotification?) {
    super.onNotificationPosted(sbn)
    refreshNotifications()
  }

  override fun onNotificationRemoved(sbn: StatusBarNotification?) {
    super.onNotificationRemoved(sbn)
    refreshNotifications()
  }

  override fun onListenerDisconnected() {
    super.onListenerDisconnected()
    Log.d("LauncherNotifService", "NotificationListenerService disconnected")
    NotificationBadgeManager.clear()
  }

  private fun refreshNotifications() {
    try {
      val activeNotifs = activeNotifications ?: emptyArray()
      val counts = mutableMapOf<String, Int>()
      val titles = mutableMapOf<String, MutableList<String>>()

      for (sbn in activeNotifs) {
        val pkg = sbn.packageName ?: continue
        // Filter ongoing notifications (like permanent service bars, music playback controllers)
        if (sbn.isOngoing) continue

        counts[pkg] = (counts[pkg] ?: 0) + 1

        val title = sbn.notification?.extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        if (!title.isNullOrBlank()) {
          val list = titles.getOrPut(pkg) { mutableListOf() }
          if (list.size < 3) {
            list.add(title)
          }
        }
      }

      NotificationBadgeManager.updateNotifications(counts, titles)
    } catch (e: Exception) {
      Log.e("LauncherNotifService", "Error refreshing notifications", e)
    }
  }
}
