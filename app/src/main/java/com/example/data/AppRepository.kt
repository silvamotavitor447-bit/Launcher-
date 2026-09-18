package com.example.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.collection.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.model.AppItem
import com.example.util.AppCategorizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(private val context: Context) {

  // Memory cache for decoded icons to ensure fluid 60/120fps scrolling and fast reloads
  private val iconCache = LruCache<String, ImageBitmap>(200)

  suspend fun getInstalledApps(): List<AppItem> =
    withContext(Dispatchers.IO) {
      val packageManager = context.packageManager
      val intent =
        Intent(Intent.ACTION_MAIN, null).apply {
          addCategory(Intent.CATEGORY_LAUNCHER)
        }

      val resolveInfos: List<ResolveInfo> =
        try {
          packageManager.queryIntentActivities(intent, 0)
        } catch (e: Exception) {
          emptyList()
        }

      val currentPackageName = context.packageName

      resolveInfos
        .filter { resolveInfo ->
          val pkg = resolveInfo.activityInfo.packageName
          // Do not list launcher itself to keep launcher pristine
          pkg != currentPackageName
        }
        .map { resolveInfo ->
          val pkgName = resolveInfo.activityInfo.packageName
          val label =
            try {
              resolveInfo.loadLabel(packageManager).toString().trim()
            } catch (e: Exception) {
              pkgName
            }

          val cachedIcon = iconCache.get(pkgName)
          val iconBitmap: ImageBitmap? =
            if (cachedIcon != null) {
              cachedIcon
            } else {
              try {
                val iconDrawable = resolveInfo.loadIcon(packageManager)
                val bitmap = drawableToImageBitmap(iconDrawable)
                iconCache.put(pkgName, bitmap)
                bitmap
              } catch (e: Exception) {
                null
              }
            }

          val category = AppCategorizer.categorize(
              appInfo = resolveInfo.activityInfo.applicationInfo,
              packageName = pkgName,
              label = label,
          )

          AppItem(
            packageName = pkgName,
            activityName = resolveInfo.activityInfo.name,
            label = if (label.isNotEmpty()) label else pkgName,
            iconBitmap = iconBitmap,
            category = category,
          )
        }
        .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
    }

  private fun drawableToImageBitmap(drawable: Drawable): ImageBitmap {
    val rawW = drawable.intrinsicWidth
    val rawH = drawable.intrinsicHeight
    // Optimized fixed dimensions for low memory pressure and crisp rendering
    val width = if (rawW > 0) rawW.coerceIn(96, 144) else 120
    val height = if (rawH > 0) rawH.coerceIn(96, 144) else 120

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap.asImageBitmap()
  }
}
