package com.pdevjay.calendar_with_schedule.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pdevjay.calendar_with_schedule.screens.calendar.data.CalendarDay
import com.pdevjay.calendar_with_schedule.screens.calendar.data.HolidayData
import com.pdevjay.calendar_with_schedule.utils.LunarCalendarUtils
import java.time.LocalDate


@Composable
fun DayCell(
    day: CalendarDay,
    dayCellPadding: Dp,
    isShowLunarDate: Boolean
) {
    val lunarMonthDay = LunarCalendarUtils.getLunarMonthDay(day.date)
    Column(
        modifier = Modifier
            .padding(dayCellPadding),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = if (day.isToday) MaterialTheme.colorScheme.error else Color.Transparent,
                    shape = RoundedCornerShape(4.dp)
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                color = if (day.isToday) Color.White else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            if (isShowLunarDate) {
                Text(
                    text = lunarMonthDay,
                    color = if (day.isToday) Color.White else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
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

