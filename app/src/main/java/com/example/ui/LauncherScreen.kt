package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AppCategory
import com.example.model.AppItem
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassHighlight
import com.example.ui.theme.GlassSurfaceEnd
import com.example.ui.theme.GlassSurfaceStart
import com.example.ui.theme.PixelBadgeBackground
import com.example.ui.theme.PixelBadgeGlassBorder
import com.example.ui.theme.PixelBadgeText
import com.example.ui.theme.PixelBlue
import com.example.ui.theme.PixelRed
import com.example.ui.theme.PixelYellow
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.HapticHelper
import com.example.util.SystemBarHelper
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LauncherScreen(
    viewModel: LauncherViewModel,
    modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsState()
  val focusManager = LocalFocusManager.current
  val coroutineScope = rememberCoroutineScope()

  var selectedAppForMenu by remember { mutableStateOf<AppItem?>(null) }
  val sheetState = rememberModalBottomSheetState()

  val safeInsets = WindowInsets.safeDrawing.asPaddingValues()
  val layoutDirection = LocalLayoutDirection.current
  val topInset = safeInsets.calculateTopPadding()
  val bottomInset = safeInsets.calculateBottomPadding()
  val startInset = safeInsets.calculateStartPadding(layoutDirection)
  val endInset = safeInsets.calculateEndPadding(layoutDirection)

  // Periodically verify notification permission state on screen focus
  LaunchedEffect(Unit) {
    viewModel.checkNotificationPermission()
  }

  // Handle back press to close settings, app drawer, or clear search
  BackHandler(enabled = uiState.isSettingsSheetOpen || uiState.isDrawerOpen || uiState.searchQuery.isNotEmpty()) {
    if (uiState.isSettingsSheetOpen) {
      viewModel.closeSettingsSheet()
    } else if (uiState.searchQuery.isNotEmpty()) {
      viewModel.clearSearch()
    } else if (uiState.isDrawerOpen) {
      viewModel.closeDrawer()
    }
  }

  // Gesture accumulation state for swipe detection on Home
  var totalDragY by remember { mutableFloatStateOf(0f) }
  var totalDragX by remember { mutableFloatStateOf(0f) }

  BoxWithConstraints(
      modifier =
          modifier
              .fillMaxSize()
              .background(AmoledBlack)
              .testTag("launcher_screen_root"),
  ) {
    val screenWidth = maxWidth
    // Adaptive column counts for phones, foldables, tablets, and desktops
    val drawerGridColumns = when {
      screenWidth >= 900.dp -> 7 // Large tablets / Chromebooks
      screenWidth >= 720.dp -> 6 // Tablets / Foldables unfolded
      screenWidth >= 520.dp -> 5 // Compact tablets / Large foldables
      else -> 4                  // Standard phones
    }

    // -------------------------------------------------------------
    // HOME SCREEN (PixelOS At-a-Glance, Liquid Glass search, gestures)
    // -------------------------------------------------------------
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(
                    top = topInset,
                    bottom = bottomInset,
                    start = startInset,
                    end = endInset,
                )
                // Gesture detector on the home screen:
                // Swipe down -> Open notification panel
                // Swipe up -> Open app drawer
                .pointerInput(uiState.isDrawerOpen) {
                  if (!uiState.isDrawerOpen) {
                    detectDragGestures(
                        onDragStart = {
                          totalDragY = 0f
                          totalDragX = 0f
                        },
                        onDragEnd = {
                          // Check vertical dominance
                          if (abs(totalDragY) > abs(totalDragX) && abs(totalDragY) > 60f) {
                            if (totalDragY > 0) {
                              // Deslizar para baixo: abrir barra de notificações
                              HapticHelper.performGestureFeedback(context, uiState.settings)
                              SystemBarHelper.expandNotificationPanel(context)
                            } else {
                              // Deslizar para cima: abrir gaveta de aplicativos
                              HapticHelper.performGestureFeedback(context, uiState.settings)
                              viewModel.openDrawer()
                            }
                          }
                          totalDragY = 0f
                          totalDragX = 0f
                        },
                        onDragCancel = {
                          totalDragY = 0f
                          totalDragX = 0f
                        },
                        onDrag = { change, dragAmount ->
                          change.consume()
                          totalDragY += dragAmount.y
                          totalDragX += dragAmount.x
                        },
                    )
                  }
                },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      // Centered adaptive container for Home screen top section
      Column(
          modifier =
              Modifier
                  .fillMaxWidth()
                  .widthIn(max = 680.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        // Top row on Home Screen with Settings shortcut (PixelOS)
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Surface(
              shape = CircleShape,
              color = Color(0x18FFFFFF),
              border = BorderStroke(1.dp, GlassBorderSubtle),
              modifier =
                  Modifier
                      .size(36.dp)
                      .clip(CircleShape)
                      .clickable {
                        HapticHelper.performGridClick(context, uiState.settings)
                        viewModel.openSettingsSheet()
                      }
                      .testTag("home_settings_button"),
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                  imageVector = Icons.Default.Tune,
                  contentDescription = stringResource(R.string.settings_title),
                  tint = TextSecondary,
                  modifier = Modifier.size(18.dp),
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 1. Google PixelOS "At a Glance" & Digital Clock Widget (Material 3 Liquid Glass)
        PixelDigitalClockWidget(
            hours = uiState.clockHours,
            minutes = uiState.clockMinutes,
            seconds = uiState.clockSeconds,
            period = uiState.clockPeriod,
            formattedDate = uiState.currentDate,
            showSeconds = uiState.settings.clockShowSeconds,
            is24Hour = uiState.settings.clockIs24Hour,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
        )

        // 2. Google PixelOS Search Bar with Liquid Glass
        PixelSearchBar(
            query = uiState.searchQuery,
            onQueryChange = {
              viewModel.onSearchQueryChanged(it)
              if (!uiState.isDrawerOpen) {
                viewModel.openDrawer()
              }
            },
            onClear = viewModel::clearSearch,
            onSearchDone = { focusManager.clearFocus() },
            onSearchBarClick = {
              viewModel.openDrawer()
            },
            onVoiceClick = {
              viewModel.openDrawer()
            },
            onLensClick = {
              viewModel.openDrawer()
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
        )

        // 3. Notification Access Request Banner (Liquid Glass)
        if (!uiState.isNotificationAccessGranted) {
          PixelNotificationPermissionBanner(
              onEnableClick = { viewModel.requestNotificationPermission() },
              onSimulateClick = {
                viewModel.simulateSampleBadges()
                Toast.makeText(context, "Badges de notificação simulados!", Toast.LENGTH_SHORT).show()
              },
              modifier =
                  Modifier
                      .fillMaxWidth()
                      .padding(horizontal = 20.dp, vertical = 8.dp),
          )
        }
      }

      Spacer(modifier = Modifier.weight(1f))

      // Home Screen Center Gesture Hints / Quick Access Affordances
      Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(horizontal = 24.dp),
      ) {
        // Notification swipe hint
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                      HapticHelper.performGestureFeedback(context, uiState.settings)
                      SystemBarHelper.expandNotificationPanel(context)
                    }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
          Icon(
              imageVector = Icons.Default.KeyboardArrowDown,
              contentDescription = stringResource(R.string.swipe_down_hint),
              tint = TextMuted,
              modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.size(6.dp))
          Text(
              text = stringResource(R.string.swipe_down_hint),
              style = MaterialTheme.typography.labelMedium,
              color = TextMuted,
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // App Drawer Swipe Up button / indicator
        Surface(
            shape = CircleShape,
            color = SurfaceElevated,
            modifier =
                Modifier
                    .size(52.dp)
                    .border(1.dp, SurfaceBorder, CircleShape)
                    .clickable {
                      HapticHelper.performGestureFeedback(context, uiState.settings)
                      viewModel.openDrawer()
                    }
                    .testTag("open_drawer_button"),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = stringResource(R.string.swipe_up_hint),
                tint = TextPrimary,
                modifier = Modifier.size(28.dp),
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = stringResource(R.string.swipe_up_hint),
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            modifier =
                Modifier
                    .clickable {
                      HapticHelper.performGestureFeedback(context, uiState.settings)
                      viewModel.openDrawer()
                    }
                    .padding(4.dp),
        )
      }

      Spacer(modifier = Modifier.height(24.dp))
    }

    // -------------------------------------------------------------
    // APP DRAWER (Slides up over the home screen with 4-column grid + Badges)
    // -------------------------------------------------------------
    AnimatedVisibility(
        visible = uiState.isDrawerOpen,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(durationMillis = 280),
        ) + fadeIn(animationSpec = tween(durationMillis = 240)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(durationMillis = 240),
        ) + fadeOut(animationSpec = tween(durationMillis = 200)),
        modifier = Modifier.fillMaxSize(),
    ) {
      Box(
          modifier =
              Modifier
                  .fillMaxSize()
                  .background(AmoledBlack)
                  .testTag("app_drawer_container"),
      ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        top = topInset,
                        start = startInset,
                        end = endInset,
                    ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          // Centered adaptive container for drawer controls
          Column(
              modifier =
                  Modifier
                      .fillMaxWidth()
                      .widthIn(max = 760.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            // Drawer Header with Close action & Title
          Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier =
                  Modifier
                      .fillMaxWidth()
                      .padding(horizontal = 16.dp, vertical = 6.dp),
          ) {
            IconButton(
                onClick = {
                  HapticHelper.performGestureFeedback(context, uiState.settings)
                  viewModel.closeDrawer()
                },
                modifier = Modifier.testTag("close_drawer_button"),
            ) {
              Icon(
                  imageVector = Icons.Default.KeyboardArrowDown,
                  contentDescription = stringResource(R.string.close_drawer),
                  tint = TextSecondary,
                  modifier = Modifier.size(28.dp),
              )
            }

            Text(
                text = stringResource(R.string.app_drawer_title),
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )

            Text(
                text = stringResource(R.string.apps_count, uiState.filteredApps.size),
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = {
                  HapticHelper.performGridClick(context, uiState.settings)
                  viewModel.openSettingsSheet()
                },
                modifier = Modifier.testTag("settings_button"),
            ) {
              Icon(
                  imageVector = Icons.Default.Tune,
                  contentDescription = stringResource(R.string.settings_title),
                  tint = TextSecondary,
                  modifier = Modifier.size(22.dp),
              )
            }
          }

          // Search Bar inside App Drawer (PixelOS Liquid Glass)
          PixelSearchBar(
              query = uiState.searchQuery,
              onQueryChange = viewModel::onSearchQueryChanged,
              onClear = viewModel::clearSearch,
              onSearchDone = { focusManager.clearFocus() },
              modifier =
                  Modifier
                      .fillMaxWidth()
                      .padding(horizontal = 18.dp, vertical = 6.dp),
          )

          // Filter bar by category
          if (uiState.availableCategories.size > 1) {
            CategoryFilterBar(
                categories = uiState.availableCategories,
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = { category ->
                  HapticHelper.performGridClick(context, uiState.settings)
                  viewModel.onCategorySelected(category)
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            )
          }

          // Compact notification permission chip in drawer if not granted
          if (!uiState.isNotificationAccessGranted) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0x18EA4335),
                border = BorderStroke(1.dp, Color(0x33EA4335)),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 4.dp)
                        .clickable { viewModel.requestNotificationPermission() },
            ) {
              Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
              ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = PixelRed,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ativar pontos de notificação nos ícones (PixelOS)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "Ativar",
                    style = MaterialTheme.typography.labelSmall,
                    color = PixelBlue,
                    fontWeight = FontWeight.Bold,
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

          // 4-Column Vertical Grid of Apps with Notification Badges
          when {
            uiState.isLoading && uiState.apps.isEmpty() -> {
              Box(
                  modifier =
                      Modifier
                          .fillMaxSize()
                          .testTag("loading_indicator"),
                  contentAlignment = Alignment.Center,
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  CircularProgressIndicator(
                      color = TextPrimary,
                      strokeWidth = 2.5.dp,
                      modifier = Modifier.size(36.dp),
                  )
                  Spacer(modifier = Modifier.height(16.dp))
                  Text(
                      text = stringResource(R.string.loading_apps),
                      style = MaterialTheme.typography.bodyMedium,
                      color = TextSecondary,
                  )
                }
              }
            }
            uiState.filteredApps.isEmpty() -> {
              Box(
                  modifier =
                      Modifier
                          .fillMaxSize()
                          .padding(32.dp)
                          .testTag("empty_state_view"),
                  contentAlignment = Alignment.Center,
              ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                  Icon(
                      imageVector = Icons.Default.Search,
                      contentDescription = null,
                      tint = TextMuted,
                      modifier = Modifier.size(48.dp),
                  )
                  Spacer(modifier = Modifier.height(16.dp))
                  Text(
                      text = stringResource(R.string.no_apps_found),
                      style = MaterialTheme.typography.titleMedium,
                      color = TextSecondary,
                      textAlign = TextAlign.Center,
                  )
                }
              }
            }
            else -> {
              LazyVerticalGrid(
                  columns = GridCells.Fixed(drawerGridColumns),
                  contentPadding =
                      PaddingValues(
                          start = 14.dp,
                          end = 14.dp,
                          top = 8.dp,
                          bottom = bottomInset + 24.dp,
                      ),
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalArrangement = Arrangement.spacedBy(16.dp),
                  modifier =
                      Modifier
                          .fillMaxSize()
                          .widthIn(max = 960.dp)
                          .testTag("app_grid"),
              ) {
                items(
                    items = uiState.filteredApps,
                    key = { it.packageName },
                ) { app ->
                  AppGridItem(
                      app = app,
                      onClick = {
                        HapticHelper.performGridClick(context, uiState.settings)
                        launchApp(
                            context = context,
                            app = app,
                            onFail = {
                              Toast.makeText(
                                      context,
                                      context.getString(R.string.cannot_launch),
                                      Toast.LENGTH_SHORT,
                                  )
                                  .show()
                            },
                        )
                      },
                      onLongClick = {
                        HapticHelper.performGridLongClick(context, uiState.settings)
                        selectedAppForMenu = app
                      },
                  )
                }
              }
            }
          }
        }
      }
    }

    // -------------------------------------------------------------
    // App Quick Action Modal Bottom Sheet (PixelOS Details + Notifs)
    // -------------------------------------------------------------
    selectedAppForMenu?.let { app ->
      ModalBottomSheet(
          onDismissRequest = { selectedAppForMenu = null },
          sheetState = sheetState,
          containerColor = SurfaceElevated,
          contentColor = TextPrimary,
          shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
      ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .widthIn(max = 580.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .padding(bottom = bottomInset + 16.dp),
        ) {
          Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(bottom = 16.dp),
          ) {
            Box(modifier = Modifier.size(52.dp)) {
              if (app.iconBitmap != null) {
                Image(
                    bitmap = app.iconBitmap,
                    contentDescription = app.label,
                    modifier =
                        Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp)),
                )
              } else {
                Box(
                    modifier =
                        Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceDark),
                    contentAlignment = Alignment.Center,
                ) {
                  Text(
                      text = app.label.take(1).uppercase(),
                      color = TextPrimary,
                      fontWeight = FontWeight.Bold,
                      fontSize = 20.sp,
                  )
                }
              }

              if (app.notificationCount > 0) {
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 2.dp, y = (-2).dp)
                            .clip(CircleShape)
                            .background(PixelBadgeBackground)
                            .border(1.dp, PixelBadgeGlassBorder, CircleShape)
                            .padding(horizontal = 4.dp, vertical = 1.dp),
                ) {
                  Text(
                      text = if (app.notificationCount > 99) "99+" else app.notificationCount.toString(),
                      color = PixelBadgeText,
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Bold,
                  )
                }
              }
            }

            Column(modifier = Modifier.padding(start = 16.dp)) {
              Text(
                  text = app.label,
                  style = MaterialTheme.typography.titleMedium,
                  color = TextPrimary,
                  fontWeight = FontWeight.SemiBold,
              )
              Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(top = 2.dp),
              ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SurfaceDark,
                    border = BorderStroke(1.dp, SurfaceBorder),
                ) {
                  Text(
                      text = getCategoryDisplayName(app.category),
                      style = MaterialTheme.typography.labelSmall,
                      color = TextSecondary,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                  )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
              }
            }
          }

          // Active Notifications Preview Section (PixelOS feature)
          if (app.notificationCount > 0) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0x22EA4335),
                border = BorderStroke(1.dp, Color(0x55EA4335)),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                      imageVector = Icons.Default.NotificationsActive,
                      contentDescription = null,
                      tint = PixelRed,
                      modifier = Modifier.size(18.dp),
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                      text =
                          if (app.notificationCount == 1) {
                            stringResource(R.string.notification_single_unread)
                          } else {
                            stringResource(R.string.notifications_unread, app.notificationCount)
                          },
                      style = MaterialTheme.typography.labelMedium,
                      fontWeight = FontWeight.Bold,
                      color = TextPrimary,
                  )
                }
                val titles = uiState.notificationTitles[app.packageName] ?: emptyList()
                titles.forEach { title ->
                  Text(
                      text = "• $title",
                      style = MaterialTheme.typography.bodySmall,
                      color = TextSecondary,
                      modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                  )
                }
              }
            }
          }

          HorizontalDivider(
              color = SurfaceBorder,
              thickness = 0.5.dp,
              modifier = Modifier.padding(vertical = 6.dp),
          )

          // Action 1: Open App
          BottomSheetActionItem(
              icon = Icons.Default.PlayArrow,
              title = "Abrir aplicativo",
              onClick = {
                coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                  selectedAppForMenu = null
                  launchApp(
                      context = context,
                      app = app,
                      onFail = {
                        Toast.makeText(
                                context,
                                context.getString(R.string.cannot_launch),
                                Toast.LENGTH_SHORT,
                            )
                            .show()
                      },
                  )
                }
              },
          )

          // Action 2: App Info Settings
          BottomSheetActionItem(
              icon = Icons.Default.Info,
              title = stringResource(R.string.app_info),
              onClick = {
                coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                  selectedAppForMenu = null
                  openAppDetailsSettings(context, app.packageName)
                }
              },
          )
        }
      }
    }

    // -------------------------------------------------------------
    // Launcher Settings Modal Bottom Sheet (PixelOS Haptic Controls)
    // -------------------------------------------------------------
    if (uiState.isSettingsSheetOpen) {
      PixelLauncherSettingsSheet(
          settings = uiState.settings,
          onHapticsToggled = viewModel::setHapticsEnabled,
          onGridHapticsToggled = viewModel::setGridHapticsEnabled,
          onGestureHapticsToggled = viewModel::setGestureHapticsEnabled,
          onClockSecondsToggled = viewModel::setClockShowSeconds,
          onClock24HToggled = viewModel::setClockIs24Hour,
          onTestHaptic = {
            viewModel.testHapticFeedback()
            Toast.makeText(
                    context,
                    context.getString(R.string.settings_haptics_tested),
                    Toast.LENGTH_SHORT,
                )
                .show()
          },
          onDismiss = viewModel::closeSettingsSheet,
          navBarInsets = PaddingValues(bottom = bottomInset),
      )
    }
  }
}

