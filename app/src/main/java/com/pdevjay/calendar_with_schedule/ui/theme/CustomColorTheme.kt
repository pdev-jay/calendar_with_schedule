package com.pdevjay.calendar_with_schedule.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class SchedyColors(
    val brandPrimary: Color,
    val brandOnPrimary: Color,
    val tagColor: Color,
    val deadlineColor: Color
)

val LocalSchedyColors = staticCompositionLocalOf<SchedyColors> {
    error("No SchedyColors provided")
}

 val LightSchedyColors = SchedyColors(
    brandPrimary = Color(0xFF1E88E5),
    brandOnPrimary = Color.White,
    tagColor = Color(0xFFFFC107),
    deadlineColor = Color(0xFFE53935)
)

 val DarkSchedyColors = SchedyColors(
    brandPrimary = Color(0xFF90CAF9),
    brandOnPrimary = Color.Black,
    tagColor = Color(0xFFFFF176),
    deadlineColor = Color(0xFFEF9A9A)
)

class CustomColorTheme {
}