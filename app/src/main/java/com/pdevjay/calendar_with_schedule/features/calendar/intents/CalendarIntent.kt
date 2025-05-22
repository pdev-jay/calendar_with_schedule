package com.pdevjay.calendar_with_schedule.features.calendar.intents

import com.pdevjay.calendar_with_schedule.features.calendar.data.CalendarMonth
import java.time.LocalDate
import java.time.YearMonth

// 사용자 액션(Intent) 정의
sealed class CalendarIntent {
    object PreviousMonth : CalendarIntent()
    object NextMonth : CalendarIntent()
    data class DateSelected(val date: LocalDate) : CalendarIntent()
    object DateUnselected : CalendarIntent()
    data class MonthChanged(val month: YearMonth) : CalendarIntent()
    object LoadNextMonths : CalendarIntent()
    object LoadPreviousMonths : CalendarIntent()
}