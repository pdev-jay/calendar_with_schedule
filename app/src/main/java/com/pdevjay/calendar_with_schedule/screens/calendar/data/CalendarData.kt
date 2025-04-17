package com.pdevjay.calendar_with_schedule.screens.calendar.data

import com.pdevjay.calendar_with_schedule.screens.schedule.data.BaseSchedule
import com.pdevjay.calendar_with_schedule.screens.schedule.data.RecurringData
import com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

data class CalendarMonth(
    val yearMonth: YearMonth,
    val days: List<CalendarDay>
){
    fun mapSchedulesToDays(
        scheduleMap: Map<LocalDate, List<BaseSchedule>>
    ): Map<LocalDate, List<BaseSchedule>> {
        return days.associateBy({ it.date }, { day ->
            scheduleMap[day.date] ?: emptyList() //  해당 날짜에 일정이 있으면 가져오고, 없으면 빈 리스트 반환
        })
    }
}

data class CalendarDay(
    val date: LocalDate,
    val dayOfWeek: DayOfWeek,
    val isToday: Boolean
){
    fun getSchedules(
        schedules: List<ScheduleData>,
        recurringSchedules: List<RecurringData>
    ): List<BaseSchedule> {
        val dailySchedules = schedules.filter { it.start.date == this.date }
        val dailyRecurring = recurringSchedules.filter { it.start.date == this.date }
        return dailySchedules + dailyRecurring
    }
}

data class CalendarWeek(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val days: List<CalendarDay>
) {
    companion object {
        fun from(date: LocalDate, allDays: List<CalendarDay>): CalendarWeek {
            val startOfWeek = date.with(DayOfWeek.SUNDAY) //  정확한 일요일 찾기
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
