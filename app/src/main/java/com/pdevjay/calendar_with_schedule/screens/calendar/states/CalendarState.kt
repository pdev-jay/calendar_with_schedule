package com.pdevjay.calendar_with_schedule.screens.calendar.states

import com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData
import java.time.LocalDate
import java.time.YearMonth

// 1. 캘린더 상태 정의 (현재 월, 선택된 날짜)
data class CalendarState(
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate? = null,
    val scheduleMap: Map<LocalDate, List<ScheduleData>> = emptyMap() // 추가
)
