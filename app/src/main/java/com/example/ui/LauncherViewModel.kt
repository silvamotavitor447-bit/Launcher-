package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppRepository
import com.example.data.LauncherSettings
import com.example.data.LauncherSettingsRepository
import com.example.model.AppCategory
import com.example.model.AppItem
import com.example.service.NotificationBadgeManager
import com.example.util.FuzzySearchHelper
import com.example.util.HapticHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class LauncherUiState(
    val apps: List<AppItem> = emptyList(),
    val filteredApps: List<AppItem> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: AppCategory = AppCategory.ALL,
    val availableCategories: List<AppCategory> = emptyList(),
    val isLoading: Boolean = true,
    val currentTime: String = "",
    val currentDate: String = "",
    val clockHours: String = "",
    val clockMinutes: String = "",
    val clockSeconds: String = "",
    val clockPeriod: String = "",
    val isDrawerOpen: Boolean = false,
    val isNotificationAccessGranted: Boolean = false,
    val notificationCounts: Map<String, Int> = emptyMap(),
    val notificationTitles: Map<String, List<String>> = emptyMap(),
    val settings: LauncherSettings = LauncherSettings(),
    val isSettingsSheetOpen: Boolean = false,
)

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

  private val repository = AppRepository(application.applicationContext)
  private val settingsRepository = LauncherSettingsRepository(application.applicationContext)

  private val _uiState = MutableStateFlow(LauncherUiState())
  val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()

  init {
    startClockTicker()
    observeSettings()
    observeNotificationBadges()
    checkNotificationPermission()
    loadApps()
  }

  private fun observeSettings() {
    viewModelScope.launch {
      settingsRepository.settings.collect { settings ->
        _uiState.update { it.copy(settings = settings) }
      }
    }
  }

  fun setHapticsEnabled(enabled: Boolean) {
    settingsRepository.setHapticsEnabled(enabled)
  }

  fun setGridHapticsEnabled(enabled: Boolean) {
    settingsRepository.setGridHapticsEnabled(enabled)
  }

  fun setGestureHapticsEnabled(enabled: Boolean) {
    settingsRepository.setGestureHapticsEnabled(enabled)
  }

  fun setClockShowSeconds(enabled: Boolean) {
    settingsRepository.setClockShowSeconds(enabled)
  }

  fun setClockIs24Hour(enabled: Boolean) {
    settingsRepository.setClockIs24Hour(enabled)
  }

  fun testHapticFeedback() {
    HapticHelper.performSampleFeedback(getApplication())
  }

  fun openSettingsSheet() {
    _uiState.update { it.copy(isSettingsSheetOpen = true) }
  }

  fun closeSettingsSheet() {
    _uiState.update { it.copy(isSettingsSheetOpen = false) }
  }

  fun checkNotificationPermission() {
    val isGranted = NotificationBadgeManager.isNotificationAccessGranted(getApplication())
    _uiState.update { it.copy(isNotificationAccessGranted = isGranted) }
  }

  fun requestNotificationPermission() {
    NotificationBadgeManager.openNotificationAccessSettings(getApplication())
  }

  fun simulateSampleBadges() {
    val packages = _uiState.value.apps.map { it.packageName }
    NotificationBadgeManager.simulateSampleNotificationsForApps(packages)
  }

  fun clearBadges() {
    NotificationBadgeManager.clear()
  }

  private fun observeNotificationBadges() {
    viewModelScope.launch {
      NotificationBadgeManager.notificationCounts.collect { counts ->
        _uiState.update { state ->
          val updatedApps = state.apps.map { app ->
            val count = counts[app.packageName] ?: 0
            if (app.notificationCount != count) app.copy(notificationCount = count) else app
          }
          state.copy(
            apps = updatedApps,
            filteredApps = filterApps(updatedApps, state.searchQuery, state.selectedCategory),
            notificationCounts = counts,
          )
        }
      }
    }
    viewModelScope.launch {
      NotificationBadgeManager.notificationTitles.collect { titles ->
        _uiState.update { it.copy(notificationTitles = titles) }
      }
    }
  }

  fun loadApps() {
    checkNotificationPermission()
    viewModelScope.launch {
      // If already has apps, do not flash full loading state to keep fluid transition
      if (_uiState.value.apps.isEmpty()) {
        _uiState.update { it.copy(isLoading = true) }
      }
      val rawAppsList = repository.getInstalledApps()
      val currentCounts = NotificationBadgeManager.notificationCounts.value
      val appsList = rawAppsList.map { app ->
        val count = currentCounts[app.packageName] ?: 0
        if (count > 0) app.copy(notificationCount = count) else app
      }

      // Calculate available non-empty categories
      val presentCategories = appsList.map { it.category }.toSet()
      val categories = listOf(AppCategory.ALL) + AppCategory.entries.filter {
        it != AppCategory.ALL && presentCategories.contains(it)
      }

      _uiState.update { state ->
        val safeCategory = if (categories.contains(state.selectedCategory)) state.selectedCategory else AppCategory.ALL
        state.copy(
          apps = appsList,
          availableCategories = categories,
          selectedCategory = safeCategory,
          filteredApps = filterApps(appsList, state.searchQuery, safeCategory),
          isLoading = false,
        )
      }
    }
  }

  fun onSearchQueryChanged(query: String) {
    _uiState.update { state ->
      state.copy(
        searchQuery = query,
        filteredApps = filterApps(state.apps, query, state.selectedCategory),
      )
    }
  }

  fun onCategorySelected(category: AppCategory) {
    _uiState.update { state ->
      state.copy(
        selectedCategory = category,
        filteredApps = filterApps(state.apps, state.searchQuery, category),
      )
    }
  }

  fun clearSearch() {
    onSearchQueryChanged("")
  }

  fun openDrawer() {
    _uiState.update { it.copy(isDrawerOpen = true) }
  }

  fun closeDrawer() {
    _uiState.update {
      it.copy(
        isDrawerOpen = false,
        searchQuery = "",
        selectedCategory = AppCategory.ALL,
        filteredApps = it.apps,
      )
    }
  }

  fun toggleDrawer() {
    _uiState.update {
      if (it.isDrawerOpen) {
        it.copy(
          isDrawerOpen = false,
          searchQuery = "",
          selectedCategory = AppCategory.ALL,
          filteredApps = it.apps,
        )
      } else {
        it.copy(isDrawerOpen = true)
      }
    }
  }

  private fun filterApps(
      apps: List<AppItem>,
      query: String,
      category: AppCategory,
  ): List<AppItem> {
    val trimmed = query.trim()
    val categoryFiltered = if (category == AppCategory.ALL) {
      apps
    } else {
      apps.filter { it.category == category }
    }

    if (trimmed.isEmpty()) {
      return categoryFiltered
    }

    // Fuzzy matching with scoring and ranking
    data class ScoredApp(val app: AppItem, val score: Int)

    return categoryFiltered
      .mapNotNull { app ->
        // Check label first, fallback to package suffix for advanced matching
        val labelScore = FuzzySearchHelper.scoreMatch(app.label, trimmed)
        val packageScore = if (labelScore == 0) {
          val simplePkg = app.packageName.substringAfterLast('.')
          FuzzySearchHelper.scoreMatch(simplePkg, trimmed) / 2
        } else 0

        val maxScore = maxOf(labelScore, packageScore)
        if (maxScore > 0) ScoredApp(app, maxScore) else null
      }
      .sortedWith(
        compareByDescending<ScoredApp> { it.score }
          .thenBy(String.CASE_INSENSITIVE_ORDER) { it.app.label }
      )
      .map { it.app }
  }

  private fun startClockTicker() {
    viewModelScope.launch {
      val locale = Locale.getDefault()
      val isPortuguese = locale.language.lowercase().startsWith("pt")
      val dateFormatPattern = if (isPortuguese) "EEEE, d 'de' MMMM" else "EEEE, MMMM d"

      val format24Hours = SimpleDateFormat("HH", locale)
      val format12Hours = SimpleDateFormat("hh", locale)
      val formatMinutes = SimpleDateFormat("mm", locale)
      val formatSeconds = SimpleDateFormat("ss", locale)
      val formatPeriod = SimpleDateFormat("a", locale)
      val dateFormat = SimpleDateFormat(dateFormatPattern, locale)

      while (isActive) {
        val now = Date()
        val is24H = _uiState.value.settings.clockIs24Hour
        val hours = if (is24H) format24Hours.format(now) else format12Hours.format(now)
        val minutes = formatMinutes.format(now)
        val seconds = formatSeconds.format(now)
        val period = if (is24H) "" else formatPeriod.format(now)
        val formattedTime = "$hours:$minutes"

        val rawDate = dateFormat.format(now)
        val formattedDate =
            rawDate.replaceFirstChar {
              if (it.isLowerCase()) it.titlecase(locale) else it.toString()
            }

        _uiState.update {
          it.copy(
              currentTime = formattedTime,
              currentDate = formattedDate,
              clockHours = hours,
              clockMinutes = minutes,
              clockSeconds = seconds,
              clockPeriod = period,
          )
        }

        delay(1000L)
      }
    }
  }
}
