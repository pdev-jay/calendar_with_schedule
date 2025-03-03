package com.pdevjay.calendar_with_schedule.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pdevjay.calendar_with_schedule.datamodels.CalendarDay
import java.time.LocalDate

@Composable
fun DayCell(
    modifier: Modifier = Modifier,
    isInTopBar: Boolean = false,
    calendarDay: CalendarDay,
    isSelected: Boolean,
    onDateSelected: (LocalDate) -> Unit
) {
    Box(
        modifier = modifier
            .clickable (
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ){ if (calendarDay.isCurrentMonth) onDateSelected(calendarDay.date) else if (isInTopBar) onDateSelected(calendarDay.date) }
            .drawBehind {
                if (calendarDay.isCurrentMonth && !isInTopBar) {
                    drawTopBorder(borderThickness = 2.dp, borderColor = Color.LightGray.copy(alpha = 0.5f))
                }
            }
            .padding(8.dp),
        contentAlignment = if (!isInTopBar) Alignment.TopCenter else Alignment.Center
    ) {
        if (calendarDay.isCurrentMonth) {
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .background(
                        color = if (isSelected) Color.Red.copy(alpha = 0.6f) else Color.Transparent,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = calendarDay.date.dayOfMonth.toString(),
                    color = when {
                        isSelected -> Color.White
                        calendarDay.isToday -> Color.Red
                        else -> Color.Black
                    },
                    textAlign = TextAlign.Center
                )
            }
        } else if (!calendarDay.isCurrentMonth && isInTopBar){
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxSize()
                    .background(
                        color = if (isSelected) Color.Red.copy(alpha = 0.6f) else Color.Transparent,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = calendarDay.date.dayOfMonth.toString(),
                    color = when {
                        isSelected -> Color.White
                        calendarDay.isToday -> Color.Red
                        else -> Color.Gray
                    },
                    textAlign = TextAlign.Center
                )
            }
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

