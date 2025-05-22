package com.pdevjay.calendar_with_schedule.features.calendar.states

import com.pdevjay.calendar_with_schedule.features.calendar.data.CalendarMonth
import com.pdevjay.calendar_with_schedule.features.calendar.data.HolidayData
import com.pdevjay.calendar_with_schedule.features.schedule.data.RecurringData
import java.time.LocalDate
import java.time.YearMonth

// 1. 캘린더 상태 정의 (현재 월, 선택된 날짜)
data class CalendarState(
    val currentMonth: YearMonth = YearMonth.now(),
    val months: MutableList<CalendarMonth> = mutableListOf(),
    val selectedDate: LocalDate? = null,
    val scheduleMap: Map<LocalDate, List<RecurringData>> = emptyMap(),
    val holidayMap: Map<LocalDate, List<HolidayData>> = emptyMap()
)
