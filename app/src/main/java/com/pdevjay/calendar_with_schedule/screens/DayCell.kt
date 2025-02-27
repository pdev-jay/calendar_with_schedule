package com.pdevjay.calendar_with_schedule.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pdevjay.calendar_with_schedule.datamodels.CalendarDay
import java.time.LocalDate

@Composable
fun DayCell(
    modifier: Modifier = Modifier,
    calendarDay: CalendarDay,
    isSelected: Boolean,
    onDateSelected: (LocalDate) -> Unit
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clickable { if (calendarDay.isCurrentMonth) onDateSelected(calendarDay.date) }
            .padding(top = 4.dp)
            .drawBehind {
                if (calendarDay.isCurrentMonth){
                    drawTopBorder(borderThickness = 2.dp, borderColor = Color.LightGray.copy(alpha = 0.5f))
                }
            }
            .background(if (isSelected) Color.Red.copy(alpha = 0.6f) else Color.Transparent, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (calendarDay.isCurrentMonth) {
            Text(
                text = calendarDay.date.dayOfMonth.toString(),
                color = if (isSelected) Color.White else if (calendarDay.isToday) Color.Red else Color.Black,
            )
        }
    }
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTopBorder(borderThickness: Dp, borderColor: Color) {
    val strokeWidth = borderThickness.toPx()
    drawLine(
        color = borderColor,
        start = androidx.compose.ui.geometry.Offset(0f, 0f),
        end = androidx.compose.ui.geometry.Offset(size.width, 0f),
        strokeWidth = strokeWidth
    )
}

