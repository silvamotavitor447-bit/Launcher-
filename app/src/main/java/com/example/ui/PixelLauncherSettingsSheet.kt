package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.LauncherSettings
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.PixelBlue
import com.example.ui.theme.PixelRed
import com.example.ui.theme.PixelYellow
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PixelLauncherSettingsSheet(
    settings: LauncherSettings,
    onHapticsToggled: (Boolean) -> Unit,
    onGridHapticsToggled: (Boolean) -> Unit,
    onGestureHapticsToggled: (Boolean) -> Unit,
    onClockSecondsToggled: (Boolean) -> Unit,
    onClock24HToggled: (Boolean) -> Unit,
    onTestHaptic: () -> Unit,
    onDismiss: () -> Unit,
    navBarInsets: PaddingValues,
    modifier: Modifier = Modifier,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(
      onDismissRequest = onDismiss,
      sheetState = sheetState,
      containerColor = AmoledBlack,
      contentColor = TextPrimary,
      shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
      dragHandle = {
        Box(
            modifier =
                Modifier
                    .padding(vertical = 12.dp)
                    .width(44.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(GlassBorder),
        )
      },
      modifier = modifier.testTag("launcher_settings_sheet"),
  ) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .widthIn(max = 580.dp)
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 20.dp)
                .padding(bottom = navBarInsets.calculateBottomPadding() + 24.dp),
    ) {
      // Header: Title & Description
      Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(bottom = 18.dp),
      ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0x224285F4),
            border = BorderStroke(1.dp, Color(0x444285F4)),
            modifier = Modifier.size(46.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = null,
                tint = PixelBlue,
                modifier = Modifier.size(24.dp),
            )
          }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column {
          Text(
              text = stringResource(R.string.settings_title),
              style = MaterialTheme.typography.titleLarge,
              color = TextPrimary,
              fontWeight = FontWeight.Bold,
          )
          Text(
              text = stringResource(R.string.settings_haptics_subtitle),
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondary,
          )
        }
      }

      // Settings Card with Liquid Glass Finish
      Surface(
          shape = RoundedCornerShape(20.dp),
          color = SurfaceElevated,
          border = BorderStroke(1.dp, GlassBorderSubtle),
          modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          // 1. Master Haptic Feedback Toggle
          SettingToggleItem(
              icon = Icons.Default.Vibration,
              iconTint = PixelBlue,
              title = stringResource(R.string.settings_haptics_master),
              description = stringResource(R.string.settings_haptics_master_desc),
              checked = settings.hapticsEnabled,
              onCheckedChange = onHapticsToggled,
              testTag = "switch_haptics_master",
          )

          HorizontalDivider(
              color = SurfaceBorder,
              thickness = 0.5.dp,
              modifier = Modifier.padding(vertical = 12.dp),
          )

          // 2. Grid Interactions Toggle (tap to open app, long press for menu, category chips)
          SettingToggleItem(
              icon = Icons.Default.Apps,
              iconTint = PixelYellow,
              title = stringResource(R.string.settings_haptics_grid),
              description = stringResource(R.string.settings_haptics_grid_desc),
              checked = settings.gridHapticsEnabled,
              enabled = settings.hapticsEnabled,
              onCheckedChange = onGridHapticsToggled,
              testTag = "switch_haptics_grid",
          )

          HorizontalDivider(
              color = SurfaceBorder,
              thickness = 0.5.dp,
              modifier = Modifier.padding(vertical = 12.dp),
          )

          // 3. Gesture Navigation Toggle (swipe up for drawer, swipe down for notifications)
          SettingToggleItem(
              icon = Icons.Default.TouchApp,
              iconTint = PixelRed,
              title = stringResource(R.string.settings_haptics_gestures),
              description = stringResource(R.string.settings_haptics_gestures_desc),
              checked = settings.gestureHapticsEnabled,
              enabled = settings.hapticsEnabled,
              onCheckedChange = onGestureHapticsToggled,
              testTag = "switch_haptics_gestures",
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 2. Digital Clock Widget Settings Card
      Surface(
          shape = RoundedCornerShape(20.dp),
          color = SurfaceElevated,
          border = BorderStroke(1.dp, GlassBorderSubtle),
          modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
              text = stringResource(R.string.settings_clock_section),
              style = MaterialTheme.typography.titleMedium,
              color = TextPrimary,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(bottom = 12.dp),
          )

          // Seconds Indicator Toggle
          SettingToggleItem(
              icon = Icons.Default.MoreTime,
              iconTint = PixelYellow,
              title = stringResource(R.string.settings_clock_seconds),
              description = stringResource(R.string.settings_clock_seconds_desc),
              checked = settings.clockShowSeconds,
              onCheckedChange = onClockSecondsToggled,
              testTag = "switch_clock_seconds",
          )

          HorizontalDivider(
              color = SurfaceBorder,
              thickness = 0.5.dp,
              modifier = Modifier.padding(vertical = 12.dp),
          )

          // 24-Hour Format Toggle
          SettingToggleItem(
              icon = Icons.Default.AccessTime,
              iconTint = PixelBlue,
              title = stringResource(R.string.settings_clock_24h),
              description = stringResource(R.string.settings_clock_24h_desc),
              checked = settings.clockIs24Hour,
              onCheckedChange = onClock24HToggled,
              testTag = "switch_clock_24h",
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // 4. Tactile Test Button with Liquid Glass Accent
      Surface(
          shape = RoundedCornerShape(16.dp),
          color = if (settings.hapticsEnabled) Color(0x224285F4) else SurfaceDark,
          border =
              BorderStroke(
                  1.dp,
                  if (settings.hapticsEnabled) Color(0x664285F4) else SurfaceBorder,
              ),
          modifier =
              Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(16.dp))
                  .clickable(enabled = settings.hapticsEnabled, onClick = onTestHaptic)
                  .testTag("test_haptic_button"),
      ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 16.dp),
        ) {
          Icon(
              imageVector = Icons.Default.GraphicEq,
              contentDescription = null,
              tint = if (settings.hapticsEnabled) PixelBlue else TextMuted,
              modifier = Modifier.size(20.dp),
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
              text = stringResource(R.string.settings_haptics_test_btn),
              style = MaterialTheme.typography.labelLarge,
              color = if (settings.hapticsEnabled) TextPrimary else TextMuted,
              fontWeight = FontWeight.SemiBold,
          )
        }
      }
    }
  }
}

@Composable
fun SettingToggleItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String = "",
) {
  Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = modifier.fillMaxWidth(),
  ) {
    Surface(
        shape = CircleShape,
        color = if (enabled) iconTint.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
        modifier = Modifier.size(38.dp),
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) iconTint else TextMuted,
            modifier = Modifier.size(20.dp),
        )
      }
    }

    Column(
        modifier =
            Modifier
                .weight(1f)
                .padding(start = 12.dp, end = 8.dp),
    ) {
      Text(
          text = title,
          style = MaterialTheme.typography.bodyMedium,
          color = if (enabled) TextPrimary else TextMuted,
          fontWeight = FontWeight.Medium,
      )
      Text(
          text = description,
          style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
          color = if (enabled) TextSecondary else TextMuted,
      )
    }

    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        colors =
            SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PixelBlue,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = SurfaceDark,
                disabledCheckedTrackColor = PixelBlue.copy(alpha = 0.3f),
                disabledUncheckedTrackColor = SurfaceDark.copy(alpha = 0.5f),
            ),
        modifier = Modifier.testTag(testTag),
    )
  }
}
