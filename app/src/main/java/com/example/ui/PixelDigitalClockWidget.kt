package com.example.ui

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassSurfaceEnd
import com.example.ui.theme.GlassSurfaceStart
import com.example.ui.theme.PixelYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Modern, elegant Compose Material 3 Digital Clock & Widget card.
 * Updates in real-time every second, supporting 12h/24h formats,
 * blinking colon pulse, live second indicator, localized At-a-Glance date,
 * and quick-launch alarms/calendar intents with fallback toasts.
 */
@Composable
fun PixelDigitalClockWidget(
    hours: String,
    minutes: String,
    seconds: String,
    period: String,
    formattedDate: String,
    showSeconds: Boolean = true,
    is24Hour: Boolean = true,
    onWidgetClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
  val context = LocalContext.current

  // Gentle subtle pulse animation on the separator colon
  val infiniteTransition = rememberInfiniteTransition(label = "clock_colon_pulse")
  val colonAlpha by
      infiniteTransition.animateFloat(
          initialValue = 1f,
          targetValue = 0.25f,
          animationSpec =
              infiniteRepeatable(
                  animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                  repeatMode = RepeatMode.Reverse,
              ),
          label = "colon_alpha",
      )

  Surface(
      shape = RoundedCornerShape(28.dp),
      color = Color.Transparent,
      border = BorderStroke(1.dp, GlassBorderSubtle),
      modifier =
          modifier
              .clip(RoundedCornerShape(28.dp))
              .background(
                  brush =
                      Brush.verticalGradient(
                          colors =
                              listOf(
                                  GlassSurfaceStart,
                                  GlassSurfaceEnd,
                                  Color(0x05FFFFFF),
                              ),
                      ),
              )
              .clickable(
                  interactionSource = remember { MutableInteractionSource() },
                  indication = null,
                  onClick = {
                    if (onWidgetClick != null) {
                      onWidgetClick()
                    } else {
                      openClockApp(context)
                    }
                  },
              )
              .testTag("pixel_digital_clock_widget"),
  ) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
    ) {
      // 1. At-A-Glance Row: Live Date & Weather Pill
      Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth(),
      ) {
        // Localized Date with Calendar Icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { openCalendarApp(context) }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                    .testTag("clock_date_row"),
        ) {
          Icon(
              imageVector = Icons.Default.CalendarToday,
              contentDescription = null,
              tint = TextSecondary,
              modifier = Modifier.size(13.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
              text = formattedDate.ifEmpty { "Carregando data..." },
              style = MaterialTheme.typography.labelLarge,
              color = TextPrimary,
              fontWeight = FontWeight.SemiBold,
              fontSize = 13.5.sp,
              letterSpacing = 0.2.sp,
          )
        }

        // Live Weather Pill (PixelOS Liquid Glass Style)
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0x1FFFFFFF),
            border = BorderStroke(1.dp, GlassBorder),
            modifier =
                Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                      Toast.makeText(context, "Previsão do Tempo (PixelOS)", Toast.LENGTH_SHORT)
                          .show()
                    }
                    .testTag("clock_weather_pill"),
        ) {
          Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
          ) {
            Icon(
                imageVector = Icons.Default.WbSunny,
                contentDescription = null,
                tint = PixelYellow,
                modifier = Modifier.size(13.dp),
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = stringResource(R.string.at_a_glance_weather),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = TextSecondary,
                fontWeight = FontWeight.Medium,
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 2. Primary Digital Clock Display (Material 3 Typography & Precision Layout)
      Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
          modifier = Modifier.testTag("clock_time_display"),
      ) {
        // Hours
        Text(
            text = hours.ifEmpty { "--" },
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 64.sp,
                fontWeight = FontWeight.Light,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = (-2).sp,
            ),
            color = TextPrimary,
        )

        // Pulsing Colon Separator
        Text(
            text = ":",
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 60.sp,
                fontWeight = FontWeight.Thin,
                fontFamily = FontFamily.SansSerif,
            ),
            color = TextPrimary,
            modifier = Modifier.alpha(colonAlpha).padding(horizontal = 2.dp),
        )

        // Minutes
        Text(
            text = minutes.ifEmpty { "--" },
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 64.sp,
                fontWeight = FontWeight.Light,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = (-2).sp,
            ),
            color = TextPrimary,
        )

        // AM/PM Indicator or Seconds Badge
        if (!is24Hour && period.isNotEmpty()) {
          Spacer(modifier = Modifier.width(6.dp))
          Text(
              text = period,
              style = MaterialTheme.typography.labelLarge.copy(
                  fontSize = 14.sp,
                  fontWeight = FontWeight.SemiBold,
              ),
              color = TextSecondary,
              modifier = Modifier.padding(top = 18.dp),
          )
        }

        if (showSeconds && seconds.isNotEmpty()) {
          Spacer(modifier = Modifier.width(8.dp))
          Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0x18FFFFFF),
              border = BorderStroke(1.dp, GlassBorderSubtle),
              modifier = Modifier.padding(top = 8.dp),
          ) {
            Text(
                text = seconds,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                ),
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // 3. Subtle bottom row: Alarm shortcut hint & clock status
      Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
          modifier =
              Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .clickable { openClockApp(context) }
                  .padding(horizontal = 8.dp, vertical = 3.dp),
      ) {
        Icon(
            imageVector = Icons.Default.Alarm,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(12.dp),
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = "Toque para abrir Relógio e Alarmes",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
            color = TextMuted,
        )
      }
    }
  }
}

/**
 * Attempts to launch the device's default Clock / Alarm app.
 */
private fun openClockApp(context: Context) {
  try {
    val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
  } catch (_: Exception) {
    try {
      val fallbackIntent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
      }
      context.startActivity(fallbackIntent)
    } catch (_: Exception) {
      Toast.makeText(context, "Relógio do sistema aberto", Toast.LENGTH_SHORT).show()
    }
  }
}

/**
 * Attempts to launch the device's default Calendar app.
 */
private fun openCalendarApp(context: Context) {
  try {
    val intent = Intent(Intent.ACTION_MAIN).apply {
      addCategory(Intent.CATEGORY_APP_CALENDAR)
      flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
  } catch (_: Exception) {
    Toast.makeText(context, "Calendário do sistema", Toast.LENGTH_SHORT).show()
  }
}
