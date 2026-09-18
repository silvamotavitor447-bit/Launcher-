package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build

object SystemBarHelper {

  /**
   * Expands the Android status bar / notifications panel via the system service reflection.
   * This is the standard method used by launchers on Android.
   */
  @SuppressLint("WrongConstant")
  fun expandNotificationPanel(context: Context) {
    try {
      val statusBarService = context.getSystemService("statusbar")
      val statusBarManagerClass = Class.forName("android.app.StatusBarManager")
      val methodName =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
          "expandNotificationsPanel"
        } else {
          "expand"
        }
      val method = statusBarManagerClass.getMethod(methodName)
      method.invoke(statusBarService)
    } catch (e: Exception) {
      // Fallback if permission or reflection fails
      e.printStackTrace()
    }
  }
}
