package com.pdevjay.calendar_with_schedule.screens.schedule

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun TimeLines(hourHeight: Dp) {
    // 24개의 수평선을 그려 시각적으로 시간대 구분
    Canvas(modifier = Modifier.fillMaxSize()) {
        for (hour in 0..24) {
            val y = hourHeight.toPx() * hour
            drawLine(
                color = Color.LightGray.copy(alpha = 0.5f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}
