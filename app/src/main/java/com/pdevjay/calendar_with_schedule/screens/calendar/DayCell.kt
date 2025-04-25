package com.pdevjay.calendar_with_schedule.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pdevjay.calendar_with_schedule.screens.calendar.data.CalendarDay
import com.pdevjay.calendar_with_schedule.screens.schedule.data.BaseSchedule

@Composable
fun DayCell(
    day: CalendarDay,
    dayCellPadding: Dp
) {
    Column(
        modifier = Modifier
            .padding(dayCellPadding),
        ){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = if (day.isToday) MaterialTheme.colorScheme.error else Color.Transparent, shape = RoundedCornerShape(4.dp)),
//                .padding(dayCellPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                color = if (day.isToday) Color.White else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ScheduleListPreview(
    backgroundColor: Color,
    textColor: Color,
    alignment: Alignment,
    title: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = backgroundColor, shape = RoundedCornerShape(2.dp))
            .padding(2.dp),
        contentAlignment = alignment
    ) {

        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

