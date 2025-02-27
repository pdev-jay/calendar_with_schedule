package com.pdevjay.calendar_with_schedule.viewmodels

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.pdevjay.calendar_with_schedule.datamodels.CalendarDay
import com.pdevjay.calendar_with_schedule.datamodels.CalendarMonth
import com.pdevjay.calendar_with_schedule.datamodels.CalendarWeek
import com.pdevjay.calendar_with_schedule.intents.CalendarIntent
import com.pdevjay.calendar_with_schedule.states.CalendarState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(CalendarState())
    val state: StateFlow<CalendarState> = _state

    fun processIntent(intent: CalendarIntent) {
        when (intent) {
            is CalendarIntent.PreviousMonth -> {
                val newMonth = _state.value.currentMonth.minusMonths(1)
                _state.value = _state.value.copy(currentMonth = newMonth)
            }

            is CalendarIntent.NextMonth -> {
                val newMonth = _state.value.currentMonth.plusMonths(1)
                _state.value = _state.value.copy(currentMonth = newMonth)
            }

            is CalendarIntent.DateSelected -> {
                _state.value = _state.value.copy(selectedDate = intent.date, isExpanded = false)
            }

            is CalendarIntent.DateUnselected -> {
                _state.value = _state.value.copy(selectedDate = null, isExpanded = true)
            }

            is CalendarIntent.MonthChanged -> {
                _state.value = _state.value.copy(currentMonth = intent.month)
            }

            is CalendarIntent.SetExpanded -> {
                _state.value = _state.value.copy(isExpanded = intent.isExpanded)
            }
        }
    }

    fun generateCalendarMonths(currentMonth: YearMonth = YearMonth.now()): List<CalendarMonth> {
        val months = mutableListOf<CalendarMonth>()

        for (i in -5..5) {
            val month = currentMonth.plusMonths(i.toLong())
            val firstDayOfMonth = month.atDay(1)
            val firstDayOffset = firstDayOfMonth.dayOfWeek.value % 7
            val daysInMonth = month.lengthOfMonth()
            val totalSlots = ((firstDayOffset + daysInMonth + 6) / 7) * 7

            val days = mutableListOf<CalendarDay>()
            for (index in 0 until totalSlots) {
                val dayNumber = index - firstDayOffset + 1
                val isCurrentMonth = dayNumber in 1..daysInMonth
                val date = if (isCurrentMonth) month.atDay(dayNumber) else LocalDate.MIN
                days.add(CalendarDay(date, isCurrentMonth, isFirstDayOfMonth = if (dayNumber == 1) true else false, isToday = date == LocalDate.now()))
            }

            val weeks = days.chunked(7).map { CalendarWeek(it) }
            months.add(CalendarMonth(month, weeks))
        }

        return months
    }
}
