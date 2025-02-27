package com.pdevjay.calendar_with_schedule.datamodels

import java.time.LocalDate
import java.time.YearMonth

data class CalendarMonth(
    val yearMonth: YearMonth,
    val weeks: List<CalendarWeek>
)

data class CalendarWeek(
    val days: List<CalendarDay>
)

data class CalendarDay(
    val date: LocalDate,
    val isCurrentMonth: Boolean, // 현재 월에 속하는지 여부
    val isSelected: Boolean = false, // 선택 여부
    val isFirstDayOfMonth: Boolean = false,
    val isToday: Boolean = false
)

// calendar를 주 단위로 컨트롤하기 위한 class
sealed class CalendarListItem {
    data class WeekItem(val week: CalendarWeek) : CalendarListItem()
}
