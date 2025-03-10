package com.pdevjay.calendar_with_schedule.screens.calendar

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.pdevjay.calendar_with_schedule.data.entity.ScheduleEntity
import com.pdevjay.calendar_with_schedule.screens.calendar.data.CalendarDay
import com.pdevjay.calendar_with_schedule.screens.calendar.data.CalendarMonth
import com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData
import java.time.LocalDate

@Composable
fun MonthItem(
    month: CalendarMonth,
    scheduleMap: Map<LocalDate, List<ScheduleData>>,
    onDayClick: (CalendarDay) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        DaysGrid(month.days, scheduleMap, onDayClick)
    }
}
