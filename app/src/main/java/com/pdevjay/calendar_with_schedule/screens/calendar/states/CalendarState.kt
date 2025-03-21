package com.pdevjay.calendar_with_schedule.screens.calendar.states

import com.pdevjay.calendar_with_schedule.screens.calendar.data.CalendarMonth
import com.pdevjay.calendar_with_schedule.screens.schedule.data.BaseSchedule
import com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData
import java.time.LocalDate
import java.time.YearMonth

// 1. 캘린더 상태 정의 (현재 월, 선택된 날짜)
data class CalendarState(
    val currentMonth: YearMonth = YearMonth.now(),
    val months: MutableList<CalendarMonth> = mutableListOf(),
    val selectedDate: LocalDate? = null,
    val scheduleMap: Map<LocalDate, List<BaseSchedule>> = emptyMap() // 추가
)
