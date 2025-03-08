package com.pdevjay.calendar_with_schedule.screens.calendar.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

data class CalendarMonth(
    val yearMonth: YearMonth,
    val days: List<CalendarDay>
)

data class CalendarDay(
    val date: LocalDate,
    val dayOfWeek: DayOfWeek,
    val isToday: Boolean
)
