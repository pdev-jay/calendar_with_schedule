package com.pdevjay.calendar_with_schedule.features.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pdevjay.calendar_with_schedule.features.calendar.data.CalendarDay
import com.pdevjay.calendar_with_schedule.features.calendar.data.CalendarMonth
import com.pdevjay.calendar_with_schedule.features.calendar.data.HolidayData
import com.pdevjay.calendar_with_schedule.features.schedule.data.BaseSchedule
import java.time.LocalDate

@Composable
fun MonthItem(
    month: CalendarMonth,
    scheduleMap: Map<LocalDate, List<BaseSchedule>>,
    holidayMap: Map<LocalDate, List<HolidayData>>,
    onDayClick: (CalendarDay) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        DaysGrid(month.days, scheduleMap, holidayMap, onDayClick)
    }
}