/**
 * PixelOS-style "At a Glance" top date, weather pill, and large digital clock.
 */
@Composable
fun PixelAtAGlanceHeader(
    currentTime: String,
    currentDate: String,
    onWeatherClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
  Column(
      modifier = modifier.testTag("clock_header"),
      horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    // PixelOS "At a Glance" top row with localized date & Liquid Glass weather pill
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.testTag("at_a_glance_widget"),
    ) {
      Text(
          text = currentDate,
          style = MaterialTheme.typography.titleMedium,
          color = TextPrimary,
          fontWeight = FontWeight.Medium,
          fontSize = 16.sp,
          letterSpacing = 0.2.sp,
      )

      Spacer(modifier = Modifier.width(10.dp))

      // Liquid Glass Weather Pill
      Surface(
          shape = RoundedCornerShape(14.dp),
          color = Color(0x1FFFFFFF),
          border = BorderStroke(1.dp, GlassBorder),
          modifier =
              Modifier
                  .clip(RoundedCornerShape(14.dp))
                  .clickable(onClick = onWeatherClick),
      ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        ) {
          Icon(
              imageVector = Icons.Default.WbSunny,
              contentDescription = null,
              tint = PixelYellow,
              modifier = Modifier.size(13.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
              text = stringResource(R.string.at_a_glance_weather),
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
              color = TextSecondary,
              fontWeight = FontWeight.Normal,
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Pixel Big Clock
    Text(
        text = currentTime.ifEmpty { "--:--" },
        style = MaterialTheme.typography.displayLarge.copy(fontSize = 66.sp),
        color = TextPrimary,
        fontWeight = FontWeight.Light,
        letterSpacing = (-2).sp,
    )
  }
}

/**
 * PixelOS Liquid Glass Search Bar with Google 'G' emblem, Voice & Lens indicators.
 */
@Composable
fun PixelSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onSearchDone: () -> Unit,
    onSearchBarClick: (() -> Unit)? = null,
    onVoiceClick: (() -> Unit)? = null,
    onLensClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
  val glassShape = RoundedCornerShape(26.dp)

  Box(
      modifier =
          modifier
              .height(52.dp)
              .clip(glassShape)
              // Liquid Glass translucent gradient background
              .background(
                  brush =
                      Brush.verticalGradient(
                          colors =
                              listOf(
                                  GlassSurfaceStart,
                                  GlassSurfaceEnd,
                              ),
                      ),
              )
              // Liquid Glass luminous refraction border
              .border(
                  width = 1.2.dp,
                  brush =
                      Brush.verticalGradient(
                          colors =
                              listOf(
                                  GlassHighlight,
                                  GlassBorder,
                                  GlassBorderSubtle,
                              ),
                      ),
                  shape = glassShape,
              )
              .then(
                  if (onSearchBarClick != null) {
                    Modifier.clickable { onSearchBarClick() }
                  } else {
                    Modifier
                  }
              )
              .testTag("search_bar_container"),
  ) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
    ) {
      // Pixel Google 'G' Emblem
      Box(
          modifier =
              Modifier
                  .size(30.dp)
                  .clip(CircleShape)
                  .background(Color(0x14FFFFFF)),
          contentAlignment = Alignment.Center,
      ) {
        GoogleGLogo(modifier = Modifier.size(18.dp))
      }

      Spacer(modifier = Modifier.width(10.dp))

      Box(
          modifier =
              Modifier
                  .weight(1f)
                  .padding(end = 4.dp),
          contentAlignment = Alignment.CenterStart,
      ) {
        if (query.isEmpty()) {
          Text(
              text = stringResource(R.string.search_pixel_placeholder),
              style = MaterialTheme.typography.bodyMedium,
              color = TextMuted,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
          )
        }
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            textStyle =
                MaterialTheme.typography.bodyLarge.copy(
                    color = TextPrimary,
                    fontSize = 15.sp,
                ),
            singleLine = true,
            cursorBrush = SolidColor(TextPrimary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearchDone() }),
            modifier = Modifier.fillMaxWidth().testTag("search_text_field"),
        )
      }

      if (query.isNotEmpty()) {
        IconButton(
            onClick = onClear,
            modifier =
                Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0x33FFFFFF))
                    .testTag("clear_search_button"),
        ) {
          Icon(
              imageVector = Icons.Default.Close,
              contentDescription = stringResource(R.string.clear_search),
              tint = TextPrimary,
              modifier = Modifier.size(16.dp),
          )
        }
      } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
              onClick = { onVoiceClick?.invoke() },
              modifier = Modifier.size(30.dp),
          ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Voz",
                tint = TextSecondary,
                modifier = Modifier.size(17.dp),
            )
          }
          IconButton(
              onClick = { onLensClick?.invoke() },
              modifier = Modifier.size(30.dp),
          ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Lens",
                tint = TextSecondary,
                modifier = Modifier.size(17.dp),
            )
          }
        }
      }
    }
  }
}

