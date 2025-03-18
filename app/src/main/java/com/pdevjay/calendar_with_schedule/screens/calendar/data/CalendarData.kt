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

data class CalendarWeek(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val days: List<CalendarDay>
) {
    companion object {
        fun from(date: LocalDate, allDays: List<CalendarDay>): CalendarWeek {
            val startOfWeek = date.with(DayOfWeek.SUNDAY) // ✅ 정확한 일요일 찾기
            val daysInWeek = (0..6).map { startOfWeek.plusDays(it.toLong()) }
                .mapNotNull { targetDate -> allDays.find { it.date == targetDate } }
            val sortedDaysInWeek = daysInWeek.sortedBy { it.date }
            return CalendarWeek(
                startDate = startOfWeek,
                endDate = startOfWeek.plusDays(6),
                days = sortedDaysInWeek
            )
        }
    }

    fun contains(date: LocalDate): Boolean {
        return !date.isBefore(startDate) && !date.isAfter(endDate)
    }
}
