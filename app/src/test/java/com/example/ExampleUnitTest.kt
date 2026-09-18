package com.example

import com.example.model.AppCategory
import com.example.model.AppItem
import com.example.util.AppCategorizer
import com.example.util.FuzzySearchHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun appList_sortsAlphabeticallyCaseInsensitive() {
    val apps = listOf(
        AppItem("pkg.c", "act.c", "WhatsApp"),
        AppItem("pkg.a", "act.a", "agenda"),
        AppItem("pkg.b", "act.b", "Calculadora")
    )
    val sorted = apps.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })

    assertEquals("agenda", sorted[0].label)
    assertEquals("Calculadora", sorted[1].label)
    assertEquals("WhatsApp", sorted[2].label)
  }

  @Test
  fun appList_filtersByQueryCorrectly() {
    val apps = listOf(
        AppItem("pkg.c", "act.c", "WhatsApp"),
        AppItem("pkg.a", "act.a", "Agenda"),
        AppItem("pkg.b", "act.b", "Calculadora")
    )
    val query = "calc"
    val filtered = apps.filter { it.label.contains(query.trim(), ignoreCase = true) }

    assertEquals(1, filtered.size)
    assertEquals("Calculadora", filtered[0].label)
  }

  @Test
  fun appCategorizer_categorizesCommonAppsAccurately() {
    val socialCategory = AppCategorizer.categorize(null, "com.whatsapp", "WhatsApp")
    assertEquals(AppCategory.SOCIAL, socialCategory)

    val toolsCategory = AppCategorizer.categorize(null, "com.android.calculator2", "Calculadora")
    assertEquals(AppCategory.TOOLS, toolsCategory)

    val gamesCategory = AppCategorizer.categorize(null, "com.mojang.minecraftpe", "Minecraft")
    assertEquals(AppCategory.GAMES, gamesCategory)

    val mediaCategory = AppCategorizer.categorize(null, "com.spotify.music", "Spotify")
    assertEquals(AppCategory.MEDIA, mediaCategory)

    val shoppingCategory = AppCategorizer.categorize(null, "com.mercadolibre", "Mercado Livre")
    assertEquals(AppCategory.SHOPPING, shoppingCategory)

    val productivityCategory = AppCategorizer.categorize(null, "com.google.android.apps.docs", "Google Docs")
    assertEquals(AppCategory.PRODUCTIVITY, productivityCategory)
  }

  @Test
  fun appList_filtersByCategoryCorrectly() {
    val apps = listOf(
        AppItem("pkg.c", "act.c", "WhatsApp", category = AppCategory.SOCIAL),
        AppItem("pkg.a", "act.a", "Agenda", category = AppCategory.PRODUCTIVITY),
        AppItem("pkg.b", "act.b", "Calculadora", category = AppCategory.TOOLS)
    )

    val socialApps = apps.filter { it.category == AppCategory.SOCIAL }
    assertEquals(1, socialApps.size)
    assertEquals("WhatsApp", socialApps[0].label)

    val toolsApps = apps.filter { it.category == AppCategory.TOOLS }
    assertEquals(1, toolsApps.size)
    assertEquals("Calculadora", toolsApps[0].label)
  }

  @Test
  fun fuzzySearch_findsAppsWithTypos() {
    // "whasapp" typo for "WhatsApp"
    val scoreWhatsApp = FuzzySearchHelper.scoreMatch("WhatsApp", "whasapp")
    assertTrue(scoreWhatsApp > 0)

    // "spootify" typo for "Spotify"
    val scoreSpotify = FuzzySearchHelper.scoreMatch("Spotify", "spootify")
    assertTrue(scoreSpotify > 0)

    // "calcultor" typo for "Calculadora"
    val scoreCalc = FuzzySearchHelper.scoreMatch("Calculadora", "calcultor")
    assertTrue(scoreCalc > 0)

    // "istagram" typo for "Instagram"
    val scoreInsta = FuzzySearchHelper.scoreMatch("Instagram", "istagram")
    assertTrue(scoreInsta > 0)
  }

  @Test
  fun fuzzySearch_findsAppsWithPartialSubstringsAndDiacritics() {
    // Diacritic normalization: "camera" matches "Câmera"
    val scoreCamera = FuzzySearchHelper.scoreMatch("Câmera", "camera")
    assertTrue(scoreCamera > 0)

    // Substring in middle: "tube" matches "YouTube"
    val scoreYoutube = FuzzySearchHelper.scoreMatch("YouTube", "tube")
    assertTrue(scoreYoutube > 0)

    // Prefix match gets higher score than subsequence
    val prefixScore = FuzzySearchHelper.scoreMatch("Calculadora", "calc")
    val subseqScore = FuzzySearchHelper.scoreMatch("Calculadora", "cld")
    assertTrue(prefixScore > subseqScore)
  }

  @Test
  fun notificationBadgeManager_managesBadgeCountsCorrectly() {
    val sampleCounts = mapOf(
        "com.whatsapp" to 4,
        "com.google.android.gm" to 12
    )
    val sampleTitles = mapOf(
        "com.whatsapp" to listOf("Mensagem de João", "Mensagem de Maria")
    )

    com.example.service.NotificationBadgeManager.updateNotifications(sampleCounts, sampleTitles)

    assertEquals(4, com.example.service.NotificationBadgeManager.getCountForPackage("com.whatsapp"))
    assertEquals(12, com.example.service.NotificationBadgeManager.getCountForPackage("com.google.android.gm"))
    assertEquals(0, com.example.service.NotificationBadgeManager.getCountForPackage("com.unknown.app"))

    val titles = com.example.service.NotificationBadgeManager.getTitlesForPackage("com.whatsapp")
    assertEquals(2, titles.size)
    assertEquals("Mensagem de João", titles[0])

    // Verify clear
    com.example.service.NotificationBadgeManager.clear()
    assertEquals(0, com.example.service.NotificationBadgeManager.getCountForPackage("com.whatsapp"))
  }

  @Test
  fun appItem_holdsNotificationCount() {
    val appWithBadge = AppItem(
        packageName = "com.whatsapp",
        activityName = "com.whatsapp.Main",
        label = "WhatsApp",
        notificationCount = 5
    )
    assertEquals(5, appWithBadge.notificationCount)

    val appWithoutBadge = AppItem(
        packageName = "com.android.calculator2",
        activityName = "com.android.calculator2.Calculator",
        label = "Calculadora"
    )
    assertEquals(0, appWithoutBadge.notificationCount)
  }

  @Test
  fun launcherSettings_defaultValuesAreEnabled() {
    val settings = com.example.data.LauncherSettings()
    assertTrue(settings.hapticsEnabled)
    assertTrue(settings.gridHapticsEnabled)
    assertTrue(settings.gestureHapticsEnabled)
  }

  @Test
  fun launcherSettings_toggleUpdatesCorrectly() {
    var settings = com.example.data.LauncherSettings()
    settings = settings.copy(hapticsEnabled = false)
    org.junit.Assert.assertFalse(settings.hapticsEnabled)

    settings = settings.copy(gridHapticsEnabled = false)
    org.junit.Assert.assertFalse(settings.gridHapticsEnabled)

    settings = settings.copy(gestureHapticsEnabled = false)
    org.junit.Assert.assertFalse(settings.gestureHapticsEnabled)
  }
}