/**
 * PixelOS Banner requesting Notification Listener Access with Liquid Glass framing.
 */
@Composable
fun PixelNotificationPermissionBanner(
    onEnableClick: () -> Unit,
    onSimulateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
  val glassShape = RoundedCornerShape(20.dp)
  Box(
      modifier =
          modifier
              .fillMaxWidth()
              .clip(glassShape)
              .background(
                  brush =
                      Brush.verticalGradient(
                          colors = listOf(
                              Color(0x22FFFFFF),
                              Color(0x0EFFFFFF),
                          ),
                      ),
              )
              .border(
                  width = 1.dp,
                  brush =
                      Brush.verticalGradient(
                          colors = listOf(
                              GlassHighlight,
                              GlassBorder,
                              GlassBorderSubtle,
                          ),
                      ),
                  shape = glassShape,
              )
              .padding(14.dp)
              .testTag("notification_permission_banner"),
  ) {
    Column {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier =
                Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0x33EA4335))
                    .border(1.dp, Color(0x66EA4335), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
          Icon(
              imageVector = Icons.Default.NotificationsActive,
              contentDescription = null,
              tint = PixelRed,
              modifier = Modifier.size(19.dp),
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
              text = stringResource(R.string.notification_permission_title),
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.SemiBold,
              color = TextPrimary,
          )
          Text(
              text = stringResource(R.string.notification_permission_desc),
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondary,
              fontSize = 11.5.sp,
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        // Quick demo simulation button
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = SurfaceDark,
            border = BorderStroke(1.dp, SurfaceBorder),
            modifier =
                Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onSimulateClick)
                    .testTag("simulate_badges_btn"),
        ) {
          Text(
              text = "Testar badges",
              style = MaterialTheme.typography.labelSmall,
              color = TextSecondary,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Direct Settings intent button
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = PixelBlue,
            modifier =
                Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onEnableClick)
                    .testTag("activate_notification_permission_btn"),
        ) {
          Text(
              text = stringResource(R.string.notification_permission_btn),
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
              color = Color.White,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
          )
        }
      }
    }
  }
}

/**
 * PixelOS 4-Color Google 'G' Emblem drawn using Canvas.
 */
@Composable
fun GoogleGLogo(modifier: Modifier = Modifier) {
  Canvas(modifier = modifier) {
    val sizePx = size.minDimension
    val strokeWidth = sizePx * 0.22f
    val radius = (sizePx - strokeWidth) / 2f
    val center = Offset(size.width / 2f, size.height / 2f)

    // Red arc (top-left)
    drawArc(
        color = Color(0xFFEA4335),
        startAngle = 180f,
        sweepAngle = 100f,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
    )
    // Yellow arc (bottom-left)
    drawArc(
        color = Color(0xFFFBBC05),
        startAngle = 120f,
        sweepAngle = 60f,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
    )
    // Green arc (bottom)
    drawArc(
        color = Color(0xFF34A853),
        startAngle = 35f,
        sweepAngle = 85f,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
    )
    // Blue arc & horizontal line (right)
    drawArc(
        color = Color(0xFF4285F4),
        startAngle = -45f,
        sweepAngle = 80f,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
    )
    // Blue crossbar
    drawLine(
        color = Color(0xFF4285F4),
        start = Offset(center.x, center.y),
        end = Offset(center.x + radius + strokeWidth / 2f, center.y),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Square,
    )
  }
}

@Composable
fun CategoryFilterBar(
    categories: List<AppCategory>,
    selectedCategory: AppCategory,
    onCategorySelected: (AppCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
  LazyRow(
      contentPadding = PaddingValues(horizontal = 18.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = modifier.testTag("category_filter_bar"),
  ) {
    items(
        items = categories,
        key = { it.name },
    ) { category ->
      val isSelected = category == selectedCategory
      val animatedBgColor by animateColorAsState(
          targetValue = if (isSelected) TextPrimary else SurfaceDark,
          animationSpec = tween(durationMillis = 180),
          label = "chip_bg",
      )
      val animatedTextColor by animateColorAsState(
          targetValue = if (isSelected) AmoledBlack else TextSecondary,
          animationSpec = tween(durationMillis = 180),
          label = "chip_text",
      )

      Surface(
          shape = RoundedCornerShape(16.dp),
          color = animatedBgColor,
          border =
              if (isSelected) {
                null
              } else {
                BorderStroke(1.dp, SurfaceBorder)
              },
          modifier =
              Modifier
                  .clip(RoundedCornerShape(16.dp))
                  .clickable { onCategorySelected(category) }
                  .testTag("category_chip_${category.name}"),
      ) {
        Text(
            text = getCategoryDisplayName(category),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = animatedTextColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
      }
    }
  }
}

@Composable
fun getCategoryDisplayName(category: AppCategory): String {
  return when (category) {
    AppCategory.ALL -> stringResource(R.string.filter_category_all)
    AppCategory.SOCIAL -> stringResource(R.string.filter_category_social)
    AppCategory.TOOLS -> stringResource(R.string.filter_category_tools)
    AppCategory.MEDIA -> stringResource(R.string.filter_category_media)
    AppCategory.GAMES -> stringResource(R.string.filter_category_games)
    AppCategory.PRODUCTIVITY -> stringResource(R.string.filter_category_productivity)
    AppCategory.SHOPPING -> stringResource(R.string.filter_category_shopping)
    AppCategory.OTHER -> stringResource(R.string.filter_category_other)
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppGridItem(
    app: AppItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
  val interactionSource = remember { MutableInteractionSource() }

  Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier =
          modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .combinedClickable(
                  interactionSource = interactionSource,
                  indication = ripple(bounded = false, radius = 36.dp, color = Color.White),
                  onClick = onClick,
                  onLongClick = onLongClick,
              )
              .padding(vertical = 6.dp, horizontal = 2.dp)
              .testTag("app_item_${app.packageName}"),
  ) {
    // App Icon Container with floating PixelOS Liquid Glass notification badge
    Box(
        modifier = Modifier.size(54.dp),
        contentAlignment = Alignment.Center,
    ) {
      // 1. App Icon with subtle rounded mask
      Box(
          modifier =
              Modifier
                  .size(50.dp)
                  .clip(RoundedCornerShape(14.dp))
                  .background(SurfaceElevated),
          contentAlignment = Alignment.Center,
      ) {
        if (app.iconBitmap != null) {
          Image(
              bitmap = app.iconBitmap,
              contentDescription = app.label,
              modifier = Modifier.fillMaxSize(),
          )
        } else {
          Text(
              text = app.label.take(1).uppercase(),
              style = MaterialTheme.typography.titleMedium,
              color = TextPrimary,
              fontWeight = FontWeight.SemiBold,
          )
        }
      }

      // 2. PixelOS Liquid Glass Notification Badge (Top-Right)
      if (app.notificationCount > 0) {
        Box(
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-3).dp)
                    .clip(CircleShape)
                    .background(
                        brush =
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFF5252),
                                    PixelBadgeBackground,
                                ),
                            ),
                    )
                    .border(1.2.dp, PixelBadgeGlassBorder, CircleShape)
                    .padding(
                        horizontal = if (app.notificationCount > 9) 5.dp else 4.dp,
                        vertical = 1.5.dp,
                    )
                    .testTag("app_badge_${app.packageName}"),
            contentAlignment = Alignment.Center,
        ) {
          Text(
              text = if (app.notificationCount > 99) "99+" else app.notificationCount.toString(),
              color = PixelBadgeText,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              lineHeight = 10.sp,
              textAlign = TextAlign.Center,
              modifier = Modifier.testTag("notification_count_badge"),
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // App Simplified Name
    Text(
        text = app.label,
        style = MaterialTheme.typography.labelSmall,
        color = TextPrimary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )

    // Minimal category badge tag
    if (app.category != AppCategory.OTHER) {
      Text(
          text = getCategoryDisplayName(app.category),
          style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
          color = TextMuted,
          maxLines = 1,
          overflow = TextOverflow.Clip,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(top = 1.dp),
      )
    }
  }
}

@Composable
fun BottomSheetActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
  Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier =
          modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .clickable(
                  onClick = onClick,
              )
              .padding(vertical = 14.dp, horizontal = 8.dp),
  ) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = TextSecondary,
        modifier = Modifier.size(22.dp),
    )
    Text(
        text = title,
        style = MaterialTheme.typography.bodyLarge,
        color = TextPrimary,
        modifier = Modifier.padding(start = 16.dp),
    )
  }
}

private fun launchApp(
    context: Context,
    app: AppItem,
    onFail: () -> Unit,
) {
  try {
    val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
    if (launchIntent != null) {
      launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
      context.startActivity(launchIntent)
    } else {
      onFail()
    }
  } catch (e: Exception) {
    onFail()
  }
}

private fun openAppDetailsSettings(context: Context, packageName: String) {
  try {
    val intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
          data = Uri.parse("package:$packageName")
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    context.startActivity(intent)
  } catch (e: Exception) {
    Toast.makeText(context, "Configurações indisponíveis", Toast.LENGTH_SHORT).show()
  }
}
